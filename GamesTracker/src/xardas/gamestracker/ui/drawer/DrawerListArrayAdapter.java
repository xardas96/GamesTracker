package xardas.gamestracker.ui.drawer;

import xardas.gamestracker.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerListArrayAdapter extends ArrayAdapter<String> {
	private String[] listElements;

	public DrawerListArrayAdapter(Context context, int resource, String[] objects) {
		super(context, resource, objects);
		this.listElements = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.drawer_list_item, null);
		}
		ImageView icon = (ImageView) convertView.findViewById(R.id.drawerIcon);
		TextView text = (TextView) convertView.findViewById(R.id.drawerText);
		text.setText(listElements[position]);
		switch (position) {
		case 0:
			icon.setImageResource(R.drawable.star);
			break;
		case 1:
			icon.setImageResource(R.drawable.calendar_month);
			break;
		case 2:
			icon.setImageResource(R.drawable.calendar_year);
			break;
		case 3:
			icon.setImageResource(R.drawable.page_search);
			break;
		}
		return convertView;
	}
}