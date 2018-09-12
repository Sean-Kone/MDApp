package gsi.com.mdapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MDAApp extends Application implements Application.ActivityLifecycleCallbacks {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({APP_STATE_FOREGROUND, APP_STATE_BACKGROUND})
    public @interface AppState {}
    public static final int APP_STATE_FOREGROUND = 1000;
    public static final int APP_STATE_BACKGROUND = 1001;

    @AppState private int mAppState;
    private int mNumStarted;
    private boolean mIsBgNotif;


    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @AppState
    public int getAppState() {
        return mAppState;
    }


    public void setBgNotif(boolean isNotif) {
        mIsBgNotif = isNotif;
    }

    public boolean isBgNotif() {
        return mIsBgNotif;
    }

    /**
     * {@link Application.ActivityLifecycleCallbacks} interface methods
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        if (mNumStarted == 0) {
            mAppState = APP_STATE_FOREGROUND;
        }
        mNumStarted++;
    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        mNumStarted--;
        if (mNumStarted == 0) {
            mAppState = APP_STATE_BACKGROUND;

            if (mIsBgNotif) {
                new MDANotificationManager(this).sendMoveToBgNotification(getString(R.string.notif_move_to_bg_msg), 1);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}
