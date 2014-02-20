package xardas.gamestracker.ui.fragments;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import xardas.gamestracker.R;
import xardas.gamestracker.giantbomb.api.FilterEnum;
import xardas.gamestracker.giantbomb.api.Game;
import xardas.gamestracker.giantbomb.api.GiantBombApi;
import xardas.gamestracker.giantbomb.api.GiantBombGamesQuery;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GamesListFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.games_list_fragment, container, false);
		int position = getArguments().getInt("selection");
		Calendar calendar = Calendar.getInstance();
		GiantBombGamesQuery query = GiantBombApi.createQuery();
		if (position == 0) { // tracked
			// TODO
		} else if (position == 1) { // out this month
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			query.addFilter(FilterEnum.expected_release_year, year + "").addFilter(FilterEnum.expected_release_month, month + "");
			InfoDownloader downloader = new InfoDownloader(rootView);
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
		} else if (position == 2) { // out this year
			int year = calendar.get(Calendar.YEAR);
			query.addFilter(FilterEnum.expected_release_year, year + "");
			InfoDownloader downloader = new InfoDownloader(rootView);
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
		} else if (position == 3) { // search
			// TODO
		}
		return rootView;
	}

	private class StableArrayAdapter extends ArrayAdapter<Game> {

		private List<Game> games;

		public StableArrayAdapter(Context context, int layoutId, int textViewResourceId, List<Game> games) {
			super(context, layoutId, textViewResourceId, games);
			this.games = games;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.game_list_item, null);
			}
			Game game = games.get(position);
			TextView title = (TextView) convertView.findViewById(R.id.titleTextView);
			title.setText(game.getName());
			TextView platforms = (TextView) convertView.findViewById(R.id.platformsTextView);
			platforms.setText(game.getPlatforms().toString());
			TextView release = (TextView) convertView.findViewById(R.id.relDateTextView);
			release.setText(game.getExpectedReleaseDay() + "-" + game.getExpectedReleaseMonth() + "-" + game.getExpectedReleaseYear());
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	private class InfoDownloader extends AsyncTask<GiantBombGamesQuery, Void, List<Game>> {
		private View rootView;

		public InfoDownloader(View rootView) {
			this.rootView = rootView;
		}

		@Override
		protected List<Game> doInBackground(GiantBombGamesQuery... params) {
			GiantBombGamesQuery q = params[0];
			List<Game> list = null;
			try {
				list = q.execute();
			} catch (Exception e) {
				Toast.makeText(getActivity(), "nope", Toast.LENGTH_SHORT).show(); //TODO
			}
			return list;
		}

		@Override
		protected void onPostExecute(final List<Game> result) {
			if (result != null) {
				Toast.makeText(getActivity(), "gotowe", Toast.LENGTH_SHORT).show(); //TODO
				final ListView listview = (ListView) rootView.findViewById(R.id.listView1);
				final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(), R.layout.game_list_item, R.id.titleTextView, result);
				listview.setAdapter(adapter);

				listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
						final String item = (String) parent.getItemAtPosition(position);
						view.animate().setDuration(500).alpha(0).withEndAction(new Runnable() {
							@Override
							public void run() {
								result.remove(item);
								adapter.notifyDataSetChanged();
								view.setAlpha(1);
							}
						});
					}

				});
			}
		}
	}

}
