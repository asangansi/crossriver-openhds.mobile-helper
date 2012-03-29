package org.openhds.android.activity;

import java.net.MalformedURLException;
import java.net.URL;

import org.openhds.android.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public abstract class AbstractActivity extends Activity {

	public static final String USERNAME_PARAM = "username";
	public static final String PASSWORD_PARAM = "password";
	public static final String FORM_ID = "form_id";

	protected URL getServerUrl(String path) {
		SharedPreferences sp = getSharedPreferences(
				getString(R.string.shared_pref_file_name), MODE_PRIVATE);
		String url = sp.getString(
				getString(R.string.shared_pref_server_url_key), "");

		if (url.trim().length() == 0) {
			showToastWithText("No server URL has been set. Set server URL from preferences");
			return null;
		}

		URL parsedUrl = null;
		try {
			parsedUrl = new URL(url + path);
		} catch (MalformedURLException e) {
			showToastWithText("Bad Server URL");
		}

		return parsedUrl;
	}

	protected void showToastWithText(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	protected String getUsernameFromIntent() {
		return getIntent().getExtras().getString(USERNAME_PARAM);
	}

	protected String getPasswordFromIntent() {
		return getIntent().getExtras().getString(PASSWORD_PARAM);
	}
	
	protected void setUsernameOnIntent(Intent intent) {
		intent.putExtra(USERNAME_PARAM, getUsernameFromIntent());		
	}
	
	protected long getFormIdFromIntent() {
		return getIntent().getExtras().getLong(FORM_ID);
	}
}
