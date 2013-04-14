package com.cantor.ipplan.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.RootPanel;

public class Form extends Composite {
	private FocusWidget firstFocusedWidget = null;
	private RootPanel root = null;

	public Form() {
		
	}
	
	public Form(RootPanel root) {
		this.root = root;
	}
	
	public RootPanel getRoot() {
		return root;
	}
	
	public FocusWidget getFirstFocusedWidget() {
		return firstFocusedWidget;
	}

	public void setFirstFocusedWidget(FocusWidget focused) {
		this.firstFocusedWidget = focused;
	}

	public void show() {
		if(firstFocusedWidget!=null)
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			public void execute() {
				firstFocusedWidget.setFocus(true);
			}
		});	
		root.clear();
		root.add(this);
		this.getElement().getStyle().setCursor(Cursor.DEFAULT);
	}
	
	protected boolean validate() {
		return true;
	}

}
