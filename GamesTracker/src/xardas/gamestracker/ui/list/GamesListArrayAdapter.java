package xardas.gamestracker.ui.list;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;

import xardas.gamestracker.R;
import xardas.gamestracker.database.GameDAO;
import xardas.gamestracker.giantbomb.api.Game;
import xardas.gamestracker.giantbomb.api.GameComparator;
import xardas.gamestracker.ui.DrawerSelection;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
		int cacheSize = maxMemory / 6;
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
		if (tracked) {
			adapter = new TrackedGamesListPageAdapter(context, game, selection, this);
			currentPageItem = 1;
		} else {
			adapter = new UntrackedGamesListPageAdapter(context, game, selection, this);
			currentPageItem = 1;
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

		final GestureDetector tapGestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				LayoutInflater factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View moreInfoView = factory.inflate(R.layout.games_list_extra_info_dialog, null);
				buildView(moreInfoView, game, true);
				final AlertDialog moreInfoDialog = new AlertDialog.Builder(context).create();
				moreInfoDialog.setView(moreInfoView);
				moreInfoDialog.setCancelable(true);
				moreInfoDialog.setCanceledOnTouchOutside(true);
				ImageButton positiveButton = (ImageButton) moreInfoView.findViewById(R.id.positiveButton);
				ImageButton negativeButton = (ImageButton) moreInfoView.findViewById(R.id.negativeButton);
				if (gameDAO.isTracked(game)) {
					positiveButton.setImageResource(R.drawable.timer);
					positiveButton.setBackgroundColor(res.getColor(R.color.purple));
					positiveButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							game.setNotify(!game.isNotify());
							gameDAO.updateGame(game);
							notifyDataSetChanged();
							moreInfoDialog.dismiss();
						}
					});
					negativeButton.setImageResource(R.drawable.star_delete);
					negativeButton.setBackgroundColor(res.getColor(R.color.red));
					negativeButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							gameDAO.deleteGame(game);
							games.remove(game);
							notifyDataSetChanged();
							moreInfoDialog.dismiss();
						}
					});
				} else {
					positiveButton.setImageResource(R.drawable.star_add);
					positiveButton.setBackgroundColor(res.getColor(R.color.green));
					positiveButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							gameDAO.addGame(game);
							notifyDataSetChanged();
							moreInfoDialog.dismiss();
						}
					});
					negativeButton.setVisibility(View.GONE);
				}
				moreInfoDialog.show();
				return super.onSingleTapConfirmed(e);
			}
		});
		myPager.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				tapGestureDetector.onTouchEvent(event);
				return false;
			}
		});
		return convertView;
	}

	@Override
	public Game getItem(int position) {
		return games.get(position);
	}

	protected void buildView(View view, final Game game, boolean extraInfo) {
		if (gameDAO.isTracked(game)) {
			view.setBackgroundResource(R.drawable.games_list_item_background_untrack);
		} else {
			view.setBackgroundResource(R.drawable.games_list_item_background_track);
		}
		ImageView timer = (ImageView) view.findViewById(R.id.timerImageView);
		if (game.isNotify()) {
			timer.setVisibility(View.VISIBLE);
		} else {
			timer.setVisibility(View.GONE);
		}
		TextView title = (TextView) view.findViewById(R.id.titleTextView);
		title.setText(game.getName());
		TextView platforms = (TextView) view.findViewById(R.id.platformsTextView);
		if (game.getPlatforms().isEmpty() || game.getPlatforms().get(0).equals("")) {
			platforms.setText(res.getString(R.string.unknown_platforms));
		} else {
			platforms.setText(game.getPlatforms().toString());
		}
		TextView release = (TextView) view.findViewById(R.id.relDateTextView);
		if (selection == DrawerSelection.TRACKED.getValue()) {
			int daysToRelease = getDateDifferenceInDays(game);
			if (daysToRelease <= 0 && game.getExpectedReleaseYear() != 0) {
				title.setTextColor(res.getColor(R.color.green));
				title.setTypeface(null, Typeface.BOLD);
			}
			if (game.getExpectedReleaseYear() == 0) {
				release.setText(res.getString(R.string.unknown_release));
			} else {
				release.setText(getDateDifferenceInDays(daysToRelease));
			}
		} else {
			if (game.getExpectedReleaseYear() == 0) {
				release.setText(res.getString(R.string.unknown_release));
			} else {
				release.setText(buildReleaseDate(game));
			}
		}
		if (extraInfo) {
			TextView extraInfoTextView = (TextView) view.findViewById(R.id.descriptionTextView);
			extraInfoTextView.setText(game.getDescription());
		}
		ImageView cover = (ImageView) view.findViewById(R.id.coverImageView);
		loadBitmap(game.getIconURL(), cover, game);
	}

	protected int getDateDifferenceInDays(Game game) {
		DateTime now = new DateTime();
		DateTime release = game.getReleaseDate().plusDays(1);
		Days d = Days.daysBetween(now, release);
		int days = d.getDays();
		return days;
	}

	protected String getDateDifferenceInDays(int days) {
		String differenceInDays;
		if (days == 1) {
			differenceInDays = String.format(res.getString(R.string.day), days);
		} else if (days == 0) {
			differenceInDays = res.getString(R.string.out_today);
		} else if (days == -1) {
			differenceInDays = String.format(res.getString(R.string.out_since_day), -days);
		} else if (days < -1) {
			differenceInDays = String.format(res.getString(R.string.out_since_days), -days);
		} else {
			differenceInDays = String.format(res.getString(R.string.days), days);
		}
		return differenceInDays;
	}

	protected String buildReleaseDate(Game game) {
		StringBuilder relDateBuilder = new StringBuilder();
		relDateBuilder.append(game.getExpectedReleaseDay() == 0 ? "" : game.getExpectedReleaseDay() + "-");
		relDateBuilder.append(game.getExpectedReleaseMonth() == 0 ? "" : game.getExpectedReleaseMonth() + "-");
		relDateBuilder.append(game.getExpectedReleaseQuarter() == 0 ? "" : "Q" + game.getExpectedReleaseQuarter() + " ");
		relDateBuilder.append(game.getExpectedReleaseYear() == 0 ? "" : game.getExpectedReleaseYear());
		return relDateBuilder.toString();
	}

	private void addBitmapToMemoryCache(Long key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			cache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemCache(Long key) {
		return cache.get(key);
	}

	protected void loadBitmap(String requestURL, ImageView imageView, Game game) {
		if (cancelPotentialWork(game, imageView)) {
			final Long imageKey = game.getId();
			final Bitmap bitmap = getBitmapFromMemCache(imageKey);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				ImageDownloader task = new ImageDownloader(requestURL, imageView, game);
				AsyncDrawable asyncDrawable = new AsyncDrawable(res, placeholder, task);
				imageView.setImageDrawable(asyncDrawable);
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
			}
		}
	}

	private boolean cancelPotentialWork(Game data, ImageView imageView) {
		ImageDownloader bitmapWorkerTask = getBitmapWorkerTask(imageView);
		if (bitmapWorkerTask != null) {
			long bitmapData = bitmapWorkerTask.getGame().getId();
			if (bitmapData == 0 || bitmapData != data.getId()) {
				bitmapWorkerTask.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}

	private ImageDownloader getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	private class ImageDownloader extends AsyncTask<Void, Void, Bitmap> {
		private String requestUrl;
		private WeakReference<ImageView> view;
		private Game game;

		private ImageDownloader(String requestUrl, ImageView view, Game game) {
			this.requestUrl = requestUrl;
			this.view = new WeakReference<ImageView>(view);
			this.game = game;
		}

		public Game getGame() {
			return game;
		}

		@Override
		protected Bitmap doInBackground(Void... objects) {
			Bitmap pic;
			File cache = new File(context.getCacheDir().getAbsolutePath() + File.separator + game.getId());
			if (!cache.exists()) {
				cache.mkdir();
			}
			long time = Calendar.getInstance().getTimeInMillis();
			File[] files = cache.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return pathname.getName().contains(game.getId() + "-small-");
				}
			});
			File cover = files.length == 0 ? null : files[0];
			boolean download = true;
			if (cover != null) {
				String coverName = cover.getName();
				String[] split = coverName.split("-small-");
				split = split[1].split("\\.");
				long savedTime = Long.valueOf(split[0]);
				download = savedTime < game.getDateLastUpdated();
			}
			if (requestUrl != null) {
				if (download) {
					try {
						URL url = new URL(requestUrl);
						URLConnection conn = url.openConnection();
						pic = BitmapFactory.decodeStream(conn.getInputStream());
						File outFile = new File(cache.getAbsolutePath() + File.separator + game.getId() + "-small-" + time + ".jpg");
						OutputStream outStream = new FileOutputStream(outFile);
						pic.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
						outStream.flush();
						outStream.close();
						Log.i("FILES", "DL");
					} catch (Exception ex) {
						if (cover.exists()) {
							pic = BitmapFactory.decodeFile(cover.getAbsolutePath());
						} else {
							pic = placeholder;
						}
					}
				} else {
					Log.i("FILES", "CACHE");
					pic = BitmapFactory.decodeFile(cover.getAbsolutePath());
				}
			} else {
				Log.i("FILES", "CACHE");
				pic = placeholder;
			}
			return pic;
		}

		@Override
		protected void onPostExecute(Bitmap output) {
			if (isCancelled()) {
				output = null;
			}
			if (view != null && view.get() != null) {
				addBitmapToMemoryCache(game.getId(), output);
				ImageView img = view.get();
				ImageDownloader bitmapWorkerTask = getBitmapWorkerTask(img);
				if (this == bitmapWorkerTask && img != null) {
					img.setImageBitmap(output);
				}
				img.setImageBitmap(cache.get(game.getId()));
			}
		}
	}

	private class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<ImageDownloader> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, ImageDownloader bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<ImageDownloader>(bitmapWorkerTask);
		}

		public ImageDownloader getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

}
