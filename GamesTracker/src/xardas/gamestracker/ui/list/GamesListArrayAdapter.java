package xardas.gamestracker.ui.list;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import xardas.gamestracker.R;
import xardas.gamestracker.database.GameDAO;
import xardas.gamestracker.giantbomb.api.Game;
import xardas.gamestracker.giantbomb.api.GameComparator;
import xardas.gamestracker.ui.DrawerSelection;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class GamesListArrayAdapter extends ArrayAdapter<Game> {
	private List<Game> games;
	private int selection;
	private Context context;
	private Resources res;
	private LruCache<Long, Bitmap> cache;
	private Bitmap placeholder;
	private GameDAO gameDAO;
	private static final int SMALL_DELAY = 200;
	private static final int LONG_DELAY = 1500;

	public GamesListArrayAdapter(Context context, int layoutId, int textViewResourceId, List<Game> games, int selection) {
		super(context, layoutId, textViewResourceId, games);
		this.games = games;
		this.selection = selection;
		this.context = context;
		res = context.getResources();
		Collections.sort(games, new GameComparator());
		placeholder = BitmapFactory.decodeResource(res, R.drawable.controller_snes);
		gameDAO = new GameDAO(context);
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int cacheSize = maxMemory / 8;
		cache = new LruCache<Long, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(Long key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	@Override
	public void addAll(Collection<? extends Game> collection) {
		super.addAll(collection);
		Collections.sort(games, new GameComparator());
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		final Game game = games.get(position);
		final boolean tracked = gameDAO.isTracked(game);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.games_list_item_pager, null, false);
		}
		int currentPageItem;
		PagerAdapter adapter;
		if (game.isOut()) {
			adapter = new TrackedReleasedGamesListPageAdapter(context, game, cache, position, placeholder);
			currentPageItem = 0;
		} else {
			if (tracked) {
				adapter = new TrackedGamesListPageAdapter(context, game, cache, selection, placeholder);
				currentPageItem = 1;
			} else {
				adapter = new UntrackedGamesListPageAdapter(context, game, cache, selection, placeholder);
				currentPageItem = 1;
			}
		}
		final ViewPager myPager = (ViewPager) convertView.findViewById(R.id.mypager);
		myPager.setAdapter(adapter);
		myPager.setCurrentItem(currentPageItem);
		myPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (tracked) {
					if (position == 0) {
						game.setNotify(!game.isNotify());
						gameDAO.updateGame(game);
						myPager.postDelayed(new Runnable() {
							public void run() {
								myPager.setCurrentItem(1, true);
								myPager.postDelayed(new Runnable() {

									public void run() {
										notifyDataSetChanged();
									};
								}, SMALL_DELAY);
							}
						}, LONG_DELAY);
					} else if (position == 2) {
						gameDAO.deleteGame(game);
						myPager.postDelayed(new Runnable() {
							public void run() {
								if (selection == DrawerSelection.TRACKED.getValue()) {
									games.remove(game);
								} else {
									myPager.setCurrentItem(1, true);
								}
								myPager.postDelayed(new Runnable() {

									public void run() {
										notifyDataSetChanged();
									};
								}, SMALL_DELAY);
							}
						}, LONG_DELAY);
					}
				} else {
					if (position == 0) {
						gameDAO.addGame(game);
						myPager.postDelayed(new Runnable() {
							public void run() {
								myPager.setCurrentItem(1, true);
								myPager.postDelayed(new Runnable() {

									public void run() {
										notifyDataSetChanged();
									};
								}, SMALL_DELAY);
							}
						}, LONG_DELAY);
					}
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		return convertView;
	}

	@Override
	public Game getItem(int position) {
		return games.get(position);
	}

}
