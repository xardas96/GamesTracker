package xardas.gamestracker.ui.list;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import xardas.gamestracker.R;
import xardas.gamestracker.async.AsyncTask;
import xardas.gamestracker.database.GameDAO;
import xardas.gamestracker.giantbomb.api.FilterEnum;
import xardas.gamestracker.giantbomb.api.Game;
import xardas.gamestracker.giantbomb.api.GiantBombApi;
import xardas.gamestracker.giantbomb.api.GiantBombGamesQuery;
import xardas.gamestracker.settings.Settings;
import xardas.gamestracker.settings.SettingsManager;
import xardas.gamestracker.ui.RefreshableFragment;
import xardas.gamestracker.ui.drawer.DrawerSelection;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class GamesListFragment extends RefreshableFragment {
	private GameDAO dao;
	private int selection;
	private ProgressBar progress;
	private int notifyDuration;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.games_list_fragment, container, false);
		SettingsManager manager = new SettingsManager(getActivity());
		Settings settings = manager.loadSettings();
		notifyDuration = settings.getDuration();
		progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
		selection = getArguments().getInt("selection");
		refresh(rootView);
		return rootView;
	}

	@Override
	public void refresh(View view) {
		if (view == null) {
			view = getView();
		}
		final View rootView = view;
		Calendar calendar = Calendar.getInstance();
		if (selection == DrawerSelection.TRACKED.getValue()) {
			dao = new GameDAO(getActivity());
			GamesListInitializer initializer = new GamesListInitializer(rootView);
			initializer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
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
			showKeyboard(searchBox);
			searchBox.setOnEditorActionListener(new OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					ListView listView = (ListView) rootView.findViewById(R.id.gamesListView);
					listView.setAdapter(null);
					String searchPhrase = searchBox.getText().toString();
					searchPhrase = searchPhrase.replace(" ", "%20");
					GiantBombGamesQuery nameQuery = GiantBombApi.createQuery();
					nameQuery.addFilter(FilterEnum.name, searchPhrase);
					hideKeyboard();
					InfoDownloader downloader = new InfoDownloader(rootView, false);
					downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nameQuery);
					return true;
				}
			});
			searchBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showKeyboard(searchBox);

				}
			});
		}

	}

	private void showKeyboard(EditText searchBox) {
		final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (android.os.Build.VERSION.SDK_INT < 11) {
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		} else {
			imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	private void hideKeyboard() {
		final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (android.os.Build.VERSION.SDK_INT < 11) {
			imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
		} else {
			imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	private class GamesListInitializer extends AsyncTask<Void, List<Game>, List<Game>> {
		private View rootView;

		public GamesListInitializer(View rootView) {
			this.rootView = rootView;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected List<Game> doInBackground(Void... params) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
			}
			List<Game> result = new ArrayList<Game>();
			while (dao.hasNext()) {
				List<Game> games = dao.getGames();
				result.addAll(games);
				publishProgress(games);
			}
			return result;
		}

		@Override
		protected void onProgressUpdate(List<Game>... values) {
			for (List<Game> list : values) {
				ListView gamesListView = (ListView) rootView.findViewById(R.id.gamesListView);
				ListAdapter adapter = gamesListView.getAdapter();
				if (adapter == null) {
					adapter = new GamesListArrayAdapter(getActivity(), R.layout.games_list_item, R.id.titleTextView, list, selection, notifyDuration);
					gamesListView.setAdapter(adapter);
				} else {
					((GamesListArrayAdapter) adapter).addAll(list);
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(List<Game> result) {
			TrackedGamesUpdater updater = new TrackedGamesUpdater(rootView);
			updater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new ArrayList<Game>(result));
		}

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
			if (getActivity() != null) {
				Toast.makeText(getActivity(), getResources().getString(R.string.updating_tracked), Toast.LENGTH_SHORT).show();
			}

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
				ListView listView = (ListView) rootView.findViewById(R.id.gamesListView);
				ListAdapter adapter = listView.getAdapter();
				((GamesListArrayAdapter) adapter).notifyDataSetChanged();
			}
			if (getActivity() != null) {
				Toast.makeText(getActivity(), getResources().getString(R.string.updated_tracked), Toast.LENGTH_SHORT).show();
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
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
			}
			if (getActivity() != null) {
				Toast.makeText(getActivity(), getResources().getString(R.string.loading_games), Toast.LENGTH_SHORT).show();
			}
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
			ListView listView = (ListView) rootView.findViewById(R.id.gamesListView);
			ListAdapter adapter = listView.getAdapter();
			if (adapter == null && getActivity() != null) {
				adapter = new GamesListArrayAdapter(getActivity(), R.layout.games_list_item, R.id.titleTextView, result, selection, notifyDuration);
				listView.setAdapter(adapter);
			} else if (adapter != null) {
				((GamesListArrayAdapter) adapter).addAll(result);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			progress.setProgress(progress.getMax());
			if (failed) {
				if (getActivity() != null) {
					Toast.makeText(getActivity(), getResources().getString(R.string.no_games_error), Toast.LENGTH_SHORT).show();
				}
			} else {
				if (getActivity() != null) {
					ListView listView = (ListView) rootView.findViewById(R.id.gamesListView);
					ListAdapter adapter = listView.getAdapter();
					if (adapter.getCount() == 0) {
						Toast.makeText(getActivity(), getResources().getString(R.string.no_games_found), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getActivity(), getResources().getString(R.string.all_loaded), Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}
}
