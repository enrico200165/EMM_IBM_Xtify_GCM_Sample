package com.xtify.rn;

import android.content.Context;

public final class RNUtility {
	private RNUtility() {
	}

	static int getResourcesId(Context context, String name, String defType) {
		int id = context.getResources().getIdentifier(name, defType, context.getPackageName());
		if (id == 0) {
			throw new RuntimeException("No resource found that matches the given name \"" + name + "\". Make sure you added XtifyRichNotification res folder to your project.");
		}
		return id;
	}

	static final String RETREVE_FROM_DB_EXTRA = "com.xtify.sdk.rn.RETREVE_FROM_DB";
	static final String RETREVE_FROM_SERVER_EXTRA = "com.xtify.sdk.rn.RETREVE_FROM_SERVER";
	static final String RICH_NOTIFICATION_ID_EXTRA = "com.xtify.sdk.rn.RICH_NOTIFICATION_ID";
	static final String RETREVE_LOCATION_EXTRA = "com.xtify.sdk.rn.RETRIEVE_LOCATION";

}
