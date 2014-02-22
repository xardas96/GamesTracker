package xardas.gamestracker.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import xardas.gamestracker.giantbomb.api.Game;
import xardas.gamestracker.giantbomb.api.GameComparator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GameDAO {
	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	private String[] allColumns = { SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_NAME, SQLiteHelper.COLUMN_DATE_LAST_UPDATED, SQLiteHelper.COLUMN_EXPECTED_RELEASE_DAY, SQLiteHelper.COLUMN_EXPECTED_RELEASE_MONTH, SQLiteHelper.COLUMN_EXPECTED_RELEASE_YEAR, SQLiteHelper.COLUMN_EXPECTED_RELEASE_QUARTER, SQLiteHelper.COLUMN_PLATFORMS, SQLiteHelper.COLUMN_ICON_URL, SQLiteHelper.COLUMN_SMALL_URL, SQLiteHelper.COLUMN_NOTIFY };

	public GameDAO(Context context) {
		dbHelper = new SQLiteHelper(context);
	}

	private void open() {
		database = dbHelper.getWritableDatabase();
	}

	private void close() {
		dbHelper.close();
	}

	public void addGame(Game game) {
		open();
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_ID, game.getId());
		values.put(SQLiteHelper.COLUMN_NAME, game.getName());
		values.put(SQLiteHelper.COLUMN_DATE_LAST_UPDATED, game.getDateLastUpdated());
		values.put(SQLiteHelper.COLUMN_EXPECTED_RELEASE_DAY, game.getExpectedReleaseDay());
		values.put(SQLiteHelper.COLUMN_EXPECTED_RELEASE_MONTH, game.getExpectedReleaseMonth());
		values.put(SQLiteHelper.COLUMN_EXPECTED_RELEASE_YEAR, game.getExpectedReleaseYear());
		values.put(SQLiteHelper.COLUMN_EXPECTED_RELEASE_QUARTER, game.getExpectedReleaseQuarter());
		StringBuilder platformsBuilder = new StringBuilder();
		for (String platform : game.getPlatforms()) {
			platformsBuilder.append(platform).append(",");
		}
		if (platformsBuilder.length() > 0) {
			platformsBuilder.setLength(platformsBuilder.length() - 1);
		}
		values.put(SQLiteHelper.COLUMN_PLATFORMS, platformsBuilder.toString());
		values.put(SQLiteHelper.COLUMN_ICON_URL, game.getIconURL());
		values.put(SQLiteHelper.COLUMN_SMALL_URL, game.getSmallURL());
		values.put(SQLiteHelper.COLUMN_NOTIFY, game.isNotify() ? 1 : 0);
		database.insert(SQLiteHelper.TABLE_GAMES, null, values);
		close();
	}

	public void deleteGame(Game game) {
		open();
		long id = game.getId();
		database.delete(SQLiteHelper.TABLE_GAMES, SQLiteHelper.COLUMN_ID + " = " + id, null);
		close();
	}

	public void updateGame(Game game) {
		open();
		long id = game.getId();
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_NAME, game.getName());
		values.put(SQLiteHelper.COLUMN_DATE_LAST_UPDATED, game.getDateLastUpdated());
		values.put(SQLiteHelper.COLUMN_EXPECTED_RELEASE_DAY, game.getExpectedReleaseDay());
		values.put(SQLiteHelper.COLUMN_EXPECTED_RELEASE_MONTH, game.getExpectedReleaseMonth());
		values.put(SQLiteHelper.COLUMN_EXPECTED_RELEASE_YEAR, game.getExpectedReleaseYear());
		values.put(SQLiteHelper.COLUMN_EXPECTED_RELEASE_QUARTER, game.getExpectedReleaseQuarter());
		StringBuilder platformsBuilder = new StringBuilder();
		for (String platform : game.getPlatforms()) {
			platformsBuilder.append(platform).append(",");
		}
		if (platformsBuilder.length() > 0) {
			platformsBuilder.setLength(platformsBuilder.length() - 1);
		}
		values.put(SQLiteHelper.COLUMN_PLATFORMS, platformsBuilder.toString());
		values.put(SQLiteHelper.COLUMN_ICON_URL, game.getIconURL());
		values.put(SQLiteHelper.COLUMN_SMALL_URL, game.getSmallURL());
		values.put(SQLiteHelper.COLUMN_NOTIFY, game.isNotify() ? 1 : 0);
		database.update(SQLiteHelper.TABLE_GAMES, values, SQLiteHelper.COLUMN_ID + " = " + id, null);
		close();
	}

	public boolean isTracked(Game game) {
		open();
		long id = game.getId();
		Cursor cursor = database.query(SQLiteHelper.TABLE_GAMES, new String[] { SQLiteHelper.COLUMN_ID }, SQLiteHelper.COLUMN_ID + "=" + id, null, null, null, null);
		boolean isTracked = cursor.moveToFirst();
		close();
		return isTracked;
	}

	public List<Game> getAllGames() {
		open();
		List<Game> games = new ArrayList<Game>();
		Cursor cursor = database.query(SQLiteHelper.TABLE_GAMES, allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Game game = parseGame(cursor);
			games.add(game);
			cursor.moveToNext();
		}
		close();
		Collections.sort(games, new GameComparator());
		return games;
	}

	private Game parseGame(Cursor cursor) {
		Game game = new Game();
		game.setId(cursor.getLong(0));
		game.setName(cursor.getString(1));
		game.setDateLastUpdated(cursor.getLong(2));
		game.setExpectedReleaseDay(cursor.getInt(3));
		game.setExpectedReleaseMonth(cursor.getInt(4));
		game.setExpectedReleaseYear(cursor.getInt(5));
		game.setExpectedReleaseQuarter(cursor.getInt(6));
		String platforms = cursor.getString(7);
		String[] split = platforms.split(",");
		List<String> platformsList = Arrays.asList(split);
		game.setPlatforms(platformsList);
		game.setIconURL(cursor.getString(8));
		game.setSmallURL(cursor.getString(9));
		game.setNotify(cursor.getInt(10) == 1);
		return game;
	}
}
