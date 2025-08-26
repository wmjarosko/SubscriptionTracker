package com.example.subscriptiontracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RenewalCheckWorker extends Worker {

    private static final String CHANNEL_ID = "subscription_renewal_channel";

    public RenewalCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        createNotificationChannel(context);

        File file = new File(context.getFilesDir(), "subscriptions.json");
        if (!file.exists()) {
            return Result.success(); // No data, nothing to do
        }

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type subscriptionListType = new TypeToken<ArrayList<Subscription>>(){}.getType();
            List<Subscription> subscriptions = gson.fromJson(reader, subscriptionListType);

            if (subscriptions == null) {
                return Result.success();
            }

            Calendar cal14Days = Calendar.getInstance();
            cal14Days.add(Calendar.DAY_OF_YEAR, 14);
            Date fourteenDaysFromNow = cal14Days.getTime();
            Date today = new Date();

            for (Subscription sub : subscriptions) {
                if (shouldNotify(sub, today, fourteenDaysFromNow)) {
                    sendNotification(context, sub);
                }
            }

        } catch (IOException e) {
            return Result.failure();
        }

        return Result.success();
    }

    private boolean shouldNotify(Subscription sub, Date today, Date fourteenDaysFromNow) {
        if (!sub.isActive() || sub.getRenewalDate() == null) {
            return false;
        }
        // Check if renewal is within the next 14 days
        boolean isUpcoming = sub.getRenewalDate().after(today) && sub.getRenewalDate().before(fourteenDaysFromNow);
        if (!isUpcoming) {
            return false;
        }
        // Check if snoozed
        if (sub.getSnoozedUntil() != null && sub.getSnoozedUntil().after(today)) {
            return false;
        }
        return true;
    }

    private void sendNotification(Context context, Subscription subscription) {
        // Intent for Snooze action
        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction("SNOOZE");
        snoozeIntent.putExtra("subscription_name", subscription.getName());
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, subscription.getName().hashCode(), snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent for Dismiss action
        Intent dismissIntent = new Intent(context, NotificationActionReceiver.class);
        dismissIntent.setAction("DISMISS");
        dismissIntent.putExtra("subscription_name", subscription.getName());
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, subscription.getName().hashCode() + 1, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_add) // Using the add icon for now
                .setContentTitle("Subscription Renewal")
                .setContentText(subscription.getName() + " is renewing soon!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_add, "Snooze 7 Days", snoozePendingIntent)
                .addAction(R.drawable.ic_add, "Dismiss", dismissPendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted. Can't post notification.
            // This should ideally not happen if permission is requested correctly in the UI.
            return;
        }
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(subscription.getName().hashCode(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Subscription Renewals";
            String description = "Channel for subscription renewal notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
