package xardas.gamestracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
	public static final String COLUMN_NOTIFY = "notify";

	private static final String DB_NAME = "games.db";
	private static final int DB_VERSION = 1;
	
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
			+ COLUMN_NOTIFY + " integer"
			+ ");";
	
	public SQLiteHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);
		onCreate(db);
	}
}
