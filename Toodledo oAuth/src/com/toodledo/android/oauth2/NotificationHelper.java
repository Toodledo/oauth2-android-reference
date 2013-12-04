package com.toodledo.android.oauth2;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class NotificationHelper {

	// display customized Toast message
	public static int SHORT_TOAST = 0;
	public static int LONG_TOAST = 1;
	private static final String TAG = "NotifierHelper";

	public static void clear(Context caller) {
		NotificationManager notifier = (NotificationManager) caller
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notifier.cancelAll();
	}

	public static void displayToast(Context caller, String toastMsg,
			int toastType) {

		try {
			LayoutInflater inflater = LayoutInflater.from(caller);

			View mainLayout = inflater.inflate(R.layout.toast_layout, null);
			View rootLayout = mainLayout.findViewById(R.id.toast_layout_root);
			TextView text = (TextView) mainLayout.findViewById(R.id.text);
			text.setText(toastMsg);

			Toast toast = new Toast(caller);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			if (toastType == SHORT_TOAST)
				toast.setDuration(Toast.LENGTH_SHORT);
			else
				toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(rootLayout);
			toast.show();
		} catch (Exception ex) {
			Log.w(TAG, ex.toString());
		}
	}

}
