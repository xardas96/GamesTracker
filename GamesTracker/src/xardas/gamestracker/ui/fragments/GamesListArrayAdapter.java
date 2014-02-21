package xardas.gamestracker.ui.fragments;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import xardas.gamestracker.R;
import xardas.gamestracker.database.GameDAO;
import xardas.gamestracker.giantbomb.api.Game;
import xardas.gamestracker.giantbomb.api.GameComparator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class GamesListArrayAdapter extends ArrayAdapter<Game> {
	private List<Game> games;

	public GamesListArrayAdapter(Context context, int layoutId, int textViewResourceId, List<Game> games) {
		super(context, layoutId, textViewResourceId, games);
		this.games = games;
		Collections.sort(games, new GameComparator());
	}

	@Override
	public void addAll(Collection<? extends Game> collection) {
		super.addAll(collection);
		Collections.sort(games, new GameComparator());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Game game = games.get(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.game_list_item, null);
			ImgDownload d = new ImgDownload(game.getIconURL(), (ImageView) convertView.findViewById(R.id.coverImageView), game);
			d.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
		} else {
			TextView title = (TextView) convertView.findViewById(R.id.titleTextView);
			String oldName = title.getText().toString();
			if (!oldName.equals(game.getName())) {
				ImgDownload d = new ImgDownload(game.getIconURL(), (ImageView) convertView.findViewById(R.id.coverImageView), game);
				d.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
			}
		}
		ImageButton button = (ImageButton) convertView.findViewById(R.id.addGameButton);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ImageButton addGameButton = (ImageButton) v;
				String tag = (String) addGameButton.getTag();
				if (tag.equals("add")) {
					GameDAO dao = new GameDAO(getContext());
					addGameButton.setImageResource(R.drawable.star_delete);
					addGameButton.setBackgroundColor(getContext().getResources().getColor(R.color.red));
					addGameButton.setTag("del");
					dao.addGame(game);
				} else {
					GameDAO dao = new GameDAO(getContext());
					addGameButton.setImageResource(R.drawable.star_add);
					addGameButton.setBackgroundColor(getContext().getResources().getColor(R.color.green));
					addGameButton.setTag("add");
					dao.deleteGame(game);
				}

			}
		});
		TextView title = (TextView) convertView.findViewById(R.id.titleTextView);
		title.setText(game.getName());
		TextView platforms = (TextView) convertView.findViewById(R.id.platformsTextView);
		platforms.setText(game.getPlatforms().toString());
		TextView release = (TextView) convertView.findViewById(R.id.relDateTextView);
		StringBuilder relDateBuilder = new StringBuilder();
		relDateBuilder.append(game.getExpectedReleaseDay() == 0 ? "" : game.getExpectedReleaseDay() + "-");
		relDateBuilder.append(game.getExpectedReleaseMonth() == 0 ? "" : game.getExpectedReleaseMonth() + "-");
		relDateBuilder.append(game.getExpectedReleaseYear());
		release.setText(relDateBuilder.toString());
		return convertView;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private class ImgDownload extends AsyncTask {
		private String requestUrl;
		private ImageView view;
		private Bitmap pic;
		private Game game;
		private boolean noPic;

		private ImgDownload(String requestUrl, ImageView view, Game game) {
			this.requestUrl = requestUrl;
			this.view = view;
			this.game = game;
		}

		@Override
		protected void onPreExecute() {
			view.setImageResource(R.drawable.controller_snes);
		}

		@Override
		protected Object doInBackground(Object... objects) {
			File cache = new File(getContext().getCacheDir().getAbsolutePath() + File.separator + game.getId());
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
			if (download) {
				Log.i("FILES", "DLING");
				try {
					URL url = new URL(requestUrl);
					URLConnection conn = url.openConnection();
					pic = BitmapFactory.decodeStream(conn.getInputStream());
					File outFile = new File(cache.getAbsolutePath() + File.separator + game.getId() + "-small-" + time + ".jpg");
					OutputStream outStream = new FileOutputStream(outFile);
					pic.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
					outStream.flush();
					outStream.close();
				} catch (Exception ex) {
					Log.e(ex.getMessage(), ex.getMessage(), ex);
					noPic = true;
				}
			} else {
				Log.i("FILES", "CACHE");
				pic = BitmapFactory.decodeFile(cover.getAbsolutePath());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object o) {
			if (!noPic) {
				view.setImageBitmap(pic);
			}
		}
	}
}
