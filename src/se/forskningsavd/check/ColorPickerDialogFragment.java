package se.forskningsavd.check;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

/**
 * A dialog showing a choice of colors, the activity must implement OnColorChangedListener
 */
public class ColorPickerDialogFragment extends DialogFragment {
  public static final Integer[] colors = {0xffbb7777, 0xff5E6801, 0xffBC991C, 0xffFF7E1B,
                                           0xffFFF64C, 0xff01D62F, 0xffD882FB, 0xff90BAD6,
                                           0xff7586AC, 0xffD4C420};
  private static final String    TAG    = "ColorPickerDialogFragment";

  public interface OnColorChangedListener {
    void colorChanged(int color);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    getDialog().setTitle("Select color");
    View v = inflater.inflate(R.layout.grid_dialog, container);

    GridView gridView = (GridView) v.findViewById(R.id.gridview);
    gridView.setAdapter(new ColorAdapter(getActivity(), colors));
    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        ((OnColorChangedListener) getActivity()).colorChanged(colors[position]);
        dismiss();
      }
    });

    v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();
      }
    });

    return v;
  }

  class ColorAdapter extends ArrayAdapter<Integer> {

    public ColorAdapter(Context context, Integer[] colors) {
      super(context, 0, colors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if(convertView == null) {
        convertView = new TextView(getContext());
        convertView.setLayoutParams(new GridView.LayoutParams(48, 48));
        convertView.setPadding(2, 2, 2, 2);
      }
      convertView.setBackgroundColor(getItem(position));

      return convertView;
    }

  }
}
