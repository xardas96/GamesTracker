package romanovsky.gamerdplus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import romanovsky.gamerdplus.database.dao.GameDAO;
import romanovsky.gamerdplus.database.dao.GenreDAO;
import romanovsky.gamerdplus.database.dao.PlatformDAO;
import romanovsky.gamerdplus.giantbomb.api.GiantBombApi;
import romanovsky.gamerdplus.giantbomb.api.core.Genre;
import romanovsky.gamerdplus.giantbomb.api.core.Platform;
import romanovsky.gamerdplus.ui.CustomFragment;
import romanovsky.gamerdplus.ui.drawer.DrawerListArrayAdapter;
import romanovsky.gamerdplus.ui.drawer.DrawerSelection;
import romanovsky.gamerdplus.ui.list.GamesListFragment;
import romanovsky.gamerdplus.ui.settings.SettingsFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends ActionBarActivity {
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private CharSequence drawerTitle;
	private CharSequence title;
	private String[] drawerListTitles;
	private CustomFragment fragment;
	private AdView adView;
	private boolean adLoaded;
	private int selectedOption = -1;
	private List<Platform> filteredPlatforms;
	private List<Genre> filteredGenres;
	
	private static final String PUBLIC_KEY = "ca8b79e01baa4e10a46ca36c648182bfe9e60c3b";
	private static final String MY_KEY = "cd726ab046e72f7aee8fc91c7fc1ea77d638be8a";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		adView = (AdView) findViewById(R.id.adView);
		checkConnection();
//		GiantBombApi.setApiKey(PUBLIC_KEY);
		GiantBombApi.setApiKey(MY_KEY);

		filteredPlatforms = new ArrayList<Platform>();
		filteredGenres = new ArrayList<Genre>();

		title = drawerTitle = getTitle();
		drawerListTitles = getResources().getStringArray(R.array.side_menu);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerList.setAdapter(new DrawerListArrayAdapter(getApplicationContext(), R.layout.drawer_list_item, drawerListTitles));
		drawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				checkConnection();
				if (selectedOption != position) {
					selectedOption = position;
					if (selectedOption == DrawerSelection.SETTINGS.getValue()) {
						fragment = new SettingsFragment();
					} else {
						fragment = new GamesListFragment();
						Bundle arguments = new Bundle();
						arguments.putInt("selection", position);
						fragment.setArguments(arguments);
					}
					FragmentManager fragmentManager = getSupportFragmentManager();
					fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
					setTitle(drawerListTitles[position]);
				}
				drawerLayout.closeDrawer(drawerList);
			}
		});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(title);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(drawerTitle);
				supportInvalidateOptionsMenu();
			}
		};
		drawerLayout.setDrawerListener(drawerToggle);

		if (savedInstanceState == null) {
			drawerList.performItemClick(drawerList, 0, -1);
			drawerList.setSelection(0);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		checkConnection();
		switch (item.getItemId()) {
		case R.id.refresh:
			if (fragment != null) {
				fragment.refresh(null);
			}
			return true;
		case R.id.filterPlatform:
			if (fragment != null) {
				final PlatformDAO platformDAO = new PlatformDAO(this);
				final List<Platform> allPlatforms;
				if (item.getGroupId() == 1) {
					allPlatforms = platformDAO.getAllPlatforms();
				} else {
					allPlatforms = platformDAO.getPopularAndFilteredPlatforms();
				}
				filteredPlatforms.clear();
				for (Platform platform : allPlatforms) {
					if (platform.isFiltered()) {
						filteredPlatforms.add(platform);
					}
				}
				Collections.sort(allPlatforms, new Comparator<Platform>() {
					@Override
					public int compare(Platform lhs, Platform rhs) {
						return lhs.getName().compareTo(rhs.getName());
					}
				});
				View menuItemView = findViewById(R.id.filter);
				PopupMenu popup = new PopupMenu(this, menuItemView);
				Menu popupMenu = popup.getMenu();
				for (int i = 0; i < allPlatforms.size(); i++) {
					Platform platform = allPlatforms.get(i);
					MenuItem popupMenuItem = popupMenu.add(0, i, 0, platform.getName());
					popupMenuItem.setChecked(platform.isFiltered());
				}
				popupMenu.setGroupCheckable(0, true, false);
				if (!allPlatforms.isEmpty()) {
					if (item.getGroupId() != 1) {
						popupMenu.add(1, R.id.filterPlatform, 0, getResources().getString(R.string.all_platforms));
					} else {
						popupMenu.add(0, R.id.filterPlatform, 0, getResources().getString(R.string.popular_platforms));
					}
				}
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (item.getGroupId() == 0 && item.getItemId() != R.id.filterPlatform) {
							Platform platform = allPlatforms.get(item.getItemId());
							item.setChecked(!item.isChecked());
							platform.setFiltered(item.isChecked());
							platformDAO.updatePlatform(platform);
							if (platform.isFiltered()) {
								filteredPlatforms.add(platform);
							} else {
								filteredPlatforms.remove(platform);
							}
							StringBuilder sb = new StringBuilder();
							sb.append("PLATFORMS:");
							for (Platform p : filteredPlatforms) {
								sb.append(p.getAbbreviation()).append(",");
							}
							if (!filteredPlatforms.isEmpty()) {
								sb.setLength(sb.length() - 1);
							}
							sb.append(";GENRES:");
							for (Genre g : filteredGenres) {
								sb.append(g.getName()).append(",");
							}
							if (!filteredGenres.isEmpty()) {
								sb.setLength(sb.length() - 1);
							}
							fragment.filter(sb.toString());
						} else {
							onOptionsItemSelected(item);
						}
						return true;
					}
				});
				popup.show();
			}
			return true;
		case R.id.filterGenre:
			if (fragment != null) {
				final GenreDAO genreDAO = new GenreDAO(this);
				genreDAO.open();
				final List<Genre> allGenres = genreDAO.getAllGenres();
				filteredGenres.clear();
				for (Genre genre : allGenres) {
					if (genre.isFiltered()) {
						filteredGenres.add(genre);
					}
				}
				Collections.sort(allGenres, new Comparator<Genre>() {
					@Override
					public int compare(Genre lhs, Genre rhs) {
						return lhs.getName().compareTo(rhs.getName());
					}
				});
				View menuItemView = findViewById(R.id.filter);
				PopupMenu popup = new PopupMenu(this, menuItemView);
				Menu popupMenu = popup.getMenu();
				for (int i = 0; i < allGenres.size(); i++) {
					Genre genre = allGenres.get(i);
					MenuItem popupMenuItem = popupMenu.add(0, i, 0, genre.getName());
					popupMenuItem.setChecked(genre.isFiltered());
				}
				popupMenu.setGroupCheckable(0, true, false);
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Genre genre = allGenres.get(item.getItemId());
						item.setChecked(!item.isChecked());
						genre.setFiltered(item.isChecked());
						genreDAO.open();
						genreDAO.updateGenre(genre);
						genreDAO.close();
						if (genre.isFiltered()) {
							filteredGenres.add(genre);
						} else {
							filteredGenres.remove(genre);
						}
						StringBuilder sb = new StringBuilder();
						sb.append("PLATFORMS:");
						for (Platform p : filteredPlatforms) {
							sb.append(p.getAbbreviation()).append(",");
						}
						if (!filteredPlatforms.isEmpty()) {
							sb.setLength(sb.length() - 1);
						}
						sb.append(";GENRES:");
						for (Genre g : filteredGenres) {
							sb.append(g.getName()).append(",");
						}
						if (!filteredGenres.isEmpty()) {
							sb.setLength(sb.length() - 1);
						}
						fragment.filter(sb.toString());
						return true;
					}
				});
				popup.show();
			}
			return true;
		case R.id.forceRefresh:
			if (fragment != null) {
				if (selectedOption == DrawerSelection.TRACKED.getValue()) {
					GameDAO gameDAO = new GameDAO(this);
					gameDAO.forceUpdate();
				}
				fragment.refresh(null);
			}
			return true;
		default:
			return drawerToggle.onOptionsItemSelected(item);
		}
	}

	private void checkConnection() {
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			adView.setVisibility(View.VISIBLE);
			if (!adLoaded) {
				AdRequest adRequest = new AdRequest.Builder().addTestDevice("8601A23B1A531F92019924C767CFC438").build();
				adView.loadAd(adRequest);
				adLoaded = true;
			}
		} else {
			adView.setVisibility(View.GONE);
			adLoaded = false;
		}
	}

	@Override
	protected void onResume() {
		if (adView != null) {
			adView.resume();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	@Override
	public void setTitle(CharSequence title) {
		this.title = title;
		getSupportActionBar().setTitle(title);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (selectedOption != DrawerSelection.SETTINGS.getValue()) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.menu, menu);
			return true;
		}
		return false;
	}
}