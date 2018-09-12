package gsi.com.mdapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ApiManager {

    private WeakReference<Context> mWeakContext;

    public interface UrlResponse {
        public void onResponse(boolean response, String content);
        public void onError(String error);
    }

    public ApiManager(Context context) {
        mWeakContext = new WeakReference<>(context);
    }

    public void getUrl(final String urlStr, final UrlResponse handler) {
        IOExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                String content = getUrlInternal(urlStr, handler);
                if (handler != null) {
                    boolean isSuccess = content != null && !content.isEmpty();
                    handler.onResponse(isSuccess, content);
                }
            }
        });
    }

    private String getUrlInternal(final String urlStr, final UrlResponse handler) {
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
            String msg;
            if (isInternetAvailable(mWeakContext.get())) {
                msg = mWeakContext.get().getString(R.string.error_bad_network);
            } else {
                msg = mWeakContext.get().getString(R.string.error_no_internet);
            }
            if (handler != null) {
                handler.onError(msg);
            }
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

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public interface MockUrlResponse {
        public void onResponse(boolean response, JSONObject jsonObject);
    }
}