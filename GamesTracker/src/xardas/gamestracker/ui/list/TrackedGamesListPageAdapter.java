package xardas.gamestracker.ui.list;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Days;

import xardas.gamestracker.R;
import xardas.gamestracker.database.GameDAO;
import xardas.gamestracker.giantbomb.api.Game;
import xardas.gamestracker.ui.DrawerSelection;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TrackedGamesListPageAdapter extends PagerAdapter {
	protected Context ctx;
	protected Resources res;
	protected Game game;
	protected GameDAO gameDAO;
	protected Map<Long, Bitmap> bitmapMap;
	protected int selection;
	protected Bitmap placeholder;
	protected int pageCount;

	public TrackedGamesListPageAdapter(Context ctx, Game game, Map<Long, Bitmap> bitmapMap, int selection, Bitmap placeholder) {
		this.ctx = ctx;
		this.game = game;
		this.bitmapMap = bitmapMap;
		this.selection = selection;
		this.placeholder = placeholder;
		gameDAO = new GameDAO(ctx);
		res = ctx.getResources();
		pageCount = 3;
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
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (position == 0) {
			view = inflater.inflate(R.layout.games_list_item_notify, null);
			TextView notify = (TextView) view.findViewById(R.id.notifyTextView);
			String notifyText;
			if (game.isNotify()) {
				notifyText = res.getString(R.string.not_notify);
			} else {
				notifyText = res.getString(R.string.notify);
			}
			notify.setText(notifyText);
		} else if (position == 1) {
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
		ImageView cover = (ImageView) view.findViewById(R.id.coverImageView);
		cover.setImageBitmap(bitmapMap.get(game.getId()));
		ImageDownloader downloader = new ImageDownloader(game.getIconURL(), cover, game);
		downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
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
		platforms.setText(game.getPlatforms().toString());
		TextView release = (TextView) view.findViewById(R.id.relDateTextView);
		if (selection == DrawerSelection.TRACKED.getValue()) {
			int daysToRelease = getDateDifferenceInDays(game);
			if(daysToRelease == 0) {
				title.setTextColor(res.getColor(R.color.green));
				title.setTypeface(null, Typeface.BOLD);
			}
			release.setText(getDateDifferenceInDays(daysToRelease));
		} else {
			release.setText(buildReleaseDate(game));
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view == ((View) obj);
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}
	
	private int getDateDifferenceInDays(Game game) {
		DateTime now = new DateTime();
		DateTime release = game.getReleaseDate();
		Days d = Days.daysBetween(now, release);
		int days = d.getDays();
		return days;
	}

	private String getDateDifferenceInDays(int days) {
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

	private String buildReleaseDate(Game game) {
		StringBuilder relDateBuilder = new StringBuilder();
		relDateBuilder.append(game.getExpectedReleaseDay() == 0 ? "" : game.getExpectedReleaseDay() + "-");
		relDateBuilder.append(game.getExpectedReleaseMonth() == 0 ? "" : game.getExpectedReleaseMonth() + "-");
		relDateBuilder.append(game.getExpectedReleaseQuarter() == 0 ? "" : "Q" + game.getExpectedReleaseQuarter() + " ");
		relDateBuilder.append(game.getExpectedReleaseYear());
		return relDateBuilder.toString();
	}

	private void onBitmapLoaded(long id, Bitmap bmp) {
		bitmapMap.put(id, bmp);
	}

	private class ImageDownloader extends AsyncTask<Void, Void, Void> {
		private String requestUrl;
		private ImageView view;
		private Bitmap pic;
		private Game game;

		private ImageDownloader(String requestUrl, ImageView view, Game game) {
			this.requestUrl = requestUrl;
			this.view = view;
			this.game = game;
		}

		@Override
		protected Void doInBackground(Void... objects) {
			File cache = new File(ctx.getCacheDir().getAbsolutePath() + File.separator + game.getId());
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
			return null;
		}

		@Override
		protected void onPostExecute(Void o) {
			onBitmapLoaded(game.getId(), pic);
			view.setImageBitmap(bitmapMap.get(game.getId()));
		}
	}

}
