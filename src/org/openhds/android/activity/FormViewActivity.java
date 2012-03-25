package org.openhds.android.activity;

import org.openhds.android.R;
import org.openhds.android.model.FormSubmissionRecord;
import org.openhds.android.storage.PersistentStore;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class FormViewActivity extends Activity {

	private Dialog dialog;
	
	private class LoadRecordTask extends AsyncTask<Void, Void, FormSubmissionRecord> {
		
		private long id;
		private PersistentStore store;

		public LoadRecordTask(long id, PersistentStore store) {
			this.id = id;
			this.store = store;
		}

		@Override
		protected FormSubmissionRecord doInBackground(Void... arg0) {
			FormSubmissionRecord submission = store.findSubmissionById(id);
			
			return submission;
		}

		@Override
		protected void onPostExecute(FormSubmissionRecord result) {
			loadFormSubmission(result);
			dialog.dismiss();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_view);
		long id = getIntent().getExtras().getLong(ActivityConstants.FORM_ID);
		dialog = ProgressDialog.show(this, "Loading", "Loading form submission...", true);
		new LoadRecordTask(id, new PersistentStore(this)).execute();
	}
	
	private void loadFormSubmission(FormSubmissionRecord record) {
		TextView tv = (TextView)findViewById(R.id.form_owner_id);
		tv.setText(record.getFormOwnerId());
		
		tv = (TextView) findViewById(R.id.location_value_txt);
		tv.setText("LOC001");
		
		tv = (TextView) findViewById(R.id.failure_messages);
		StringBuilder builder = new StringBuilder();
		int cnt = 1;
		for(String error : record.getErrors()) {
			builder.append(cnt + ". " + error + "\n\n");
			cnt++;
		}
		tv.setText(builder.toString());
	}

}
