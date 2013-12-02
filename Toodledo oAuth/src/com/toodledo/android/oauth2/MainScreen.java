package com.toodledo.android.oauth2;

import java.io.IOException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.client.auth.oauth2.Credential;

public class MainScreen extends Activity {

	private SharedPreferences prefs;
	private OAuth2Helper oAuth2Helper;
	protected int elapsedTime;
	private Button refresh;
	private Button getUserInfo;
	private TextView fldAppId;
	private TextView fldSecret;
	private TextView fldAuth;
	private TextView fldAccess;
	private TextView fldRefresh;

	private TextView fldEmail;
	private TextView emailLbl;
	private LinearLayout userInfoLayout;
	private LinearLayout credLayout;
	private Credential credential;
	private Oauth2Params oauthParams;
	public static String authCode;
	public static String email;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		oAuth2Helper = new OAuth2Helper(this.prefs);
		oauthParams = ApplicationController.getInstance().getOauth2Params();
		fldAppId = (TextView) findViewById(R.id.app_id);
		fldSecret = (TextView) findViewById(R.id.secret);
		fldAuth = (TextView) findViewById(R.id.authorization);
		fldAccess = (TextView) findViewById(R.id.access);
		fldRefresh = (TextView) findViewById(R.id.refresh_token);
		fldEmail = (TextView) findViewById(R.id.user_email_2);
		refresh = (Button) findViewById(R.id.refresh_btn);
		emailLbl = (TextView) findViewById(R.id.user_email_lbl);
		userInfoLayout = (LinearLayout) findViewById(R.id.user_info);
		credLayout = (LinearLayout) findViewById(R.id.main_layout);
		getUserInfo = (Button) findViewById(R.id.get_user_email);
 

		try {
			credential = oAuth2Helper.loadCredential();
		} catch (IOException e) {
			e.printStackTrace();
		}

		setFields();
		getUserInfo.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Performs an authorized API call.
				performApiCall();
				userInfoLayout.setVisibility(View.VISIBLE);

			}

		});
		refresh.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				refresh();

			}

		});

	}

	protected void refresh() {
		fldAccess.setText("");
		fldRefresh.setText("");
		new TokenRefreher().execute();

	}

	private void setFields() {

		fldAppId.setText(Constants.OAUTH2PARAMS.getClientId());
		fldSecret.setText(Constants.OAUTH2PARAMS.getClientSecret());
		fldAuth.setText(Constants.OAUTH2PARAMS.getAuthorizationCode()); 
		fldAuth.setText(loadAuthCode());
		fldAccess.setText(credential.getAccessToken());
		fldRefresh.setText(credential.getRefreshToken());

	}

	/**
	 * Performs an authorized API call.
	 */
	private void performApiCall() {
		new ApiCallExecutor().execute();
	}

	private class ApiCallExecutor extends AsyncTask<Uri, Void, Void> {

		String apiResponse = null;

		JSONObject jsonObj;

		@Override
		protected Void doInBackground(Uri... params) {

			try {
				apiResponse = oAuth2Helper.executeApiCall();
				jsonObj = new JSONObject(apiResponse);
				Log.i(Constants.TAG, "Received response from API : "
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

	private class TokenRefreher extends AsyncTask<Uri, Void, Void> {

		@Override
		protected Void doInBackground(Uri... params) {

			try {
				oAuth2Helper.refreshToken();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			credLayout.invalidate();
			try {
				credential = oAuth2Helper.loadCredential();
			} catch (IOException e) {
				e.printStackTrace();
			}
			credential.getAccessToken();
			credential.getRefreshToken();
			setFields();

		}
	}

	public String loadAuthCode() {

		String clientId = oauthParams.getClientId();
		Log.i(Constants.TAG, "Loading authCode for client Id " + clientId);

		authCode = prefs.getString(clientId + "_AUTH_CODE", "NOAUTHCODE");

		return authCode;

	}
}