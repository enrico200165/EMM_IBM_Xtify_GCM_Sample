package com.xtify.samples.gcm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.xtify.rn.HttpHelper;
import com.xtify.rn.HttpHelper.Response;
import com.xtify.rn.RichNotifInboxActivity;
import com.xtify.sdk.api.NotificationsPreference;
import com.xtify.sdk.api.XtifyLocation;
import com.xtify.sdk.api.XtifyLocation.LocationUpdateListener;
import com.xtify.sdk.api.XtifySDK;

public class MainActivity extends Activity { 

	final String PROJECT_NUM = "1097835681706"; // This is the Google Project Number

	
	//Update Application Key, API Key, Google Sender Id and Welcome Text and Title here.  
	//Update preferences and other strings in the strings.xml file.  
	//Update example push notification copy and HTML and bottom of MainActivity page.
	//Update drawables / logos.
	// private static final String XTIFY_APP_KEY = "8b0da8bb-3183-4a11-ab27-e1107a755599";
	private static final String XTIFY_APP_KEY = "0b7427f9-e2d9-4514-9402-63a0b6d84dcd";	
	// private static final String XTIFY_API_KEY = "0ca544bf-fd54-49a3-a951-83b890a3d661";
	private static final String XTIFY_API_KEY = "f1becb16-6531-4904-8cbe-1cc44c8c7952";
	//private static final String SENDER_ID = "602227018018";
	  private static final String SENDER_ID = "1097835681706";
	
	private static final String welcome_text_title = "Welcome to Bed Bath & Beyond";
	private static final String welcome_text_body = "Thank you for downloading the Xtify demonstration application for Bed Bath & Beyond.\n\n"+
	        "This application will demonstrate simple and rich push notifications as well as illustrate integration points with Bed Bath & Beyond\'s own CRM.\n\n"+
			"Set your message preferences and choose to receive news and promotions as well as special location alerts.";
	private XtifyLocation xtifyLocation;

	@Override
	protected void onStart() {
		super.onStart(); 
		// ev seder ID probabilmente stessa cosa di project number
		XtifySDK.start(getApplicationContext(), XTIFY_APP_KEY, SENDER_ID);			
	} 
 
	public static String getDeviceId(Context context) {
		return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		//TextView verTextView = (TextView) findViewById(R.id.version_tv);
		//verTextView.setText("SDK version: " + XtifySDK.getSdkVerNumber());
		xtifyLocation = new XtifyLocation(getApplicationContext());
		/*
		 * Customizing the default SDK notification
		 */
		NotificationsPreference.setSoundEnabled(getApplicationContext(), true);
		NotificationsPreference.setSound(getApplicationContext(), R.raw.notification_sound);
		NotificationsPreference.setVibrateEnabled(getApplicationContext(), true);
		long[] vibrate = { 0, 100, 200, 300 };
		NotificationsPreference.setVibrationPattern(getApplicationContext(), vibrate);
		NotificationsPreference.setIcon(getApplicationContext(), R.drawable.ic_notif_xtify);
		NotificationsPreference.setLightsEnabled(getApplicationContext(), true);
		int ledARGB = 0x00a2ff;
		int ledOnMS = 300;
		int ledOffMS = 1000;
		NotificationsPreference.setLights(getApplicationContext(), new int[] { ledARGB, ledOnMS, ledOffMS }); 
	}

	public void displayRNInbox(View view) {
		Intent intent = new Intent(this, RichNotifInboxActivity.class);
		this.startActivity(intent);
	}

