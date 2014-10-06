package com.xtify.rn;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class NotificationDBA {

	private static final String DATABASE_TABLE_NOTIFS = "notifs";
	private static final String KEY_ROWID = "_id";
	private Context context;

	public NotificationDBA(Context context) {
		this.context = context;
	}

	
	public void insertNotif(RichNotification rn) {
		SQLiteDatabase db = DBAdapter.getSQLiteDb(context);
		ContentValues initialValues = new ContentValues();
		initialValues.put("actionData", rn.getActionData());
		initialValues.put("actionLabel", rn.getActionLabel());
		initialValues.put("actionType", rn.getActionType());
		initialValues.put("content", rn.getContent());
		initialValues.put("date", rn.getDate());
		initialValues.put("subject", rn.getSubject());
		initialValues.put("ruleLat", rn.getRuleLat());
		initialValues.put("ruleLon", rn.getRuleLon());
		initialValues.put("mid", rn.getMid());
		initialValues.put("expirationDate", rn.getExpirationDate());
		try {
			db.insertOrThrow(DATABASE_TABLE_NOTIFS, null, initialValues);
		} catch (Exception e) {
		}
	}

	public void deleteNotif(String mid) {
		SQLiteDatabase db = DBAdapter.getSQLiteDb(context);
		db.delete(DATABASE_TABLE_NOTIFS, "mid" + "='" + mid + "'", null);
	}

	public void removeAllNotifs() {
		SQLiteDatabase db = DBAdapter.getSQLiteDb(context);
		db.delete(DATABASE_TABLE_NOTIFS, null, null); // If whereClause is null, it will delete all rows.
	}


	public ArrayList<RichNotification> getAllNotifs() {
		SQLiteDatabase db = DBAdapter.getSQLiteDb(context);
		Cursor cur = db.query(DATABASE_TABLE_NOTIFS,
				new String[] { KEY_ROWID, "actionData", "actionLabel", "actionType", "content", "date", "subject", "ruleLat", "ruleLon", "mid", "expirationDate" }, null, null, null, null, null);
		ArrayList<RichNotification> richNotifList = new ArrayList<RichNotification>();
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			final DateFormat dateFormatOut = new SimpleDateFormat("MMMMMMMMM d, yyyy");
			String timestamp = Calendar.getInstance().getTime().toString(); 
			try {
				timestamp = dateFormatOut.format(ISO8601.toDate(cur.getString(5)));
			} catch (ParseException e) {
				Date now = new Date();
				timestamp = dateFormatOut.format(now);
			}
			richNotifList.add(new RichNotification(cur.getString(1), cur.getString(2), cur.getString(3), cur.getString(4), timestamp, cur.getString(6), cur.getDouble(7), cur.getDouble(8), cur
					.getString(9), cur.getString(10)));
			cur.moveToNext();
		}
		cur.close();
		return richNotifList;
	}

	public RichNotification getNotifByMid(String mid) throws SQLException {
		SQLiteDatabase db = DBAdapter.getSQLiteDb(context);
		Cursor cur =
		db.query(true, DATABASE_TABLE_NOTIFS, new String[] { KEY_ROWID, "actionData", "actionLabel", "actionType", "content", "date", "subject", "ruleLat", "ruleLon", "mid", "expirationDate" }, "mid"
				+ "='" + mid + "'", null, null, null, null, null);
		if (cur != null) {
			cur.moveToFirst();
		}
		RichNotification rn = new RichNotification(cur.getString(1), cur.getString(2), cur.getString(3), cur.getString(4), cur.getString(5), cur.getString(6), cur.getDouble(7),
				cur.getDouble(8), cur.getString(9), cur.getString(10));
		cur.close();
		return rn;

	}
}
