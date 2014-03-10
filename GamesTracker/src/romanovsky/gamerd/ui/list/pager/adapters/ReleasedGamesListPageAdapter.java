package romanovsky.gamerd.ui.list.pager.adapters;

import romanovsky.gamerd.R;
import romanovsky.gamerd.giantbomb.api.Game;
import romanovsky.gamerd.ui.list.GamesListExpandableListAdapter;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ReleasedGamesListPageAdapter extends TrackedGamesListPageAdapter {

	public ReleasedGamesListPageAdapter(Context ctx, Game game, int selection, GamesListExpandableListAdapter parentAdapter, int notifyDuration) {
		super(ctx, game, selection, parentAdapter, notifyDuration);
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
			view = inflater.inflate(R.layout.games_list_item, null);
			parentAdapter.buildView(view, game, false);
		} else {
			view = inflater.inflate(R.layout.games_list_item_untrack, null);
			TextView track = (TextView) view.findViewById(R.id.untrackTextView);
			String trackText = String.format(res.getString(R.string.stopped_tracking_game), game.getName());
			track.setText(trackText);
		}
		((ViewPager) container).addView(view, 0);
		return view;
	}

}
