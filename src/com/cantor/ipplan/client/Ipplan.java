package com.cantor.ipplan.client;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.ValidatorFactory;

import com.cantor.ipplan.shared.HttpStatusText;
import com.cantor.ipplan.db.up.PUser;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.validation.client.impl.Validation;

public class Ipplan implements EntryPoint {

	private static Logger rootLogger = Logger.getLogger("iPPlan");
	
	public void onModuleLoad() {
		// логирование
		login();
		
		/*
		final Button sendButton = new Button("Send");
		final TextBox nameField = new TextBox();
		nameField.setText("GWT User");
		final Label errorLabel = new Label();

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);

		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();

		// Create the popup dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Remote Procedure Call");
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				sendButton.setEnabled(true);
				sendButton.setFocus(true);
			}
		});


		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
		*/
	}
	
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
	
	private static DialogBox configEventBox(String errtext) {
		Button closeButton = null;
		Label textToServerLabel = null;
		final DialogBox eventBox = new DialogBox();
		eventBox.setText("Сообщение сервера");
		eventBox.setAnimationEnabled(true);
		closeButton = new Button("Закрыть");
		closeButton.getElement().setId("closeButton");

		textToServerLabel = new Label();
		textToServerLabel.addStyleName("serverResponseLabelError");

		VerticalPanel dialogVPanel = new VerticalPanel();
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
	
	private void login() {
		LoginServiceAsync service = GWT.create(LoginService.class);
		service.isLogged(new AsyncCallback<PUser>() {
			
			@Override
			public void onSuccess(PUser user) {
				if(user==null) {
					FormLogin f = new FormLogin(RootPanel.get("formContainer"));
					f.show();
				} else {
					FormProfile f = new FormProfile(RootPanel.get("formContainer"),user);
					f.show();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showError(caught);
			}

		});
		
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

	/*
	// Create a handler for the sendButton and nameField
	class MyHandler implements ClickHandler, KeyUpHandler {
		 // Fired when the user clicks on the sendButton.
		public void onClick(ClickEvent event) {
			sendNameToServer();
		}

		// Fired when the user types in the nameField.
		public void onKeyUp(KeyUpEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				sendNameToServer();
			}
		}

		 // Send the name from the nameField to the server and wait for a response.
		
		private void sendNameToServer() {
			// First, we validate the input.
			errorLabel.setText("");
			String textToServer = nameField.getText();
			if (!FieldVerifier.isValidName(textToServer)) {
				errorLabel.setText("Please enter at least four characters");
				return;
			}

			// Then, we send the input to the server.
			sendButton.setEnabled(false);
			textToServerLabel.setText(textToServer);
			serverResponseLabel.setText("");
			greetingService.greetServer(textToServer,
					new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							// Show the RPC error message to the user
							dialogBox
									.setText("Remote Procedure Call - Failure");
							serverResponseLabel
									.addStyleName("serverResponseLabelError");
							serverResponseLabel.setHTML(SERVER_ERROR);
							dialogBox.center();
							closeButton.setFocus(true);
						}

						public void onSuccess(String result) {
							dialogBox.setText("Remote Procedure Call");
							serverResponseLabel
									.removeStyleName("serverResponseLabelError");
							serverResponseLabel.setHTML(result);
							dialogBox.center();
							closeButton.setFocus(true);
						}
					});
		}
	}
	*/
	
}
