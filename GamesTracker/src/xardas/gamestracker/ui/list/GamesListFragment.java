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
import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class GamesListFragment extends Fragment {
	private GameDAO dao;
	private int selection;
	private ProgressBar progress;

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.games_list_fragment, container, false);
		progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
		progress.getProgressDrawable().setColorFilter(getResources().getColor(R.color.green), Mode.SRC_IN);
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
			updater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new ArrayList<Game>(games));
		} else if (selection == DrawerSelection.THIS_MONTH.getValue()) {
			GiantBombGamesQuery monthQuery = GiantBombApi.createQuery();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			monthQuery.addFilter(FilterEnum.expected_release_year, "" + year).addFilter(FilterEnum.expected_release_month, "" + month);
			InfoDownloader downloader = new InfoDownloader(rootView, true);
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, monthQuery);
		} else if (selection == DrawerSelection.NEXT_MONTH.getValue()) {
			GiantBombGamesQuery monthQuery = GiantBombApi.createQuery();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 2;
			monthQuery.addFilter(FilterEnum.expected_release_year, "" + year).addFilter(FilterEnum.expected_release_month, "" + month);
			InfoDownloader downloader = new InfoDownloader(rootView, true);
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, monthQuery);
		} else if (selection == DrawerSelection.YEAR.getValue()) {
			GiantBombGamesQuery query = GiantBombApi.createQuery();
			int year = calendar.get(Calendar.YEAR);
			query.addFilter(FilterEnum.expected_release_year, year + "");
			InfoDownloader downloader = new InfoDownloader(rootView, true);
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
		} else if (selection == DrawerSelection.SEARCH.getValue()) {
			final EditText searchBox = (EditText) rootView.findViewById(R.id.searchText);
			searchBox.setVisibility(View.VISIBLE);
			searchBox.requestFocus();
			final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
			searchBox.setOnEditorActionListener(new OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_SEARCH) {
						String searchPhrase = searchBox.getText().toString();
						searchPhrase = searchPhrase.replace(" ", "%20");
						GiantBombGamesQuery nameQuery = GiantBombApi.createQuery();
						nameQuery.addFilter(FilterEnum.name, searchPhrase);
						InfoDownloader downloader = new InfoDownloader(rootView, false);
						downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nameQuery);
						imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
						return true;
					}
					return false;
				}
			});
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
		protected void onPreExecute() {
			progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
			progress.setProgress(0);
		}

		@Override
		protected Void doInBackground(List<Game>... params) {
			List<Game> games = params[0];
			progress.setMax(games.size());
			for (Game game : games) {
				GiantBombGamesQuery gameQuery = GiantBombApi.createQuery();
				gameQuery.addFilter(FilterEnum.id, game.getId() + "");
				try {
					Game newGame = gameQuery.execute(false).get(0);
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
				publishProgress((Void) null);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			progress.incrementProgressBy(1);
		}

		@Override
		protected void onPostExecute(Void result) {
			progress.setProgress(progress.getMax());
			if (updated) {
				ListView listview = (ListView) rootView.findViewById(R.id.gamesListView);
				ListAdapter adapter = listview.getAdapter();
				((GamesListArrayAdapter) adapter).notifyDataSetChanged();
			}
		}
	}

	private class InfoDownloader extends AsyncTask<GiantBombGamesQuery, List<Game>, Void> {
		private View rootView;
		private boolean untilToday;
		private ProgressBar progress;
		private boolean failed;
		private int totalResults = -1;

		public InfoDownloader(View rootView, boolean untilToday) {
			this.rootView = rootView;
			this.untilToday = untilToday;
		}

		@Override
		protected void onPreExecute() {
			progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
			progress.setProgress(0);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(GiantBombGamesQuery... params) {
			GiantBombGamesQuery query = params[0];
			while (!query.reachedOffset() && !failed) {
				List<Game> result;
				try {
					result = query.execute(untilToday);
					if (totalResults == -1) {
						totalResults = query.getTotalResults();
						progress.setMax(totalResults);
					}
					publishProgress(result);
				} catch (Exception ex) {
					failed = true;
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(List<Game>... values) {
			List<Game> result = new ArrayList<Game>();
			for (List<Game> value : values) {
				result.addAll(value);
			}
			progress.incrementProgressBy(result.size());
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
			progress.setProgress(progress.getMax());
			if (failed) {
				Toast.makeText(getActivity(), getResources().getString(R.string.no_games_error), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity(), getResources().getString(R.string.all_loaded), Toast.LENGTH_LONG).show();
			}
		}
	}
}
