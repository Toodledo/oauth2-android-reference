package com.toodledo.android.oauth2;

import android.app.Application;

public class ApplicationController extends Application {

	/**
	 * Log or request TAG
	 */
	public static final String TAG = "ApplicationContext";
	
	private Parameters oauth2Params;

	/**
	 * A singleton instance of the application class for easy access in other
	 * places
	 */
	private static ApplicationController sInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		setParameters(new Parameters());
		
		// initialize the singleton
		sInstance = this;
	}

	/**
	 * @return ApplicationController singleton instance
	 */
	public static synchronized ApplicationController getInstance() {
		return sInstance;
	}

	public Parameters getParameters() {
		return oauth2Params;
	}

	public void setParameters(Parameters oauth2Params) {
		this.oauth2Params = oauth2Params;
	}
}