package com.example.subscriptiontracker;

import android.app.Application;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class SubscriptionTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleRenewalChecks();
    }

    private void scheduleRenewalChecks() {
        PeriodicWorkRequest renewalCheckWorkRequest =
                new PeriodicWorkRequest.Builder(RenewalCheckWorker.class, 24, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "renewal_check_work",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                renewalCheckWorkRequest);
    }
}
