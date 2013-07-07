package com.cantor.ipplan.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cantor.ipplan.shared.HttpStatusText;
import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.cantor.ipplan.client.widgets.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.cantor.ipplan.client.widgets.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

public class Ipplan implements EntryPoint, ValueChangeHandler<String>  {

	public static String USER_AGENT;
	public static boolean USER_AGENT_IPHONE;
	public static DefaultFormat DEFAULT_DATE_FORMAT;
	public static DefaultFormat DEFAULT_DATETIME_FORMAT;
	public static DefaultFormat ALTERNATE_DATETIME_FORMAT;

	private static Logger rootLogger = Logger.getLogger("iPPlan");
	static String INIT_TOKEN = "";
    protected static Map<String, Class> tokenForms = new HashMap<String, Class>();
	private static Stack<Dialog> activeDialogs = new Stack<Dialog>();
	
    public Ipplan() {
    	super();
    	USER_AGENT = Form.getUserAgent();
    	USER_AGENT_IPHONE = USER_AGENT.indexOf("iPhone")>=0;
    	DEFAULT_DATE_FORMAT = new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy"));
    	DEFAULT_DATETIME_FORMAT = new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy, HH:mm"));
    	ALTERNATE_DATETIME_FORMAT = new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd MMMM yyyy, HH:mm"));
		init();
    }
    
	public void onModuleLoad() {
		String initToken = History.getToken();
		if(initToken.isEmpty()) 
			History.newItem(INIT_TOKEN,false);
		History.fireCurrentHistoryState();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		showForm(token);
	}
	
	public void init() {
		History.addValueChangeHandler(this);
	}
	
	public void showForm(String token) {
		String id =  null;
		int n = token.indexOf(',');
		if(n>=0) {
			String addParam = token.substring(n+1);
			token = token.substring(0, n);
			if(addParam.startsWith("session")) {
				id = addParam.split("=")[1];
			}
		}
		refreshForm(tokenForms.get(token),id);
	}
	
	public void refreshForm(final Class type, String session_id) {
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

	private static Dialog configEventBox(String errtext) {
		Button closeButton = null;
		Label textToServerLabel = null;
		final Dialog eventBox = new Dialog("Сообщение сервера",true);
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

	private static Dialog configContinueConfirmationBox(String text,ClickHandler ok) {
		final Dialog eventBox = new Dialog("Требуется подтверждение",true);
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.setWidth("300px");
		dialogVPanel.setSpacing(5);
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML(text));
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		HorizontalPanel hp = new HorizontalPanel();
		hp.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		hp.setSpacing(5);
		
		dialogVPanel.add(hp);
		Button cancelButton = new Button("Отмена");
		eventBox.setButtonCancel(cancelButton);
		eventBox.setWidget(dialogVPanel);
		
		Button okButton = new Button("Да");
		okButton.addClickHandler(ok);

		hp.add(okButton);
		hp.add(cancelButton);
		
		cancelButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				eventBox.hide();
			}
		});
		return eventBox;
	}
	
	private static Dialog configSaveConfirmationBox(String text,ClickHandler ok,ClickHandler withoutSave) {
		final Dialog eventBox = new Dialog("Требуется подтверждение",true);
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.setWidth("300px");
		dialogVPanel.setSpacing(5);
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML(text));
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		HorizontalPanel hp = new HorizontalPanel();
		hp.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		hp.setSpacing(5);
		
		dialogVPanel.add(hp);
		Button cancelButton = new Button("Отмена");
		eventBox.setButtonCancel(cancelButton);
		eventBox.setWidget(dialogVPanel);
		
		Button withoutSaveButton = new Button("Не сохранять");
		withoutSaveButton.addClickHandler(withoutSave);
		
		Button okButton = new Button("Сохранить");
		okButton.addClickHandler(ok);

		hp.add(okButton);
		hp.add(withoutSaveButton);
		hp.add(cancelButton);
		
		cancelButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				eventBox.hide();
			}
		});
		return eventBox;
	}
	
	public static void showError(Throwable e) {
		String s = e.getMessage();
		if (e instanceof StatusCodeException) {
			int code = ((StatusCodeException) e).getStatusCode();
			s = "Ошибка сети. Код "+code+":"+HttpStatusText.get(code);
		};	
		Dialog box = configEventBox(s);
		activeDialogs.push(box);
		box.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				activeDialogs.pop();
			}
		});
		box.center();
	}

	public static void showError(String s) {
		Dialog box = configEventBox(s);
		activeDialogs.push(box);
		box.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				activeDialogs.pop();
			}
		});
		box.center();
	}
	
	public static Dialog showContinueConfirmation(String text, ClickHandler ok) {
		Dialog box = configContinueConfirmationBox(text,ok);
		activeDialogs.push(box);
		box.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				activeDialogs.pop();
			}
		});
		box.center();
		return box;
	}
	
	
	public static Dialog showSaveConfirmation(String text, ClickHandler ok,ClickHandler withoutSave) {
		Dialog box = configSaveConfirmationBox(text,ok,withoutSave);
		activeDialogs.push(box);
		box.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				activeDialogs.pop();
			}
		});
		box.center();
		return box;
	}

	public static DialogBox getActiveDialog() {
		return activeDialogs.peek();
	}


	public static String getPhoneLink(String phstr) {
		String phone,phonedisplay;
		String tolink = "callto:";
		if(USER_AGENT_IPHONE) tolink = "tel:";
		phonedisplay = phstr;
		String[] phonecomp = phstr.split(":");
		if(phonecomp.length>1) {
			phone = phonecomp[1].replaceAll("\\s|[()-]","");
		} else {
			phone = phonecomp[0].replaceAll("\\s|[()-]","");
		}
		return "<a href=\""+tolink+phone+"\">"+phonedisplay+"</a>";
	}

	
}
