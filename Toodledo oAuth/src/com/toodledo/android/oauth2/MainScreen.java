package com.toodledo.android.oauth2;

import java.io.IOException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.api.client.auth.oauth2.Credential;

public class MainScreen extends Activity {

	private SharedPreferences prefs;
	private OAuth2Helper oAuth2Helper;
	private Credential credential;
	private Parameters oauthParams;

	private EditText fldAppId;
	private EditText fldSecret;
	private EditText fldAuth;
	private EditText fldAccess;
	private EditText fldRefresh;
	private EditText fldEmail;

	private Button authorizeBtn;
	private Button refreshBtn;
	private Button getXsCdeBtn;
	private Button getUserInfoBtn;
	private Button clrCredBtn;
	private String appId, secret;
	private static String accessToken, refreshToken;
	public static String authCode;
	public static String email;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		oauthParams = ApplicationController.getInstance().getParameters();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		oAuth2Helper = new OAuth2Helper(this.prefs, oauthParams);

		fldAppId = (EditText) findViewById(R.id.app_id);
		fldSecret = (EditText) findViewById(R.id.app_secret);

		fldAuth = (EditText) findViewById(R.id.auth_code);

		fldAccess = (EditText) findViewById(R.id.access);
		fldRefresh = (EditText) findViewById(R.id.refresh);

		fldEmail = (EditText) findViewById(R.id.user_info);

		authorizeBtn = (Button) findViewById(R.id.authorize);
		getXsCdeBtn = (Button) findViewById(R.id.get_access_code);
		refreshBtn = (Button) findViewById(R.id.refresh_btn);
		getUserInfoBtn = (Button) findViewById(R.id.get_user_email);
		clrCredBtn = (Button) findViewById(R.id.clr_cred);

		disableButtons();

		try {
			credential = oAuth2Helper.loadCredential();
			loadLoginInfo();
		} catch (IOException e) {
			e.printStackTrace();
		}

		authorizeBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				appId = fldAppId.getText().toString();
				secret = fldSecret.getText().toString();
				if (appId != null && appId.length() != 0 && secret != null
						&& secret.length() != 0) {
					oauthParams.setClientId(appId);
					oauthParams.setClientSecret(secret);
					storeLoginInfo(appId, secret);
					startOauthFlow();

				} else {
					NotificationHelper.displayToast(getApplicationContext(),
							"Please provide a valid App ID and Secret",
							NotificationHelper.SHORT_TOAST);
				}

			}
		});

		getXsCdeBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				getAccessToken();
			}

		});

		refreshBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				refresh();
			}

		});

		getUserInfoBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Performs an authorized API call.
				performApiCall();

			}

		});

		clrCredBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				clearCredentials();
			}

		});

		fldAppId.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			public void afterTextChanged(Editable s) {
				appId = s.toString();
				storeLoginInfo(appId, secret);
			}
		});

		fldSecret.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			public void afterTextChanged(Editable s) {
				secret = s.toString();
				storeLoginInfo(appId, secret);
			}
		});

		fldAuth.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				authCode = s.toString();
				storeAuthCode(authCode);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}
		});

		fldAccess.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {

				credential.setAccessToken(s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		fldRefresh.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				credential.setRefreshToken(s.toString());

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}
		});

		loadLoginInfo();
		fldAppId.setText(appId);
		fldSecret.setText(secret);

	}

	/**
	 * Starts the activity that takes care of the OAuth2 flow
	 * 
	 * @param oauth2Params
	 */
	private void startOauthFlow() {
		startActivity(new Intent().setClass(this,
				OAuthAccessTokenActivity.class));

	}

	protected void getAccessToken() {

		new AccessTokenGetter().execute();

	}

	protected void refresh() {
		new TokenRefreher().execute();

	}

	/**
	 * Clears our credentials (token and token secret) from the shared
	 * preferences. We also setup the authorizer (without the token). After
	 * this, no more authorized API calls will be possible.
	 * 
	 * @throws IOException
	 */
	private void clearCredentials() {
		try {
			new OAuth2Helper(prefs, oauthParams).clearCredentials();
			Editor editor = prefs.edit();
			editor.remove("APP_ID");
			editor.remove("SECRET");
			editor.remove(oauthParams.getClientId() + "_AUTH_CODE");
			editor.commit();
			clearFields();
			disableButtons();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void disableButtons() {

		int grayColor = getResources().getColor(R.color.Gray);

		getXsCdeBtn.setEnabled(false);
		refreshBtn.setEnabled(false);
		getUserInfoBtn.setEnabled(false);

		getUserInfoBtn.setTextColor(grayColor);
		refreshBtn.setTextColor(grayColor);
		getXsCdeBtn.setTextColor(grayColor);

	}

	private void clearFields() {
		fldAppId.setText("");
		fldSecret.setText("");
		fldAuth.setText("");
		fldAccess.setText("");
		fldRefresh.setText("");
		fldEmail.setText("");

	}

	/**
	 * Performs an authorized API call.
	 */
	private void performApiCall() {
		new ApiCallExecutor().execute();
	}

	private class AccessTokenGetter extends AsyncTask<Uri, Void, Void> {

		@Override
		protected Void doInBackground(Uri... params) {

			try {
				authCode = loadAuthCode();
				oAuth2Helper.retrieveAndStoreAccessToken(authCode);

			} catch (Exception ex) {

				ex.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				credential = oAuth2Helper.loadCredential();
			} catch (IOException e) {
				e.printStackTrace();
			}
			accessToken = credential.getAccessToken();
			refreshToken = credential.getRefreshToken();

			fldAccess.setText(accessToken);
			fldRefresh.setText(refreshToken);
			getUserInfoBtn.setEnabled(true);
			refreshBtn.setEnabled(true);
			getUserInfoBtn.setTextColor(getResources().getColor(R.color.White));
			refreshBtn.setTextColor(getResources().getColor(R.color.White));
		}

	}

	private class TokenRefreher extends AsyncTask<Uri, Void, Void> {

		@Override
		protected Void doInBackground(Uri... params) {

			try {
				String rfrshTok = fldRefresh.getText().toString();
				oAuth2Helper.refreshToken(rfrshTok);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				credential = oAuth2Helper.loadCredential();
			} catch (IOException e) {
				e.printStackTrace();
			}
			accessToken = credential.getAccessToken();
			refreshToken = credential.getRefreshToken();
			fldAccess.setText(accessToken);
			fldRefresh.setText(refreshToken);

		}
	}

	private class ApiCallExecutor extends AsyncTask<Uri, Void, Void> {

		String apiResponse = null;
		JSONObject jsonObj;

		@Override
		protected Void doInBackground(Uri... params) {

			try {
				apiResponse = oAuth2Helper.executeApiCall();
				jsonObj = new JSONObject(apiResponse);
				Log.i(Parameters.TAG, "Received response from API : "
						+ apiResponse);
				email = jsonObj.getString("email");

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			fldEmail.setText(email);
		}

	}

	public String loadAuthCode() {

		String clientId = oauthParams.getClientId();
		Log.i(Parameters.TAG, "Loading authCode for client Id " + clientId);

		authCode = prefs.getString(clientId + "_AUTH_CODE", "");

		return authCode;

	}

	public String getErrorMsg() {

		return prefs.getString("toodledo_error", "");
	}

	public void storeLoginInfo(String appId, String secret) {

		Log.i(Parameters.TAG, "Storing login info ");

		Editor editor = prefs.edit();

		editor.putString("APP_ID", appId);
		editor.putString("SECRET", secret);

		editor.commit();

	}

	public void loadLoginInfo() {

		Log.i(Parameters.TAG, "Loading login info");

		appId = prefs.getString("APP_ID", "");
		secret = prefs.getString("SECRET", "");

	}

	@Override
	protected void onStart() {
		super.onStart();
		String code = loadAuthCode();
		if (code.length() > 0) {
			getXsCdeBtn.setEnabled(true);
			getXsCdeBtn.setTextColor(getResources().getColor(R.color.White));
		}
		// fldAuth.setText(code);

	}

	@Override
	protected void onResume() {
		super.onResume();
		String code = loadAuthCode();
		fldAuth.setText(code);
	}

	public void storeAuthCode(String authorizationCode) {

		String clientId = oauthParams.getClientId();
		Log.i(Parameters.TAG, "Storing authCode for client Id " + clientId);
		Editor editor = prefs.edit();
		editor.putString(clientId + "_AUTH_CODE", authorizationCode);
		editor.commit();

	}

}