package org.openhds.android.tasks;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.openhds.android.BadXmlException;
import org.openhds.android.model.FormSubmissionRecord;
import org.openhds.android.storage.PersistentStore;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.AsyncTask;

/**
 * AsyncTask that fetches partial forms (and potentially associated validation
 * failure messages for those forms) and stores them in SQL Lite DB
 */
public class DownloadFormsTask extends
		AsyncTask<Void, Void, DownloadFormsTask.EndResult> {

	private static final int UNAUTHORIZED_STATUS_CODE = 401;
	private static final int SUCCESS_STATUS_CODE = 200;
	private String password;
	private String user;
	private TaskListener listener;
	private URL url;
	private PersistentStore storage;

	public interface TaskListener {
		void onFailedAuthentication();

		void onBadXmlResponse();

		void onConnectionError();

		void onConnectionTimeout();

		void onSuccess();
	}

	enum EndResult {
		BAD_AUTHENTICATION, BAD_XML, CONNECTION_ERROR, CONNECTION_TIMEOUT, SUCCESS
	}

	public DownloadFormsTask(URL url, String user, String password,
			TaskListener listener, Context context) {
		this.url = url;
		this.user = user;
		this.password = password;
		this.listener = listener;
		storage = new PersistentStore(context);
	}

	@Override
	protected DownloadFormsTask.EndResult doInBackground(Void... arg0) {
		DefaultHttpClient httpClient = buildHttpClient();
		HttpHost host = new HttpHost(url.getHost(), url.getPort());
		HttpGet httpget = new HttpGet(url.getPath());

		try {
			HttpResponse response = httpClient.execute(host, httpget);
			if (authenticationFailed(response.getStatusLine().getStatusCode())) {
				return EndResult.BAD_AUTHENTICATION;
			} else if (!isRequestSuccess(response.getStatusLine()
					.getStatusCode())) {
				return EndResult.CONNECTION_ERROR;
			}
			handleResponseData(response);
			return EndResult.SUCCESS;
		} catch (ClientProtocolException e) {
			return EndResult.CONNECTION_ERROR;
		} catch (ConnectTimeoutException e) {
			return EndResult.CONNECTION_TIMEOUT;
		} catch (IOException e) {
			return EndResult.CONNECTION_ERROR;
		} catch (BadXmlException e) {
			return EndResult.BAD_XML;
		}
	}

	private DefaultHttpClient buildHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		setHttpClientParams(httpClient);
		setHttpClientCredentials(httpClient);
		return httpClient;
	}

	private void setHttpClientParams(DefaultHttpClient httpClient) {
		httpClient.getParams().setIntParameter(
				HttpConnectionParams.CONNECTION_TIMEOUT, 60 * 1000);
	}

	private void setHttpClientCredentials(DefaultHttpClient httpClient) {
		AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
				AuthScope.ANY_REALM);
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				user, password);
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(scope, creds);

		httpClient.setCredentialsProvider(credsProvider);
	}

	private boolean authenticationFailed(int statusCode) {
		return statusCode == UNAUTHORIZED_STATUS_CODE;
	}

	private boolean isRequestSuccess(int statusCode) {
		return statusCode == SUCCESS_STATUS_CODE;
	}

	private void handleResponseData(HttpResponse response) throws IOException,
			BadXmlException {
		HttpEntity entity = response.getEntity();
		StringReader reader = new StringReader(EntityUtils.toString(entity));

		List<FormSubmissionRecord> records = parseResponseXml(reader);
		saveRecords(records);
	}

	private List<FormSubmissionRecord> parseResponseXml(StringReader reader)
			throws BadXmlException, IOException {
		try {
			XmlPullParser parser = buildXmlPullParser(reader);
			validateStartOfXmlDocument(parser);
			validateDocumentElement(parser);
			return parseSubmissionSet(parser);
		} catch (XmlPullParserException e) {
			throw new BadXmlException("Bad XML Document");
		}
	}

	private XmlPullParser buildXmlPullParser(StringReader reader)
			throws XmlPullParserException {
		XmlPullParser parser = XmlPullParserFactory.newInstance()
				.newPullParser();
		parser.setInput(reader);
		return parser;
	}

	private void validateStartOfXmlDocument(XmlPullParser parser)
			throws XmlPullParserException, BadXmlException, IOException {
		int eventType = parser.getEventType();
		if (eventType != XmlPullParser.START_DOCUMENT) {
			throw new BadXmlException("Start of document");
		}
	}

	private void validateDocumentElement(XmlPullParser parser)
			throws XmlPullParserException, IOException, BadXmlException {
		int eventType = parser.next();
		if (!isStartTag(eventType)
				&& !"formSubmissionSet".equals(parser.getName())) {
			throw new BadXmlException("formSubmissionSet");
		}
	}

	private boolean isStartTag(int eventType) {
		return eventType == XmlPullParser.START_TAG;
	}

	private List<FormSubmissionRecord> parseSubmissionSet(XmlPullParser parser)
			throws XmlPullParserException, IOException, BadXmlException {
		List<FormSubmissionRecord> records = new ArrayList<FormSubmissionRecord>();
		int eventType = parser.next();
		if (!isStartTag(eventType) && !"submissions".equals(parser.getName())) {
			throw new BadXmlException("submissions");
		}
		while (!isEndTag(eventType) || !"submissions".equals(parser.getName())) {
			records.add(parseSubmission(parser));
			eventType = parser.next();
		}

		return records;
	}

	private boolean isEndTag(int eventType) {
		return eventType == XmlPullParser.END_TAG;
	}

	private FormSubmissionRecord parseSubmission(XmlPullParser parser)
			throws XmlPullParserException, IOException, BadXmlException {
		int eventType = parser.getEventType();
		if (!isStartTag(eventType)
				&& !"formSubmission".equals(parser.getName())) {
			throw new BadXmlException("formSubmission");
		}

		FormSubmissionRecord record = new FormSubmissionRecord();

		while (!isEndTag(eventType)
				|| !"formSubmission".equals(parser.getName())) {
			eventType = parser.next();
			if (isStartTag(eventType) && "formOwnerId".equals(parser.getName())) {
				checkTextPresent(parser);
				record.setFormOwnerId(parser.getText());
			} else if (isStartTag(eventType)
					&& "formType".equals(parser.getName())) {
				checkTextPresent(parser);
				record.setFormType(parser.getText());
			} else if (isStartTag(eventType)
					&& "formInstance".equals(parser.getName())) {
				checkTextPresent(parser);
				record.setPartialFormData(parser.getText());
			} else if (isStartTag(eventType)
					&& "formErrors".equals(parser.getName())) {
				parseFormErrors(parser, record);
			}
		}

		return record;
	}

	private void checkTextPresent(XmlPullParser parser)
			throws XmlPullParserException, BadXmlException, IOException {
		int eventType = parser.next();
		if (eventType != XmlPullParser.TEXT) {
			throw new BadXmlException("formOwnerId");
		}

	}

	private void parseFormErrors(XmlPullParser parser,
			FormSubmissionRecord record) throws XmlPullParserException,
			IOException, BadXmlException {
		int eventType = parser.next();
		
		if (isEndTag(eventType)) {
			return; // no form errors
		}
		
		if (!isStartTag(eventType) && !"formError".equals(parser.getName())) {
			throw new BadXmlException("formErrors");
		}

		eventType = parser.next();
		if (!"error".equals(parser.getName())) {
			throw new BadXmlException("error");
		}

		eventType = parser.next();
		if (!isTextEvent(eventType)) {
			throw new BadXmlException("No error text");
		}

		record.addErrorMessage(parser.getText());

		parser.next(); // error tag
		parser.next(); // formError tag
	}

	private boolean isTextEvent(int eventType) {
		return eventType == XmlPullParser.TEXT;
	}

	private void saveRecords(List<FormSubmissionRecord> records) {
		for(FormSubmissionRecord record : records) {
			storage.saveFormSubmission(record);
		}
	}	

	@Override
	protected void onPostExecute(DownloadFormsTask.EndResult result) {
		switch (result) {
		case BAD_AUTHENTICATION:
			listener.onFailedAuthentication();
			break;
		case BAD_XML:
			listener.onBadXmlResponse();
			break;
		case CONNECTION_ERROR:
			listener.onConnectionError();
			break;
		case CONNECTION_TIMEOUT:
			listener.onConnectionTimeout();
			break;
		case SUCCESS:
			listener.onSuccess();
		}
	}
}
