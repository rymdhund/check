package se.forskningsavd.check;

import java.util.ArrayList;
import java.util.List;

import se.forskningsavd.check.database.DataChangedListener;
import se.forskningsavd.check.database.ReminderDataSource;
import se.forskningsavd.check.model.Reminder;
import se.forskningsavd.check.model.ReminderList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class HomeFragment extends Fragment implements DataChangedListener {
	private static final String TAG = "HomeFragment";
    //private final TabbedActivity mTabbedActivity;

    private HomeReminderAdapter mReminderAdapter;

	private ReminderDataSource dataSource;
    private ArrayList<DataChangedListener> dataChangedListeners = new ArrayList<DataChangedListener>();

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.home_fragment, container, false);
		
		ListView lv = (ListView) view.findViewById(R.id.reminder_listview);

		dataSource = new ReminderDataSource(getActivity());
		dataSource.open();

		initList();
        mReminderAdapter = new HomeReminderAdapter(getActivity(), dataSource);

		lv.setAdapter(mReminderAdapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
					long id) {
				Reminder r = mReminderAdapter.getItem(position);
				dataSource.saveCheck(r);
				mReminderAdapter.notifyDataSetChanged();
                notifyDataChangedListeners();
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
		menu.add(1, 1, 1, "Edit");
		menu.add(1, 2, 2, "Uncheck");
		menu.add(1, 3, 3, "Delete");
	}

	// This method is called when user selects an Item in the Context menu
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.d(TAG, "context menu selected. Item "+itemId+" info.id: "+info.id);

		if(itemId == 1){
			Reminder r = mReminderAdapter.getItem(info.position);
			Intent i = new Intent(getActivity(), NewReminder.class);
			i.putExtra(EditFragment.EXTRA_REMINDER, r);
			startActivity(i);
		}else if(itemId == 2){
			dataSource.uncheckCheck(info.id);
			mReminderAdapter.notifyDataSetChanged();
            notifyDataChangedListeners();
		}else if(itemId == 3){
			Log.d(TAG, "Deleting "+info.id);
			dataSource.deleteReminder(info.id);
			mReminderAdapter.notifyDataSetChanged();
            notifyDataChangedListeners();
		}

		return true;
	}

	private void initList() {
		if(dataSource.getAllReminders().isEmpty()){
			Log.d(TAG, "adding reminders");
			dataSource.saveReminder(new Reminder("backup", 7, Color.GREEN));
			dataSource.saveReminder(new Reminder("springa", 3, Color.YELLOW));
			dataSource.saveReminder(new Reminder("vatten", 3, 0, 5, Color.BLUE));
			dataSource.saveReminder(new Reminder("ring", 0, Color.MAGENTA));
		}
	}

    public void addDataChangedListener(DataChangedListener dcl){
        dataChangedListeners.add(dcl);
    }

    @Override
    public void onDataChanged() {
        mReminderAdapter.notifyDataSetChanged();
    }

    private void notifyDataChangedListeners(){
        for(DataChangedListener dcl: dataChangedListeners) dcl.onDataChanged();
    }

    private class HomeReminderAdapter extends BaseAdapter {
        private final Context mContext;
        private final ReminderDataSource mDataSource;
        private List<Reminder> mList;

        public HomeReminderAdapter(Context context, ReminderDataSource dataSource){
            mContext = context;
            mDataSource = dataSource;
            mList = dataSource.getAllReminders();
        }

        @Override
        public boolean hasStableIds(){
            // We have db ids that are stable, tell list-view so it can do more magic
            return true;
        }

        @Override
        public void notifyDataSetChanged(){
            mList = dataSource.getAllReminders();
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Reminder getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mList.get(position).getDbId();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.reminder_row, parent, false);
            Reminder r = getItem(position);

            TextView doneView = (TextView) rowView.findViewById(R.id.done_textview);
            if(r.isDone()) doneView.setText("[done]");
            else if(r.getCheckCount() > 0) doneView.setText(""+r.getCheckCount()+"/"+r.getMaxCheckCount());
            else doneView.setText("");

            ((TextView) rowView.findViewById(R.id.name_textview)).setText(r.getName());

            rowView.findViewById(R.id.inner_row).getBackground().setColorFilter(r.getColor(), PorterDuff.Mode.MULTIPLY);
            //setBackgroundColor(r.getColor());

            return rowView;
        }
    }
}
