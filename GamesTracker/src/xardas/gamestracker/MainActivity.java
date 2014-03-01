package xardas.gamestracker;

import xardas.gamestracker.giantbomb.api.GiantBombApi;
import xardas.gamestracker.ui.RefreshableFragment;
import xardas.gamestracker.ui.drawer.DrawerListArrayAdapter;
import xardas.gamestracker.ui.drawer.DrawerSelection;
import xardas.gamestracker.ui.list.GamesListFragment;
import xardas.gamestracker.ui.settings.SettingsFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private CharSequence drawerTitle;
	private CharSequence title;
	private String[] drawerListTitles;
	private RefreshableFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		GiantBombApi.setApiKey("ca8b79e01baa4e10a46ca36c648182bfe9e60c3b");

		title = drawerTitle = getTitle();
		drawerListTitles = getResources().getStringArray(R.array.side_menu);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerList.setSelector(R.drawable.drawer_list_selector);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerList.setAdapter(new DrawerListArrayAdapter(getApplicationContext(), R.layout.drawer_list_item, drawerListTitles));
		drawerList.setOnItemClickListener(new OnItemClickListener() {
			private int selected = -1;

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(title);
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(drawerTitle);
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

	@Override
	public void setTitle(CharSequence title) {
		this.title = title;
		getActionBar().setTitle(title);
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