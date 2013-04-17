package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;


public class FormProfile extends Form {

	private PUserWrapper user;

	public FormProfile(RootPanel root, PUserWrapper user) {
		super(root);
		this.user = user;
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setStyleName("gwt-Form");
		initWidget(verticalPanel);
		verticalPanel.setSize("634px", "513px");
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		verticalPanel.add(horizontalPanel);
		horizontalPanel.setWidth("629px");
		
		Label label = new Label("Профиль пользователя");
		horizontalPanel.add(label);
		label.setWidth("370px");
		label.setStyleName("gwt-FormCaption");
		
		Button button = new Button("Выйти");
		button.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				LoginServiceAsync service = GWT.create(LoginService.class);
				service.logout(new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						com.google.gwt.user.client.Window.Location.reload();
					}
					public void onFailure(Throwable caught) {
					}
				});
			}
		});
		horizontalPanel.add(button);
		horizontalPanel.setCellHorizontalAlignment(button, HasHorizontalAlignment.ALIGN_RIGHT);
	}

}
