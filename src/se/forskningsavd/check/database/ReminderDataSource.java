package se.forskningsavd.check.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.forskningsavd.check.model.Check;
import se.forskningsavd.check.model.Reminder;
import se.forskningsavd.check.model.TimeUtils;

public class ReminderDataSource {
  private static final String TAG = "RemindersDataSource";
  private static ReminderDataSource instance;

  private DatabaseHelper dbHelper;
  private ArrayList<DataChangedListener>  dataChangedListeners = new ArrayList<DataChangedListener>();

  private String[] allReminderColumns = {
      DatabaseHelper.COLUMN_REMINDERS_ID,
      DatabaseHelper.COLUMN_REMINDERS_NAME,
      DatabaseHelper.COLUMN_REMINDERS_DAY_INTERVAL,
      DatabaseHelper.COLUMN_REMINDERS_COUNT,
      DatabaseHelper.COLUMN_REMINDERS_MAX_COUNT,
      DatabaseHelper.COLUMN_REMINDERS_COLOR
  };

  public static ReminderDataSource getInstance(Context context){
    if(instance == null){
      instance = new ReminderDataSource(context);
    }
    return instance;
  }

  private ReminderDataSource(Context context) {
    dbHelper = new DatabaseHelper(context);
  }

  public void addDataChangedListener(DataChangedListener dcl) {
    dataChangedListeners.add(dcl);
  }

  private void notifyDataChangedListeners() {
    for (DataChangedListener dcl : dataChangedListeners) dcl.onDataChanged();
  }

  public void saveReminder(Reminder reminder) {
    if (reminder.getDbId() == 0) {
      insertNewReminder(reminder);
    } else {
      updateReminder(reminder);
    }
    notifyDataChangedListeners();
  }

