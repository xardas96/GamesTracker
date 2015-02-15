package romanovsky.gamerdplus.database.dao;

import java.util.ArrayList;
import java.util.List;

import romanovsky.gamerdplus.database.SQLiteHelper;
import romanovsky.gamerdplus.giantbomb.api.core.Genre;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class GenreDAO extends AbstractDAO {
	private String[] allColumns = { 
			SQLiteHelper.COLUMN_ID
			, SQLiteHelper.COLUMN_NAME
			, SQLiteHelper.COLUMN_FILTERED
			};

	public GenreDAO(Context context) {
		super(context);
	}
	
	public void addGenre(Genre genre) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_NAME, genre.getName());
		values.put(SQLiteHelper.COLUMN_FILTERED, 0);
		database.insert(SQLiteHelper.TABLE_GENRES, null, values);
	}
	
	public void updateGenre(Genre genre) {
		long id = genre.getId();
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_NAME, genre.getName());
		values.put(SQLiteHelper.COLUMN_FILTERED, genre.isFiltered() ? 1 : 0);
		database.update(SQLiteHelper.TABLE_GENRES, values, SQLiteHelper.COLUMN_ID + " = " + id, null);
	}
	
	public List<Genre> getAllGenres() {
		List<Genre> genres = new ArrayList<Genre>();
		Cursor cursor = database.query(SQLiteHelper.TABLE_GENRES, allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Genre genre = parseGenre(cursor);
			genres.add(genre);
			cursor.moveToNext();
		}
		return genres;
	}

	private Genre parseGenre(Cursor cursor) {
		Genre genre = new Genre();
		genre.setId(cursor.getInt(0));
		genre.setName(cursor.getString(1));
		genre.setFiltered(cursor.getInt(2) == 1);
		return genre;
	}
	
}
