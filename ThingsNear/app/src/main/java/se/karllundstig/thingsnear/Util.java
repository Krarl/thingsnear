package se.karllundstig.thingsnear;

import android.content.Context;

import java.util.concurrent.TimeUnit;

public final class Util {
    //ska inte gå att skapa
    private Util() {}

    public static String GetTimeLength(long length, Context context) {
        int seconds = (int) TimeUnit.SECONDS.convert(length, TimeUnit.MILLISECONDS);
        int minutes = (int)TimeUnit.MINUTES.convert(length, TimeUnit.MILLISECONDS);
        int hours = (int)TimeUnit.HOURS.convert(length, TimeUnit.MILLISECONDS);
        int days = (int)TimeUnit.DAYS.convert(length, TimeUnit.MILLISECONDS);
        int months = days / 30;
        int years = months / 12;

        String elapsed;
        if (years > 0)
            elapsed = years + " " + (years == 1 ? context.getString(R.string.year) : context.getString(R.string.years));
        else if (months > 0)
            elapsed = months + " " + (months == 1 ? context.getString(R.string.month) : context.getString(R.string.months));
        else if (days > 0)
            elapsed = days + " " + (days == 1 ? context.getString(R.string.day) : context.getString(R.string.days));
        else if (hours > 0)
            elapsed = hours + " " + (hours == 1 ? context.getString(R.string.hour) : context.getString(R.string.hours));
        else if (minutes > 0)
            elapsed = minutes + " " + (minutes == 1 ? context.getString(R.string.minute) : context.getString(R.string.minutes));
        else
            elapsed = seconds + " " + (seconds == 1 ? context.getString(R.string.second) : context.getString(R.string.seconds));

        return elapsed;
    }

    public static double CalculateDistance(double lat1, double long1, double lat2, double long2) {
        //hämtad från http://stackoverflow.com/a/123305
        double earthRadius = 6371000.0; //meter
        double dLat = Math.toRadians(lat2 - lat1);
        double dLong = Math.toRadians(long2 - long1);
        double sindLat = Math.sin(dLat / 2);
        double sindLong = Math.sin(dLong / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLong, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
