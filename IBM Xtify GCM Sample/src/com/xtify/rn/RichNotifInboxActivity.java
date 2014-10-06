package com.xtify.rn;

import static com.xtify.rn.RNUtility.*;

import com.xtify.rn.HttpHelper.Response;
import com.xtify.sdk.NotificationsUtility;
import com.xtify.sdk.api.XtifySDK;
import com.xtify.sdk.metrics.MetricAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class RichNotifInboxActivity extends ListActivity {
	private static List<RichNotification> rnList = new LinkedList<RichNotification>();
	private Context context;

	public RichNotifInboxActivity() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		XtifySDK.addMetric(context.getApplicationContext(), MetricAction.RN_INBOX_CLICKED, null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		String url = NotificationsUtility.getUserRNUrl(context) + "&includePending=true";
		new GetRNInboxTask(url).execute();
		setContentView(getResourcesId(context, "activity_rn_inbox", "layout"));
	}

	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id) {
		super.onListItemClick(lv, v, position, id);
		int size = rnList.size();
		RichNotification rn = rnList.get(size - position - 1);
		Intent notifIntent = new Intent(context, RichNotifActivity.class);
		notifIntent.setPackage(context.getPackageName());
		notifIntent.putExtra(RNUtility.RETREVE_LOCATION_EXTRA, RNUtility.RETREVE_FROM_DB_EXTRA);
		notifIntent.putExtra(RNUtility.RICH_NOTIFICATION_ID_EXTRA, rn.getMid());
		notifIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		context.startActivity(notifIntent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Menu");
		menu.add(0, v.getId(), 0, "Delete");
	}

	@Override
	protected void onPause() {
		if (pd != null)
			pd.dismiss();
		super.onPause();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getTitle() == "Delete") {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			int position = info.position;
			int size = rnList.size();
			int delPos = size - position - 1;
			RichNotification r = rnList.get(delPos);
			NotificationDBA notificationEntity = new NotificationDBA(context.getApplicationContext());
			notificationEntity.deleteNotif(r.getMid());
			rnList.remove(delPos);
			getListView().invalidateViews();
		} else {
			return false;
		}
		return true;
	}

	ProgressDialog pd;

	private class GetRNInboxTask extends AsyncTask<Void, Void, Boolean> {
		String queryUrl = null;
		ProgressDialog pd;

		public GetRNInboxTask(String queryUrl) {
			super();
			pd = ProgressDialog.show(context, "Loading..", "Fetching Notifications", true, false);
			this.queryUrl = queryUrl;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				Response res = HttpHelper.get(queryUrl);
				if (res.getHttpResponseCode() == 200) {
					ArrayList<RichNotification> rnList = RichNotification.getRnFronJsonString(res.getResponseMessage());

					if (rnList != null) {
						NotificationDBA notificationEntity = new NotificationDBA(context.getApplicationContext());
						Iterator<RichNotification> iterator = rnList.iterator();
						while (iterator.hasNext()) {
							notificationEntity.insertNotif(iterator.next());
						}
					}
				}

			} catch (Exception e) {
			}
			NotificationDBA notificationEntity = new NotificationDBA(context.getApplicationContext());
			ArrayList<RichNotification> richNotifList = notificationEntity.getAllNotifs();
			rnList.clear();
			Iterator<RichNotification> itr = richNotifList.iterator();
			while (itr.hasNext()) {
				RichNotification rn = itr.next();
				rn.setContent(getDescriptionTrim(rn.getContent()));
				rnList.add(rn);
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			registerForContextMenu(getListView());
			NotifListAdapter adapter = new NotifListAdapter(context);
			setListAdapter(adapter);
			pd.dismiss();
		}

	}

	private String getDescriptionTrim(String description) {
		if (description == null) {
			return "";
		}
		description = NotificationsUtility.stripHtmlTags(description);
		if (description.startsWith("\t")) {
			description = description.substring("\t".length());
		}
		if (description.length() > 128) {
			description = description.substring(0, 125) + "...";
		}
		return description;
	}

	private class NotifListAdapter extends BaseAdapter {

		public NotifListAdapter(Context context) {
		}

		public int getCount() {
			return rnList.size();
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout view;
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = (LinearLayout) layoutInflater.inflate(getResourcesId(context, "list_item_rn_inbox", "layout"), null);
			} else {
				view = (LinearLayout) convertView;
			}
			ViewHolder holder = (ViewHolder) view.getTag();
			if (holder == null) {
				holder = new ViewHolder(view);
				view.setTag(holder);
			}
			RichNotification rn = rnList.get(rnList.size() - position - 1);
			holder.titleTV.setText(rn.getSubject());
			holder.contentTV.setText(rn.getContent());
			holder.dateTV.setText(rn.getDate());
			if (rn.isExpired()) {
				view.setBackgroundColor(Color.rgb(182, 182, 182));
			} else {
				view.setBackgroundColor(Color.TRANSPARENT);
			}
			return view;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

	}

	private class ViewHolder {
		TextView titleTV;
		TextView contentTV;
		TextView dateTV;

		ViewHolder(View base) {
			this.titleTV = (TextView) base.findViewById(getResourcesId(context, "rn_title", "id"));
			this.contentTV = (TextView) base.findViewById(getResourcesId(context, "rn_content", "id"));
			this.dateTV = (TextView) base.findViewById(getResourcesId(context, "rn_date", "id"));
		}
	}

}
