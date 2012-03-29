package org.openhds.android.activity;

import org.openhds.android.R;
import org.openhds.android.storage.PersistentStore;
import org.openhds.android.tasks.AbstractHttpTask.RequestContext;
import org.openhds.android.tasks.AbstractHttpTask.TaskListener;
import org.openhds.android.tasks.AuthenticateTask;
import org.openhds.android.tasks.LoginTask;

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

public class LoginActivity extends AbstractActivity {

	private TextView userTxt;
	private TextView passTxt;
	private PersistentStore store;

	private static final int PROGRESS_DIALOG = 0;
	private static final int DIALOG_ADMIN_CREATED = 1;
	private static final int DIALOG_BAD_AUTH = 2;
	private static final int DIALOG_ERROR_ADMIN = 4;
	private static final int DIALOG_NEW_USER = 8;
	private static final int DIALOG_CREATED_USER = 16;
	private static final int DIALOG_CONNECTION_ERROR = 32;
	private static final int DIALOG_ERROR_USER = 64;

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
		case DIALOG_CONNECTION_ERROR:
			dialog = buildFailedDialog("There was a problem communicating with the server");
			break;
		case DIALOG_ERROR_USER:
			dialog = buildFailedDialog("There was an error creating the user");
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
								dialog.dismiss();
								showDialog(PROGRESS_DIALOG);
								executeAuthenticateTask();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
	}

	private void executeAuthenticateTask() {
		RequestContext requestCtx = new RequestContext();
		requestCtx.url(getServerUrl("/api/user/authenticate"))
				.user(getUsernameValue().toUpperCase())
				.password(getPasswordValue());
		AuthenticateTask authenticateTask = new AuthenticateTask(requestCtx,
				new AuthenticateListener(), getPersistentStore());
		authenticateTask.execute();
	}

	private PersistentStore getPersistentStore() {
		if (store == null) {
			store = new PersistentStore(this);
		}

		return store;
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
		LoginTask loginTask = new LoginTask(ps, getUsernameValue().toUpperCase(),
				getPasswordValue(), new LoginListener());
		loginTask.execute(false);
	}

	private void startMainFormActivity() {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.putExtra(USERNAME_PARAM, getUsernameValue());
		intent.putExtra(PASSWORD_PARAM, getPasswordValue());
		passTxt.setText("");
		startActivity(intent);
	}

	private String getUsernameValue() {
		return userTxt.getText().toString();
	}

	private String getPasswordValue() {
		return passTxt.getText().toString();
	}

	private boolean isValidated() {
		String user = getUsernameValue();
		if ("".equals(user.trim())) {
			Toast.makeText(getBaseContext(), "Please enter in a user",
					Toast.LENGTH_LONG).show();
			return false;
		}

		String password = getPasswordValue();
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

	private class AuthenticateListener implements AuthenticateTask.TaskListener {

		public void onFailedAuthentication() {
			removeAndShow(DIALOG_BAD_AUTH);
		}

		public void onConnectionError() {
			removeAndShow(DIALOG_CONNECTION_ERROR);
		}

		public void onConnectionTimeout() {
			removeAndShow(DIALOG_CONNECTION_ERROR);
		}

		public void onSuccess() {
			removeAndShow(DIALOG_CREATED_USER);
		}

		public void onFailure() {
			removeAndShow(DIALOG_ERROR_USER);
		}
	}

	private void removeAndShow(int toShow) {
		removeDialog(PROGRESS_DIALOG);
		showDialog(toShow);
	}
}
