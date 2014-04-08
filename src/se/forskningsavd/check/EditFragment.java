package se.forskningsavd.check;

import java.util.List;

import se.forskningsavd.check.database.ReminderDataSource;
import se.forskningsavd.check.model.Reminder;
import se.forskningsavd.check.model.ReminderList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class EditFragment extends Fragment {
	private static final String TAG = "EditFragment";

	protected static final String EXTRA_REMINDER = "REMINDER";

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
				Reminder r = mReminderAdapter.getItem(position);
				
				Intent i = new Intent(getActivity(), NewReminder.class);
				i.putExtra(EXTRA_REMINDER, r);
				startActivity(i);
			}
		});
		registerForContextMenu(lv);

		return view;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo aInfo = (AdapterContextMenuInfo) menuInfo;

		Reminder r =  mReminderAdapter.getItem(aInfo.position);

		menu.setHeaderTitle("Options for " + r.getName());
		menu.add(1, 3, 3, "Delete");
	}

	// This method is called when user selects an Item in the Context menu
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		Toast.makeText(getActivity(), "Item id ["+itemId+"]", Toast.LENGTH_SHORT).show();
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		if(itemId == 3){
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
	}

	private class ReminderArrayAdapter extends ArrayAdapter<Reminder> {
		private final Context mContext;
		private final List<Reminder> mReminders;

		public ReminderArrayAdapter(Context context, List<Reminder> values) {
			super(context, R.layout.reminder_edit_row, values);
			mContext = context;
			mReminders = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.reminder_edit_row, parent, false);
			Reminder r = mReminders.get(position);

			TextView nameView = (TextView) rowView.findViewById(R.id.name_textview);
			nameView.setText(r.getName());

			TextView intervalView = ((TextView) rowView.findViewById(R.id.interval_textview));
			intervalView.setText(""+r.getDayInterval());
			
			((TextView) rowView.findViewById(R.id.color_textview)).setBackgroundColor(r.getColor());
			
			((TextView) rowView.findViewById(R.id.max_count_textview)).setText(""+r.getMaxCheckCount());

			return rowView;
		}
	}
}
