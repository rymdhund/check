package se.forskningsavd.check;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Random;

import se.forskningsavd.check.database.ReminderDataSource;
import se.forskningsavd.check.model.Reminder;

public class NewReminder extends Activity implements ColorPickerDialog.OnColorChangedListener {
  private static final String TAG         = "NewReminder";
  private static final String STATE_COLOR = "reminderColor";

  private Reminder  reminder;
  private int       color;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_reminder);

    Intent intent = getIntent();
    reminder = (Reminder) intent.getSerializableExtra(EditFragment.EXTRA_REMINDER);

    if (reminder != null) {
      ((EditText) findViewById(R.id.name_edittext)     ).setText(reminder.getName());
      ((EditText) findViewById(R.id.interval_edittext) ).setText("" + reminder.getDayInterval());
      ((EditText) findViewById(R.id.max_count_edittext)).setText("" + reminder.getMaxCheckCount());
      color = reminder.getColor();
      if (reminder.getDayInterval() != 0) {
        ((CheckBox) findViewById(R.id.interval_checkbox)).setChecked(true);
        findViewById(R.id.text1            ).setEnabled(true);
        findViewById(R.id.interval_edittext).setEnabled(true);
        findViewById(R.id.text2            ).setEnabled(true);
      }
    } else {
      ((EditText) findViewById(R.id.interval_edittext) ).setText("1");
      ((EditText) findViewById(R.id.max_count_edittext)).setText("1");
      color = randomColor();
    }

    if (savedInstanceState != null) {
      // Restore color from saved state
      color = savedInstanceState.getInt(STATE_COLOR);
    }

    findViewById(R.id.color_view).setBackgroundColor(color);

    final NewReminder parent = this;
    findViewById(R.id.color_view).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new ColorPickerDialog(parent, parent, color).show();
      }
    });
  }

  public void onIntervalCheckBoxClick(View view) {
    if (((CheckBox) view).isChecked()) {
      findViewById(R.id.text1            ).setEnabled(true);
      findViewById(R.id.interval_edittext).setEnabled(true);
      findViewById(R.id.text2            ).setEnabled(true);
    } else {
      findViewById(R.id.text1            ).setEnabled(false);
      findViewById(R.id.interval_edittext).setEnabled(false);
      findViewById(R.id.text2            ).setEnabled(false);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.new_reminder, menu);
    return true;
  }

  public void makeNew(View view) {
    Log.d(TAG, "Creating new reminder");
    String name = ((EditText) findViewById(R.id.name_edittext)).getText().toString();
    int interval = 0;
    if (((CheckBox) findViewById(R.id.interval_checkbox)).isChecked()) {
      interval = Integer.parseInt(
          ((EditText) findViewById(R.id.interval_edittext)).getText().toString());
    }

    int maxCount = Integer.parseInt(
        ((EditText) findViewById(R.id.max_count_edittext)).getText().toString());

    Reminder r = new Reminder(name, interval, 0, maxCount, color);
    if (reminder != null) {
      r.setDbId(reminder.getDbId());
    }
    ReminderDataSource rds = ReminderDataSource.getInstance(this);
    rds.saveReminder(r);

    Intent i = new Intent(this, TabbedActivity.class);
    startActivity(i);
  }

  private int randomColor() {
    Random r = new Random();
    return r.nextInt();
  }

  @Override
  public void colorChanged(int color) {
    this.color = color;
    findViewById(R.id.color_view).setBackgroundColor(color);
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putInt(STATE_COLOR, color);
    super.onSaveInstanceState(savedInstanceState);
  }
}
