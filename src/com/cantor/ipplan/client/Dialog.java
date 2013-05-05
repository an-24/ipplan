package com.cantor.ipplan.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

public class Dialog extends DialogBox {

	private FocusWidget firstFocusedWidget = null;
	private FlexTable table;
	private int rowError = -1;
	private Button buttonCancel;
	private Button buttonOk;
	
	public Dialog(String caption) {
		super();
		setText(caption);
		setAnimationEnabled(true);
		setGlassEnabled(true);
		
		table = new FlexTable();
		table.setCellSpacing(4);
		table.setCellPadding(10);
		
		FormPanel f = new FormPanel();
		f.setWidget(table);
		setWidget(f);
		
		getElement().getStyle().setProperty("width","auto");
	}

	public Button getButtonCancel() {
		return buttonCancel;
	}

	public void setButtonCancel(Button buttonCancel) {
		this.buttonCancel = buttonCancel;
	}

	public Button getButtonOk() {
		return buttonOk;
	}

	public void setButtonOk(Button buttonOk) {
		this.buttonOk = buttonOk;
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
	
	@Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONKEYDOWN:
            	switch (event.getNativeEvent().getKeyCode()) {
				case KeyCodes.KEY_ESCAPE:
					if(buttonCancel!=null) buttonCancel.click(); else hide();
					break;
				case KeyCodes.KEY_ENTER:
					if(buttonOk!=null) {
						buttonOk.click();
						event.cancel();
					}
					break;
				default:
					break;
				}
                break;
        }
    }	
}
