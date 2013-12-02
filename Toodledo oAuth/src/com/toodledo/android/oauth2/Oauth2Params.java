package com.toodledo.android.oauth2;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential.AccessMethod; 

/**
 * 
 * Enum that encapsulates the various OAuth2 connection parameters for the
 * different providers
 * 
 * We capture the following properties for the demo application
 * 
 * clientId clientSecret scope rederictUri apiUrl tokenServerUrl
 * authorizationServerEncodedUrl accessMethod
 * 
 * @author sokkarÍ
 * 
 */
public enum Oauth2Params {

	TOODLEDO_OAUTH2(
			"AndroidOauth",
			"api528fb92e33393",
			"https://AndroidOauth:api528fb92e33393@api.toodledo.com/3/account/token.php",
			"https://api.toodledo.com/3/account/authorize.php", BearerToken
					.queryParameterAccessMethod(), "", "http://localhost",
			"ashraf.m.sokkar@gmail.com", "https://api.toodledo.com/3",
			"52985ab8bfccb");

	private String clientId;
	private String clientSecret;
	private String scope;
	private String rederictUri;
	private String userId;
	private String apiUrl;
	private String state;
	private String getJsonUrl = "/account/get.php";

	private String tokenServerUrl;
	private String authorizationServerEncodedUrl;
	private String authorizationCode;

	private AccessMethod accessMethod;

	Oauth2Params(String clientId, String clientSecret, String tokenServerUrl,
			String authorizationServerEncodedUrl, AccessMethod accessMethod,
			String scope, String rederictUri, String userId, String apiUrl,
			String state) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.tokenServerUrl = tokenServerUrl;
		this.authorizationServerEncodedUrl = authorizationServerEncodedUrl;
		this.accessMethod = accessMethod;
		this.scope = scope;
		this.rederictUri = rederictUri;
		this.userId = userId;
		this.apiUrl = apiUrl;
		this.state = state;
	}

	public String getClientId() {
		if (this.clientId == null || this.clientId.length() == 0) {
			throw new IllegalArgumentException(
					"Please provide a valid clientId in the Oauth2Params class");
		}
		return clientId;
	}

	public String getClientSecret() {
		if (this.clientSecret == null || this.clientSecret.length() == 0) {
			throw new IllegalArgumentException(
					"Please provide a valid clientSecret in the Oauth2Params class");
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

	public String getAuthorizationServerEncodedUrl() {
		return authorizationServerEncodedUrl;
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

	public String getGetJsonUrl() {
		return getJsonUrl;
	}

	public void setGetJsonUrl(String getJsonUrl) {
		this.getJsonUrl = getJsonUrl;
	}
 
	public String getAuthorizationCode() {
		return authorizationCode;
	}

	public void setAuthorizationCode(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}
}
