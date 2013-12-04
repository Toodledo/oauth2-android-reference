package com.toodledo.android.oauth2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.tooldedo.android.oauth2.store.SharedPreferencesCredentialStore;

public class OAuth2Helper {

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private final CredentialStore credentialStore;

	private AuthorizationCodeFlow flow;

	private Parameters oauth2Params;

	public OAuth2Helper(SharedPreferences sharedPreferences,
			Parameters oauth2Params) {
		this.credentialStore = new SharedPreferencesCredentialStore(
				sharedPreferences);
		this.oauth2Params = oauth2Params;
		this.flow = new AuthorizationCodeFlow.Builder(
				oauth2Params.getAccessMethod(), HTTP_TRANSPORT, JSON_FACTORY,
				new GenericUrl(oauth2Params.getTokenServerUrl()),
				new ClientParametersAuthentication(oauth2Params.getClientId(),
						oauth2Params.getClientSecret()),
				oauth2Params.getClientId(),
				oauth2Params.getAuthorizationServerUrl()).setCredentialStore(
				this.credentialStore).build();

	}

	public String getAuthorizationUrl() {
		String authorizationUrl = flow.newAuthorizationUrl()
				.setRedirectUri(oauth2Params.getRederictUri())
				.setScopes(convertScopesToString(oauth2Params.getScope()))
				.build();
		return authorizationUrl;
	}

	public void retrieveAndStoreAccessToken(String authorizationCode)
			throws IOException {
		Log.i(Parameters.TAG, "retrieveAndStoreAccessToken for code "
				+ authorizationCode);
		 
			TokenResponse tokenResponse = flow
					.newTokenRequest(authorizationCode)
					.setScopes(convertScopesToString(oauth2Params.getScope()))
					.setRedirectUri(oauth2Params.getRederictUri()).execute();

			Log.i(Parameters.TAG, "Found tokenResponse :");
			Log.i(Parameters.TAG,
					"Access Token : " + tokenResponse.getAccessToken());
			Log.i(Parameters.TAG,
					"Refresh Token : " + tokenResponse.getRefreshToken());

			flow.createAndStoreCredential(tokenResponse,
					oauth2Params.getUserId());
	 
	}

	public String executeApiCall() throws IOException {
		Credential cred = loadCredential();
		String apiCallUrl = "https://" + oauth2Params.getApiUrl()
				+ oauth2Params.getGetJsonUrl() + "?" + "access_token="
				+ cred.getAccessToken() + "&f=json";
		Log.i(Parameters.TAG, "Executing API call at url " + apiCallUrl);

		HttpResponse response = HTTP_TRANSPORT.createRequestFactory(cred)
				.buildGetRequest(new GenericUrl(apiCallUrl)).execute();

		return response.parseAsString();
	}

	public Credential loadCredential() throws IOException {
		return flow.loadCredential(oauth2Params.getUserId());
	}

	public void clearCredentials() throws IOException {
		flow.getCredentialStore().delete(oauth2Params.getUserId(), null);
	}

	private Collection<String> convertScopesToString(String scopesConcat) {
		String[] scopes = scopesConcat.split(",");
		Collection<String> collection = new ArrayList<String>();
		Collections.addAll(collection, scopes);
		return collection;
	}

	public void refreshToken(String refreshToken) throws IOException {
		String currentRefreshToken = refreshToken; 
		Log.i(Parameters.TAG, "retrieveAndStoreNEWAccessToken for code "
				+ currentRefreshToken);
		TokenResponse tokenResponse = flow.newTokenRequest(currentRefreshToken)
				.setGrantType("refresh_token")
				.set("refresh_token", currentRefreshToken).execute();
		Log.i(Parameters.TAG, "Found new tokenResponse :");
		Log.i(Parameters.TAG,
				"New Access Token : " + tokenResponse.getAccessToken());
		Log.i(Parameters.TAG,
				"New Refresh Token : " + tokenResponse.getRefreshToken());
		flow.createAndStoreCredential(tokenResponse, oauth2Params.getUserId());
	} 
}