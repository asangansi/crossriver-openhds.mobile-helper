package org.openhds.android.tasks;

import org.apache.http.HttpResponse;
import org.openhds.android.model.User;
import org.openhds.android.storage.PersistentStore;

/**
 * Task to initially authenticate a user when they first use the application
 */
public class AuthenticateTask extends AbstractHttpTask<Void, Void> {

	private PersistentStore store;

	public AuthenticateTask(AbstractHttpTask.RequestContext requestCtx,
			TaskListener listener, PersistentStore store) {
		super(requestCtx, listener);
		this.store = store;
	}

	@Override
	protected EndResult handleResponseData(HttpResponse response) {
		User user = new User();
		user.setName(requestCtx.user);
		user.setPassword(requestCtx.password);

		store.addUser(user);

		return EndResult.SUCCESS;
	}

}
