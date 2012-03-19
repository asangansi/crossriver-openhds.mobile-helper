package org.openhds.android.model;

import java.util.ArrayList;
import java.util.List;

public class FormSubmissionRecord {
	
	private String formOwnerId;
	private String formType;
	private String partialForm;
	private List<String> errors = new ArrayList<String>();

	public void setFormOwnerId(String text) {
		this.formOwnerId = text;
	}

	public void setFormType(String text) {
		this.formType = text;
	}

	public void setPartialFormData(String text) {
		this.partialForm = text;
	}

	public void addErrorMessage(String text) {
		errors .add(text);
	}

}
