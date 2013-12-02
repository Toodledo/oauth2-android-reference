package com.toodledo.android.oauth2;

import android.app.Application;
import android.text.TextUtils;

public class ApplicationController extends Application {

	/**
	 * Log or request TAG
	 */
	public static final String TAG = "ApplicationContext";
	private Oauth2Params oauth2Params;

	/**
	 * A singleton instance of the application class for easy access in other
	 * places
	 */
	private static ApplicationController sInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		setOauth2Params(Constants.OAUTH2PARAMS);
		// initialize the singleton
		sInstance = this;
	}

	/**
	 * @return ApplicationController singleton instance
	 */
	public static synchronized ApplicationController getInstance() {
		return sInstance;
	}

	public Oauth2Params getOauth2Params() {
		return oauth2Params;
	}

	public void setOauth2Params(Oauth2Params oauth2Params) {
		this.oauth2Params = oauth2Params;
	}
}