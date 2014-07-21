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

import java.util.ArrayList;
import java.util.List;

import se.forskningsavd.check.database.DataChangedListener;
import se.forskningsavd.check.database.ReminderDataSource;
import se.forskningsavd.check.model.Reminder;

public class HomeFragment extends Fragment {
  private static final int    FRAGMENT_ID = 1;
  private static final String TAG         = "HomeFragment";

  private HomeReminderAdapter             mReminderAdapter;
  private ReminderDataSource              dataSource;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.home_fragment, container, false);
    ListView lv = (ListView) view.findViewById(R.id.reminder_listview);

    dataSource = ReminderDataSource.getInstance(getActivity());

    mReminderAdapter = new HomeReminderAdapter(getActivity(), dataSource);
    dataSource.addDataChangedListener(mReminderAdapter);

    lv.setAdapter(mReminderAdapter);
    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                              long id) {
        Reminder r = mReminderAdapter.getItem(position);
        dataSource.saveCheck(r.getDbId());
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

    Reminder r = mReminderAdapter.getItem(aInfo.position);

    menu.setHeaderTitle("Options for " + r.getName());
    menu.add(FRAGMENT_ID, 1, 1, "Edit");
    menu.add(FRAGMENT_ID, 2, 2, "Uncheck");
    menu.add(FRAGMENT_ID, 3, 3, "Delete");
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    if (item.getGroupId() == FRAGMENT_ID) {
      int itemId = item.getItemId();
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      Log.d(TAG, "context menu selected. Item " + itemId + " info.id: " + info.id);

      if (itemId == 1) {
        Reminder r = mReminderAdapter.getItem(info.position);
        Intent i = new Intent(getActivity(), NewReminder.class);
        i.putExtra(EditFragment.EXTRA_REMINDER, r);
        startActivity(i);
      } else if (itemId == 2) {
        dataSource.uncheckCheck(info.id);
      } else if (itemId == 3) {
        dataSource.deleteReminder(info.id);
      }

      return true;
    }
    return false;
  }



  private class HomeReminderAdapter extends BaseAdapter  implements DataChangedListener{
    private final Context mContext;
    private final ReminderDataSource mDataSource;
    private List<Reminder> mList;

    public HomeReminderAdapter(Context context, ReminderDataSource dataSource) {
      mContext = context;
      mDataSource = dataSource;
      mList = dataSource.getDueReminders();
    }

    @Override
    public boolean hasStableIds() {
      // We have db ids that are stable, tell list-view so it can do more magic
      return true;
    }

    @Override
    public void onDataChanged() {
      notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
      mList = mDataSource.getDueReminders();
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

      View     rowView  = inflater.inflate(R.layout.reminder_row, parent, false);
      Reminder r        = getItem(position);
      TextView doneView = (TextView) rowView.findViewById(R.id.done_textview);

      if (r.isDone()) doneView.setText("[done]");
      else if (r.getCheckCount() > 0)
        doneView.setText("" + r.getCheckCount() + "/" + r.getMaxCheckCount());
      else doneView.setText("");

      ((TextView) rowView.findViewById(R.id.name_textview)).setText(r.getName());

      rowView.findViewById(R.id.inner_row).getBackground().setColorFilter(r.getColor(), PorterDuff.Mode.MULTIPLY);

      return rowView;
    }
  }
}
