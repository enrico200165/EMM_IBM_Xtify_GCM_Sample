package com.xtify.samples.gcm;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.xtify.sdk.api.XtifyLocation;
import com.xtify.sdk.api.XtifySDK;

public class TagSettingActivity extends PreferenceActivity {

	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		addPreferencesFromResource(R.layout.activity_tag_pref);
		setupPrefsListener(getString(R.string.example1_key));
		setupPrefsListener(getString(R.string.example2_key));
		setupPrefsListener(getString(R.string.example3_key));
		setupPrefsListener(getString(R.string.example4_key));
		setupPrefsListener(getString(R.string.example5_key));
		setupPrefsListener(getString(R.string.notif_key));
		setupPrefsListener(getString(R.string.location_key));
	}

	private void setupPrefsListener(final String key) {
		Preference pref = (Preference) findPreference(key);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				boolean isChecked = preference.getSharedPreferences().getBoolean(key, false);
				if (isChecked) {
					addTag(key);
				} else {
					unTag(key);
				}
				return true;
			}
		});


	final String locationKey = getString(R.string.location_key);
	Preference locationPref = (Preference) findPreference(locationKey);
	locationPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			boolean isChecked = preference.getSharedPreferences().getBoolean(locationKey, false);
			if (isChecked) {
				//XtifyLocation.enableRepetitiveLocUpdate(TagSettingActivity.this);
				XtifyLocation.enableRepetitiveLocUpdate(getApplicationContext());
				Log.i(locationKey,"-- Location Enabled");
			} else {
				//XtifyLocation.disableRepetitiveLocUpdate(TagSettingActivity.this);
				XtifyLocation.disableRepetitiveLocUpdate(getApplicationContext());
				Log.i(locationKey,"-- Location Disabled");
			}
			return true;
		}
	});
	
	final String notifKey = getString(R.string.notif_key);
	Preference notifPref = (Preference) findPreference(notifKey);
	notifPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			boolean isChecked = preference.getSharedPreferences().getBoolean(notifKey, false);
			if (isChecked) {
				XtifySDK.enableNotification(TagSettingActivity.this);
				Log.i(notifKey,"-- Notification Enabled");
			} else {
				XtifySDK.disableNotification(TagSettingActivity.this);
				Log.i(notifKey,"-- Notification Disabled");
			}
			return true;
		}
	});
	}


	
	private void addTag(String tag) {
		XtifySDK.addTag(context, tag);
		Log.i(tag,"-- Preference Added");
	}

	private void unTag(String tag) {
		XtifySDK.unTag(context, tag);
		Log.i(tag,"-- Preference Removed");
	}

}