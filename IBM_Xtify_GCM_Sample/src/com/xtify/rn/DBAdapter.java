package com.xtify.rn;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {
 
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "rnDb";
	private static final String DATABASE_CREATE = "create table if not exists notifs (_id integer primary key autoincrement, " + "actionData text, actionLabel text, actionType text, content text,"
			+ "date text, subject text, ruleLat real, ruleLon real, mid text unique not null,expirationDate text);";

	private static DatabaseHelper instance;

	private static synchronized DatabaseHelper getHelper(Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context);
		}
		return instance;
	}

	public static SQLiteDatabase getSQLiteDb(Context context) throws SQLException {
		return getHelper(context).getWritableDatabase();
	}

	private DBAdapter() {
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS notifs");
			onCreate(db);
		}
	}

}
