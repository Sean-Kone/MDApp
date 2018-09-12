package gsi.com.mdapp;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ApiManager {

    public interface UrlResponse {
        public void onResponse(boolean response, String content);
    }

    public void getUrl(final String urlStr, final UrlResponse handler) {
        IOExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                String content = getUrlInternal(urlStr);
                if (handler != null) {
                    boolean isSuccess = content != null && !content.isEmpty();
                    handler.onResponse(isSuccess, content);
                }
            }
        });
    }

    private String getUrlInternal(final String urlStr) {
        String result = null;
        HttpsURLConnection conn = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(urlStr);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("secret-key", "$2a$10$ILku3W3S5Xg3xFSVDfUj9eAM38BHLNAjSPe3M7cUA8wHvUBaCmGDi");
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setConnectTimeout(1000*4);
            conn.setReadTimeout(1000*4);
            conn.connect();

            int status = conn.getResponseCode();
            if (status == 200) {
                inputStream = conn.getInputStream();
                StringBuilder sb = new StringBuilder();
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                result = sb.toString();
            }
        } catch (MalformedURLException e) {
            MDALogger.logStackTrace(e);
        } catch (InterruptedIOException e) {
            MDALogger.logStackTrace(e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            MDALogger.logStackTrace(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                MDALogger.logStackTrace(e);
            }
        }
        return result;
    }

    public interface MockUrlResponse {
        public void onResponse(boolean response, JSONObject jsonObject);
    }
}