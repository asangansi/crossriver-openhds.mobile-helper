package org.openhds.android.tasks;

import org.openhds.android.model.User;
import org.openhds.android.storage.PersistentStore;

import android.os.AsyncTask;

/**
 * The Login task will verify a local user exists in the database If no user
 * exist, the user must initially make a request to download forms with the
 * typed in username/password. If successfully
 */
public class LoginTask extends AsyncTask<Boolean, Void, LoginTask.Result> {
	private PersistentStore storage;
	private String username;
	private String password;
	private Listener listener;

	public enum Result {
		NEW_USER, AUTHENTICATED, BAD_AUTHENTICATION, CREATED_USER_SUCCESS
	}

	public interface Listener {
		void onNewUser();

		void onAuthenticated();

		void onBadAuthentication();

		void onCreatedUser();
	}

	public LoginTask(PersistentStore storage, String user, String password,
			Listener listener) {
		this.storage = storage;
		this.username = user;
		this.password = password;
		this.listener = listener;
	}

	@Override
	protected Result doInBackground(Boolean... params) {
		User user = storage.findUserByUsername(username);
		if (user != null && user.getPassword().equals(password)) {
			return Result.AUTHENTICATED;
		} else if (user != null) {
			return Result.BAD_AUTHENTICATION;
		}

		return Result.NEW_USER;
	}

	@Override
	protected void onPostExecute(Result result) {
		switch (result) {
		case BAD_AUTHENTICATION:
			listener.onBadAuthentication();
			break;
		case AUTHENTICATED:
			listener.onAuthenticated();
			break;
		case NEW_USER:
			listener.onNewUser();
			break;
		case CREATED_USER_SUCCESS:
			listener.onCreatedUser();
		}
	}

}
