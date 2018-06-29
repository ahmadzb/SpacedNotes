package com.diplinkblaze.spacednote.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.note.NoteToPdfService;

import java.util.ArrayList;

/**
 * Created by Ahmad on 06/28/18.
 * All rights reserved.
 */
public class ExportNotifications {

    private static final String CHANNEL_ID = "export";
    private static final int EXPORT_NOTE_TO_PDF_NOTIFICATION_ID = 0;
    private static final int EXPORT_FAILURE_ID = 1;

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_export);
            String description = context.getString(R.string.channel_export_description);
            int importance = NotificationManager.IMPORTANCE_MIN;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void postNoteToPdfNotification(Context context) {
        createChannel(context);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setSmallIcon(R.drawable.ic_import_export_black_24dp);
        builder.setContentTitle(context.getString(R.string.exporting));
        String state = "";
        ArrayList<String> logList = NoteToPdfService.getExportState().getLogListCapture();
        if (!logList.isEmpty()) {
            state = logList.get(0);
        }
        builder.setContentText(state);
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(EXPORT_NOTE_TO_PDF_NOTIFICATION_ID, builder.build());
    }

    public static void postExportFailureNotification(Context context) {
        createChannel(context);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setSmallIcon(R.drawable.ic_import_export_black_24dp);
        builder.setContentTitle(context.getString(R.string.export));
        builder.setContentText(context.getString(R.string.export_failed));
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setOngoing(false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(EXPORT_FAILURE_ID, builder.build());
    }

    public static void dismissExportNoteToPdfNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(EXPORT_NOTE_TO_PDF_NOTIFICATION_ID);
    }
    public static void dismissExportFailureNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(EXPORT_FAILURE_ID);
    }
}