	public void updateLocationNow(View view) {
		Toast.makeText(this, "Updating location...", Toast.LENGTH_SHORT).show();
		final Handler handler = new Handler();
		xtifyLocation.updateLocation((5 * 60 * 1000), new LocationUpdateListener() {
			@Override
			public void onUpdateComplete(boolean result, Location location) {
				if (result) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, "Location sent successfully", Toast.LENGTH_SHORT).show();
						}
					});

				} else {
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, "Error while updating location (Connection unavailable or timeout) ", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		});
	}

	
	//Update the text below with an appropriate welcome message
	public void showWelcome(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(welcome_text_title);
		builder.setMessage(welcome_text_body);
		builder.setPositiveButton("Continue", null);
		AlertDialog alert = builder.create();
		alert.show();

	}
	
	public void enableRepetitiveLocUpdate(View view) {
		Toast.makeText(MainActivity.this, "Updating location every 15 min ...", Toast.LENGTH_SHORT).show();
		// Location is disabled by default. Call the following static method to
		// enable repetitive location update.
		XtifyLocation.enableRepetitiveLocUpdate(getApplicationContext());
	}

	public void disableRepetitiveLocUpdate(View view) {
		Toast.makeText(MainActivity.this, "Stopping location update...", Toast.LENGTH_SHORT).show();
		XtifyLocation.disableRepetitiveLocUpdate(getApplicationContext());
	}

	public void disableNotification(View view) {
		Toast.makeText(MainActivity.this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
		XtifySDK.disableNotification(getApplicationContext());
	}

	public void enableNotification(View view) {
		Toast.makeText(MainActivity.this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
		XtifySDK.enableNotification(getApplicationContext());
	}

	public void displayTagScreen(View view) {
		Intent intent = new Intent(getApplicationContext(), TagSettingActivity.class);
		startActivity(intent);
	}

	public void showAppKey(View view) {
		showDialog(XtifySDK.getAppKey(getApplicationContext()));
	}

	public void showXid(View view) {
		String xid = XtifySDK.getXidKey(getApplicationContext());
		String dialogMsg;
		/*
		 * The XID is not available until the device is registered. If you want
		 * to obtain the XID and send it to your server you should do it in
		 * XtifyNotifier onRegistered method.
		 */
		if (xid != null) {
			dialogMsg = xid;
		} else {
			dialogMsg = "Device is not yet registered with Xtify";
		}
		showDialog(dialogMsg);
	}

	public void showDialog(String msg) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_dialog, (ViewGroup) findViewById(R.id.ll));
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setMessage(msg).setCancelable(false)
				.setPositiveButton("Sign-In", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		
		alert.show();
	}

	
	//The names of these methods can be changed if necessary.
	public void postExample1(View view) {
		Toast.makeText(this, "Creating Wedding Registry...", Toast.LENGTH_SHORT).show();
		sendJson2("Congratulations!", "Get started with your bridal registry at Bed Bath & Beyond!", "Let's Get Started!", "View the Book", "<center><img src=\"http://i.imgur.com/1Xidjsa.png\" width=\"100%\" /></center>");
	}
	
	public void postExample2(View view) {
		Toast.makeText(this, "Upcoming Wedding...", Toast.LENGTH_SHORT).show();
		sendJson2("Finish your registry", "It's almost time to say 'I Do!'", "Add to registry", "Cheers to your big day!", "<center><img src=\"http://i.imgur.com/zPxxY0u.png\" width=\"100%\" /></center>");
	}

	public void postExample3(View view) {
		Toast.makeText(this, "Card Balance...", Toast.LENGTH_SHORT).show();
		sendJson2("Your Card Balance", "Spend your balance before it expires!", "Check", "Your Card Balance", "Your card balance is: <b>$50.00</b><br /><br /><a href=\"http://www.bedbathbeyond.com\">Start Shopping!</a>");
	}
	
	public void postExample4(View view) {
		Toast.makeText(this, "Coupon Request...", Toast.LENGTH_SHORT).show();
		sendJson2("New Spring Arrivals!", "Update your bedroom with our new collection!", "Shop the Collection", "Lighten Up This Spring", "<center><img src=\"http://i.imgur.com/53BxanH.png\" width=\"100%\" /></center>");
	}

	
   public void sendJson2(final String subject, final String message, final String buttonLabel, final String richSubject, final String richBody) {
	   Thread thread = new Thread() {
	   
	       final String richBodyEsc = richBody.replace("\"", "\\\"");

		   @Override
		   public void run() {
			   String TAG = MainActivity.class.getName();

			   try {
				   final String apiKey = XTIFY_API_KEY;
			       final String xid = XtifySDK.getXidKey(getApplicationContext());
			       final String url = "http://api.xtify.com/2.0/push";
				   
			       
				   String pushContent = "{" +
				   		"\"apiKey\":\""+apiKey+"\"," +
		   				"\"appKey\":\""+XTIFY_APP_KEY+"\"," +
   						"\"xids\": [\""+xid+"\"],"+
		   				"\"content\": { " +
   							"\"subject\": \""+subject+"\"," +
   							"\"message\": \""+message+"\"," +
   							"\"action\": { " +
   								"\"type\": \"RICH\"," +
   								"\"label\":\""+buttonLabel+"\"" +
   							"}," +
							"\"rich\": { " +
   								"\"subject\":\""+richSubject+"\"," +
   								"\"message\":\""+richBodyEsc+"\"" +
   							"}" +
						"}" +
					"}";

				   Log.i(TAG, "JsonPost Request: " + pushContent.toString());
				   
				   Response response = HttpHelper.post(url, pushContent, HttpHelper.CONTENT_TYPE_JSON);
			         
			           
			       /*Checking response */
			       if(response!=null){
			    	   Log.i(TAG, "JsonPost Response: " + response.toString());
			       }
			
		       } catch(Exception e) {
		    	   e.printStackTrace();
		    	   Log.i(TAG,"JsonPost Exception: " + e.toString());
			   }
		   }

	   };
	   thread.start(); 
   }
	
}







