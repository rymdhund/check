package se.forskningsavd.check.database;

import java.util.Date;
import java.util.List;

import se.forskningsavd.check.model.Reminder;
import se.forskningsavd.check.model.ReminderList;
import se.forskningsavd.check.model.TimeUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ReminderDataSource {
	private static final String TAG = "RemindersDataSource";
	
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	
	private String[] allReminderColumns = { DatabaseHelper.COLUMN_REMINDERS_ID,
			DatabaseHelper.COLUMN_REMINDERS_NAME,
			DatabaseHelper.COLUMN_REMINDERS_DAY_INTERVAL,
			DatabaseHelper.COLUMN_REMINDERS_COUNT,
			DatabaseHelper.COLUMN_REMINDERS_MAX_COUNT,
			DatabaseHelper.COLUMN_REMINDERS_COLOR
	};
	
	public ReminderDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void saveReminder(Reminder reminder) {
		if(reminder.getDbId() == 0){
			insertNewReminder(reminder);
		}else{
			updateReminder(reminder);
		}
	}
	
	private void updateReminder(Reminder reminder) {
		String where = DatabaseHelper.COLUMN_REMINDERS_ID + "=" + reminder.getDbId();
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_REMINDERS_NAME, reminder.getName());
		values.put(DatabaseHelper.COLUMN_REMINDERS_DAY_INTERVAL, reminder.getName());
		values.put(DatabaseHelper.COLUMN_REMINDERS_COUNT, reminder.getCheckCount());
		values.put(DatabaseHelper.COLUMN_REMINDERS_MAX_COUNT, reminder.getMaxCheckCount());
		values.put(DatabaseHelper.COLUMN_REMINDERS_COLOR, reminder.getColor());
		database.update(DatabaseHelper.TABLE_REMINDERS, values, where, null);
	}

	private void insertNewReminder(Reminder reminder){
		Log.d(TAG, "Inserting new reminder");
		
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_REMINDERS_NAME, reminder.getName());
		values.put(DatabaseHelper.COLUMN_REMINDERS_DAY_INTERVAL, reminder.getName());
		values.put(DatabaseHelper.COLUMN_REMINDERS_COUNT, reminder.getCheckCount());
		values.put(DatabaseHelper.COLUMN_REMINDERS_MAX_COUNT, reminder.getMaxCheckCount());
		values.put(DatabaseHelper.COLUMN_REMINDERS_COLOR, reminder.getColor());
		
		long id = database.insert(DatabaseHelper.TABLE_REMINDERS, null,
				values);
		reminder.setDbId(id);
	}
	
	public void deleteReminder(long id) {
		database.delete(DatabaseHelper.TABLE_REMINDERS,
				DatabaseHelper.COLUMN_REMINDERS_ID + " = " + id, null);
	}

	public List<Reminder> getAllReminders() {
		Log.d(TAG, "Getting all reminders");
		
		List<Reminder> reminders = new ReminderList();

		Cursor cursor = database.query(DatabaseHelper.TABLE_REMINDERS,
				allReminderColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Reminder reminder = cursorToReminder(cursor);
			reminders.add(reminder);
			cursor.moveToNext();
		}
		cursor.close();
		return reminders;
	}
	
	private Reminder cursorToReminder(Cursor cursor) {
		long id = cursor.getInt(0);
		String name = cursor.getString(1);
		int dayInterval = cursor.getInt(2);
		int maxCount = cursor.getInt(4);
		int color = cursor.getInt(5);
		int count = getCheckCount(id);
		
		
		
		Reminder r = new Reminder(name, dayInterval, count, maxCount, color, id);
		
		r.setLastChecked(getLastChecked(id));

		
		
		return r;
	}
	
	private Date getLastChecked(long id) {
		String timeColumn[] =  {DatabaseHelper.COLUMN_CHECKS_TIME};
		String where = DatabaseHelper.COLUMN_CHECKS_REMINDER_ID +"="+id;
		String order = DatabaseHelper.COLUMN_CHECKS_TIME + " desc";
		Cursor cursorChecks = database.query(DatabaseHelper.TABLE_CHECKS,
				timeColumn,
				where, 
				null, null, null, order);
		cursorChecks.moveToFirst();
		Date lastChecked = null;
		if(!cursorChecks.isAfterLast()){
			lastChecked = new Date(cursorChecks.getLong(0));
		}
		cursorChecks.close();
		return lastChecked;
	}

	private int getCheckCount(long reminderId){
		long dayStart = TimeUtils.startOfDayTimestamp();
		long dayEnd = dayStart+TimeUtils.LENGTH_OF_A_DAY;
		String countColumn[] =  {"COUNT(*)"};
		String where = DatabaseHelper.COLUMN_CHECKS_REMINDER_ID +"="+reminderId
				+" and " + DatabaseHelper.COLUMN_CHECKS_TIME + " >= "+dayStart
				+" and " + DatabaseHelper.COLUMN_CHECKS_TIME + " < "+dayEnd;
		Cursor cursorChecks = database.query(DatabaseHelper.TABLE_CHECKS,
				countColumn,
				where, 
				null, null, null, null);
		cursorChecks.moveToFirst();
		int count = 0;
		if(!cursorChecks.isAfterLast()){
			count = cursorChecks.getInt(0);
		}
		cursorChecks.close();
		return count;
	}

	public void saveCheck(Reminder r) {
		Log.d(TAG, "Inserting new check");
		
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_CHECKS_REMINDER_ID, r.getDbId());
		values.put(DatabaseHelper.COLUMN_CHECKS_TIME, System.currentTimeMillis());
		
		database.insert(DatabaseHelper.TABLE_CHECKS, null,
				values);
		r.setLastChecked(new Date());
		r.check();
	}

	/**
	 * Remove last check for this reminder
	 * 
	 * @param r
	 */
	public void uncheckCheck(Reminder r) {
		String idColumn[] = {DatabaseHelper.COLUMN_CHECKS_ID};
		String where = DatabaseHelper.COLUMN_CHECKS_REMINDER_ID +"="+r.getDbId();
		String order = DatabaseHelper.COLUMN_CHECKS_TIME + " desc";
		Cursor cursor = database.query(DatabaseHelper.TABLE_CHECKS,
				idColumn,
				where, 
				null, null, null, order);
		cursor.moveToFirst();
		if(!cursor.isAfterLast()){
			String deleteWhere = DatabaseHelper.COLUMN_CHECKS_ID + "="+cursor.getInt(0);
			database.delete(DatabaseHelper.TABLE_CHECKS, deleteWhere, null);
		}
		cursor.close();
		
		r.setLastChecked(getLastChecked(r.getDbId()));
		r.setCheckCount(getCheckCount(r.getDbId()));

		
	}
}