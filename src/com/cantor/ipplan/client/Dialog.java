package com.cantor.ipplan.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DialogBox.Caption;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;

public class Dialog extends DialogBox {

	private FocusWidget firstFocusedWidget = null;
	private FlexTable table;
	private Button buttonCancel;
	private Button buttonOk;
	private ClickHandler okHandler;
	private ClickHandler cancelHandler;
	private boolean canceled;
	private List<Integer> errorList = new ArrayList<Integer>();

	public Dialog(String caption, boolean noPanelButton) {
		super();
		setText(caption);
		setAnimationEnabled(true);
		setGlassEnabled(true);
		
		Element ecapt = getCaption().asWidget().getElement();
		Element div = DOM.createDiv();
		Event.setEventListener(div, new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				buttonCancel.click();
			}
		});
		DOM.sinkEvents((com.google.gwt.user.client.Element) div, Event.ONMOUSEDOWN);
		
		div.setClassName("gwt-Close-Dialog");
		ecapt.appendChild(div);
		
		table = new FlexTable();
		table.setCellSpacing(4);
		table.setCellPadding(10);
		
		VerticalPanel pm = new VerticalPanel();
		pm.setWidth("100%");
		pm.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		pm.add(table);
		
		FormPanel f = new FormPanel();
		f.setAction("#");
		f.setMethod("POST");
		f.addSubmitHandler(new SubmitHandler() {
			@Override
			public void onSubmit(SubmitEvent event) {
				event.cancel();
			}
		});
		
		f.setWidget(pm);
		setWidget(f);
		
		HorizontalPanel p = new HorizontalPanel();
		p.setSpacing(10);
		p.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		p.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		pm.add(p);
		
		if(noPanelButton) {
			buttonCancel = new Button("Отменить");
			buttonCancel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(cancelHandler!=null) {
						cancelHandler.onClick(event);
					}
					hide();
				}
			});
			buttonOk = new Button("Добавить");
			buttonOk.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					canceled = false;
					submit();
					if(okHandler!=null) {
						okHandler.onClick(event);
						if(isCanceled()) return;
					}
					hide();
				}
			});
			buttonOk.addStyleName("mainCommand");
			p.add(buttonOk);
			p.add(buttonCancel);
		}
		
		getElement().getStyle().setProperty("width","auto");
	}
	
	public Dialog(String caption) {
		this(caption, true);
	}

	public boolean isCanceled() {
		return canceled;
	}
	
	public void cancel() {
		canceled = true;
	}

	public Button getButtonCancel() {
		return buttonCancel;
	}

	public void setButtonCancel(Button buttonCancel) {
		this.buttonCancel = buttonCancel;
	}

	public void setButtonOk(Button buttonOk) {
		this.buttonOk = buttonOk;
	}

	public void setButtonCancelHandler(ClickHandler handler) {
		this.cancelHandler = handler;
	}

	public Button getButtonOk() {
		return buttonOk;
	}

	public void setButtonOkClickHandler(ClickHandler handler) {
		this.okHandler = handler;
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

	public void showError(Widget w,String message) {
		for (int r = 0, len = table.getRowCount(); r < len; r++) 
			for (int c = 0, len1 = table.getCellCount(r); c < len1; c++) { 
				if(w==table.getWidget(r, c))
					showError(r+1,message);
		}
	}
	
	public void showError(int beforeRow,String message) {
		int rowError = table.insertRow(beforeRow);
		Label l = new Label(message);
		l.setStyleName("errorHint");
		table.getCellFormatter().setHorizontalAlignment(rowError, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.getCellFormatter().setVerticalAlignment(rowError, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		table.setWidget(rowError, 0, l);
		table.getFlexCellFormatter().setColSpan(rowError, 0, 3);
		errorList.add(rowError);
	}

	public void resetErrors() {
		int offs = 0;
		for (int row : errorList ) { 
			table.removeRow(row+offs);
			offs--;
		}
		errorList.clear();
	}
	
	@Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONKEYDOWN:
            	switch (event.getNativeEvent().getKeyCode()) {
				case KeyCodes.KEY_ESCAPE:
					buttonCancel.click();
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

	private void submit() {
		((FormPanel)getWidget()).submit();
	}	

}
