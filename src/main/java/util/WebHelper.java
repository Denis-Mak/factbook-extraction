package util;

import org.apache.tika.io.TikaInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 */
public class WebHelper {
    private static final Logger log = LoggerFactory.getLogger(WebHelper.class);

    private static final String DEFAULT_USER_AGENT = "factbook-robot";

    public static String getUrl(String url){
        return getUrl(url, DEFAULT_USER_AGENT);
    }

    public static String getUrl(String url, String userAgent){
        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader in = null;
        InputStream is = WebHelper.getInputStream(url, userAgent);
        try {
            in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e){
            log.error("Error get results: {}", e);
        }finally {
            try {
                if (in != null) in.close();
                if (is != null) is.close();
            } catch (IOException e) {
                log.error("Couldn't close buffer error: {}", e);
            }
        }

        return sb.toString();
    }

    public static InputStream getInputStream(String url){
        return WebHelper.getInputStream(url, DEFAULT_USER_AGENT);
    }

    public static InputStream getInputStream(String url, String userAgent){
        InputStream is = null;
        try {
            URL myUrl = new URL(url);
            HttpURLConnection c = (HttpURLConnection)myUrl.openConnection();
            if (userAgent != null) {
                System.setProperty("http.agent", "");
                c.setRequestProperty("User-Agent", userAgent);
            }
            is = c.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return TikaInputStream.get(is);
    }
}
