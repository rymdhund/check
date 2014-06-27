package se.forskningsavd.check.model;

/**
 * Created by oau on 6/25/14.
 */
public class Check {
    private final String reminderName;
    private final long timestamp;
    private final long dbId;
    private final int reminderColor;

    public Check(String reminderName, int reminderColor, long timestamp, long dbId){
        this.reminderName = reminderName;
        this.timestamp = timestamp;
        this.dbId = dbId;
        this.reminderColor = reminderColor;
    }


    public String getReminderName() {
        return reminderName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDbId() {
        return dbId;
    }

    public int getReminderColor() {
        return reminderColor;
    }
}
