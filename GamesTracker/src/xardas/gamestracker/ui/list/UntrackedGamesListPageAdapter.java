package xardas.gamestracker.ui.list;

import java.util.Map;

import xardas.gamestracker.R;
import xardas.gamestracker.giantbomb.api.Game;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UntrackedGamesListPageAdapter extends TrackedGamesListPageAdapter {

	public UntrackedGamesListPageAdapter(Context ctx, Game game, Map<Long, Bitmap> bitmapMap, int selection, Bitmap placeholder) {
		super(ctx, game, bitmapMap, selection, placeholder);
		pageCount = 2;
	}

	@Override
	public int getCount() {
		return pageCount;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = null;
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (position == 0) {
			view = inflater.inflate(R.layout.games_list_item_track, null);
			TextView track = (TextView) view.findViewById(R.id.trackTextView);
			String trackText = String.format(res.getString(R.string.tracking_game), game.getName());
			track.setText(trackText);
		} else {
			view = inflater.inflate(R.layout.games_list_item, null);
			buildView(view);
		}
		((ViewPager) container).addView(view, 0);
		return view;
	}

}
