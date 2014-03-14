package romanovsky.gamerd.ui.list;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import romanovsky.gamerd.R;
import romanovsky.gamerd.async.AsyncTask;
import romanovsky.gamerd.database.dao.GameDAO;
import romanovsky.gamerd.database.dao.PlatformDAO;
import romanovsky.gamerd.giantbomb.api.FilterEnum;
import romanovsky.gamerd.giantbomb.api.GiantBombApi;
import romanovsky.gamerd.giantbomb.api.GiantBombGamesQuery;
import romanovsky.gamerd.giantbomb.api.core.Game;
import romanovsky.gamerd.giantbomb.api.core.Platform;
import romanovsky.gamerd.settings.Settings;
import romanovsky.gamerd.settings.SettingsManager;
import romanovsky.gamerd.ui.CustomFragment;
import romanovsky.gamerd.ui.drawer.DrawerSelection;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class GamesListFragment extends CustomFragment {
	private GameDAO dao;
	private int selection;
	private ProgressBar progress;
	private ExpandableListView listView;
	private int notifyDuration;
	private boolean canNotify;
	private List<Integer> expandSections;
	private boolean expanded;
	private boolean keyboardShown;
	private static final int MONTH_ARRAY_MAX = 14;
	@SuppressWarnings("rawtypes")
	private AsyncTask workingTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.games_fragment, container, false);
		SettingsManager manager = new SettingsManager(getActivity());
		Settings settings = manager.loadSettings();
		canNotify = settings.isNotify();
		notifyDuration = settings.getDuration();
		expandSections = settings.getAutoExpand();
		progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
		selection = getArguments().getInt("selection");
		listView = (ExpandableListView) rootView.findViewById(R.id.gamesListView);
		refresh(rootView);
		return rootView;
	}

	@Override
	public void refresh(View view) {
		if (view == null) {
			view = getView();
		}
		dao = new GameDAO(getActivity());
		final View rootView = view;
		Calendar calendar = Calendar.getInstance();
		if (selection == DrawerSelection.TRACKED.getValue()) {
			GamesListInitializer initializer = new GamesListInitializer(rootView);
			workingTask = initializer;
			initializer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
		} else if (selection == DrawerSelection.THIS_MONTH.getValue()) {
			GiantBombGamesQuery monthQuery = GiantBombApi.createQuery();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			monthQuery.addFilter(FilterEnum.expected_release_year, "" + year).addFilter(FilterEnum.expected_release_month, "" + month);
			InfoDownloader downloader = new InfoDownloader(rootView, false);
			workingTask = downloader;
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, monthQuery);
		} else if (selection == DrawerSelection.NEXT_MONTH.getValue()) {
			GiantBombGamesQuery monthQuery = GiantBombApi.createQuery();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 2;
			monthQuery.addFilter(FilterEnum.expected_release_year, "" + year).addFilter(FilterEnum.expected_release_month, "" + month);
			InfoDownloader downloader = new InfoDownloader(rootView, false);
			workingTask = downloader;
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, monthQuery);
		} else if (selection == DrawerSelection.YEAR.getValue()) {
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			GiantBombGamesQuery[] queries = new GiantBombGamesQuery[MONTH_ARRAY_MAX - month];
			for (int i = month; i < MONTH_ARRAY_MAX - 1; i++) {
				GiantBombGamesQuery query = GiantBombApi.createQuery();
				query.addFilter(FilterEnum.expected_release_year, "" + year).addFilter(FilterEnum.expected_release_month, "" + i);
				queries[i - month] = query;
			}
			// TODO giantbomb legacy
			GiantBombGamesQuery query = GiantBombApi.createQuery();
			query.addFilter(FilterEnum.original_release_date, "2014-01-01 00:00:00".replace(" ", "%20"));
			queries[MONTH_ARRAY_MAX - 1 - month] = query;
			InfoDownloader downloader = new InfoDownloader(rootView, false);
			workingTask = downloader;
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, queries);
		} else if (selection == DrawerSelection.SEARCH.getValue()) {
			final EditText searchBox = (EditText) rootView.findViewById(R.id.searchText);
			searchBox.setVisibility(View.VISIBLE);
			setKeyboardFocus(searchBox);
			searchBox.setOnEditorActionListener(new OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					listView.setAdapter((GamesListExpandableAdapter) null);
					String searchPhrase = searchBox.getText().toString();
					searchPhrase = searchPhrase.replace(" ", "%20");
					GiantBombGamesQuery nameQuery = GiantBombApi.createQuery();
					nameQuery.addFilter(FilterEnum.name, searchPhrase);
					hideKeyboard();
					InfoDownloader downloader = new InfoDownloader(rootView, false);
					workingTask = downloader;
					downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nameQuery);
					return true;
				}
			});
		}
	}

	@Override
	public void onPause() {
		hideKeyboard();
		if (workingTask != null) {
			workingTask.cancel(true);
		}
		super.onPause();
	}

	@SuppressLint("Recycle")
	public void setKeyboardFocus(final EditText primaryTextField) {
		(new Handler()).postDelayed(new Runnable() {
			public void run() {
				primaryTextField.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
				primaryTextField.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
				keyboardShown = true;
			}
		}, 20);
	}

	private void expandListSections() {
		if (!expanded && listView != null) {
			for (Integer section : expandSections) {
				listView.expandGroup(section);
			}
			expanded = true;
		}
	}

	private void hideKeyboard() {
		if (keyboardShown) {
			final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			if (android.os.Build.VERSION.SDK_INT < 11) {
				imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
			} else {
				imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
			keyboardShown = false;
		}
	}

	private String createFilter() {
		PlatformDAO platformDAO = new PlatformDAO(getActivity());
		List<Platform> allPlatforms = platformDAO.getAllPlatforms();
		StringBuilder sb = new StringBuilder();
		for (Platform p : allPlatforms) {
			if (p.isFiltered()) {
				sb.append(p.getAbbreviation()).append(",");
			}
		}
		if (sb.length() != 0) {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	private class GamesListInitializer extends AsyncTask<Void, List<Game>, List<Game>> {
		private View rootView;
		private String filter;

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
			filter = createFilter();
			for (List<Game> list : values) {
				ExpandableListAdapter adapter = listView.getExpandableListAdapter();
				if (adapter == null) {
					adapter = new GamesListExpandableAdapter(getActivity(), list, selection, notifyDuration, canNotify, filter);
					listView.setAdapter(adapter);
				} else {
					((GamesListExpandableAdapter) adapter).setFilter(filter);
					((GamesListExpandableAdapter) adapter).addAll(list);
				}
				if (!isCancelled()) {
					expandListSections();
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(List<Game> result) {
			workingTask = null;
			expanded = false;
			if (result.isEmpty()) {
				TextView nothingTracked = (TextView) rootView.findViewById(R.id.nothingTrackedTextView);
				nothingTracked.setVisibility(View.VISIBLE);
			} else {
				TrackedGamesUpdater updater = new TrackedGamesUpdater(rootView);
				workingTask = updater;
				updater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new ArrayList<Game>(result));
			}
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
			PlatformDAO platformDAO = new PlatformDAO(getActivity());
			List<Platform> allPlatforms = platformDAO.getAllPlatforms();
			List<Game> games = params[0];
			progress.setMax(games.size());
			for (Game game : games) {
				if (!isCancelled()) {
					GiantBombGamesQuery gameQuery = GiantBombApi.createQuery();
					gameQuery.addFilter(FilterEnum.id, game.getId() + "");
					try {
						Game newGame = gameQuery.execute(false).get(0);
						Set<Platform> discoveredPlatforms = gameQuery.getDiscoveredPlatforms();
						for (Platform discoveredPlatform : discoveredPlatforms) {
							if (!allPlatforms.contains(discoveredPlatform)) {
								allPlatforms.add(discoveredPlatform);
								platformDAO.addPlatform(discoveredPlatform);
							}
						}
						discoveredPlatforms.clear();
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
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			progress.incrementProgressBy(1);
		}

		@Override
		protected void onPostExecute(Void result) {
			workingTask = null;
			if (!isCancelled()) {
				progress.setProgress(progress.getMax());
				if (updated) {
					ExpandableListAdapter adapter = listView.getExpandableListAdapter();
					((GamesListExpandableAdapter) adapter).notifyDataSetChanged();
				}
				if (getActivity() != null) {
					Toast.makeText(getActivity(), getResources().getString(R.string.updated_tracked), Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private class InfoDownloader extends AsyncTask<GiantBombGamesQuery, List<Game>, Void> {
		private View rootView;
		private boolean untilToday;
		private ProgressBar progress;
		private boolean failed;
		private int totalResults = -1;
		private boolean maxProgressSet;
		private boolean multipleQueries;
		private boolean lastIteration;
		private String filter;

		public InfoDownloader(View rootView, boolean untilToday) {
			this.rootView = rootView;
			this.untilToday = untilToday;
		}

		@Override
		protected void onPreExecute() {
			progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
			progress.setProgress(0);
			if (getActivity() != null) {
				Toast.makeText(getActivity(), getResources().getString(R.string.loading_games), Toast.LENGTH_SHORT).show();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(GiantBombGamesQuery... params) {
			PlatformDAO platformDAO = new PlatformDAO(getActivity());
			List<Platform> allPlatforms = platformDAO.getAllPlatforms();
			multipleQueries = params.length > 1;
			for (int i = 0; i < params.length; i++) {
				lastIteration = i == params.length - 1;
				GiantBombGamesQuery query = params[i];
				List<Game> results = new ArrayList<Game>();
				while (!query.reachedOffset() && !failed) {
					List<Game> result;
					try {
						result = query.execute(untilToday);
						Set<Platform> discoveredPlatforms = query.getDiscoveredPlatforms();
						for (Platform discoveredPlatform : discoveredPlatforms) {
							if (!allPlatforms.contains(discoveredPlatform)) {
								allPlatforms.add(discoveredPlatform);
								platformDAO.addPlatform(discoveredPlatform);
							}
						}
						discoveredPlatforms.clear();
						if (!maxProgressSet && !multipleQueries) {
							totalResults = query.getTotalResults();
							progress.setMax(totalResults);
							maxProgressSet = true;
						} else if (!maxProgressSet && multipleQueries) {
							progress.setMax(params.length);
							maxProgressSet = true;
						}
						for (Game game : result) {
							game.setTracked(dao.isTracked(game));
						}
						if (multipleQueries) {
							results.addAll(result);
						}
						if (!multipleQueries || lastIteration) {
							publishProgress(result);
						}
					} catch (Exception ex) {
						failed = true;
					}
				}
				if (multipleQueries && !failed || lastIteration && !failed) {
					publishProgress(results);
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(List<Game>... values) {
			filter = createFilter();
			List<Game> result = new ArrayList<Game>();
			for (List<Game> value : values) {
				result.addAll(value);
			}
			if (multipleQueries) {
				if (lastIteration) {
					progress.setMax(progress.getMax() + 1);
				}
				progress.incrementProgressBy(1);
			} else {
				progress.incrementProgressBy(result.size());
			}
			ExpandableListAdapter adapter = listView.getExpandableListAdapter();
			if (adapter == null && getActivity() != null) {
				adapter = new GamesListExpandableAdapter(getActivity(), result, selection, notifyDuration, canNotify, filter);
				listView.setAdapter(adapter);
			} else if (adapter != null) {
				((GamesListExpandableAdapter) adapter).setFilter(filter);
				((GamesListExpandableAdapter) adapter).addAll(result);
			}
			if (!isCancelled()) {
				expandListSections();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			workingTask = null;
			if (!isCancelled()) {
				expanded = false;
				progress.setProgress(progress.getMax());
				if (failed) {
					if (getActivity() != null) {
						Toast.makeText(getActivity(), getResources().getString(R.string.no_games_error), Toast.LENGTH_SHORT).show();
					}
				} else {
					if (getActivity() != null) {
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

	@Override
	public void filter(int filterType, String filter) {
		GamesListExpandableAdapter adapter = (GamesListExpandableAdapter) listView.getExpandableListAdapter();
		if (adapter != null) {
			adapter.getFilter().filter(filter);
		}
	}
}
