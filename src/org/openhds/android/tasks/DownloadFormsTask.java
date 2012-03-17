package org.openhds.android.tasks;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

/**
 * AsyncTask that fetches partial forms (and potentially associated validation
 * failure messages for those forms)
 */
public class DownloadFormsTask extends AsyncTask<Void, Void, Void> {
	
	private String password;
	private String user;

	public DownloadFormsTask(String user, String password) {
		this.user = user;
		this.password = password;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		Credentials creds = getCredentials();
		httpClient.getCredentialsProvider().setCredentials(null, creds);
		HttpHost host = new HttpHost("localhost", 8080);
		HttpGet httpget = new HttpGet("/download-forms");
		
		try {
			HttpResponse response = httpClient.execute(host, httpget);
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
		
		return null;
	}

	private Credentials getCredentials() {
		return new UsernamePasswordCredentials(user, password);
	}

}
