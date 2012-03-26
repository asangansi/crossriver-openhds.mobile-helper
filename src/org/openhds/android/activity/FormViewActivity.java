package org.openhds.android.activity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openhds.android.InstanceProviderAPI;
import org.openhds.android.R;
import org.openhds.android.model.FormSubmissionRecord;
import org.openhds.android.storage.PersistentStore;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FormViewActivity extends Activity {

	private static final int ODK_FORM_ENTRY_RESULT = 1;
	
	private Dialog dialog;
	private PersistentStore store;
	private long formId;
	private FormSubmissionRecord record;

	private class LoadRecordTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			record = store.findSubmissionById(formId);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			loadFormSubmission();
			dialog.dismiss();
		}
	}

	/**
	 * Async Task that will attempt to write the form instance data to disk, and
	 * then use the ODK Content Provider to create a form instance record
	 */
	private class OdkFormLoaderTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			File root = Environment.getExternalStorageDirectory();
			String destinationPath = root.getAbsolutePath() + File.separator
					+ "Android" + File.separator + "data" + File.separator
					+ "org.openhds.mobile" + File.separator + "files";

			File baseDir = new File(destinationPath);
			if (!baseDir.exists()) {
				boolean created = baseDir.mkdirs();
				if (!created) {
					Log.e("FormViewActivity", "Could not create directory");
				}
			}

			destinationPath += File.separator + record.getSaveDate() + ".xml";
			File targetFile = new File(destinationPath);
			if (!targetFile.exists()) {
				try {
					FileWriter writer = new FileWriter(targetFile);
					writer.write(record.getPartialForm());
					writer.close();
				} catch (IOException e) {
				}
			}

			if (record.getOdkUri() == null) {
				ContentValues values = new ContentValues();
				values.put(
						InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
						targetFile.getAbsolutePath());
				values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
						record.getFormType());
				values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
						"widgets");
				Uri uri = getContentResolver()
						.insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
								values);
				if (uri == null) {
					// show error
				}
				record.setOdkUri(uri.toString());
				store.updateOdkUri(record.getId(), uri);
			}

			Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(record.getOdkUri()));
			List<ResolveInfo> resolved = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			startActivityForResult(intent, ODK_FORM_ENTRY_RESULT);
			return null;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_view);
		formId = getIntent().getExtras().getLong(ActivityConstants.FORM_ID);
		dialog = ProgressDialog.show(this, "Loading",
				"Loading form submission...", true);
		store = new PersistentStore(this);
		new LoadRecordTask().execute();
	}

	private void loadFormSubmission() {
		TextView tv = (TextView) findViewById(R.id.form_owner_id);
		tv.setText(record.getFormOwnerId());

		tv = (TextView) findViewById(R.id.location_value_txt);
		tv.setText("LOC001");

		tv = (TextView) findViewById(R.id.failure_messages);
		StringBuilder builder = new StringBuilder();
		int cnt = 1;
		for (String error : record.getErrors()) {
			builder.append(cnt + ". " + error + "\n\n");
			cnt++;
		}
		tv.setText(builder.toString());

		Button editOdkBtn = (Button) findViewById(R.id.edit_in_odk_btn);
		editOdkBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				dialog = ProgressDialog.show(FormViewActivity.this, "Loading",
						"Loading form into ODK...", true);
				new OdkFormLoaderTask().execute();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case ODK_FORM_ENTRY_RESULT:
			handleFormEntry(resultCode);
		}
	}

	private void handleFormEntry(int resultCode) {
		if (resultCode == RESULT_OK) {
			Toast.makeText(this, "Success!", Toast.LENGTH_LONG).show();
		} else {
			
		}
	}

}
