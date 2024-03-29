package org.openhds.android;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This class was taken from the OpenDataKit project version 1.1.7
 */
public final class InstanceProviderAPI {
	public static final String AUTHORITY = "org.odk.collect.android.provider.odk.instances";

	// This class cannot be instantiated
	private InstanceProviderAPI() {
	}

	// status for instances
	public static final String STATUS_INCOMPLETE = "incomplete";
	public static final String STATUS_COMPLETE = "complete";
	public static final String STATUS_SUBMITTED = "submitted";
	public static final String STATUS_SUBMISSION_FAILED = "submissionFailed";

	/**
	 * Notes table
	 */
	public static final class InstanceColumns implements BaseColumns {
		// This class cannot be instantiated
		private InstanceColumns() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/instances");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.instance";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.instance";

		// These are the only things needed for an insert
		public static final String DISPLAY_NAME = "displayName";
		public static final String SUBMISSION_URI = "submissionUri";
		public static final String INSTANCE_FILE_PATH = "instanceFilePath";
		public static final String JR_FORM_ID = "jrFormId";
		// public static final String FORM_ID = "formId";

		// these are generated for you (but you can insert something else if you
		// want)
		public static final String STATUS = "status";
		public static final String LAST_STATUS_CHANGE_DATE = "date";
		public static final String DISPLAY_SUBTEXT = "displaySubtext";
		// public static final String DISPLAY_SUB_SUBTEXT = "displaySubSubtext";

		// public static final String DEFAULT_SORT_ORDER = "modified DESC";
		// public static final String TITLE = "title";
		// public static final String NOTE = "note";
		// public static final String CREATED_DATE = "created";
		// public static final String MODIFIED_DATE = "modified";
	}
}