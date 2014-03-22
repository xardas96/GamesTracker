package romanovsky.gamerd.ui.list;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;

import romanovsky.gamerd.R;
import romanovsky.gamerd.async.AsyncTask;
import romanovsky.gamerd.database.dao.GameDAO;
import romanovsky.gamerd.giantbomb.api.GameReleaseDateComparator;
import romanovsky.gamerd.giantbomb.api.core.Game;
import romanovsky.gamerd.ui.drawer.DrawerSelection;
import romanovsky.gamerd.ui.list.filters.GamesListFilter;
import romanovsky.gamerd.ui.list.pager.adapters.ReleasedGamesListPageAdapter;
import romanovsky.gamerd.ui.list.pager.adapters.TrackedGamesListPageAdapter;
import romanovsky.gamerd.ui.list.pager.adapters.UntrackedGamesListPageAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GamesListExpandableAdapter extends BaseExpandableListAdapter implements Filterable {
	private String[] categories;
	private List<Game> games;
	private List<Game> outGames;
	private List<Game> gamesForFilter;
	private List<Game> outGamesForFilter;
	private Filter filter;
	private String filterString;
	private int selection;
	private Context context;
	private Resources res;
	private LruCache<Long, Bitmap> cache;
	private Bitmap placeholder;
	private GameDAO gameDAO;
	private int notifyDuration;
	private boolean canNotify;
	private int coverViewSizePixels;
	private static final int SMALL_DELAY = 200;
	private static final int LONG_DELAY = 1500;
	private static final int COVER_SIZE_DIP = 80;

	public GamesListExpandableAdapter(Context context, List<Game> games, int selection, int notifyDuration, boolean canNotify, String filterString) {
		this.outGames = new ArrayList<Game>();
		this.games = new ArrayList<Game>();
		this.gamesForFilter = new ArrayList<Game>();
		this.outGamesForFilter = new ArrayList<Game>();
		this.filterString = filterString;
		filter = new GamesListFilter(this);
		addAll(games);
		this.selection = selection;
		this.context = context;
		res = context.getResources();
		categories = res.getStringArray(R.array.list_categories);
		placeholder = BitmapFactory.decodeResource(res, R.drawable.controller_snes);
		gameDAO = new GameDAO(context);
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int cacheSize = maxMemory / 6;
		cache = new LruCache<Long, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(Long key, Bitmap bitmap) {
				return bitmap.getWidth() * bitmap.getHeight() / 1024;
			}
		};
		this.notifyDuration = notifyDuration;
		this.canNotify = canNotify;
		coverViewSizePixels = Math.round(COVER_SIZE_DIP * context.getResources().getDisplayMetrics().density);
	}

	public void setFilter(String filterString) {
		this.filterString = filterString;
	}

	public void addAll(Collection<? extends Game> collection) {
		for (Game game : collection) {
			if (game.isOutFor() <= 0 && game.getExpectedReleaseYear() != 0) {
				if (!outGamesForFilter.contains(game)) {
					outGamesForFilter.add(game);
				}
			} else {
				if (!gamesForFilter.contains(game)) {
					gamesForFilter.add(game);
				}
			}
		}
		sort(new GameReleaseDateComparator());
		filter.filter(filterString);
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.games_list_item_pager, null, false);
		}
		final Game game;
		if (groupPosition == 0) {
			game = outGames.get(childPosition);
		} else {
			game = games.get(childPosition);
		}
		int currentPageItem;
		PagerAdapter adapter;
		if (game.isTracked()) {
			if (game.isOutFor() <= 0 && game.getExpectedReleaseYear() != 0 || !canNotify) {
				adapter = new ReleasedGamesListPageAdapter(context, game, selection, this, notifyDuration);
				currentPageItem = 0;
			} else {
				adapter = new TrackedGamesListPageAdapter(context, game, selection, this, notifyDuration);
				currentPageItem = 1;
			}
		} else {
			adapter = new UntrackedGamesListPageAdapter(context, game, selection, this, notifyDuration);
			currentPageItem = 1;
		}
		final ViewPager viewPager = (ViewPager) convertView.findViewById(R.id.view_pager);
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(currentPageItem);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				final PagerAdapter adapter = viewPager.getAdapter();
				if (game.isTracked()) {
					if (game.isOutFor() <= 0 && game.getExpectedReleaseYear() != 0) {
						if (adapter instanceof ReleasedGamesListPageAdapter && position == 1) {
							gameDAO.deleteGame(game);
							viewPager.postDelayed(new Runnable() {
								public void run() {
									if (selection == DrawerSelection.TRACKED.getValue()) {
										outGames.remove(game);
									}
									viewPager.postDelayed(new Runnable() {

										public void run() {
											notifyDataSetChanged();
										};
									}, SMALL_DELAY);
								}
							}, LONG_DELAY);
						}
					} else {
						if (position == 0) {
							game.setNotify(!game.isNotify());
							gameDAO.updateGame(game);
							viewPager.postDelayed(new Runnable() {
								public void run() {
									viewPager.postDelayed(new Runnable() {

										public void run() {
											notifyDataSetChanged();
										};
									}, SMALL_DELAY);
								}
							}, LONG_DELAY);
						} else if (position == 2) {
							gameDAO.deleteGame(game);
							viewPager.postDelayed(new Runnable() {
								public void run() {
									if (selection == DrawerSelection.TRACKED.getValue()) {
										games.remove(game);
									}
									viewPager.postDelayed(new Runnable() {

										public void run() {
											notifyDataSetChanged();
										};
									}, SMALL_DELAY);
								}
							}, LONG_DELAY);
						}
					}
				} else {
					if (position == 0) {
						gameDAO.addGame(game);
						viewPager.postDelayed(new Runnable() {
							public void run() {
								viewPager.postDelayed(new Runnable() {

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
				moreInfoView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						moreInfoDialog.dismiss();
					}
				});
				ImageButton positiveButton = (ImageButton) moreInfoView.findViewById(R.id.positiveButton);
				ImageButton negativeButton = (ImageButton) moreInfoView.findViewById(R.id.negativeButton);
				if (game.isTracked()) {
					if (game.isOutFor() <= 0 && game.getExpectedReleaseYear() != 0 || !canNotify) {
						positiveButton.setVisibility(View.GONE);
					} else {
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
					}
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
		viewPager.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				tapGestureDetector.onTouchEvent(event);
				return false;
			}
		});
		return convertView;
	}

	public List<Game> getGamesForFilter() {
		return gamesForFilter;
	}

	public List<Game> getOutGamesForFilter() {
		return outGamesForFilter;
	}

	public void setGames(List<Game> games) {
		this.games = games;
	}

	public void setOutGames(List<Game> outGames) {
		this.outGames = outGames;
	}

	@SuppressWarnings("deprecation")
	public void buildView(View view, final Game game, boolean extraInfo) {
		int daysToRelease = getDateDifferenceInDays(game);
		if (game.isTracked()) {
			if (game.isOutFor() <= 0 && game.getExpectedReleaseYear() != 0 || !canNotify) {
				view.setBackgroundResource(R.drawable.games_list_item_background_released);
			} else {
				view.setBackgroundResource(R.drawable.games_list_item_background_untrack);
			}
		} else {
			view.setBackgroundResource(R.drawable.games_list_item_background_track);
		}
		ImageView timer = (ImageView) view.findViewById(R.id.timerImageView);
		if (game.isNotify()) {
			timer.setVisibility(View.VISIBLE);
		} else if (!game.isNotify() || !game.isTracked()) {
			timer.setVisibility(View.GONE);
		}
		TextView title = (TextView) view.findViewById(R.id.titleTextView);
		title.setText(game.getName());
		if (!game.getPlatforms().isEmpty() && !game.getPlatforms().get(0).equals("")) {
			Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
			ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.platformsLayout);
			int childSize = 0;
			for (int i = 0; i < game.getPlatforms().size(); i++) {
				viewGroup.measure(display.getWidth(), display.getHeight());
				if (i == 1) {
					childSize = viewGroup.getMeasuredWidth();
				}
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.platform_text_view, null);
				int occupiedSpace = (i + 2) * childSize;
				int wholeSpace = display.getWidth() - coverViewSizePixels;
				if (occupiedSpace > wholeSpace && !extraInfo && i != game.getPlatforms().size() - 1) {
					TextView tv = (TextView) ll.findViewById(R.id.platformNameTextView);
					int remainingPlatforms = game.getPlatforms().size() - i;
					tv.setText("+ " + remainingPlatforms);
					viewGroup.addView(ll);
					break;
				} else {
					String platf = game.getPlatforms().get(i);
					TextView tv = (TextView) ll.findViewById(R.id.platformNameTextView);
					tv.setText(platf);
					viewGroup.addView(ll);
				}
			}
		}
		if (!game.getGenres().isEmpty() && !game.getGenres().get(0).equals("")) {
			ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.genresLayout);
			for (String genre : game.getGenres()) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.genre_text_view, null);
				TextView tv = (TextView) ll.findViewById(R.id.genreTextView);
				tv.setText(genre);
				viewGroup.addView(ll);
			}
		}
		TextView releaseEstimate = (TextView) view.findViewById(R.id.relDateEstimateTextView);
		if (selection == DrawerSelection.TRACKED.getValue()) {
			if (daysToRelease <= 0 && game.getExpectedReleaseYear() != 0) {
				title.setTextColor(res.getColor(R.color.green));
				title.setTypeface(null, Typeface.BOLD);
			}
			if (game.getExpectedReleaseYear() == 0) {
				releaseEstimate.setText(res.getString(R.string.unknown_release));
			} else {
				if (Math.abs(daysToRelease) <= 30) {
					releaseEstimate.setText(getDateDifferenceInDays(daysToRelease));
				} else {
					releaseEstimate.setText(buildReleaseDate(game));
				}
			}
		} else {
			if (game.getExpectedReleaseYear() == 0) {
				releaseEstimate.setText(res.getString(R.string.unknown_release));
			} else {
				releaseEstimate.setText(buildReleaseDate(game));
			}
		}
		if (extraInfo) {
			TextView extraInfoTextView = (TextView) view.findViewById(R.id.descriptionTextView);
			extraInfoTextView.setText(game.getDescription());
			TextView siteDetailURL = (TextView) view.findViewById(R.id.linkTextView);
			String href = "<a href=\"" + game.getSiteDetailURL() + "\">" + res.getString(R.string.more_info) + "</a>";
			siteDetailURL.setText(Html.fromHtml(href));
			siteDetailURL.setMovementMethod(LinkMovementMethod.getInstance());
			if (game.getExpectedReleaseYear() == 0) {
				releaseEstimate.setText(res.getString(R.string.unknown_release));
			} else {
				releaseEstimate.setText(getDateDifferenceInDays(daysToRelease));
				TextView releaseDate = (TextView) view.findViewById(R.id.fullDateTextView);
				releaseDate.setVisibility(View.VISIBLE);
				releaseDate.setText(buildReleaseDate(game));
			}

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

	public void sort(Comparator<Game> comparator) {
		Collections.sort(games, comparator);
		Collections.sort(outGames, comparator);
	}

	private void addBitmapToMemoryCache(Long key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null && key != null) {
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
						if (cover != null && cover.exists()) {
							cover.delete();
						}
						Log.i("FILES", "DL");
					} catch (Exception ex) {
						if (cover != null && cover.exists()) {
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

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return groupPosition == 0 ? outGames.size() : games.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public int getGroupCount() {
		return categories.length;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.games_list_category_header, null);
		}
		if (groupPosition == 0) {
			convertView.setBackgroundColor(res.getColor(R.color.green));
		} else {
			convertView.setBackgroundColor(res.getColor(R.color.red));
		}
		((CheckedTextView) convertView).setText(categories[groupPosition]);
		((CheckedTextView) convertView).setChecked(isExpanded);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}

}
