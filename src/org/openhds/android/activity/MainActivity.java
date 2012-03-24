package org.openhds.android.activity;

import java.net.MalformedURLException;
import java.net.URL;

import org.openhds.android.R;
import org.openhds.android.R.id;
import org.openhds.android.R.layout;
import org.openhds.android.R.string;
import org.openhds.android.tasks.DownloadFormsTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private String username;
	private String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		username = getIntent().getExtras().getString(ActivityConstants.USERNAME_PARAM);
		password = getIntent().getExtras().getString(ActivityConstants.PASSWORD_PARAM);

		Button downloadBtn = (Button) findViewById(R.id.download_btn);
		downloadBtn.setOnClickListener(new DownloadButtonListener());
		
		Button viewFormBtn = (Button) findViewById(R.id.view_form_btn);
		viewFormBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(), FormListActivitity.class);
				intent.putExtra(ActivityConstants.USERNAME_PARAM, username);
				startActivity(intent);
			}
		});
		
		Button logoutBtn = (Button) findViewById(R.id.logout_btn);
		logoutBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	private class DownloadButtonListener implements OnClickListener {
		public void onClick(View arg0) {
			SharedPreferences sp = getSharedPreferences(
					getString(R.string.shared_pref_file_name), MODE_PRIVATE);
			String url = sp.getString(
					getString(R.string.shared_pref_server_url_key), "");

			if (url.trim().length() == 0) {
				showToastWithText("No server URL has been set. Set server URL from preferences");
				return;
			}
			URL parsedUrl = null;
			try {
				parsedUrl = new URL(url);
			} catch (MalformedURLException e) {
				showToastWithText("Bad Server URL");
				return;
			}

			DownloadFormsTask task = new DownloadFormsTask(parsedUrl, username,
					password, new DownloadFormsTask.TaskListener() {
						public void onFailedAuthentication() {
							showToastWithText("Bad username and/or password");
						}

						public void onBadXmlResponse() {
							showToastWithText("There was a problem reading response from server");
						}

						public void onConnectionError() {
							showToastWithText("There was a error with the network connection");
						}

						public void onConnectionTimeout() {
							showToastWithText("Connection to the server timed out");
						}

						public void onSuccess() {
							showToastWithText("Download all forms successfully");
						}
					}, getBaseContext());
			task.execute();
		}
	}

	private void showToastWithText(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Server Preferences");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent(getApplicationContext(),
				AppPreferencesActivity.class));
		return true;
	}
}
