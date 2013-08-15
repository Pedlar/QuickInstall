package com.hostgator.quickinstall;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BufferedHttpEntity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

class Api {
    static private HttpClient httpClient;
    static private boolean logged_in;
    static private String httpCookie;
    static private MyRedirectHandler handler;
    static private boolean session_id = false;

    static private String username;
    static private String password;
    static private String domain;
    static private Context mContext;
    
    public Api(Context context, String un, String pw, String dom) {
        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter("http.protocol.single-cookie-header", true);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        
        handler = new MyRedirectHandler();
        ((AbstractHttpClient) httpClient).setRedirectHandler(handler);
        
        username = un;
        password = pw;
        domain = dom;
        mContext = context;
        
        logged_in = false;
        httpCookie = "";
    }
    
    public void login() {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("user", username));
        nameValuePairs.add(new BasicNameValuePair("pass", password));
        HttpResponse response = postData(domain, "login/", nameValuePairs);
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            response.getEntity().writeTo(byteStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String body = new String(byteStream.toString());
        if(handler.lastRedirectedUri != null) {
            Matcher matcher = Pattern.compile("\\/cpsess\\d+\\/")
                                     .matcher(handler.lastRedirectedUri.toString());
            if(matcher.find()) {
                httpCookie = matcher.group().replaceAll("\\/", "");
                logged_in = true;
                session_id = true;
                Log.d("QuickInstall", httpCookie);
            } else {
                Header[] headers = handler.lastResponse.getHeaders("Set-Cookie");
                for(Header header : headers) {
                    Log.d("QuickInstall", header.getName() + ": " + header.getValue());
                    if(header.getValue().contains("cpsession=")) {
                       httpCookie = header.getValue();
                       logged_in = true;
                    }
                }
            }
        }
    }
    
    private HttpResponse postData(String domain, String query, List<NameValuePair> nameValuePairs) {
        // Create a new HttpClient and Post Header
        HttpPost httppost;
        if(session_id) {
            httppost = new HttpPost("http://" + domain + ":2082/" + httpCookie + query);
        } else {
            httppost = new HttpPost("http://" + domain + ":2082/" + query);
        }
        try {
            if(httpCookie.length() > 0 && !session_id)
                httppost.setHeader("Cookie", httpCookie);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpClient.execute(httppost);
            return response;
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Log.e("QuickInstall", "Caught Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("QuickInstall", "Caught Exception");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    private HttpResponse postGet(String domain, String query) {
        // Create a new HttpClient and Post Header
        HttpGet httpget;
        if(session_id) {
            httpget = new HttpGet("http://" + domain + ":2082/" + httpCookie + query);
        } else {
            httpget = new HttpGet("http://" + domain + ":2082/" + query);
        }
        try {
            if(httpCookie.length() > 0 && !session_id)
                httpget.setHeader("Cookie", httpCookie);
            
            HttpResponse response = httpClient.execute(httpget);
            return response;
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Log.e("QuickInstall", "Caught Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("QuickInstall", "Caught Exception");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    private String getBody(HttpResponse response) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            response.getEntity().writeTo(byteStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String body = new String(byteStream.toString());
        return body;
    }
    
    public boolean isLoggedIn() {
        return logged_in;
    }
    
    public class MyRedirectHandler extends DefaultRedirectHandler {

        public URI lastRedirectedUri;
        public HttpResponse lastResponse;

        @Override
        public boolean isRedirectRequested(HttpResponse response, HttpContext context) {

            return super.isRedirectRequested(response, context);
        }

        @Override
        public URI getLocationURI(HttpResponse response, HttpContext context)
                throws ProtocolException {
            lastResponse = response;
            lastRedirectedUri = super.getLocationURI(response, context);

            return lastRedirectedUri;
        }

    }
    
    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
        sb.append(Integer.toHexString((array[i]
            & 0xFF) | 0x100).substring(1,3));        
        }
        return sb.toString();
    }
    
    public Bitmap getIcon(String uri) {
        HttpGet httpget = new HttpGet(uri);
        try {
            /* TODO: */
            /*Change to Assets folder*/
            Bitmap icon = null;
            File cacheDir = mContext.getCacheDir();
            byte[] byteHex = MessageDigest.getInstance("MD5").digest(uri.getBytes("CP1252"));
            String uriHex = hex(byteHex);
            File iconCache = new File(cacheDir.getAbsolutePath() + "/" + uriHex);
            Log.d("QuickInstall", "Cache Dir: " + cacheDir.getAbsolutePath());
            if((iconCache.exists())) { 
                //Log.d("QuickInstall", "Cache Icon: " + uri);
                FileInputStream fIn = new FileInputStream(iconCache);
                byte[] data = new byte[(int) iconCache.length()];
                fIn.read(data);
                fIn.close();
                icon = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
            /*
             * Need to thread this code, so if it doens't exist it will next time.
             {
                //Log.d("QuickInstall", "Get Icon: " + uri);
                HttpResponse response = httpClient.execute(httpget);
                BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(response.getEntity());
                icon = BitmapFactory.decodeStream(bufferedHttpEntity.getContent());
                if(icon != null) {
                    FileOutputStream fOus = new FileOutputStream(iconCache);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    icon.compress(CompressFormat.PNG, 0, bos);
                    fOus.write(bos.toByteArray());
                    fOus.flush();
                    fOus.close();
                }
            } else
             */
            return icon;
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Log.e("QuickInstall", "Caught Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("QuickInstall", "Caught Exception");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public String getInstallApps() {
        HttpResponse response = postGet(domain, "/json-api/cpanel?cpanel_jsonapi_module=QuickInstall&cpanel_jsonapi_func=app_list&cpanel_jsonapi_apiversion=2&names&nocache=1");
        String body = getBody(response);
        return body;
    }

    public String getAppList() {
        HttpResponse response = postGet(domain, "/json-api/cpanel?cpanel_jsonapi_module=QuickInstall&cpanel_jsonapi_func=manage_apps&cpanel_jsonapi_apiversion=2&names&nocache=1");
        String body = getBody(response);
        return body;
    }
    
    public String removeApplication(String id) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("id", id));
        HttpResponse response = postData(domain, "/json-api/cpanel?cpanel_jsonapi_module=QuickInstall&cpanel_jsonapi_func=uninstall&cpanel_jsonapi_apiversion=2&names&nocache=1", nameValuePairs);
        
        String body = getBody(response);
        return body;
    }
    
    public String upgradeApplication(String id) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("id", id));
        HttpResponse response = postData(domain, "/json-api/cpanel?cpanel_jsonapi_module=QuickInstall&cpanel_jsonapi_func=upgrade&cpanel_jsonapi_apiversion=2&names&nocache=1", nameValuePairs);
        
        String body = getBody(response);
        return body;
    }
    
    public String getApplicationInfo(String pkgName) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("app", pkgName));
        HttpResponse response = postData(domain, "/json-api/cpanel?cpanel_jsonapi_module=QuickInstall&cpanel_jsonapi_func=app_info&cpanel_jsonapi_apiversion=2&names&nocache=1", nameValuePairs);
        
        String body = getBody(response);
        return body;
    }
    
    public String installApplication(List<NameValuePair> nameValuePairs) {
        HttpResponse response = postData(domain, "/json-api/cpanel?cpanel_jsonapi_module=QuickInstall&cpanel_jsonapi_func=install&cpanel_jsonapi_apiversion=2&names&nocache=1", nameValuePairs);
        String body = getBody(response);
        return body;
    }
    
}