  private void updateReminder(Reminder reminder) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    String        where  = DatabaseHelper.COLUMN_REMINDERS_ID + "=" + reminder.getDbId();

    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.COLUMN_REMINDERS_NAME,         reminder.getName());
    values.put(DatabaseHelper.COLUMN_REMINDERS_DAY_INTERVAL, reminder.getDayInterval());
    values.put(DatabaseHelper.COLUMN_REMINDERS_COUNT,        reminder.getCheckCount());
    values.put(DatabaseHelper.COLUMN_REMINDERS_MAX_COUNT,    reminder.getMaxCheckCount());
    values.put(DatabaseHelper.COLUMN_REMINDERS_COLOR,        reminder.getColor());

    db.update(DatabaseHelper.TABLE_REMINDERS, values, where, null);
  }

  private void insertNewReminder(Reminder reminder) {
    Log.d(TAG, "Inserting new reminder");

    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.COLUMN_REMINDERS_NAME,         reminder.getName());
    values.put(DatabaseHelper.COLUMN_REMINDERS_DAY_INTERVAL, reminder.getDayInterval());
    values.put(DatabaseHelper.COLUMN_REMINDERS_COUNT,        reminder.getCheckCount());
    values.put(DatabaseHelper.COLUMN_REMINDERS_MAX_COUNT,    reminder.getMaxCheckCount());
    values.put(DatabaseHelper.COLUMN_REMINDERS_COLOR,        reminder.getColor());

    SQLiteDatabase db = dbHelper.getWritableDatabase();
    long id = db.insert(DatabaseHelper.TABLE_REMINDERS, null, values);
    reminder.setDbId(id);
  }

  public void deleteReminder(long id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(
        DatabaseHelper.TABLE_REMINDERS,
        DatabaseHelper.COLUMN_REMINDERS_ID + " = " + id, null);
    notifyDataChangedListeners();
  }

  public List<Reminder> getAllReminders() {
    Log.d(TAG, "Getting all reminders");

    List<Reminder> reminders = new ArrayList<Reminder>();
    String orderBy = DatabaseHelper.COLUMN_REMINDERS_NAME + " ASC";

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor = db.query(DatabaseHelper.TABLE_REMINDERS,
        allReminderColumns, null, null, null, null, orderBy);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Reminder reminder = cursorToReminder(cursor);
      reminders.add(reminder);
      cursor.moveToNext();
    }
    cursor.close();
    return reminders;
  }

  public Reminder getReminder(long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String where  = DatabaseHelper.COLUMN_REMINDERS_ID + "=" + id;
    Cursor cursor = db.query(DatabaseHelper.TABLE_REMINDERS,
        allReminderColumns, where, null, null, null, null);

    Reminder reminder = null;
    cursor.moveToFirst();
    if (!cursor.isAfterLast()) {
      reminder = cursorToReminder(cursor);
    }
    cursor.close();
    return reminder;
  }

  private Reminder cursorToReminder(Cursor cursor) {
    long id         = cursor.getInt(0);
    String name     = cursor.getString(1);
    int dayInterval = cursor.getInt(2);
    int maxCount    = cursor.getInt(4);
    int color       = cursor.getInt(5);
    int count       = getCheckCount(id);

    Reminder r = new Reminder(name, dayInterval, count, maxCount, color, id);
    r.setLastChecked(getLastChecked(id));

    return r;
  }

  private Date getLastChecked(long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String timeColumn[] = {DatabaseHelper.COLUMN_CHECKS_TIME};
    String where        = DatabaseHelper.COLUMN_CHECKS_REMINDER_ID + "=" + id;
    String order        = DatabaseHelper.COLUMN_CHECKS_TIME + " desc";
    Cursor cursorChecks = db.query(DatabaseHelper.TABLE_CHECKS,
        timeColumn,
        where,
        null, null, null, order);

    Date lastChecked = null;
    cursorChecks.moveToFirst();
    if (!cursorChecks.isAfterLast()) {
      lastChecked = new Date(cursorChecks.getLong(0));
    }
    cursorChecks.close();
    return lastChecked;
  }

  private int getCheckCount(long reminderId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    long   dayStart      = TimeUtils.startOfDayTimestamp();
    long   dayEnd        = dayStart + TimeUtils.LENGTH_OF_A_DAY;
    String countColumn[] = {"COUNT(*)"};

    String where = DatabaseHelper.COLUMN_CHECKS_REMINDER_ID + "=" + reminderId
        + " and " + DatabaseHelper.COLUMN_CHECKS_TIME + " >= " + dayStart
        + " and " + DatabaseHelper.COLUMN_CHECKS_TIME + " < " + dayEnd;
    Cursor cursorChecks = db.query(DatabaseHelper.TABLE_CHECKS,
        countColumn,
        where,
        null, null, null, null);

    int count = 0;
    cursorChecks.moveToFirst();
    if (!cursorChecks.isAfterLast()) {
      count = cursorChecks.getInt(0);
    }
    cursorChecks.close();
    return count;
  }

  public void saveCheck(long reminderDbId) {
    Log.d(TAG, "save check "+reminderDbId);
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.COLUMN_CHECKS_REMINDER_ID, reminderDbId);
    values.put(DatabaseHelper.COLUMN_CHECKS_TIME,        System.currentTimeMillis());

    db.insert(DatabaseHelper.TABLE_CHECKS, null, values);
    notifyDataChangedListeners();
  }

  public void deleteCheck(long dbId) {
    Log.d(TAG, "delete delete " + dbId);

    SQLiteDatabase db = dbHelper.getWritableDatabase();
    String deleteWhere = DatabaseHelper.COLUMN_CHECKS_ID + "=" + dbId;
    db.delete(DatabaseHelper.TABLE_CHECKS, deleteWhere, null);
    notifyDataChangedListeners();
  }

  /**
   * Remove last check for this reminder
   *
   * @param dbId Id of reminder to uncheck
   */
  public void uncheckCheck(long dbId) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    String idColumn[] = {DatabaseHelper.COLUMN_CHECKS_ID};
    String where      = DatabaseHelper.COLUMN_CHECKS_REMINDER_ID + "=" + dbId;
    String order      = DatabaseHelper.COLUMN_CHECKS_TIME + " desc";
    Cursor cursor     = db.query(DatabaseHelper.TABLE_CHECKS,
        idColumn,
        where,
        null, null, null, order);

    cursor.moveToFirst();
    if (!cursor.isAfterLast()) {
      String deleteWhere = DatabaseHelper.COLUMN_CHECKS_ID + "=" + cursor.getInt(0);
      db.delete(DatabaseHelper.TABLE_CHECKS, deleteWhere, null);
    }
    cursor.close();
    notifyDataChangedListeners();
  }

  public List<Check> getAllChecks() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    ArrayList<Check> checks = new ArrayList<Check>();
    Cursor cursor = db.rawQuery(
        "SELECT c.*,r.name, r.color FROM " + DatabaseHelper.TABLE_CHECKS + " c "
        + "INNER JOIN " + DatabaseHelper.TABLE_REMINDERS + " r "
        + "ON c." + DatabaseHelper.COLUMN_CHECKS_REMINDER_ID + "=r." + DatabaseHelper.COLUMN_REMINDERS_ID
        + " ORDER BY c.time DESC", null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Check c = new Check(cursor.getString(3), cursor.getInt(4), cursor.getLong(2), cursor.getLong(0));
      checks.add(c);
      cursor.moveToNext();
    }
    cursor.close();

    return checks;
  }

  public List<Reminder> getDueReminders() {
    // TODO: optimize
    Log.d(TAG, "Getting due reminders");

    List<Reminder> all = getAllReminders();
    List<Reminder> due = new ArrayList<Reminder>();

    for (Reminder r : all) {
      if (r.isDue()) {
        due.add(r);
      }
    }
    return due;
  }
}