package com.xtify.rn;

import com.xtify.rn.HttpHelper.Response;
import com.xtify.sdk.NotificationsUtility;
import com.xtify.sdk.api.XtifySDK;
import com.xtify.sdk.metrics.MetricAction;
import static com.xtify.rn.RNUtility.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RichNotifActivity extends Activity {
	private static final String TAG = "RichNotifActivity" ;
	private ProgressDialog pd;
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		context = this;
		setContentView(getResourcesId(context, "activity_rn_notif", "layout"));
		try {
			Intent intent = getIntent();
			XtifySDK.addMetric(context.getApplicationContext(), MetricAction.NOTIF_DISP, intent.getStringExtra(RNUtility.RICH_NOTIFICATION_ID_EXTRA));
			getRNView(this, intent);
		} catch (Exception e) {
		}
	}

	public void getRNView(Context context, Intent intent) throws MalformedURLException, IOException, JSONException {
		String retreveLocation = intent.getStringExtra(RNUtility.RETREVE_LOCATION_EXTRA);
		if (retreveLocation.equals(RNUtility.RETREVE_FROM_DB_EXTRA)) {
			NotificationDBA notificationEntity = new NotificationDBA(context);
			String rnMid = intent.getStringExtra(RNUtility.RICH_NOTIFICATION_ID_EXTRA);
			RichNotification rn = notificationEntity.getNotifByMid(rnMid);
			if (rn.isExpired()) {
				setContentView(getExpirationWebView(context));
			} else {
				setView(rn);
			}
		} else if (retreveLocation.equals(RNUtility.RETREVE_FROM_SERVER_EXTRA)) {
			String mid = intent.getStringExtra(RNUtility.RICH_NOTIFICATION_ID_EXTRA);
			;
			XtifySDK.addMetric(context, MetricAction.SN_CLICKED, mid);
			String url = NotificationsUtility.getUserRNUrl(context) + "&mid=" + mid;
			new GetNotifTask(url).execute();
		}
	}

	private static final String DEFAULT_EXPIRATION_MESSAGE = "<html> <body> <center> Message  Expired.</center>  </body> </html>";

	WebView getExpirationWebView(Context context) {
		WebView webView = new WebView(context.getApplicationContext());
		String webViewPath = RichNotificationManger.getExpirationMessagePath(context.getApplicationContext());
		if (webViewPath != null && !(webViewPath.length() == 0)) {
			webView.loadUrl(webViewPath);
		} else {
			webView.loadData(DEFAULT_EXPIRATION_MESSAGE, "text/html", "UTF-8");
		}
		return webView;
	}

	@Override
	protected void onPause() {
		if (pd != null)
			pd.dismiss();
		super.onPause();
	}

	private class GetNotifTask extends AsyncTask<Void, Void, RichNotification> {
		String rnQuery = null;
		RichNotification rn;

		public GetNotifTask(String s) {
			super();
			pd = ProgressDialog.show(context, "Loading..", "Getting Notification", true, false);
			this.rnQuery = s;
		}

		@Override
		protected RichNotification doInBackground(Void... params) {
			Response resp;
			try {
				Log.i(TAG, "Rn Query is : " + rnQuery);
				resp = HttpHelper.get(rnQuery);
				if (resp.getHttpResponseCode() == 200 || resp.getHttpResponseCode() == 204) {
					ArrayList<RichNotification> rnList = RichNotification.getRnFronJsonString(resp.getResponseMessage());
					if (rnList.size() > 0) {
						rn = rnList.get(0);
					}
				}
				return rn;
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(RichNotification resultRN) {
			pd.dismiss();
			if (resultRN != null) {
				insertRN(resultRN);
				rn = resultRN;
			} else {
				if (rn == null) {
					rn = new RichNotification(null, null, null, "Content unavailable", "", "", null, null, null, null);
				}
			}
			try {
				if (rn.isExpired()) {
					setContentView(getExpirationWebView(context));
				} else {
					setView(rn);
				}
			} catch (Exception e) {
			}
		}

	}

	private void insertRN(RichNotification rn) {
		NotificationDBA notificationEntity = new NotificationDBA(context);
		notificationEntity.insertNotif(rn);
	}

	ImageView share;
	ImageView map;
	RelativeLayout rl;

	@SuppressLint("SetJavaScriptEnabled")
	public void setView(final RichNotification rn) {
		WebView notifContentView = (WebView) findViewById(getResourcesId(context, "notif_content_webview", "id"));
		String subject = rn.getSubject() == null ? "" : rn.getSubject();
		String content = rn.getContent() == null ? "" : rn.getContent();
		notifContentView.loadDataWithBaseURL(null, "<div style=\"font-size: 17pt;\"><p>" + subject + "</p></div><div style=\"font-size: 10pt;\">" + content + "</div>",
				"text/html", "UTF-8", "");
		notifContentView.getSettings().setJavaScriptEnabled(true);
		notifContentView.getSettings().setPluginsEnabled(true);
		share = (ImageView) findViewById(getResourcesId(context, "share_button", "id"));
		share.setVisibility(View.VISIBLE);
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				share.startAnimation(AnimationUtils.loadAnimation(context, getResourcesId(context, "fade", "anim")));
				XtifySDK.addMetric(context.getApplicationContext(), MetricAction.NOTIF_RICH_SHARED, rn.getMid());
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, NotificationsUtility.stripHtmlTags(rn.getSubject()).toString());
				intent.putExtra(Intent.EXTRA_TEXT, NotificationsUtility.stripHtmlTags((rn.getContent()).toString()));
				startActivity(Intent.createChooser(intent, "Share notification via"));
			}
		});

		if (rn.getRuleLat() != null && rn.getRuleLon() != null) {
			map = (ImageView) findViewById(getResourcesId(context, "map_button", "id"));
			map.setVisibility(View.VISIBLE);
			map.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					map.startAnimation(AnimationUtils.loadAnimation(context, getResourcesId(context, "fade", "anim")));
					XtifySDK.addMetric(context.getApplicationContext(), MetricAction.NOTIF_RICH_MAP, rn.getMid());
					String uri = "geo:" + String.valueOf(rn.getRuleLat()) + "," + String.valueOf(rn.getRuleLon());
					startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
				}
			});
		}

		if (rn.getHasAction()) {
			rl = (RelativeLayout) findViewById(getResourcesId(context, "notif_action_root", "id"));
			rl.setVisibility(View.VISIBLE);
			TextView tv = (TextView) findViewById(getResourcesId(context, "notif_action_description", "id"));
			tv.setText(rn.getActionLabel());
			rl.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					rl.startAnimation(AnimationUtils.loadAnimation(context, getResourcesId(context, "fade", "anim")));
					XtifySDK.addMetric(context.getApplicationContext(), MetricAction.NOTIF_RICH_ACTION, rn.getMid());
					if (rn.getActionType().equals("PHN")) {
						callPhone(rn.getActionData());
					} else if (rn.getActionType().equals("WEB")) {
						openWebPage(rn.getActionData());
					} else if (rn.getActionType().equals("CST")) {
						sendCustomIntent(rn.getActionData(), RichNotifActivity.this);
					} else {
						return;
					}
				}
			});
		}
	}

	private void callPhone(String phoneNumber) {
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:" + phoneNumber));
			startActivity(callIntent);
		} catch (ActivityNotFoundException e) {
		}
	}

	private void openWebPage(String webUrl) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(webUrl));
		startActivity(i);
	}

	private void sendCustomIntent(String intentAction, Context context) {
		Intent intent = new Intent(intentAction);
		context.sendBroadcast(intent);
	}

}