package se.forskningsavd.check;

import java.util.Random;

import se.forskningsavd.check.database.ReminderDataSource;
import se.forskningsavd.check.model.Reminder;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class NewReminder extends Activity {
	private static final String TAG = "NewReminder";
	
	private Reminder reminder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_reminder);
		
		Intent intent = getIntent();
		reminder = (Reminder)intent.getSerializableExtra(EditFragment.EXTRA_REMINDER);
		
		if(reminder != null){
			((EditText)findViewById(R.id.name_edittext)).setText(reminder.getName());
			((EditText)findViewById(R.id.interval_edittext)).setText(""+reminder.getDayInterval());
			((EditText)findViewById(R.id.max_count_edittext)).setText(""+reminder.getMaxCheckCount());
            findViewById(R.id.color_view).setBackgroundColor(reminder.getColor());
		}else{
			((EditText)findViewById(R.id.interval_edittext)).setText("1");
			((EditText)findViewById(R.id.max_count_edittext)).setText("1");
            findViewById(R.id.color_view).setBackgroundColor(randomColor());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_reminder, menu);
		return true;
	}
	
	public void makeNew(View view){
		Log.d(TAG, "Creating new reminder");
		String name = ((EditText)findViewById(R.id.name_edittext)).getText().toString();
		int interval = Integer.parseInt(
				((EditText)findViewById(R.id.interval_edittext)).getText().toString());
		int maxCount = Integer.parseInt(
				((EditText)findViewById(R.id.max_count_edittext)).getText().toString());
        int color = findViewById(R.id.color_view).getDrawingCacheBackgroundColor();
		
		Reminder r = new Reminder(name, interval, 0, maxCount, color);
		if(reminder != null){
			r.setDbId(reminder.getDbId());
			r.setColor(reminder.getColor());
		}
		ReminderDataSource rds = new ReminderDataSource(this);
		rds.open();
		rds.saveReminder(r);
		rds.close();
		
		Intent i = new Intent(this, TabbedActivity.class);
		startActivity(i);
	}
	
	private int randomColor(){
		Random r = new Random();
		return r.nextInt();
	}

}
