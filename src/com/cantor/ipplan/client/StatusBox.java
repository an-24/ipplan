package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.StatusWrapper;
import com.google.gwt.user.client.ui.InlineHTML;


public class StatusBox extends InlineHTML {

	StatusWrapper status;
	
	public StatusBox(StatusWrapper status) {
		super(status.statusName);
		this.status = status; 
		setStyleName("link");
	}

	public StatusWrapper getStatus() {
		return status;
	}

	public void setStatus(StatusWrapper status) {
		this.status = status;
	}
}
