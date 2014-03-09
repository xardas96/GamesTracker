package romanovsky.gamerd;

import romanovsky.gamerd.giantbomb.api.GiantBombApi;
import romanovsky.gamerd.ui.RefreshableFragment;
import romanovsky.gamerd.ui.drawer.DrawerListArrayAdapter;
import romanovsky.gamerd.ui.drawer.DrawerSelection;
import romanovsky.gamerd.ui.list.GamesListFragment;
import romanovsky.gamerd.ui.settings.SettingsFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
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
	private RefreshableFragment fragment;
	private AdView adView;
	private boolean adLoaded;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		adView = (AdView) findViewById(R.id.adView);
		checkConnection();
		GiantBombApi.setApiKey("ca8b79e01baa4e10a46ca36c648182bfe9e60c3b");

		title = drawerTitle = getTitle();
		drawerListTitles = getResources().getStringArray(R.array.side_menu);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerList.setAdapter(new DrawerListArrayAdapter(getApplicationContext(), R.layout.drawer_list_item, drawerListTitles));
		drawerList.setOnItemClickListener(new OnItemClickListener() {
			private int selected = -1;

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				checkConnection();
				if (selected != position) {
					selected = position;
					if (selected == DrawerSelection.SETTINGS.getValue()) {
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
		switch (item.getItemId()) {
		case R.id.refresh:
			if (fragment != null) {
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
				AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice("8601A23B1A531F92019924C767CFC438")
				.build();
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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
}