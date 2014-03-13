package romanovsky.gamerd.database.dao;

import java.util.ArrayList;
import java.util.List;

import romanovsky.gamerd.database.SQLiteHelper;
import romanovsky.gamerd.giantbomb.api.core.Platform;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PlatformDAO {
	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;

	private String[] allColumns = { 
			SQLiteHelper.COLUMN_ID
			, SQLiteHelper.COLUMN_NAME
			, SQLiteHelper.COLUMN_ABBREVIATION
			, SQLiteHelper.COLUMN_FILTERED
			};

	public PlatformDAO(Context context) {
		dbHelper = new SQLiteHelper(context);
	}

	private void open() {
		database = dbHelper.getWritableDatabase();
	}

	private void close() {
		dbHelper.close();
	}

	public void addPlatform(Platform platform) {
		open();
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_NAME, platform.getName());
		values.put(SQLiteHelper.COLUMN_ABBREVIATION, platform.getAbbreviation());
		values.put(SQLiteHelper.COLUMN_FILTERED, 0);
		database.insert(SQLiteHelper.TABLE_PLATFORMS, null, values);
		close();
	}

	public void updatePlatform(Platform platform) {
		open();
		long id = platform.getId();
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_NAME, platform.getName());
		values.put(SQLiteHelper.COLUMN_ABBREVIATION, platform.getAbbreviation());
		values.put(SQLiteHelper.COLUMN_FILTERED, platform.isFiltered() ? 1 : 0);
		database.update(SQLiteHelper.TABLE_PLATFORMS, values, SQLiteHelper.COLUMN_ID + " = " + id, null);
		close();
	}

	public List<Platform> getAllPlatforms() {
		open();
		List<Platform> platforms = new ArrayList<Platform>();
		Cursor cursor = database.query(SQLiteHelper.TABLE_PLATFORMS, allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Platform platform = parsePlatform(cursor);
			platforms.add(platform);
			cursor.moveToNext();
		}
		close();
		return platforms;
	}

	private Platform parsePlatform(Cursor cursor) {
		Platform platform = new Platform();
		platform.setId(cursor.getInt(0));
		platform.setName(cursor.getString(1));
		platform.setAbbreviation(cursor.getString(2));
		platform.setFiltered(cursor.getInt(3) == 1);
		return platform;
	}
}
