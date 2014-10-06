package com.xtify.samples.gcm;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.xtify.rn.RichNotificationManger;
import com.xtify.sdk.api.XtifyBroadcastReceiver;
import com.xtify.sdk.api.XtifySDK;

public class XtifyNotifier extends XtifyBroadcastReceiver {
	String TAG = XtifyNotifier.class.getName();
	private static final String NOTIFICATION_TITLE = "com.xtify.sdk.NOTIFICATION_TITLE";
	private static final String NOTIFICATION_CONTENT = "com.xtify.sdk.NOTIFICATION_CONTENT";

	@Override
	public void onMessage(Context context, Bundle msgExtras) {
		Log.i(TAG, "-- Notification recived");
		Log.i(TAG, "Notification Title: " + msgExtras.getString(NOTIFICATION_TITLE));
		Log.i(TAG, "Notification Content: " + msgExtras.getString(NOTIFICATION_CONTENT));
		RichNotificationManger.processNotifExtras(context, msgExtras);
	}

	@Override
	public void onRegistered(Context context) {
		Log.i(TAG, "-- SDK registerd");
		Log.i(TAG, "XID is: " + XtifySDK.getXidKey(context));
		Log.i(TAG, "GCM registrationId is: " + XtifySDK.getRegistrationId(context));
	}

	@Override
	public void onC2dmError(Context context, String errorId) {
		Log.i(TAG, "ErrorId: " + errorId);
	}

}
