package gsi.com.mdapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationCancelClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int pushType = intent.getIntExtra(MDA.NotificationType.KEY, MDA.NotificationType.DEFAULT);
        switch (pushType) {
            case MDA.NotificationType.MOVE_TO_BG: {
                onReceiveMoveToBgRequest(context, intent);
                break;
            }
        }
    }

    private void onReceiveActionlessReject(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notification_id", -1);
        int cancelMethod = intent.getIntExtra(MDA.NotificationActionMethod.TAG, MDA.NotificationActionMethod.DEFAULT);
        switch (cancelMethod) {
            case MDA.NotificationActionMethod.REJECT: {
                dismissNotification(context, notificationId);
                break;
            }
        }
    }

    private void onReceiveMoveToBgRequest(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notification_id", -1);
        int cancelMethod = intent.getIntExtra(MDA.NotificationActionMethod.TAG, MDA.NotificationActionMethod.DEFAULT);
        switch (cancelMethod) {
            case MDA.NotificationActionMethod.ACCEPT: {
                dismissNotification(context, notificationId);
                break;
            }
            case MDA.NotificationActionMethod.REJECT: {
                dismissNotification(context, notificationId);
                break;
            }
            case MDA.NotificationActionMethod.SWIPE_DISMISS: {
                break;
            }
        }
    }

    private void dismissNotification(Context context, int notificationId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(MDANotificationManager.NOTIFICATION_TAG, notificationId);
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }
}
