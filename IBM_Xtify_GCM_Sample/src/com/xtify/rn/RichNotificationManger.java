package com.xtify.rn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;

import com.xtify.sdk.NotificationsUtility;

public class RichNotificationManger {
	private static final String NOTIF_ACTION_DATA = "com.xtify.sdk.NOTIF_ACTION_DATA";
	private static final String NOTIF_ACTION_TYPE = "com.xtify.sdk.NOTIF_ACTION_TYPE";
	private static final String PREFS_NAME = "XTIFY_RN_DATA";
	private static final String EXPIRATION_MESSAGE_PATH = "EXPIRATION_MESSAGE_PATH";
	private static final String NAME = "com.xtify.sdk.rn.RN_WL";
	private static volatile PowerManager.WakeLock lockStatic = null;

	private synchronized static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic == null) {
			PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, NAME);
			lockStatic.setReferenceCounted(true);
		}
		return (lockStatic);
	}

	/**
	 * Returns the current expiration message path or empty string if there's
	 * nothing set.
	 * 
	 * @param context
	 * @return expirationMessagePath the current expiration file path.
	 */
	public static String getExpirationMessagePath(Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(EXPIRATION_MESSAGE_PATH, "");
	}

	/**
	 * 
	 * Sets the expiration message path to a local webpage or a url. To load a
	 * local webpage, you have to add it to your assets folder and pass a path
	 * that start with "file:///android_asset/" e.g.
	 * ("file:///android_asset//myFile.html"). To load a file on the web you
	 * need to pass the full url starts with the protocol name e.g.
	 * ("http://www.my-site.com/myFile.html").
	 * 
	 * @param context
	 * @param path
	 */
	public static void setExpirationMessagePath(Context context, String path) {
		SharedPreferences installationPreference = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = installationPreference.edit();
		editor.putString(EXPIRATION_MESSAGE_PATH, path);
		editor.commit();
	}

	/**
	 * Process the notification extras and check if it includes rich
	 * notification, if it does it will show a notification in the notification
	 * bar and when the user click on it the notification will be retrieve it
	 * from Xtify server.
	 * 
	 * @param context
	 * @param msgExtras
	 */
	public static void processNotifExtras(Context context, Bundle msgExtras) {
		getLock(context.getApplicationContext()).acquire();
		try {
			String actionType = msgExtras.getString(NOTIF_ACTION_TYPE);
			if (actionType != null && actionType.equals("com.xtify.sdk.RICH_NOTIF")) {
				String rnId = msgExtras.getString(NOTIF_ACTION_DATA);
				if (rnId != null) {
					msgExtras.putString(RNUtility.RETREVE_LOCATION_EXTRA, RNUtility.RETREVE_FROM_SERVER_EXTRA);
					msgExtras.putString(RNUtility.RICH_NOTIFICATION_ID_EXTRA, rnId);
					int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
					NotificationsUtility.showNotification(context, msgExtras, flags , RichNotifActivity.class);
					}
			}
		} finally {
			getLock(context.getApplicationContext()).release();
		}
	}
}
