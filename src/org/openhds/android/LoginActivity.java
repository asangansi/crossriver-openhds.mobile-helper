package org.openhds.android;

import org.openhds.android.storage.PersistentStore;
import org.openhds.android.tasks.LoginTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private TextView userTxt;
	private TextView passTxt;

	private static final int PROGRESS_DIALOG = 0;
	private static final int DIALOG_ADMIN_CREATED = 1;
	private static final int DIALOG_BAD_AUTH = 2;
	private static final int DIALOG_ERROR_ADMIN = 3;
	private static final int DIALOG_NEW_USER = 4;
	private static final int DIALOG_CREATED_USER = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		// getApplicationContext().deleteDatabase("form_submission");

		Button loginBtn = (Button) findViewById(R.id.login_btn);
		userTxt = (TextView) findViewById(R.id.user_txt);
		passTxt = (TextView) findViewById(R.id.password_txt);
		loginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (!isValidated()) {
					return;
				}
				showProgressDialog();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_ADMIN_CREATED:
			dialog = buildSuccessDialog("Successfully created admin user");
			break;
		case DIALOG_BAD_AUTH:
			dialog = buildFailedDialog("Failed to authenticate username and password");
			break;
		case PROGRESS_DIALOG:
			dialog = buildProgressDialog();
			break;
		case DIALOG_ERROR_ADMIN:
			dialog = buildFailedDialog("There was an error creating the admin user");
			break;
		case DIALOG_CREATED_USER:
			dialog = buildSuccessDialog("Successfully created new user");
			break;
		case DIALOG_NEW_USER:
			dialog = buildNewUserDialog();
			break;
		default:
			dialog = null;
		}

		return dialog;
	}

	private Dialog buildNewUserDialog() {
		return buildGenericOkDialog("User not recognized",
				"Do you want to authenticate with the server?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
	}

	private Dialog buildFailedDialog(String message) {
		return buildGenericOkDialog("Error", message).setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
	}

	private AlertDialog.Builder buildGenericOkDialog(String title,
			String message) {
		return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert).setTitle(title)
				.setMessage(message);
	}

	private Dialog buildSuccessDialog(String message) {
		return buildGenericOkDialog("Success", message).setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						startMainFormActivity();
					}
				}).create();
	}

	private Dialog buildProgressDialog() {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setCancelable(true);
		dialog.setTitle("Please Wait");
		dialog.setMessage("Trying to authenticate...");
		dialog.setIndeterminate(true);

		return dialog;
	}

	protected void showProgressDialog() {
		showDialog(PROGRESS_DIALOG);
		PersistentStore ps = new PersistentStore(this);
		LoginTask loginTask = new LoginTask(ps, userTxt.getText().toString(),
				passTxt.getText().toString(), new LoginListener());
		loginTask.execute(false);
	}

	private void startMainFormActivity() {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.putExtra("username", userTxt.getText().toString());
		intent.putExtra("password", passTxt.getText().toString());
		passTxt.setText("");
		startActivity(intent);
	}

	private Dialog buildCreatedUserDialog() {
		return null;
	}

	private boolean isValidated() {
		String user = userTxt.getText().toString();
		if ("".equals(user.trim())) {
			Toast.makeText(getBaseContext(), "Please enter in a user",
					Toast.LENGTH_LONG).show();
			return false;
		}

		String password = passTxt.getText().toString();
		if ("".equals(password.trim())) {
			Toast.makeText(getBaseContext(), "Please enter in a password",
					Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	private class LoginListener implements LoginTask.Listener {
		public void onNewUser() {
			removeAndShow(DIALOG_NEW_USER);
		}

		public void onAuthenticated() {
			removeDialog(PROGRESS_DIALOG);
			startMainFormActivity();
		}

		public void onBadAuthentication() {
			removeAndShow(DIALOG_BAD_AUTH);
		}

		public void onErrorCreatingAdmin() {
			removeAndShow(DIALOG_ERROR_ADMIN);
		}

		public void onCreatedAdmin() {
			removeAndShow(DIALOG_ADMIN_CREATED);
		}

		public void onCreatedUser() {
			removeAndShow(DIALOG_CREATED_USER);
		}
	}

	private void removeAndShow(int toShow) {
		removeDialog(PROGRESS_DIALOG);
		showDialog(toShow);
	}
}
