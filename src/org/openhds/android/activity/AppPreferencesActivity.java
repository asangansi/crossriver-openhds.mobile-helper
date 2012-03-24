package org.openhds.android.activity;

import org.openhds.android.R;
import org.openhds.android.R.layout;
import org.openhds.android.R.string;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class AppPreferencesActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private Editor editor;

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		sp.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.server_preferences);
		SharedPreferences sp1 = getSharedPreferences(
				getString(R.string.shared_pref_file_name), MODE_PRIVATE);
		editor = sp1.edit();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		editor.putString(key, sharedPreferences.getString(key, ""));
		editor.commit();
	}

}
