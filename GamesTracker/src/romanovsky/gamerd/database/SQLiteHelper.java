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
	public static final String COLUMN_GENRES = "genres";
	
	public static final String TABLE_PLATFORMS = "platforms";
	public static final String COLUMN_ABBREVIATION = "abbreviation";
	public static final String COLUMN_FILTERED = "filtered";
	
	public static final String TABLE_GENRES = "genres";

	private static final String DB_NAME = "games.db";
	private static final int DB_VERSION = 6;
	
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
			+ COLUMN_API_DETAIL + " text,"
			+ COLUMN_GENRES + " text"
			+ ");";
	
	private static final String DB_UPDATE_API_DETAIL = "ALTER TABLE " 
		+ TABLE_GAMES
		+ " ADD COLUMN " 
		+ COLUMN_API_DETAIL + " text;";	
	
	private static final String DB_CREATE_PLATFORMS = "CREATE TABLE "
			+ TABLE_PLATFORMS + "(" 
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_NAME + " text not null, "
			+ COLUMN_ABBREVIATION + " text, "
			+ COLUMN_FILTERED + " integer"
			+ ");";
	
	private static final String DB_UPDATE_GENRES = "ALTER TABLE "
			+ TABLE_GAMES
			+ " ADD COLUMN " 
			+ COLUMN_GENRES + " text;";
	
	private static final String DB_CREATE_GENRES = "CREATE TABLE "
			+ TABLE_GENRES + "(" 
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_NAME + " text not null, "
			+ COLUMN_ABBREVIATION + " text, "
			+ COLUMN_FILTERED + " integer"
			+ ");";
			
	public SQLiteHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE);
		createPlatforms(db);
		createGenres(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
			addApiDetailUrl(db);
		}
		if (oldVersion < 3) {
			createPlatforms(db);
		}
		if (oldVersion < 4) {
			addGenres(db);
		}
		if(oldVersion < 5) {
			createGenres(db);
		}
		if (oldVersion < 6) {
			Log.e("UPDATE DB", "UPDATE DB");
			try {
				addGenres(db);
			} catch (Exception e) {
				Log.e("COLUMN GENRES ALREADY ADDED", e.getMessage());
			}
			Log.e("UPDATED DB", "UPDATED DB");
		}
	} 
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	
	private void createGenres(SQLiteDatabase db) {
		db.execSQL(DB_CREATE_GENRES);
		forceUpdate(db);
	}

	private void addGenres(SQLiteDatabase db) {
		db.execSQL(DB_UPDATE_GENRES);
		forceUpdate(db);
	}

	private void createPlatforms(SQLiteDatabase db) {
		db.execSQL(DB_CREATE_PLATFORMS);
	}

	private void addApiDetailUrl(SQLiteDatabase db) {
		db.execSQL(DB_UPDATE_API_DETAIL);
		forceUpdate(db);
	}

	private void forceUpdate(SQLiteDatabase db) {
		db.execSQL("UPDATE " + TABLE_GAMES + " SET " + COLUMN_DATE_LAST_UPDATED + " = 0");
	}
}
