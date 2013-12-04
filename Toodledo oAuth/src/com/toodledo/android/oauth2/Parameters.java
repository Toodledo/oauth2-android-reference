package com.toodledo.android.oauth2;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential.AccessMethod;

public class Parameters {

	public static final String TAG = "Android Oauth2 Example";
	private String clientId = "xyz";
	private String clientSecret = "api123";
	private String apiUrl = "api.toodledo.com/3";
	private String authorizationServerUrl = "https://" + apiUrl
			+ "/account/authorize.php";
	private String tokenServerUrl = "https://" + clientId + ":" + clientSecret
			+ "@" + apiUrl + "/account/token.php";
	private String scope = "basic";
	private String rederictUri = "http://localhost";

	public void setRederictUri(String rederictUri) {
		this.rederictUri = rederictUri;
	}

	private String userId = "";
	private String state = SessionIdentifierGenerator.nextSessionId();
	private AccessMethod accessMethod = BearerToken
			.queryParameterAccessMethod();
	private String getJsonUrl = "/account/get.php";

	public String getClientId() {
		if (this.clientId == null || this.clientId.length() == 0) {
			throw new IllegalArgumentException(
					"Please provide a valid clientId.");
		}
		return clientId;
	}

	public String getClientSecret() {
		if (this.clientSecret == null || this.clientSecret.length() == 0) {
			throw new IllegalArgumentException(
					"Please provide a valid clientSecret.");
		}
		return clientSecret;
	}

	public String getScope() {
		return scope;
	}

	public String getRederictUri() {
		return rederictUri;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public String getTokenServerUrl() {
		return tokenServerUrl;
	}

	public AccessMethod getAccessMethod() {
		return accessMethod;
	}

	public String getUserId() {
		return userId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getAuthorizationServerUrl() {
		return authorizationServerUrl;
	}

	public void setAuthorizationServerUrl(String authorizationServerUrl) {
		this.authorizationServerUrl = authorizationServerUrl;
	}

	public String getGetJsonUrl() {
		return getJsonUrl;
	}

	public void setGetJsonUrl(String getJsonUrl) {
		this.getJsonUrl = getJsonUrl;
	}

}
