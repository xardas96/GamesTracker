package romanovsky.gamerd.database.dao;

import romanovsky.gamerd.database.SQLiteHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class AbstractDAO {
	protected SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	
	public AbstractDAO(Context context) {
		dbHelper = new SQLiteHelper(context);
	}
	
	public void open() {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
}
