package romanovsky.gamerd.ui.list.pager.adapters;

import romanovsky.gamerd.R;
import romanovsky.gamerd.giantbomb.api.Game;
import romanovsky.gamerd.ui.list.GamesListExpandableListAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TrackedGamesListPageAdapter extends PagerAdapter {
	protected Context ctx;
	protected Resources res;
	protected Game game;
	protected int pageCount;
	protected LayoutInflater inflater;
	protected GamesListExpandableListAdapter parentAdapter;
	protected int notifyDuration;

	public TrackedGamesListPageAdapter(Context ctx, Game game, int selection, GamesListExpandableListAdapter parentAdapter, int notifyDuration) {
		this.ctx = ctx;
		this.game = game;
		res = ctx.getResources();
		pageCount = 3;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.parentAdapter = parentAdapter;
		this.notifyDuration = notifyDuration;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	@Override
	public int getCount() {
		return pageCount;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = null;
		if (position == 0) {
			view = inflater.inflate(R.layout.games_list_item_notify, null);
			TextView notify = (TextView) view.findViewById(R.id.notifyTextView);
			String notifyText;
			if (game.isNotify()) {
				notifyText = res.getString(R.string.not_remind);
			} else {
				String label;
				if (notifyDuration == 1) {
					label = res.getString(R.string.remind_day);
				} else {
					label = res.getString(R.string.remind_days);
				}
				notifyText = String.format(label, notifyDuration);
			}
			notify.setText(notifyText);
		} else if (position == 1) {
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

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view == ((View) obj);
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}
}
