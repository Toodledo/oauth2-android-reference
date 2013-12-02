package com.toodledo.android.oauth2;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.auth.oauth2.Credential;

public class IntroScreen extends Activity {

 
	private Timer timer = new Timer();
	protected int elapsedTime;
	private Button btnOAuthToodledo;
	private Button btnClearToodledo;
	private Button btnApiToodledo;
	private EditText fldAppId;
	private EditText fldSecret;
	private SharedPreferences prefs;
	private String appId, secret; 
	private Credential credential;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		btnOAuthToodledo = (Button) findViewById(R.id.btn_oauth_toodledo);
		btnClearToodledo = (Button) findViewById(R.id.btn_clear_toodledo);
		btnApiToodledo = (Button) findViewById(R.id.btn_api_toodledo);

		fldAppId = (EditText) findViewById(R.id.app_id);
		fldSecret = (EditText) findViewById(R.id.secret);
		fldAppId.setText("AndroidOauth");
		fldSecret.setText("api528fb92e33393");
		 appId = fldAppId.getText().toString();
		 secret = fldSecret.getText().toString();
		btnOAuthToodledo.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				
//				appId = fldAppId.getText().toString();
//				secret = fldSecret.getText().toString();
				if (appId != null && appId.length() != 0 && secret != null
						&& secret.length() != 0) {
					startOauthFlow(Oauth2Params.TOODLEDO_OAUTH2);
				
				} else {
					NotifierHelper.displayToast(getApplicationContext(),
							"Please provide a valid App ID and Secret",
							NotifierHelper.SHORT_TOAST);
				}

			}
		});
 
		btnApiToodledo.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				 
				startMainScreen(Oauth2Params.TOODLEDO_OAUTH2);
				finish();
			}

			});

		 
		 
		btnClearToodledo.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				clearCredentials(Oauth2Params.TOODLEDO_OAUTH2);
			}

		});

	}

	/**
	 * Starts the activity that takes care of the OAuth2 flow
	 * 
	 * @param oauth2Params
	 */
	private void startOauthFlow(Oauth2Params oauth2Params) {
		Constants.OAUTH2PARAMS = oauth2Params;

		startActivity(new Intent().setClass(this,
				OAuthAccessTokenActivity.class));	 
	}

	/**
	 * Starts the main screen where we show the API results.
	 * 
	 * @param oauth2Params
	 */
	private void startMainScreen(Oauth2Params oauth2Params) {
		Constants.OAUTH2PARAMS = oauth2Params;
		
		startActivity(new Intent().setClass(this, MainScreen.class));
	}

	/**
	 * Clears our credentials (token and token secret) from the shared
	 * preferences. We also setup the authorizer (without the token). After
	 * this, no more authorized API calls will be possible.
	 * 
	 * @throws IOException
	 */
	private void clearCredentials(Oauth2Params oauth2Params) {
		try {
			new OAuth2Helper(prefs, oauth2Params).clearCredentials();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		startTimer();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopTimer();
	}

	private void stopTimer() {
		timer.cancel();
	}

	private String getTokenStatusText(Oauth2Params oauth2Params)
			throws IOException {
		credential = new OAuth2Helper(this.prefs, oauth2Params)
				.loadCredential();
		if (credential.getAccessToken() != null) {
			btnApiToodledo.setVisibility(View.VISIBLE);
		}
		String output = null;
		if (credential == null || credential.getAccessToken() == null) {
			output = "No access token found.";
		} else if (credential.getExpirationTimeMilliseconds() != null) {
			output = credential.getExpiresInSeconds() + " seconds remaining"; 
		} else {
			output = credential.getAccessToken() + "[does not expire]";
		}
		return output;
	}

	protected void startTimer() {
		Log.i(Constants.TAG, " +++++ Started timer");
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Log.i(Constants.TAG, " +++++ Refreshing data");
				try {
					Message msg = new Message();
					Bundle bundle = new Bundle();

					bundle.putString("toodledo",
							getTokenStatusText(Oauth2Params.TOODLEDO_OAUTH2));
					msg.setData(bundle);
					mHandler.sendMessage(msg);

				} catch (Exception ex) {
					ex.printStackTrace();
					timer.cancel();
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("toodledo", ex.getMessage());
					msg.setData(bundle);
					mHandler.sendMessage(msg);
				}

			}
		}, 0, 1000);
	}

	private static class WeakRefHandler extends Handler {
		private WeakReference<Activity> ref;

		public WeakRefHandler(Activity ref) {
			this.ref = new WeakReference<Activity>(ref);
		}

		@Override
		public void handleMessage(Message msg) {
			Activity f = ref.get();

			((TextView) f.findViewById(R.id.txt_oauth_toodledo)).setText(msg
					.getData().getString("toodledo"));

		}
	}

	private WeakRefHandler mHandler = new WeakRefHandler(this);

	private void setMessage(String msg) {
		((TextView) findViewById(R.id.txt_oauth_toodledo)).setText(msg);
	}

	@Override
	protected void onStart() {
		super.onStart();

	}
}
