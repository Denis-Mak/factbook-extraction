package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 */
public class WebHelper {
    private static final Logger log = LoggerFactory.getLogger(WebHelper.class);
    public static String getUrl(String url){
        return getUrl(url, null);
    }

    public static String getUrl(String url, String userAgent){
        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader in = null;
        try {
            URL myUrl = new URL(url);
            java.net.URLConnection c = myUrl.openConnection();
            if (userAgent != null) {
                System.setProperty("http.agent", "");
                c.setRequestProperty("User-Agent", userAgent);
            }
            in = new BufferedReader(new InputStreamReader(myUrl.openStream(), "UTF-8"));
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e){
            log.error("Error get results: {}", e);
        }finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                log.error("Couldn't close buffer error: {}", e);
            }
        }

        return sb.toString();
    }
}
