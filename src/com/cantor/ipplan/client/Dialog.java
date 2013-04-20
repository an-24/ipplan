package com.cantor.ipplan.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

public class Dialog extends DialogBox {

	private FocusWidget firstFocusedWidget = null;
	private FlexTable table;
	private int rowError = -1;
	
	public Dialog(String caption) {
		super();
		setText(caption);
		setAnimationEnabled(true);
		table = new FlexTable();
		table.setCellSpacing(4);
		table.setCellPadding(10);
		setWidget(table);
		getElement().getStyle().setProperty("width","auto");
	}
	
	public FlexTable getContent() {
		return table;
	}

	public FocusWidget getFirstFocusedWidget() {
		return firstFocusedWidget;
	}

	public void setFirstFocusedWidget(FocusWidget focused) {
		this.firstFocusedWidget = focused;
	}

	public void center() {
		if(firstFocusedWidget!=null)
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			public void execute() {
				firstFocusedWidget.setFocus(true);
			}
		});
		super.center();
	}

	public void showError(int beforeRow,String message) {
		rowError = table.insertRow(beforeRow);
		Label l = new Label(message);
		l.setStyleName("serverResponseLabelError");
		table.getCellFormatter().setHorizontalAlignment(rowError, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.getCellFormatter().setVerticalAlignment(rowError, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		table.setWidget(rowError, 0, l);
		table.getFlexCellFormatter().setColSpan(rowError, 0, 2);
	}

	public void resetErrors() {
		if(rowError>=0) table.removeRow(rowError);
		rowError = -1;
	}
}
