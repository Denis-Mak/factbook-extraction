package it.factbook.extraction.util;

import org.apache.tika.io.TikaInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

/**
 *
 */
public class WebHelper {
    private static final Logger log = LoggerFactory.getLogger(WebHelper.class);

    private static final String DEFAULT_USER_AGENT = "factbook-robot";

    public static String getUrl(String url) throws IOException{
        return getUrl(url, DEFAULT_USER_AGENT);
    }

    public static String getUrl(String url, String userAgent) throws IOException {
        return getUrl(url, userAgent, null, null);
    }

    public static String getUrl(String url, String userAgent, String username, String password)
            throws IOException{
        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader in = null;
        InputStream is = getInputStream(url, userAgent, username, password);
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

    public static InputStream getInputStream(String url) throws IOException{
        return getInputStream(url, DEFAULT_USER_AGENT);
    }

    public static InputStream getInputStream (String url, String userAgent) throws IOException{
        return getInputStream(url, userAgent, null, null);
    }

    public static InputStream getInputStream(String url, String userAgent, String username, String password)
            throws IOException{
        InputStream is = null;
        URL myUrl = new URL(url);
        if (username != null) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            });
        }
        HttpURLConnection c = (HttpURLConnection)myUrl.openConnection();
        if (userAgent != null) {
            System.setProperty("http.agent", "");
            c.setRequestProperty("User-Agent", userAgent);
        }
        c.setRequestMethod("GET");
        is = c.getInputStream();

        return TikaInputStream.get(is);
    }
}
