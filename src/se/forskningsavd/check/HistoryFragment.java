package se.forskningsavd.check;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import se.forskningsavd.check.database.DataChangedListener;
import se.forskningsavd.check.database.ReminderDataSource;
import se.forskningsavd.check.model.Check;
import se.forskningsavd.check.model.TimeUtils;

public class HistoryFragment extends Fragment {
  private static final int    FRAGMENT_ID = 3;
  private static final String TAG         = "EditFragment";

  private HistoryAdapter            adapter;
  private ReminderDataSource        dataSource;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.home_fragment, container, false);

    ListView lv = (ListView) view.findViewById(R.id.reminder_listview);

    dataSource = ReminderDataSource.getInstance(getActivity());

    adapter = new HistoryAdapter(getActivity(), dataSource);
    dataSource.addDataChangedListener(adapter);
    lv.setAdapter(adapter);
    registerForContextMenu(lv);

    return view;
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
                                  ContextMenuInfo menuInfo) {

    super.onCreateContextMenu(menu, v, menuInfo);
    AdapterContextMenuInfo aInfo = (AdapterContextMenuInfo) menuInfo;

    Check check = adapter.getItem(aInfo.position);

    menu.setHeaderTitle("Options for " + check.getReminderName());
    menu.add(FRAGMENT_ID, 3, 3, "Delete");
  }

  // This method is called when user selects an Item in the Context menu
  @Override
  public boolean onContextItemSelected(MenuItem item) {
    if (item.getGroupId() == FRAGMENT_ID) {
      int itemId = item.getItemId();
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      if (itemId == 3) {
        Log.d(TAG, "Deleting check " + info.id);
        dataSource.deleteCheck(info.id);
      }
      return true;
    }
    return false;
  }



  private class HistoryAdapter extends BaseAdapter implements DataChangedListener{
    private final Context             mContext;
    private final ReminderDataSource  mDataSource;
    private       List<Check>         mList;

    public HistoryAdapter(Context context, ReminderDataSource dataSource) {
      mContext = context;
      mDataSource = dataSource;
      mList = dataSource.getAllChecks();
    }
    @Override
    public void onDataChanged() {
      notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
      mList = mDataSource.getAllChecks();
      super.notifyDataSetChanged();
    }

    @Override
    public boolean hasStableIds() {
      // We have db ids that are stable, tell list-view so it can do more magic
      return true;
    }

    @Override
    public int getCount() {
      return mList.size();
    }

    @Override
    public Check getItem(int position) {
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

      View rowView = inflater.inflate(R.layout.check_history_row, parent, false);
      Check check = getItem(position);

      boolean showSeparator =
          position == 0 ||
              !TimeUtils.isSameDate(getItem(position - 1).getTimestamp(), check.getTimestamp());

      TextView separator = (TextView) rowView.findViewById(R.id.separator);
      if (showSeparator) {
        separator.setText(TimeUtils.getRelativeDay(check.getTimestamp()));
        separator.setBackgroundColor(Color.GRAY);
        separator.setVisibility(View.VISIBLE);
      } else {
        separator.setVisibility(View.GONE);
      }

      ((TextView) rowView.findViewById(R.id.check_name)).setText(check.getReminderName());

      int flags = DateUtils.FORMAT_ABBREV_RELATIVE | DateUtils.FORMAT_ABBREV_TIME;
      TextView timeView = ((TextView) rowView.findViewById(R.id.check_time));
      timeView.setText(DateUtils.getRelativeTimeSpanString(check.getTimestamp(),
          System.currentTimeMillis(),
          DateUtils.MINUTE_IN_MILLIS,
          flags));

      rowView.findViewById(R.id.check_container)
          .getBackground()
          .setColorFilter(check.getReminderColor(), PorterDuff.Mode.MULTIPLY);

      return rowView;
    }
  }
}
