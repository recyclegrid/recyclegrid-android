package com.recyclegrid.core;

import android.content.Context;

import com.recyclegrid.app.R;

import java.util.Date;

public class TimeSpanHumanizer {
    private Context _context;

    public TimeSpanHumanizer(Context context) {
        _context = context;
    }

    public String Humanize(long milliseconds) {
        Date currentDate = new Date();
        long currentMilliseconds = currentDate.getTime();
        long difference = (currentMilliseconds - milliseconds) / 60000;

        String humanTimeSpan = _context.getString(R.string.now);

        if (difference >= 1 && difference < 2) {
            humanTimeSpan = _context.getString(R.string.minute_ago);
        }

        if (difference >= 2) {
            humanTimeSpan = _context.getString(R.string.minutes_ago, difference);
        }

        difference = difference / 60;

        if (difference >= 1 && difference < 2) {
            humanTimeSpan = _context.getString(R.string.hour_ago);
        }

        if (difference >= 2) {
            humanTimeSpan = _context.getString(R.string.hours_ago, difference);
        }

        difference = difference / 24;

        if (difference >= 1 && difference < 2) {
            humanTimeSpan = _context.getString(R.string.day_ago);
        }

        if (difference >= 2) {
            humanTimeSpan = _context.getString(R.string.days_ago, difference);
        }

        difference = difference / 7;

        if (difference >= 1 && difference < 2) {
            humanTimeSpan = _context.getString(R.string.week_ago);
        }

        if (difference >= 2) {
            humanTimeSpan = _context.getString(R.string.weeks_ago, difference);
        }

        difference = difference / 30;

        if (difference >= 1 && difference < 2) {
            humanTimeSpan = _context.getString(R.string.month_ago);
        }

        if (difference >= 2) {
            humanTimeSpan = _context.getString(R.string.months_ago, difference);
        }

        return humanTimeSpan;
    }
}
