package romanovsky.gamerd.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {
	public static final String TABLE_GAMES = "games";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DATE_LAST_UPDATED = "dateLastUpdated";
	public static final String COLUMN_EXPECTED_RELEASE_DAY = "expectedReleaseDay";
	public static final String COLUMN_EXPECTED_RELEASE_MONTH = "expectedReleaseMonth";
	public static final String COLUMN_EXPECTED_RELEASE_YEAR = "expectedReleaseYear";
	public static final String COLUMN_PLATFORMS = "platforms";
	public static final String COLUMN_EXPECTED_RELEASE_QUARTER = "expectedReleaseQuarter";
	public static final String COLUMN_ICON_URL = "iconUrl";
	public static final String COLUMN_SITE_DETAIL_URL = "siteDetailURL";
	public static final String COLUMN_NOTIFY = "notify";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_API_DETAIL = "apiDetailUrl";

	private static final String DB_NAME = "games.db";
	private static final int DB_VERSION = 2;
	
	private static final String DB_CREATE = "CREATE TABLE "
			+ TABLE_GAMES + "(" 
			+ COLUMN_ID	+ " integer primary key, "
			+ COLUMN_NAME + " text not null, "
			+ COLUMN_DATE_LAST_UPDATED + " integer, "
			+ COLUMN_EXPECTED_RELEASE_DAY + " integer, "
			+ COLUMN_EXPECTED_RELEASE_MONTH + " integer, "
			+ COLUMN_EXPECTED_RELEASE_YEAR + " integer, "
			+ COLUMN_EXPECTED_RELEASE_QUARTER + " integer, "
			+ COLUMN_PLATFORMS + " text, "
			+ COLUMN_ICON_URL + " text, "
			+ COLUMN_SITE_DETAIL_URL + " text, "
			+ COLUMN_NOTIFY + " integer, "
			+ COLUMN_DESCRIPTION + " text,"
			+ COLUMN_API_DETAIL + " text"
			+ ");";
	private static final String DB_UPDATE_API_DETAIL = "ALTER TABLE " 
		+ TABLE_GAMES
		+ " ADD COLUMN " 
		+ COLUMN_API_DETAIL + " text;";	
	
	public SQLiteHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
			Log.e("UPDATE DB", "UPDATE DB");
			addApiDetailUrl(db);
			Log.e("UPDATED DB", "UPDATED DB");
		}
	}

	private void addApiDetailUrl(SQLiteDatabase db) {
		db.execSQL(DB_UPDATE_API_DETAIL);
		forceUpdate(db);
	}

	private void forceUpdate(SQLiteDatabase db) {
		db.execSQL("UPDATE " + TABLE_GAMES + " SET " + COLUMN_DATE_LAST_UPDATED + " = 0");
	}
}