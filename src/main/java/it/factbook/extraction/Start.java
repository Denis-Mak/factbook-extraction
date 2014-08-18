package it.factbook.extraction;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 */
public class Start implements Daemon{
    private static final Logger log = LoggerFactory.getLogger(Start.class);

    private static volatile boolean cancelled = false;

    private Thread myThread;

    private ConfigurableApplicationContext ctx;

    private static Start app = new Start();

    public static void main (String[] ars) throws Exception{
        Start app = new Start();
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
        ctx = new AnnotationConfigApplicationContext(Class.forName(buildConfig.getString("build.profile")));
        init();  // init here because windows service starts calling one method
        myThread.start();
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
    }

    @Override
    public void destroy() {
        myThread = null;
    }
}
