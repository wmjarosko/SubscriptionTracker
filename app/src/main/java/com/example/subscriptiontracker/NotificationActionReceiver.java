package com.example.subscriptiontracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String subscriptionName = intent.getStringExtra("subscription_name");

        if (action == null || subscriptionName == null) {
            return;
        }

        File file = new File(context.getFilesDir(), "subscriptions.json");
        if (!file.exists()) {
            return;
        }

        List<Subscription> subscriptions = loadSubscriptions(file);
        if (subscriptions == null) {
            return;
        }

        Subscription targetSubscription = null;
        for (Subscription sub : subscriptions) {
            if (sub.getName().equals(subscriptionName)) {
                targetSubscription = sub;
                break;
            }
        }

        if (targetSubscription == null) {
            return;
        }

        if ("SNOOZE".equals(action)) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 7);
            targetSubscription.setSnoozedUntil(cal.getTime());
        } else if ("DISMISS".equals(action)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(targetSubscription.getRenewalDate());
            cal.add(Calendar.DAY_OF_YEAR, 1); // Snooze until after renewal
            targetSubscription.setSnoozedUntil(cal.getTime());
        }

        saveSubscriptions(file, subscriptions);

        // Cancel the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(subscriptionName.hashCode());
    }

    private List<Subscription> loadSubscriptions(File file) {
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type subscriptionListType = new TypeToken<ArrayList<Subscription>>(){}.getType();
            return gson.fromJson(reader, subscriptionListType);
        } catch (IOException e) {
            return null;
        }
    }

    private void saveSubscriptions(File file, List<Subscription> subscriptions) {
        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new Gson();
            gson.toJson(subscriptions, writer);
        } catch (IOException e) {
            // Log error or handle
        }
    }
}
