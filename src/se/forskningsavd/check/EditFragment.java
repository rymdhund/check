package se.forskningsavd.check;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import se.forskningsavd.check.database.DataChangedListener;
import se.forskningsavd.check.database.ReminderDataSource;
import se.forskningsavd.check.model.Reminder;

public class EditFragment extends Fragment implements DataChangedListener{
	private static final String TAG         = "EditFragment";
    private static final int    FRAGMENT_ID = 2;

	protected static final String EXTRA_REMINDER = "REMINDER";

    private EditReminderAdapter mReminderAdapter;
	private ReminderDataSource dataSource;
    private List<DataChangedListener> dataChangedListeners = new ArrayList<DataChangedListener>();

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.edit_fragment, container, false);

		ListView lv = (ListView) view.findViewById(R.id.reminder_listview);

		dataSource = new ReminderDataSource(getActivity());
		dataSource.open();

		mReminderAdapter = new EditReminderAdapter(getActivity(), dataSource);

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
		menu.add(FRAGMENT_ID, 3, 3, "Delete");
	}

	// This method is called when user selects an Item in the Context menu
	@Override
	public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == FRAGMENT_ID) {
            int itemId = item.getItemId();
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            if (itemId == 3) {
                Log.d(TAG, "Deleting " + info.id);
                dataSource.deleteReminder(info.id);
                mReminderAdapter.notifyDataSetChanged();
                notifyDataChangedListeners();
            }
            return true;
        }
        return false;
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

    private class EditReminderAdapter extends BaseAdapter {
        private final Context mContext;
        private final ReminderDataSource mDataSource;
        private List<Reminder> mList;

        public EditReminderAdapter(Context context, ReminderDataSource dataSource){
            mContext =    context;
            mDataSource = dataSource;
            mList =       dataSource.getAllReminders();
        }

        @Override
        public void notifyDataSetChanged(){
            mList = mDataSource.getAllReminders();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.reminder_edit_row, parent, false);
            Reminder r = getItem(position);

            ((TextView)rowView.findViewById(R.id.name_textview)        ).setText(r.getName());
            ((TextView) rowView.findViewById(R.id.description_textview)).setText(r.getDescription());

            rowView.findViewById(R.id.inner_row).getBackground().setColorFilter(r.getColor(), PorterDuff.Mode.MULTIPLY);

            return rowView;
        }
    }
}
