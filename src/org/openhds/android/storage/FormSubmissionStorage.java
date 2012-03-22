package org.openhds.android.storage;

import org.openhds.android.model.FormSubmissionRecord;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Persistent store  
 */
public class FormSubmissionStorage extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "form_submission";
	private static final int DATABASE_VERSION = 1;

	private static final String FORM_TABLE_NAME = "formsubmission";
	private static final String KEY_ID = "id";
	private static final String KEY_FORMOWNER_ID = "form_owner_id";
	private static final String KEY_FORM_TYPE = "form_type";
	private static final String KEY_FORM_INSTANCE = "form_instance";
	
	private static final String ERROR_TABLE_NAME = "formsubmission_msg";
	private static final String KEY_FORM_ID = "form_id";
	private static final String KEY_FORM_MSG = "message";
	
	private static final String FORM_DB_CREATE = "CREATE TABLE "+ FORM_TABLE_NAME +
			" (" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_FORMOWNER_ID + " TEXT, " +
					KEY_FORM_TYPE + " TEXT, " + KEY_FORM_INSTANCE + " TEXT)";
	
	private static final String MESSAGE_DB_CREATE = "CREATE TABLE " + ERROR_TABLE_NAME +
			" (" + KEY_FORM_ID + " INTEGER, " + KEY_FORM_MSG + " TEXT)";

	public FormSubmissionStorage(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(FORM_DB_CREATE);
		db.execSQL(MESSAGE_DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	
	public void saveFormSubmission(FormSubmissionRecord fs) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(KEY_FORMOWNER_ID, fs.getFormOwnerId());
			cv.put(KEY_FORM_TYPE, fs.getFormType());
			cv.put(KEY_FORM_INSTANCE, fs.getFormInstance());
			long rowId = db.insert(FORM_TABLE_NAME, null, cv);
			
			for(String error : fs.getErrors()) {
				cv = new ContentValues();
				cv.put(KEY_FORM_ID, rowId);
				cv.put(KEY_FORM_MSG, error);
				db.insert(ERROR_TABLE_NAME, null, cv);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
}
