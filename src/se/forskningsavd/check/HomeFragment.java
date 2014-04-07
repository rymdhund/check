package se.forskningsavd.check;

import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;

import se.forskningsavd.check.database.ReminderDataSource;
import se.forskningsavd.check.model.Reminder;
import se.forskningsavd.check.model.ReminderList;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class HomeFragment extends SherlockFragment {
	private static final String TAG = "HomeFragment";
	
	private ReminderArrayAdapter mReminderAdapter;
	private List<Reminder> mReminders = new ReminderList();

	private ReminderDataSource dataSource;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.home_fragment, container, false);
		
		ListView lv = (ListView) view.findViewById(R.id.reminder_listview);

		dataSource = new ReminderDataSource(getActivity());
		dataSource.open();

		initList();
		mReminderAdapter = new ReminderArrayAdapter(getActivity(), mReminders);

		lv.setAdapter(mReminderAdapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
					long id) {
				Toast.makeText(getActivity(), "Item with id ["+id+"] - Position ["+position+"]", Toast.LENGTH_SHORT).show();
				Reminder r = mReminderAdapter.getItem(position);
				dataSource.saveCheck(r);
				mReminderAdapter.notifyDataSetChanged();
			}
		});
		registerForContextMenu(lv);

        return view;
    }
	
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo aInfo = (AdapterContextMenuInfo) menuInfo;

		Reminder r =  mReminderAdapter.getItem(aInfo.position);

		menu.setHeaderTitle("Options for " + r.getName());
		menu.add(1, 1, 1, "Details");
		menu.add(1, 2, 2, "Uncheck");
		menu.add(1, 3, 3, "Delete");
	}

	// This method is called when user selects an Item in the Context menu
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		Toast.makeText(getActivity(), "Item id ["+itemId+"]", Toast.LENGTH_SHORT).show();
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		if(itemId == 2){
			Reminder r = mReminders.get((int)info.id);
			dataSource.uncheckCheck(r);
			mReminderAdapter.notifyDataSetChanged();
		}else if(itemId == 3){
			Log.d(TAG, "Deleting "+info.id);
			Reminder r = mReminders.get((int)info.id);
			dataSource.deleteReminder(r.getDbId());
			mReminders.remove((int)info.id);
			mReminderAdapter.notifyDataSetChanged();
		}
		return true;
	}

	private void initList() {
		mReminders = dataSource.getAllReminders();

		if(mReminders.isEmpty()){
			Log.d(TAG, "adding reminders");
			dataSource.saveReminder(new Reminder("backup", 7, Color.GREEN));
			dataSource.saveReminder(new Reminder("springa", 3, Color.YELLOW));
			dataSource.saveReminder(new Reminder("vatten", 3, 0, 5, Color.BLUE));
			dataSource.saveReminder(new Reminder("ring", 0, Color.MAGENTA));
			mReminders = dataSource.getAllReminders();
		}
	}

	private class ReminderArrayAdapter extends ArrayAdapter<Reminder> {
		private final Context mContext;
		private final List<Reminder> mReminders;

		public ReminderArrayAdapter(Context context, List<Reminder> values) {
			super(context, R.layout.reminder_row, values);
			mContext = context;
			mReminders = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.reminder_row, parent, false);
			Reminder r = mReminders.get(position);

			TextView doneView = (TextView) rowView.findViewById(R.id.done_textview);
			if(r.isDone()) doneView.setText("[done]");
			else if(r.getCheckCount() > 0) doneView.setText(""+r.getCheckCount()+"/"+r.getMaxCheckCount());
			else doneView.setText("");

			((TextView) rowView.findViewById(R.id.name_textview)).setText(r.getName());

			return rowView;
		}
	}
}
