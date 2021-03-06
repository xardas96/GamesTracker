package romanovsky.gamerdplus.database.dao;

import java.util.ArrayList;
import java.util.List;

import romanovsky.gamerdplus.database.SQLiteHelper;
import romanovsky.gamerdplus.giantbomb.api.core.Platform;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PlatformDAO extends AbstractDAO{
	private String[] allColumns = { 
			SQLiteHelper.COLUMN_ID
			, SQLiteHelper.COLUMN_NAME
			, SQLiteHelper.COLUMN_ABBREVIATION
			, SQLiteHelper.COLUMN_FILTERED
			};
	private String[] popularPlatforms = {
			"Linux"
			, "Mac"
			, "Nintendo 3DS"
			, "PC"
			, "PlayStation 3"
			, "PlayStation 4"
			, "PlayStation Vita"
			, "Wii U"
			, "Xbox 360"
			, "Xbox One"
			};

	public PlatformDAO(Context context) {
		super(context);
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

	public List<Platform> getPopularAndFilteredPlatforms() {
		open();
		List<Platform> platforms = new ArrayList<Platform>();
		StringBuilder popularPlatformsBuiler = new StringBuilder();
		popularPlatformsBuiler.append("(");
		for (String popularPlatform : popularPlatforms) {
			popularPlatformsBuiler.append("'").append(popularPlatform).append("'").append(",");
		}
		popularPlatformsBuiler.setLength(popularPlatformsBuiler.length() - 1);
		popularPlatformsBuiler.append(")");
		String whereClause = SQLiteHelper.COLUMN_NAME + " IN " + popularPlatformsBuiler.toString() + " OR " + SQLiteHelper.COLUMN_FILTERED + " = 1";
		Cursor cursor = database.query(SQLiteHelper.TABLE_PLATFORMS, allColumns, whereClause, null, null, null, null);
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
