package com.example.subscriptiontracker;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.TimeZone;

public class CalendarHelper {

    public static void addEventToCalendar(Context context, Subscription subscription) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Events.DTSTART, subscription.getRenewalDate().getTime());
        values.put(CalendarContract.Events.DTEND, subscription.getRenewalDate().getTime());
        values.put(CalendarContract.Events.TITLE, "Subscription Renewal: " + subscription.getName());
        values.put(CalendarContract.Events.DESCRIPTION, "Renewal for " + subscription.getName());
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // Default calendar
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        if (subscription.isMonthly()) {
            values.put(CalendarContract.Events.RRULE, "FREQ=MONTHLY;COUNT=12");
        }

        cr.insert(CalendarContract.Events.CONTENT_URI, values);
    }
}
