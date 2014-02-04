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

	private WebView webview;
	private SharedPreferences prefs;
	private OAuth2Helper oAuth2Helper;
	private Parameters oauthParams;
	private ProgressDialog progressDialog;
	private String storedState, returnedState;
	boolean handled = false;
	private boolean hasLoggedIn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("just a sec...");
			progressDialog.show();
		}
		Log.i(Parameters.TAG, "Starting task to retrieve request token.");
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		oauthParams = ApplicationController.getInstance().getParameters();
		oAuth2Helper = new OAuth2Helper(this.prefs, oauthParams);
		storedState = oauthParams.getState();
		webview = new WebView(this);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setVisibility(View.VISIBLE);
		setContentView(webview);

		String authorizationUrl = oauthParams.getAuthorizationServerUrl()
				+ "?response_type=code&client_id=" + oauthParams.getClientId()
				+ "&state=" + oauthParams.getState() + "&scope=" + oauthParams.getScope();
		Log.i(Parameters.TAG, "Using authorizationUrl = " + authorizationUrl);

		handled = false;

		webview.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap bitmap) {
				Log.d(Parameters.TAG, "onPageStarted : " + url + " handled = "
						+ handled);

			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				boolean shouldOverride = false;

				if (!url.startsWith(oauthParams.getAuthorizationServerUrl())) {
					progressDialog = new ProgressDialog(
							OAuthAccessTokenActivity.this);
					progressDialog.setMessage("just a sec...");
					progressDialog.show();
					webview.setVisibility(View.INVISIBLE);

					new ProcessToken(url, oAuth2Helper).execute();

					shouldOverride = true;
				}

				return shouldOverride;
			}

			@Override
			public void onPageFinished(final WebView view, final String url) {
				Log.d(Parameters.TAG, "onPageFinished : " + url + " handled = "
						+ handled);
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
			}

		});

		webview.loadUrl(authorizationUrl);
	}



	@Override
	protected void onResume() {
		super.onResume();
		Log.i(Parameters.TAG, "onResume called with " + hasLoggedIn);
		if (hasLoggedIn) {
			finish();
		}
	}

	private class ProcessToken extends AsyncTask<Uri, Void, Void> {

		String url;
		boolean startActivity = false;

		public ProcessToken(String url, OAuth2Helper oAuth2Helper) {
			this.url = url; }

		@Override
		protected Void doInBackground(Uri... params) {
 
			if (!url.startsWith(oauthParams.getAuthorizationServerUrl())) {
				Log.i(Parameters.TAG, "Redirect URL found" + url);
				handled = true;
				try {
					if (url.indexOf("code=") != -1) {
						String authorizationCode = extractCodeFromUrl(url);
						if (!returnedState.equalsIgnoreCase(storedState)) {
							Log.i(Parameters.TAG, "Invalid state parameter");
							finish();
							return null;

						}
						Log.i(Parameters.TAG, "Found code = "
								+ authorizationCode);
						storeAuthCode(authorizationCode); 
						startActivity = true;
						hasLoggedIn = true;

					} else if (url.indexOf("error=") != -1) {
						
						cleareAuthCode();
						startActivity = true;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				Log.i(Parameters.TAG, "Not doing anything for url " + url);
			}
			return null;
		}

		private String extractCodeFromUrl(String url) throws Exception {
			String redirectUri = url.substring(0, url.indexOf("code="));
			oauthParams.setRederictUri(redirectUri);
			returnedState = url.substring(url.indexOf("state=") + 6);
			String encodedCode = url.substring(oauthParams.getRederictUri()
					.length() + 5, url.indexOf(";"));
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
				Log.i(Parameters.TAG, " ++++++++++++ Starting mainscreen again");
				startActivity(new Intent(OAuthAccessTokenActivity.this,
						MainScreen.class));

				finish();
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
			}

		}

	}

	public void storeAuthCode(String authorizationCode) {

		String clientId = oauthParams.getClientId();
		Log.i(Parameters.TAG, "Storing authCode for client Id " + clientId);

		Editor editor = prefs.edit();

		editor.putString(clientId + "_AUTH_CODE", authorizationCode);

		editor.commit();

	}
	
	public void cleareAuthCode() {
 
		Log.i(Parameters.TAG, "Clearing existing authCode");
		Editor editor = prefs.edit(); 
		editor.remove(oauthParams.getClientId() + "_AUTH_CODE"); 		
		editor.commit();

	}
}
