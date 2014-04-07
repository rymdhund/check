package se.forskningsavd.check.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "reminders.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_REMINDERS = "reminders";
	public static final String COLUMN_REMINDERS_ID = "_id";
	public static final String COLUMN_REMINDERS_NAME = "name";
	public static final String COLUMN_REMINDERS_DAY_INTERVAL = "day_interval";
	public static final String COLUMN_REMINDERS_COUNT = "count";
	public static final String COLUMN_REMINDERS_MAX_COUNT = "max_count";
	public static final String COLUMN_REMINDERS_COLOR = "color";
	
	
	public static final String TABLE_CHECKS = "checks";
	public static final String COLUMN_CHECKS_ID = "_id";
	public static final String COLUMN_CHECKS_REMINDER_ID = "reminder_id";
	public static final String COLUMN_CHECKS_TIME = "time";

	
	private static final String REMINDERS_TABLE_CREATE = "CREATE TABLE "
			+ TABLE_REMINDERS + " ("
			+ COLUMN_REMINDERS_ID	+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ COLUMN_REMINDERS_NAME + " TEXT,"
			+ COLUMN_REMINDERS_DAY_INTERVAL + " INTEGER,"
			+ COLUMN_REMINDERS_COUNT + " INTEGER,"
			+ COLUMN_REMINDERS_MAX_COUNT + " INTEGER,"
			+ COLUMN_REMINDERS_COLOR + " INTEGER)";
	
	private static final String CHECKS_TABLE_CREATE = "CREATE TABLE "
			+ TABLE_CHECKS + " ("
			+ COLUMN_CHECKS_ID	+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ COLUMN_CHECKS_REMINDER_ID	+ " INTEGER,"
			+ COLUMN_CHECKS_TIME	+ " INTEGER)";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(REMINDERS_TABLE_CREATE);
		database.execSQL(CHECKS_TABLE_CREATE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
		onCreate(db);
	}
}
