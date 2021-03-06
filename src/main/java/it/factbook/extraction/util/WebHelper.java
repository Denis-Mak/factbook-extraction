package it.factbook.extraction.util;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.tika.io.TikaInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;

/**
 * Collection of methods to handle HTTP request, response and authentication.
 */
public class WebHelper {
    private static final Logger log = LoggerFactory.getLogger(WebHelper.class);

    private static final String DEFAULT_USER_AGENT = "factbook-robot";

    /**
     * Downloads a page from the provided URL
     *
     * @param url url of the page
     * @return content of the page
     * @throws IOException if page is unavailable
     */
    public static String getContent(String url) throws IOException{
        return getContent(url, DEFAULT_USER_AGENT);
    }

    /**
     * Downloads a page from the provided URL
     *
     * @param url url of the page
     * @param userAgent string to put into user-agent HTTP request property
     * @return content of the page
     * @throws IOException if page is unavailable
     */
    public static String getContent(String url, String userAgent) throws IOException {
        return getContent(url, userAgent, null, null);
    }

    /**
     * Downloads a page with authorization from the provided URL.
     *
     * @param url url of the page
     * @param userAgent string to put into user-agent HTTP request property
     * @param username login
     * @param password password for authorization
     * @return content of the page
     * @throws IOException if page is unavailable
     */
    public static String getContent(String url, String userAgent, String username, String password)
            throws IOException{
        return readInputStream(getInputStream(url, userAgent, username, password));
    }

    /**
     * Downloads a page with OAuth authorization from the provided URL.
     *
     * @param url url of the page
     * @param clientKey unique identifier of the client
     * @param clientSecret public part of the OAuth key
     * @return content of the page
     * @throws IOException if page is unavailable
     */
    public static String getContentOAuth(String url, String clientKey, String clientSecret)
            throws IOException{
        return readInputStream(getInputStreamOAuth(url, clientKey, clientSecret));
    }

    /**
     * Reads the provided {@link InputStream}
     *
     * @param is an instance of {@link InputStream}
     * @return page content as a string
     */
    private static String readInputStream(InputStream is){
        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader in = null;

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

    /**
     * Opens input stream of the provided URL with basic authentication.
     *
     * @param url url of the page
     * @param userAgent string to put into user-agent HTTP request property
     * @param username login
     * @param password password for authorization
     * @return content of the page
     * @throws IOException if page is unavailable
     */
    public static InputStream getInputStream(String url, String userAgent, String username, String password)
            throws IOException{
        InputStream is;
        URL myUrl = new URL(url);
        if (username != null) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            });
        }
        HttpURLConnection connection = (HttpURLConnection)myUrl.openConnection();
        if (userAgent != null) {
            System.setProperty("http.agent", "");
            connection.setRequestProperty("User-Agent", userAgent);
        }
        connection.setRequestMethod("GET");
        is = connection.getInputStream();

        return TikaInputStream.get(is);
    }

    /**
     * Opens input stream of the provided URL with OAuth authentication.
     *
     * @param url url of the page
     * @param clientKey unique identifier of the client
     * @param clientSecret public part of the OAuth key
     * @return content of the page
     * @throws IOException if page is unavailable
     */
    public static InputStream getInputStreamOAuth (String url, String clientKey, String clientSecret)
            throws IOException{
        OAuthConsumer consumer = new DefaultOAuthConsumer(clientKey, clientSecret);
        URL myUrl = new URL(url);
        HttpsURLConnection c = (HttpsURLConnection) myUrl.openConnection();
        try {
            log.info("Signing the oAuth consumer");
            consumer.sign(c);

        } catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
            log.error("Error signing the consumer", e);

        }
        c.connect();
        int responseCode = c.getResponseCode();
        InputStream is = responseCode==200?c.getInputStream():c.getErrorStream();

        return TikaInputStream.get(is);
    }

    /**
     * Decodes the specified URL from the <CODE>application/x-www-form-urlencoded</CODE> MIME format.
     *
     * @param url any URL as a string
     * @return decoded URL
     */
    public static String decodeURL(String url){
        try {
            String decodedUrl = URLDecoder.decode(url, "UTF-8");
            int startDecodedPart = getStartDecodedPart(url, decodedUrl);
            if (startDecodedPart > -1 && Character.isLetterOrDigit(decodedUrl.charAt(startDecodedPart))) {
                return decodedUrl;
            } else {
                return url;
            }
        } catch (UnsupportedEncodingException | IllegalArgumentException e){
            return url;
        }
    }

    private static int getStartDecodedPart(String rawUrl, String decodedUrl){
        int i = 0;
        char[] rawUrlArr = rawUrl.toCharArray();
        char[] decodedUrlArr = decodedUrl.toCharArray();
        for (; i < decodedUrl.length(); i++){
            if (rawUrlArr[i] != decodedUrlArr[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes all tags from text
     *
     * @param str input text
     * @return text cleaned of tags
     */
    public static String removeTags(String str){
        return str.replaceAll("<[^>]*>", "");
    }
}
