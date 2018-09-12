package gsi.com.mdapp;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Guy Sharony on 07/09/2018.
 */
public abstract class MDA {


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ACTION_TYPE_UNKNOWN, ACTION_TYPE_NOTIFICATION, ACTION_TYPE_LOCATION, ACTION_TYPE_ANIMATION, ACTION_TYPE_CALL})
    public @interface ActionType {}
    public static final String ACTION_TYPE_UNKNOWN= "unknown";
    public static final String ACTION_TYPE_NOTIFICATION = "notification";
    public static final String ACTION_TYPE_LOCATION = "location";
    public static final String ACTION_TYPE_ANIMATION = "animation";
    public static final String ACTION_TYPE_CALL = "call";

    public abstract class NotificationType {
        public static final int DEFAULT = -1;
        public static final int MOVE_TO_BG = 1001;
        public static final String KEY = "notif_type";
    }


    public abstract class NotificationActionMethod {
        public static final String TAG = "notif_cancel_method";
        public static final int DEFAULT = -1;
        public static final int ACCEPT = 1002;
        public static final int REJECT = 1000;
        public static final int SWIPE_DISMISS = 1001;
        public static final int DISCONNECT = 1003;
    }
}
