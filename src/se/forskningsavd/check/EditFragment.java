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

import java.util.List;

import se.forskningsavd.check.database.DataChangedListener;
import se.forskningsavd.check.database.ReminderDataSource;
import se.forskningsavd.check.model.Reminder;

public class EditFragment extends Fragment {
  private static final   int    FRAGMENT_ID    = 2;
  private static final   String TAG            = "EditFragment";
  protected static final String EXTRA_REMINDER = "REMINDER";

  private EditReminderAdapter       reminderAdapter;
  private ReminderDataSource        dataSource;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.edit_fragment, container, false);

    ListView lv = (ListView) view.findViewById(R.id.reminder_listview);

    dataSource = ReminderDataSource.getInstance(getActivity());

    reminderAdapter = new EditReminderAdapter(getActivity(), dataSource);
    dataSource.addDataChangedListener(reminderAdapter);

    lv.setAdapter(reminderAdapter);
    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                              long id) {
        Reminder r = reminderAdapter.getItem(position);

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

    Reminder r = reminderAdapter.getItem(aInfo.position);

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
      }
      return true;
    }
    return false;
  }

  private class EditReminderAdapter extends BaseAdapter implements DataChangedListener {
    private final Context            context;
    private final ReminderDataSource dataSource;
    private List<Reminder>           reminderList;

    public EditReminderAdapter(Context context, ReminderDataSource dataSource) {
      this.context            = context;
      this.dataSource         = dataSource;
      this.reminderList       = dataSource.getAllReminders();
    }

    @Override
    public void onDataChanged() {
      notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
      reminderList = dataSource.getAllReminders();
      super.notifyDataSetChanged();
    }

    @Override
    public boolean hasStableIds() {
      // We have db ids that are stable, tell list-view so it can do more magic
      return true;
    }

    @Override
    public int getCount() {
      return reminderList.size();
    }

    @Override
    public Reminder getItem(int position) {
      return reminderList.get(position);
    }

    @Override
    public long getItemId(int position) {
      return reminderList.get(position).getDbId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = (LayoutInflater) context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      if(convertView == null)
        convertView = inflater.inflate(R.layout.reminder_edit_row, parent, false);
      Reminder r = getItem(position);

      ((TextView) convertView.findViewById(R.id.name_textview)       ).setText(r.getName()       );
      ((TextView) convertView.findViewById(R.id.description_textview)).setText(r.getDescription());

      convertView.findViewById(R.id.inner_row).getBackground().setColorFilter(r.getColor(), PorterDuff.Mode.MULTIPLY);

      return convertView;
    }
  }
}
