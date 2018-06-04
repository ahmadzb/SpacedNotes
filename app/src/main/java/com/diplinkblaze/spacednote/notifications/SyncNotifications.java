package com.diplinkblaze.spacednote.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.main.SyncActivity;
import com.diplinkblaze.spacednote.main.SyncService;

import java.util.ArrayList;

/**
 * Created by Ahmad on 05/26/18.
 * All rights reserved.
 */
public class SyncNotifications {

    private static final String CHANNEL_ID = "sync";
    private static final int SYNC_NOTIFICATION_ID = 0;
    private static final int SYNC_FAILURE_ID = 1;

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_sync);
            String description = context.getString(R.string.channel_sync_description);
            int importance = NotificationManager.IMPORTANCE_MIN;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void postSyncStateNotification(Context context) {
        createChannel(context);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setSmallIcon(R.drawable.ic_sync_white_24dp);
        builder.setContentTitle(context.getString(R.string.syncing));
        String state = "";
        ArrayList<String> logList = SyncService.getSyncState().getLogListCapture();
        if (!logList.isEmpty()) {
            state = logList.get(0);
        }
        builder.setContentText(state);
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setOngoing(true);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(context, 0, SyncActivity.getVisitIntent(context), 0);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(SYNC_NOTIFICATION_ID, builder.build());
    }

    public static void postSyncFailureNotification(Context context) {
        createChannel(context);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setSmallIcon(R.drawable.ic_sync_white_24dp);
        builder.setContentTitle(context.getString(R.string.sync));
        builder.setContentText(context.getString(R.string.sync_failed));
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setOngoing(false);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(context, 0, SyncActivity.getVisitIntent(context), 0);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(SYNC_FAILURE_ID, builder.build());
    }

    public static void dismissSyncStateNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(SYNC_NOTIFICATION_ID);
    }
    public static void dismissSyncFailureNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(SYNC_FAILURE_ID);
    }
}
