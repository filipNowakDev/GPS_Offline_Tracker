package com.filipnowakdev.gps_offline_tracker.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.activities.MainActivity;

public class NotificationService
{
    private static final int RECORDING_NOTIFICATION_ID = 0;

    private final static String NOTIFICATION_CHANNEL_ID = "com.filipnowakdev.gps_offline_tracker.notifications";
    private NotificationCompat.Builder recordingNotificationBuilder;
    private Context context;
    private PendingIntent mainActivityIntent;

    public NotificationService(Context context)
    {
        this.context = context;
        initIntent(context);

        createNotificationChannel(context);
        initNotificationBuilder(context);
    }

    private void initIntent(Context context)
    {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.mainActivityIntent = PendingIntent.getActivity(context, 0, intent, 0);
    }

    private void initNotificationBuilder(Context context)
    {
        this.recordingNotificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.tape)
                .setContentTitle("GPS Tracker")
                .setContentText("Recording is on.")
                .setContentIntent(mainActivityIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);
    }

    private void createNotificationChannel(Context context)
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    public void displayRecordingNotification()
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.context);
        notificationManager.notify(RECORDING_NOTIFICATION_ID, recordingNotificationBuilder.build());
    }

    public void hideRecordingNotification()
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.context);
        notificationManager.cancel(RECORDING_NOTIFICATION_ID);
    }


}
