package romanovsky.gamerdplus.ui.list;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import romanovsky.gamerdplus.R;
import romanovsky.gamerdplus.async.AsyncTask;
import romanovsky.gamerdplus.database.dao.GameDAO;
import romanovsky.gamerdplus.database.dao.GenreDAO;
import romanovsky.gamerdplus.database.dao.PlatformDAO;
import romanovsky.gamerdplus.giantbomb.api.FilterEnum;
import romanovsky.gamerdplus.giantbomb.api.GiantBombApi;
import romanovsky.gamerdplus.giantbomb.api.core.Game;
import romanovsky.gamerdplus.giantbomb.api.core.Genre;
import romanovsky.gamerdplus.giantbomb.api.core.Platform;
import romanovsky.gamerdplus.giantbomb.api.queries.GiantBombGameQuery;
import romanovsky.gamerdplus.giantbomb.api.queries.GiantBombGamesQuery;
import romanovsky.gamerdplus.settings.Settings;
import romanovsky.gamerdplus.settings.SettingsManager;
import romanovsky.gamerdplus.ui.CustomFragment;
import romanovsky.gamerdplus.ui.drawer.DrawerSelection;
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
	private ProgressBar extraProgress;
	private ExpandableListView listView;
	private int notifyDuration;
	private boolean autoUpdate;
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
		autoUpdate = settings.isAutoUpdate();
		canNotify = settings.isNotify();
		notifyDuration = settings.getDuration();
		expandSections = settings.getAutoExpand();
		progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
		extraProgress = (ProgressBar) rootView.findViewById(R.id.extraProgressBar);
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
		if (workingTask != null) {
			workingTask.cancel(true);
		}
		dao = new GameDAO(getActivity());
		final View rootView = view;
		Calendar calendar = Calendar.getInstance();
		if (selection == DrawerSelection.TRACKED.getValue()) {
			GamesListInitializer initializer = new GamesListInitializer(rootView);
			workingTask = initializer;
			initializer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
		} else if (selection == DrawerSelection.THIS_MONTH.getValue()) {
			GiantBombGamesQuery monthQuery = GiantBombApi.createGamesQuery();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			monthQuery.addFilter(FilterEnum.expected_release_year, "" + year).addFilter(FilterEnum.expected_release_month, "" + month);
			InfoDownloader downloader = new InfoDownloader(rootView, false);
			workingTask = downloader;
			downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, monthQuery);
		} else if (selection == DrawerSelection.NEXT_MONTH.getValue()) {
			GiantBombGamesQuery monthQuery = GiantBombApi.createGamesQuery();
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
				GiantBombGamesQuery query = GiantBombApi.createGamesQuery();
				query.addFilter(FilterEnum.expected_release_year, "" + year).addFilter(FilterEnum.expected_release_month, "" + i);
				queries[i - month] = query;
			}
			// TODO giantbomb legacy
			GiantBombGamesQuery query = GiantBombApi.createGamesQuery();
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
					if (workingTask != null) {
						workingTask.cancel(true);
						expanded = false;
					}
					listView.setAdapter((GamesListExpandableAdapter) null);
					String searchPhrase = searchBox.getText().toString();
					searchPhrase = searchPhrase.replace(" ", "%20");
					GiantBombGamesQuery nameQuery = GiantBombApi.createGamesQuery();
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
		sb.append("PLATFORMS:");
		boolean filtered = false;
		for (Platform p : allPlatforms) {
			if (p.isFiltered()) {
				if (!filtered) {
					filtered = true;
				}
				sb.append(p.getAbbreviation()).append(",");
			}
		}
		if (filtered) {
			sb.setLength(sb.length() - 1);
		}
		sb.append(";GENRES:");
		filtered = false;
		GenreDAO genreDAO = new GenreDAO(getActivity());
		genreDAO.open();
		List<Genre> allGenres = genreDAO.getAllGenres();
		genreDAO.close();
		for (Genre g : allGenres) {
			if (g.isFiltered()) {
				if (!filtered) {
					filtered = true;
				}
				sb.append(g.getName()).append(",");
			}
		}
		if (filtered) {
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
			} else if (autoUpdate) {
				TrackedGamesUpdater updater = new TrackedGamesUpdater(rootView);
				workingTask = updater;
				updater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new ArrayList<Game>(result));
			} else {
				progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
				progress.setProgress(progress.getMax());
			}
		}

	}

	private class TrackedGamesUpdater extends AsyncTask<List<Game>, Void, List<Game>> {
		private View rootView;
		private int updated;

		public TrackedGamesUpdater(View rootView) {
			this.rootView = rootView;
		}

		@Override
		protected void onPreExecute() {
			progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
			progress.setProgress(0);
			extraProgress.setVisibility(View.VISIBLE);
			if (getActivity() != null) {
				Toast.makeText(getActivity(), getResources().getString(R.string.updating_tracked), Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected List<Game> doInBackground(List<Game>... params) {
			PlatformDAO platformDAO = new PlatformDAO(getActivity());
			List<Platform> allPlatforms = platformDAO.getAllPlatforms();
			GenreDAO genreDAO = new GenreDAO(getActivity());
			genreDAO.open();
			Set<Genre> discoveredGenres = new HashSet<Genre>();
			List<Genre> allGenres = genreDAO.getAllGenres();
			List<Game> games = params[0];
			progress.setMax(games.size());
			for (Game game : games) {
				if (!isCancelled()) {
					GiantBombGamesQuery gameQuery = GiantBombApi.createGamesQuery();
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
							GiantBombGameQuery singleGameQuery = GiantBombApi.createGameQuery(newGame);
							List<Genre> genres = singleGameQuery.execute(false);
							List<String> genreNames = new ArrayList<String>();
							for (Genre genre : genres) {
								discoveredGenres.add(genre);
								genreNames.add(genre.getName());
							}
							newGame.setGenres(genreNames);
							rewriteGame(game, newGame);
							dao.updateGame(game);
							updated++;
							game.setUpdated(true);
							Log.i("updated", game.getName());
						} else {
							Log.i("not updated", game.getName());
						}
					} catch (Exception e) {
						Log.e("not updated", e.getMessage());
					}
					publishProgress((Void) null);
				}
			}
			for (Genre discoveredGenre : discoveredGenres) {
				if (!allGenres.contains(discoveredGenre)) {
					allGenres.add(discoveredGenre);
					genreDAO.addGenre(discoveredGenre);
				}
			}
			genreDAO.close();
			return games;
		}

		private void rewriteGame(Game game, Game newGame) {
			game.setApiDetailURL(newGame.getApiDetailURL());
			game.setDateLastUpdated(newGame.getDateLastUpdated());
			game.setDescription(newGame.getDescription());
			game.setExpectedReleaseDay(newGame.getExpectedReleaseDay());
			game.setExpectedReleaseMonth(newGame.getExpectedReleaseMonth());
			game.setExpectedReleaseQuarter(newGame.getExpectedReleaseQuarter());
			game.setExpectedReleaseYear(newGame.getExpectedReleaseYear());
			game.setGenres(newGame.getGenres());
			game.setIconURL(newGame.getIconURL());
			game.setName(newGame.getName());
			game.setPlatforms(newGame.getPlatforms());
			game.setSiteDetailURL(newGame.getSiteDetailURL());
			if (game.isNotify() && newGame.isOutFor() >= 0) {
				game.setNotify(false);
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			progress.incrementProgressBy(1);
		}

		@Override
		protected void onPostExecute(List<Game> games) {
			workingTask = null;
			if (!isCancelled()) {
				progress.setProgress(progress.getMax());
				extraProgress.setVisibility(View.GONE);
				if (updated > 0) {
					ExpandableListAdapter adapter = listView.getExpandableListAdapter();
					((GamesListExpandableAdapter) adapter).getGamesForFilter().clear();
					((GamesListExpandableAdapter) adapter).getOutGamesForFilter().clear();
					((GamesListExpandableAdapter) adapter).addAll(games);
					((GamesListExpandableAdapter) adapter).notifyDataSetChanged();
				}
				if (getActivity() != null) {
					String msg;
					if (updated == 1) {
						msg = String.format(getResources().getString(R.string.updated_tracked_one), updated);
					} else {
						msg = String.format(getResources().getString(R.string.updated_tracked_many), updated);
					}
					Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
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
			extraProgress.setVisibility(View.VISIBLE);
			if (getActivity() != null) {
				Toast.makeText(getActivity(), getResources().getString(R.string.loading_games), Toast.LENGTH_SHORT).show();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(GiantBombGamesQuery... params) {
			PlatformDAO platformDAO = new PlatformDAO(getActivity());
			List<Platform> allPlatforms = platformDAO.getAllPlatforms();
			final GenreDAO genreDAO = new GenreDAO(getActivity());
			genreDAO.open();
			final Map<Genre, Genre> discoveredGenres = new ConcurrentHashMap<Genre, Genre>();
			final List<Genre> allGenres = genreDAO.getAllGenres();
			multipleQueries = params.length > 1;
			for (int i = 0; i < params.length; i++) {
				lastIteration = i == params.length - 1;
				GiantBombGamesQuery query = params[i];
				List<Game> results = new ArrayList<Game>();
				while (!query.reachedOffset() && !failed && !isCancelled()) {
					List<Game> result;
					try {
						result = query.execute(untilToday);
						ExecutorService es = Executors.newFixedThreadPool(result.size());
						for (final Game game : result) {
							Runnable r = new Runnable() {
								@Override
								public void run() {
									try {
										List<Genre> genres = GiantBombApi.createGameQuery(game).execute(false);
										List<String> genreNames = new ArrayList<String>();
										for (Genre genre : genres) {
											discoveredGenres.put(genre, genre);
											genreNames.add(genre.getName());
										}
										game.setGenres(genreNames);
									} catch (Exception e) {
										Log.e(getClass().getSimpleName(), e.getMessage());
									}
								}
							};
							es.submit(r);
						}
						es.shutdown();

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
						if (!isCancelled()) {
							if (multipleQueries) {
								results.addAll(result);
							}
							if (!multipleQueries || lastIteration) {
								publishProgress(result);
							}
						}
					} catch (Exception ex) {
						failed = true;
					}
					if (multipleQueries && !failed || lastIteration && !failed) {
						publishProgress(results);
					}
				}
			}
			for (Genre discoveredGenre : discoveredGenres.keySet()) {
				if (!allGenres.contains(discoveredGenre)) {
					allGenres.add(discoveredGenre);
					genreDAO.addGenre(discoveredGenre);
				}
			}
			genreDAO.close();
			return null;
		}

		@Override
		protected void onProgressUpdate(List<Game>... values) {
			if (!isCancelled()) {
				filter = createFilter();
				List<Game> result = new ArrayList<Game>();
				for (List<Game> value : values) {
					result.addAll(value);
				}
				if (multipleQueries) {
					if (progress.getProgress() == progress.getMax() - 1) {
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
				expandListSections();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			workingTask = null;
			if (!isCancelled()) {
				expanded = false;
				progress.setProgress(progress.getMax());
				extraProgress.setVisibility(View.GONE);
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
	public void filter(String filter) {
		GamesListExpandableAdapter adapter = (GamesListExpandableAdapter) listView.getExpandableListAdapter();
		if (adapter != null) {
			adapter.getFilter().filter(filter);
		}
	}
}
