package gsi.com.mdapp;

import android.util.Log;

public class MDALogger {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "MDALogger";

    public static void logStackTrace(Exception e) {
        if (DEBUG) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static void logMessage(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void logError(String error) {
        if (DEBUG) {
            Log.e(TAG, error);
        }
    }
}
