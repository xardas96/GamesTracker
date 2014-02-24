package xardas.gamestracker.ui.list;

import xardas.gamestracker.R;
import xardas.gamestracker.giantbomb.api.Game;
import xardas.gamestracker.ui.DrawerSelection;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TrackedReleasedGamesListPageAdapter extends UntrackedGamesListPageAdapter {

	public TrackedReleasedGamesListPageAdapter(Context ctx, Game game, LruCache<Long, Bitmap> cache, int selection, Bitmap placeholder) {
		super(ctx, game, cache, selection, placeholder);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = null;
		if (position == 0) {
			view = inflater.inflate(R.layout.games_list_item, null);
			buildView(view);
		} else {
			view = inflater.inflate(R.layout.games_list_item_untrack, null);
			TextView track = (TextView) view.findViewById(R.id.untrackTextView);
			String trackText = String.format(res.getString(R.string.stopped_tracking_game), game.getName());
			track.setText(trackText);
		}
		((ViewPager) container).addView(view, 0);
		return view;
	}

	protected void buildView(View view) {
		view.setBackgroundResource(R.drawable.games_list_item_background_out);
		ImageView timer = (ImageView) view.findViewById(R.id.timerImageView);
		if (game.isNotify()) {
			timer.setVisibility(View.VISIBLE);
		} else {
			timer.setVisibility(View.GONE);
		}
		TextView title = (TextView) view.findViewById(R.id.titleTextView);
		title.setText(game.getName());
		TextView platforms = (TextView) view.findViewById(R.id.platformsTextView);
		platforms.setText(game.getPlatforms().toString());
		TextView release = (TextView) view.findViewById(R.id.relDateTextView);
		if (selection == DrawerSelection.TRACKED.getValue()) {
			int daysToRelease = getDateDifferenceInDays(game);
			if (daysToRelease == 0) {
				title.setTextColor(res.getColor(R.color.green));
				title.setTypeface(null, Typeface.BOLD);
			}
			release.setText(getDateDifferenceInDays(daysToRelease));
		} else {
			release.setText(buildReleaseDate(game));
		}
		ImageView cover = (ImageView) view.findViewById(R.id.coverImageView);
		loadBitmap(game.getIconURL(), cover, game);
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
