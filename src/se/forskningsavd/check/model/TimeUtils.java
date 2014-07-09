package se.forskningsavd.check.model;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
  public static final long LENGTH_OF_A_DAY = 24L * 3600L * 1000L;

  public static long startOfDayTimestamp() {
    return startOfDayTimestamp(new Date());
  }

  public static long startOfDayTimestamp(Date d) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    return cal.getTimeInMillis();
  }

  public static long startOfDayTimestamp(long timestamp) {
    return startOfDayTimestamp(new Date(timestamp));
  }

  public static int daysFrom(long timestamp) {
    return daysFrom(new Date(timestamp));
  }

  public static int daysFrom(Date d) {
    long t1 = startOfDayTimestamp(d);
    long t2 = startOfDayTimestamp();
    return (int) ((t2 - t1) / LENGTH_OF_A_DAY);
  }

  public static boolean isSameDate(long timestamp1, long timestamp2) {
    long diff = timestamp2 - startOfDayTimestamp(timestamp1);
    return diff >= 0L && diff < LENGTH_OF_A_DAY;
  }

  public static String getRelativeDay(long timestamp) {
    int d = daysFrom(timestamp);

    if (d == 0) return "today";
    if (d == 1) return "yesterday";
    return "" + d + "days ago";
  }
}
