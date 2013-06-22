package com.cantor.ipplan.client;

import com.google.gwt.user.client.ui.FlexTable;

public class TabAnalytical extends FlexTable {

	private FormMain form;
	private DatabaseServiceAsync dbservice;
	
	public TabAnalytical(FormMain form, DatabaseServiceAsync dbservice) {
		super();
		this.form = form;
		this.dbservice = dbservice;
		setSize("100%", "3cm");
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		
	}
}
