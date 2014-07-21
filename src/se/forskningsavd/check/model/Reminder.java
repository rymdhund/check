package se.forskningsavd.check.model;

import java.io.Serializable;
import java.util.Date;

public class Reminder implements Serializable {
  private static final long serialVersionUID = 1L;

  private String name;
  private int    dayInterval;
  private int    count;
  private int    maxCount;
  private int    color;
  private Date   lastChecked;
  private long   dbId;

  public Reminder(String name, int dayInterval, int checkCount, int maxCheckCount, int color, long dbId) {
    this.name         = name;
    this.dayInterval  = dayInterval;
    this.count        = checkCount;
    this.maxCount     = maxCheckCount;
    this.color        = color;
    lastChecked       = null;
    this.dbId         = dbId;
  }

  public Reminder(String name, int dayInterval, int count, int maxCount, int color) {
    this(name, dayInterval, count, maxCount, color, 0);
  }

  public boolean isDone() {
    return count >= maxCount;
  }

  public void check() {
    if (count < maxCount) count++;
  }

  public String getName() {
    return name;
  }

  public int getCheckCount() {
    return count;
  }

  public int getMaxCheckCount() {
    return maxCount;
  }

  public long getDbId() {
    return dbId;
  }

  public int getColor() {
    return color;
  }

  public void setDbId(long id) {
    dbId = id;
  }

  public void setLastChecked(Date date) {
    lastChecked = date;
  }

  public boolean isDue() {
    if (lastChecked == null) return true;            // never checked it
    if (dayInterval == 0)     return false;          // one time check, don't show again
    if (maxCount > 1) {
      if (TimeUtils.daysFrom(lastChecked) == 0) {
        return !isDone();
      }
      return TimeUtils.daysFrom(lastChecked) >= dayInterval;
    } else {
      return TimeUtils.daysFrom(lastChecked) >= dayInterval;
    }
  }

  public int getDayInterval() {
    return dayInterval;
  }

  public String getDescription() {
    StringBuilder desc = new StringBuilder();
    if (dayInterval == 0) {
      // Non-repeated multicheck is a special case
      if (maxCount > 1) return String.format("%d times", maxCount);
      desc.append("Once");
    } else if (dayInterval == 1) {
      desc.append("Every day");
    } else {
      desc.append(String.format("Every %d days", dayInterval));
    }
    if (maxCount > 1) {
      desc.append(String.format(", %d times", maxCount));
    }
    return desc.toString();
  }
}
