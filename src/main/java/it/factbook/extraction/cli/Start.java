package it.factbook.extraction.cli;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class Start implements Daemon{
    private static final Logger log = LoggerFactory.getLogger(Start.class);

    private static volatile boolean cancelled = false;

    private Thread myThread;

    private ConfigurableApplicationContext ctx;

    private static Start app = new Start();

    private static class TempFileCleaner extends Thread {
        private static Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));

        @Override
        public void run() {
            try {
                while (true) {
                    try (DirectoryStream<Path> stream =
                                 Files.newDirectoryStream(tempDir, "{wapiti-model-,test,result}*.{bin,crf}")) {
                        for (Path entry: stream) {
                            long lastModified = Files.getLastModifiedTime(entry).toMillis();
                            if (System.currentTimeMillis() - lastModified > 60000) {
                                Files.deleteIfExists(entry);
                            }
                        }
                    } catch (IOException x) {
                        System.err.println("TempFileCleaner error");
                        x.printStackTrace();
                    }
                    Thread.sleep(60000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static TempFileCleaner tempFileCleaner = new TempFileCleaner();

    public static void main (String[] ars) throws Exception{
        app.init();
        app.start();
    }

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {

    }

    private void init(){
        myThread = new Thread(){
            @Override
            public synchronized void start() {
                Start.cancelled = false;
                super.start();
            }

            @Override
            public void run() {
                while(!cancelled){
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        log.error("Thread terminated, {}", e);
                        cancelled = true;
                    }
                }
            }
        };
    }

    //Windows service start
    public static void start(String[] args){
        app.init();
        try {
            app.start();
        } catch (Exception e) {
            log.error("Service start exception: {}", e);
        }
    }

    //Windows service stop
    public static void stop(String[] args){
        try {
            app.stop();
        } catch (Exception e) {
            log.error("Service stop exception: {}", e);
        }
    }

    // Unix daemon start
    @Override
    public void start() throws Exception {
        Start.cancelled = false;
        PropertiesConfiguration buildConfig = new PropertiesConfiguration("build.properties");
        buildConfig.load();
        System.setProperty("http.keepAlive", "false");
        ctx = new AnnotationConfigApplicationContext(Class.forName(buildConfig.getString("build.profile")));
        init();  // init here because windows service starts calling one method
        myThread.start();
        tempFileCleaner.start();
        log.info("Loaded config: {}", buildConfig.getString("build.profile"));
        log.info("All listners, exchanges and queues raised and ready.");
    }


    // Unix daemon stop
    @Override
    public void stop() throws Exception {
        Start.cancelled = true;
        ctx.close();
        log.info("factbook-it.factbook.extraction stopped");
        myThread.interrupt();
        myThread.join();
        tempFileCleaner.interrupt();
        tempFileCleaner.join();
    }

    @Override
    public void destroy() {
        myThread = null;
        tempFileCleaner = null;
    }
}
