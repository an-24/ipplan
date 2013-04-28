package com.cantor.ipplan.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cantor.ipplan.shared.HttpStatusText;
import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Ipplan implements EntryPoint, ValueChangeHandler<String>  {

	private static Logger rootLogger = Logger.getLogger("iPPlan");
	static String INIT_TOKEN = "";
    protected static Map<String, Class> tokenForms = new HashMap<String, Class>();
	
	public void onModuleLoad() {
		String initToken = History.getToken();
		if(initToken.isEmpty()) 
			History.newItem(INIT_TOKEN);
		History.addValueChangeHandler(this);
		History.fireCurrentHistoryState();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		showForm(token);
	}
	
	public void showForm(String token) {
		refreshForm(tokenForms.get(token));
	}
	
	public void refreshForm(final Class type) {
	};
	
	public static void log(Level l, String message) {
		rootLogger.log(l,message);
	}
	
	public static void info(String message) {
		rootLogger.log(Level.INFO,message);
	}

	public static void warning(String message) {
		rootLogger.log(Level.WARNING,message);
	}
	
	public static void error(String message) {
		rootLogger.log(Level.SEVERE,message);
	}

	public static void error(Throwable e) {
		rootLogger.log(Level.SEVERE,e.getMessage(),e);
	}

	public static void error(String message, Throwable e) {
		rootLogger.log(Level.SEVERE,message,e);
	}
	
	protected RootPanel getRootInHTML() {
		return RootPanel.get("formContainer");
	}

	private static DialogBox configEventBox(String errtext) {
		Button closeButton = null;
		Label textToServerLabel = null;
		final DialogBox eventBox = new Dialog("Сообщение сервера");
		closeButton = new Button("Закрыть");
		closeButton.getElement().setId("closeButton");

		textToServerLabel = new Label();
		textToServerLabel.addStyleName("serverResponseLabelError");

		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.setWidth("500px");
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>С сервера пришло сообщение:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		eventBox.setWidget(dialogVPanel);
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				eventBox.hide();
			}
		});
			
		textToServerLabel.setText(errtext);
		closeButton.setFocus(true);
		return eventBox;
	}

	private static DialogBox configEventBox(String errtext, ClickHandler click) {
		DialogBox eventBox = configEventBox(errtext);
		VerticalPanel panel = (VerticalPanel) eventBox.getWidget();
		Button closeButton = (Button) panel.getWidget(2);
		closeButton.addClickHandler(click);
		return eventBox;
		
	}
	
	public static void showError(Throwable e) {
		String s = e.getMessage();
		if (e instanceof StatusCodeException) {
			int code = ((StatusCodeException) e).getStatusCode();
			s = "Ошибка сети. Код "+code+":"+HttpStatusText.get(code);
		};	
		DialogBox eventBox = configEventBox(s);
		eventBox.center();
	}



	
}
