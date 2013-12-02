package com.toodledo.android.oauth2;

import java.net.URLDecoder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Execute the OAuthRequestTokenTask to retrieve the request, and authorize the
 * request. After the request is authorized by the user, the callback URL will
 * be intercepted here.
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
public class OAuthAccessTokenActivity extends Activity {

	private SharedPreferences prefs;
	private OAuth2Helper oAuth2Helper;
	private Oauth2Params oauthParams;
 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(Constants.TAG, "Starting task to retrieve request token.");
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		oauthParams = ApplicationController.getInstance().getOauth2Params();
		oAuth2Helper = new OAuth2Helper(this.prefs);
		
		webview = new WebView(this);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setVisibility(View.VISIBLE);
		setContentView(webview);
 
		String authorizationUrl = oauthParams.getApiUrl()
				+ "/account/authorize.php?response_type=code&client_id="
				+ oauthParams.getClientId() + "&state="
				+ oauthParams.getState() + "&scope=basic";
		
		Log.i(Constants.TAG, "Using authorizationUrl = " + authorizationUrl);

		handled = false;

		webview.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap bitmap) {
				Log.d(Constants.TAG, "onPageStarted : " + url + " handled = "
						+ handled);
			}

			@Override
			public void onPageFinished(final WebView view, final String url) {
				Log.d(Constants.TAG, "onPageFinished : " + url + " handled = "
						+ handled);

				if (url.startsWith(Constants.OAUTH2PARAMS.getRederictUri())) {
					webview.setVisibility(View.INVISIBLE);

					if (!handled) {
						new ProcessToken(url, oAuth2Helper).execute();

					}
				} else {
					webview.setVisibility(View.VISIBLE);
				}
			}

		});

		webview.loadUrl(authorizationUrl);
	}

	private WebView webview;

	boolean handled = false;
	private boolean hasLoggedIn;

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(Constants.TAG, "onResume called with " + hasLoggedIn);
		if (hasLoggedIn) {
			finish();
		}
	}

	private class ProcessToken extends AsyncTask<Uri, Void, Void> {

		String url;
		boolean startActivity = false;

		public ProcessToken(String url, OAuth2Helper oAuth2Helper) {
			this.url = url;
		}

		@Override
		protected Void doInBackground(Uri... params) {

			if (url.startsWith(Constants.OAUTH2PARAMS.getRederictUri())) {
				Log.i(Constants.TAG, "Redirect URL found" + url);
				handled = true;
				try {
					if (url.indexOf("code=") != -1) {
						String authorizationCode = extractCodeFromUrl(url);
					 	oauthParams.setAuthorizationCode(authorizationCode);
						Log.i(Constants.TAG, "Found code = "
								+ authorizationCode);
						storeAuthCode(authorizationCode);
						oAuth2Helper
								.retrieveAndStoreAccessToken(authorizationCode);
						startActivity = true;
						hasLoggedIn = true;

					} else if (url.indexOf("error=") != -1) {
						startActivity = true;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				Log.i(Constants.TAG, "Not doing anything for url " + url);
			}
			return null;
		}

		private String extractCodeFromUrl(String url) throws Exception {
			String encodedCode = url.substring(Constants.OAUTH2PARAMS
					.getRederictUri().length() + 7, url.indexOf(";"));
			return URLDecoder.decode(encodedCode, "UTF-8");
		}

		@Override
		protected void onPreExecute() {

		}

		/**
		 * When we're done and we've retrieved either a valid token or an error
		 * from the server, we'll return to our original activity
		 */
		@Override
		protected void onPostExecute(Void result) {
			if (startActivity) {
				Log.i(Constants.TAG, " ++++++++++++ Starting mainscreen again");
				startActivity(new Intent(OAuthAccessTokenActivity.this,
						MainScreen.class));

				finish();
			}

		}

	}

	public void storeAuthCode(String authorizationCode) {

		String clientId = oauthParams.getClientId();
		Log.i(Constants.TAG, "Storing authCode for client Id " + clientId);

		Editor editor = prefs.edit();

		editor.putString(clientId + "_AUTH_CODE", authorizationCode);

		editor.commit();

	}
}
