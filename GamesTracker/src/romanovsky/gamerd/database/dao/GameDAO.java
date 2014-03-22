package romanovsky.gamerd.database.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import romanovsky.gamerd.database.SQLiteHelper;
import romanovsky.gamerd.giantbomb.api.core.Game;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class GameDAO extends AbstractDAO {
	private String[] allColumns = {
			SQLiteHelper.COLUMN_ID
			, SQLiteHelper.COLUMN_NAME
			, SQLiteHelper.COLUMN_DATE_LAST_UPDATED
			, SQLiteHelper.COLUMN_EXPECTED_RELEASE_DAY
			, SQLiteHelper.COLUMN_EXPECTED_RELEASE_MONTH
			, SQLiteHelper.COLUMN_EXPECTED_RELEASE_YEAR
			, SQLiteHelper.COLUMN_EXPECTED_RELEASE_QUARTER
			, SQLiteHelper.COLUMN_PLATFORMS
			, SQLiteHelper.COLUMN_ICON_URL
			, SQLiteHelper.COLUMN_SITE_DETAIL_URL
			, SQLiteHelper.COLUMN_NOTIFY
			, SQLiteHelper.COLUMN_DESCRIPTION
			, SQLiteHelper.COLUMN_API_DETAIL
			, SQLiteHelper.COLUMN_GENRES
	};
	private static final int LIMIT = 20;
	private int offset = 0;
	private boolean next = true;

	public GameDAO(Context context) {
		super(context);
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
		values.put(SQLiteHelper.COLUMN_SITE_DETAIL_URL, game.getSiteDetailURL());
		values.put(SQLiteHelper.COLUMN_NOTIFY, game.isNotify() ? 1 : 0);
		values.put(SQLiteHelper.COLUMN_DESCRIPTION, game.getDescription());
		values.put(SQLiteHelper.COLUMN_API_DETAIL, game.getApiDetailURL());
		StringBuilder genresBuilder = new StringBuilder();
		for (String platform : game.getGenres()) {
			genresBuilder.append(platform).append(",");
		}
		if (genresBuilder.length() > 0) {
			genresBuilder.setLength(genresBuilder.length() - 1);
		}
		values.put(SQLiteHelper.COLUMN_GENRES, genresBuilder.toString());
		database.insert(SQLiteHelper.TABLE_GAMES, null, values);
		game.setTracked(true);
		close();
	}

	public void deleteGame(Game game) {
		open();
		long id = game.getId();
		database.delete(SQLiteHelper.TABLE_GAMES, SQLiteHelper.COLUMN_ID + " = " + id, null);
		game.setTracked(false);
		game.setNotify(false);
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
		values.put(SQLiteHelper.COLUMN_SITE_DETAIL_URL, game.getSiteDetailURL());
		values.put(SQLiteHelper.COLUMN_NOTIFY, game.isNotify() ? 1 : 0);
		values.put(SQLiteHelper.COLUMN_DESCRIPTION, game.getDescription());
		values.put(SQLiteHelper.COLUMN_API_DETAIL, game.getApiDetailURL());
		StringBuilder genresBuilder = new StringBuilder();
		for (String platform : game.getGenres()) {
			genresBuilder.append(platform).append(",");
		}
		if (genresBuilder.length() > 0) {
			genresBuilder.setLength(genresBuilder.length() - 1);
		}
		values.put(SQLiteHelper.COLUMN_GENRES, genresBuilder.toString());
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

	public boolean hasNext() {
		return next;
	}

	public List<Game> getAllGames() {
		open();
		List<Game> games = new ArrayList<Game>();
		Cursor cursor = database.query(SQLiteHelper.TABLE_GAMES, allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Game game = parseGame(cursor);
			game.setTracked(true);
			games.add(game);
			cursor.moveToNext();
		}
		close();
		return games;
	}

	public List<Game> getGames() {
		open();
		List<Game> games = new ArrayList<Game>();
		Cursor cursor = database.query(SQLiteHelper.TABLE_GAMES, allColumns, null, null, null, null, null, offset + "," + LIMIT);
		next = cursor.moveToFirst();
		if (!next) {
			offset = 0;
		} else {
			while (!cursor.isAfterLast()) {
				Game game = parseGame(cursor);
				game.setTracked(true);
				games.add(game);
				cursor.moveToNext();
				offset++;
			}
		}
		close();
		return games;
	}

	public Game getGame(long id) {
		open();
		Game game;
		Cursor cursor = database.query(SQLiteHelper.TABLE_GAMES, allColumns, SQLiteHelper.COLUMN_ID + "=" + id, null, null, null, null);
		cursor.moveToFirst();
		game = parseGame(cursor);
		game.setTracked(true);
		close();
		return game;
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
		game.setSiteDetailURL(cursor.getString(9));
		game.setNotify(cursor.getInt(10) == 1);
		game.setDescription(cursor.getString(11));
		game.setApiDetailURL(cursor.getString(12));
		String genres = cursor.getString(13);
		if(genres!=null) {
		split = genres.split(",");
		List<String> genresList = Arrays.asList(split);
		game.setGenres(genresList);
		}
		return game;
	}
}
