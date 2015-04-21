package com.example.david.androidtest;

import android.content.SharedPreferences;
import android.util.Pair;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by David on 01/05/14.
 */
public class WebClient {
    SharedPreferences sharedPreferences;

    private static HttpClient httpclient = getHttpClient();

    private static HttpContext localContext = new BasicHttpContext();

    private static String makeRequest(String url) throws Exception {
        return makeRequest(url, Collections.<NameValuePair>emptyList());
    }

    /**
     * Make an HTTP Request
     *
     * @param url
     * @param post
     * @return
     */
    private static String makeRequest(String url, List<NameValuePair> post) throws Exception {
        String returnContent = null;

        // Create a new HttpClient and Post Header
        HttpPost httppost = new HttpPost(url);

        try {

            // POST
            httppost.setEntity(new UrlEncodedFormEntity(post));

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] {
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
                    }
            }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });


            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost, localContext);

            // TODO: Check if this way is efficient
            returnContent = EntityUtils.toString(response.getEntity());

        } catch (ClientProtocolException e) {
            throw new Exception("UNKNOWN_ERROR");
        } catch (IOException e) {
            throw new Exception("NETWORK_ERROR");
        }

        return returnContent;
    }

    /**
     * Insert log
     *
     * @throws Exception
     */
    public static Boolean insertLocationLog(String url, String carId, String latitude, String longitude) throws Exception {

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("carId", carId));
        postData.add(new BasicNameValuePair("latitude", latitude));
        postData.add(new BasicNameValuePair("longitude", longitude));

        String content = makeRequest(url, postData);

        return true;
    }

    public static ServerSettings getServerSettings(String url) {
        ServerSettings serverSettings = new ServerSettings();

        try {
            String content = makeRequest(url);
            JSONObject jsonObject = new JSONObject(content);

            Integer serverPingTime = Integer.valueOf(jsonObject.getString("SERVER_PING_TIME"));
            Boolean serverAlert = jsonObject.getString("ALERT").equals("On");

            serverSettings.setServerPingTime(serverPingTime);
            serverSettings.setServerAlert(serverAlert);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return serverSettings;
    }

    public static HttpClient getHttpClient() {

        DefaultHttpClient client = null;

        try {

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new CustomSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            // Setting up parameters
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "utf-8");
            params.setBooleanParameter("http.protocol.expect-continue", false);

            // Setting timeout
            HttpConnectionParams.setConnectionTimeout(params, 30000);
            HttpConnectionParams.setSoTimeout(params, 30000);

            // Registering schemes for both HTTP and HTTPS
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            // Creating thread safe client connection manager
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            // Creating HTTP client
            client = new DefaultHttpClient(ccm, params);

            // Registering user name and password for authentication
            //client.getCredentialsProvider().setCredentials(
            //        new AuthScope(null, -1),
            //        new UsernamePasswordCredentials(mUsername, mPassword));

        } catch (Exception e) {
            client = new DefaultHttpClient();
        }

        return client;

    }

}
