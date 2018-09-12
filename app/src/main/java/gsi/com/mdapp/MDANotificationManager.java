package gsi.com.mdapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class MDANotificationManager {

    public static final int NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_TAG = "notificationTag";

    private static final String CANCEL_ACTION_RECEIVER_NAME = "gsi.com.mdapp.NotificationCancelClickReceiver";

    private Context mContext;
    private NotificationManager mNotificationManager;

    public MDANotificationManager(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void cancel(String notificationTag, int notificationId) {
        mNotificationManager.cancel(notificationTag, notificationId);
    }

    public static void cancelAll(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    void sendMoveToBgNotification(String message, int soundType) {
        int pushType = MDA.NotificationType.MOVE_TO_BG;
        int notificationId = NOTIFICATION_ID;
        Intent acceptIntent = new Intent(mContext, MainActivity.class);
        acceptIntent.putExtra(MDA.NotificationType.KEY, pushType);
        acceptIntent.putExtra("message", message);
        acceptIntent.putExtra("notification_id", notificationId);
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent acceptPendingIntent = PendingIntent.getActivity(mContext, 0, acceptIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        PendingIntent deletePendingIntent = setMoveToBgPendingIntent(MDA.NotificationActionMethod.SWIPE_DISMISS, notificationId);


        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(R.drawable.ic_notif_accept, mContext.getString(R.string.accept_button_text), acceptPendingIntent).build();

        NotificationCompat.Action dismissAction = buildActionlessReject(pushType, notificationId);

        NotificationCompat.Builder builder = startNotificationBuild(message)
                .addAction(dismissAction)
                .addAction(acceptAction)
                .setDeleteIntent(deletePendingIntent);

        addNotificationSound(builder, soundType);
        Notification notification = completeNotificationBuild(builder);
        mNotificationManager.notify(NOTIFICATION_TAG, notificationId, notification);
    }

    private NotificationCompat.Action buildActionlessReject(int pushType, int notificationId) {
        int requestCode = pushType + MDA.NotificationActionMethod.REJECT;
        Intent dismissIntent = new Intent(CANCEL_ACTION_RECEIVER_NAME);
        dismissIntent.setClass(mContext, NotificationCancelClickReceiver.class);
        dismissIntent.putExtra("notification_id", notificationId);
        dismissIntent.putExtra(MDA.NotificationActionMethod.TAG, MDA.NotificationActionMethod.REJECT);
        dismissIntent.putExtra(MDA.NotificationType.KEY, pushType);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(mContext, requestCode, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action dismissAction = new NotificationCompat.Action.Builder(R.drawable.ic_notif_reject, mContext.getString(R.string.dismiss_button_text), dismissPendingIntent).build();
        return dismissAction;
    }


    private PendingIntent setMoveToBgPendingIntent(int cancelMethodType, int notificationId) {
        int pushType = MDA.NotificationType.MOVE_TO_BG;
        int requestCode = pushType + cancelMethodType;
        Intent intent = new Intent(CANCEL_ACTION_RECEIVER_NAME);
        intent.setClass(mContext, NotificationCancelClickReceiver.class);
        intent.putExtra("notification_id", notificationId);
        intent.putExtra(MDA.NotificationActionMethod.TAG, cancelMethodType);
        intent.putExtra(MDA.NotificationType.KEY, pushType);
        PendingIntent pendingtIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingtIntent;
    }

    private NotificationCompat.Builder startNotificationBuild(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentTitle(mContext.getResources().getString(R.string.app_name))
                .setContentText(message)
                .setOnlyAlertOnce(true)
                .setLights(Color.BLUE, 500, 500)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        return builder;
    }

    private Notification completeNotificationBuild(NotificationCompat.Builder builder) {
        return builder.build();
    }

    private void addNotificationSound(NotificationCompat.Builder builder, int soundType) {
        long vibratePattern[] = new long[] { 500, 500, 500, 500 };
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        switch (soundType) {
            case 0: { // Sound only
                builder.setSound(sound);
                break;
            }
            case 1: { // Sound and vibrate
                builder.setSound(sound);
                builder.setVibrate(vibratePattern);
                break;
            }
            case 2: { // Vibrate only
                builder.setVibrate(vibratePattern);
                break;
            }
            case 3: { // silent
                break;
            }
            default: {
                builder.setSound(sound);
                builder.setVibrate(vibratePattern);
                break;
            }
        }
    }

}
