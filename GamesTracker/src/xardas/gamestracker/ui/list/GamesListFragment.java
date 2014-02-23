package xardas.gamestracker.ui.list;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import xardas.gamestracker.R;
import xardas.gamestracker.database.GameDAO;
import xardas.gamestracker.giantbomb.api.FilterEnum;
import xardas.gamestracker.giantbomb.api.Game;
import xardas.gamestracker.giantbomb.api.GameComparator;
import xardas.gamestracker.giantbomb.api.GiantBombApi;
import xardas.gamestracker.giantbomb.api.GiantBombGamesQuery;
import xardas.gamestracker.ui.DrawerSelection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class GamesListFragment extends Fragment {
	private GameDAO dao;
	private int selection;

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.games_list_fragment, container, false);
		selection = getArguments().getInt("selection");
		Calendar calendar = Calendar.getInstance();
		if (selection == DrawerSelection.TRACKED.getValue()) {
			dao = new GameDAO(getActivity());
			List<Game> games = dao.getAllGames();
			Collections.sort(games, new GameComparator());
			ListView gamesListView = (ListView) rootView.findViewById(R.id.gamesListView);
			GamesListArrayAdapter adapter = new GamesListArrayAdapter(getActivity(), R.layout.games_list_item, R.id.titleTextView, games, selection);
			gamesListView.setAdapter(adapter);
			TrackedGamesUpdater updater = new TrackedGamesUpdater(rootView);
			updater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, games);
		} else if (selection == DrawerSelection.THIS_MONTH.getValue()) {
			GiantBombGamesQuery monthQuery = GiantBombApi.createQuery();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			monthQuery.addFilter(FilterEnum.expected_release_year, "" + year).addFilter(FilterEnum.expected_release_month, "" + month);
			InfoDownloader downloader = new InfoDownloader(rootView);
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, monthQuery);
		} else if (selection == DrawerSelection.NEXT_MONTH.getValue()) {
			GiantBombGamesQuery monthQuery = GiantBombApi.createQuery();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 2;
			monthQuery.addFilter(FilterEnum.expected_release_year, "" + year).addFilter(FilterEnum.expected_release_month, "" + month);
			InfoDownloader downloader = new InfoDownloader(rootView);
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, monthQuery);
		} else if (selection == DrawerSelection.YEAR.getValue()) {
			GiantBombGamesQuery query = GiantBombApi.createQuery();
			int year = calendar.get(Calendar.YEAR);
			query.addFilter(FilterEnum.expected_release_year, year + "");
			InfoDownloader downloader = new InfoDownloader(rootView);
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
		} else if (selection == DrawerSelection.SEARCH.getValue()) {
			// TODO
		}
		return rootView;
	}

	private class TrackedGamesUpdater extends AsyncTask<List<Game>, Void, Void> {
		private View rootView;
		private boolean updated;

		public TrackedGamesUpdater(View rootView) {
			this.rootView = rootView;
		}

		@Override
		protected Void doInBackground(List<Game>... params) {
			List<Game> games = params[0];
			for (Game game : games) {
				GiantBombGamesQuery gameQuery = GiantBombApi.createQuery();
				gameQuery.addFilter(FilterEnum.id, game.getId() + "");
				try {
					Game newGame = gameQuery.execute().get(0);
					if (newGame.getDateLastUpdated() > game.getDateLastUpdated()) {
						dao.updateGame(newGame);
						updated = true;
						Log.i("updated", newGame.getName());
					} else {
						Log.i("not updated", newGame.getName());
					}
				} catch (Exception e) {
					Log.e("not updated", e.getMessage(), e);
				}

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (updated) {
				ListView listview = (ListView) rootView.findViewById(R.id.gamesListView);
				ListAdapter adapter = listview.getAdapter();
				((GamesListArrayAdapter) adapter).notifyDataSetChanged();
			}
		}
	}

	private class InfoDownloader extends AsyncTask<GiantBombGamesQuery, List<Game>, Void> {
		private View rootView;
		private ProgressBar progress;
		private boolean failed;

		public InfoDownloader(View rootView) {
			this.rootView = rootView;
		}

		@Override
		protected void onPreExecute() {
			progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
			progress.setVisibility(View.VISIBLE);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(GiantBombGamesQuery... params) {
			GiantBombGamesQuery query = params[0];
			while (!query.reachedOffset() && !failed) {
				List<Game> result;
				try {
					result = query.execute();
					publishProgress(result);
				} catch (Exception ex) {
					failed = true;
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(List<Game>... values) {
			progress.setVisibility(View.GONE);
			List<Game> result = new ArrayList<Game>();
			for (List<Game> value : values) {
				result.addAll(value);
			}
			ListView listview = (ListView) rootView.findViewById(R.id.gamesListView);
			ListAdapter adapter = listview.getAdapter();
			if (adapter == null) {
				adapter = new GamesListArrayAdapter(getActivity(), R.layout.games_list_item, R.id.titleTextView, result, selection);
				listview.setAdapter(adapter);
			} else {
				((GamesListArrayAdapter) adapter).addAll(result);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO
			progress.setVisibility(View.GONE);
			if (failed) {
				Toast.makeText(getActivity(), "nie ma internetów", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), "gotowe", Toast.LENGTH_LONG).show();
			}
		}
	}
}
