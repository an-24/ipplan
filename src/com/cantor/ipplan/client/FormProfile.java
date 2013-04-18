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
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.InlineLabel;


public class FormProfile extends Form {

	private PUserWrapper user;

	public FormProfile(RootPanel root, PUserWrapper user) {
		super(root);
		this.user = user;
		
		VerticalPanel p0 = new VerticalPanel();
		p0.setStyleName("gwt-Form");
		initWidget(p0);
		p0.setSize("634px", "513px");
		
		HorizontalPanel p1 = new HorizontalPanel();
		p1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		p0.add(p1);
		p1.setWidth("629px");
		
		Label l1 = new Label("Профиль пользователя");
		p1.add(l1);
		l1.setWidth("325px");
		l1.setStyleName("gwt-FormCaption");
		
		Button btnExit = new Button("Выйти");
		btnExit.addClickHandler(new ClickHandler() {
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
		p1.add(btnExit);
		p1.setCellHorizontalAlignment(btnExit, HasHorizontalAlignment.ALIGN_RIGHT);
		
		HorizontalPanel p3 = new HorizontalPanel();
		p0.add(p3);
		p3.setHeight("");
		
		Label lblUser = new Label("user23");
		lblUser.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		lblUser.addStyleName("lpad10");
		lblUser.addStyleName("bold-text");
		lblUser.setWordWrap(false);
		lblUser.setText("user");
		p3.add(lblUser);
		p3.setCellVerticalAlignment(lblUser, HasVerticalAlignment.ALIGN_MIDDLE);
		
		Label l2 = new Label(", последний раз был: ");
		p3.add(l2);
		p3.setCellVerticalAlignment(l2, HasVerticalAlignment.ALIGN_MIDDLE);
		
		DateLabel lbLastaccess = new DateLabel();
		lbLastaccess.setValue(user.getLastAccess());
		p3.add(lbLastaccess);
		p3.setCellVerticalAlignment(lbLastaccess, HasVerticalAlignment.ALIGN_MIDDLE);
		
		VerticalPanel p2 = new VerticalPanel();
		p0.add(p2);
		p2.setSize("100%", "45px");
		
		Button btnOpenDB = new Button("Открыть базу данных");
		p2.add(btnOpenDB);
		btnOpenDB.setWidth("162px");
		p2.setCellVerticalAlignment(btnOpenDB, HasVerticalAlignment.ALIGN_MIDDLE);
		p2.setCellHorizontalAlignment(btnOpenDB, HasHorizontalAlignment.ALIGN_CENTER);
		
		TabPanel tabPanel = new TabPanel();
		tabPanel.setAnimationEnabled(true);
		p0.add(tabPanel);
		tabPanel.setSize("100%", "434px");
		
		FlexTable Tabl1 = new FlexTable();
		tabPanel.add(Tabl1, "Общие сведения", false);
		Tabl1.setSize("100%", "3cm");
		
		FlexTable Tab2 = new FlexTable();
		tabPanel.add(Tab2, "Оплата сервиса", false);
		Tab2.setSize("100%", "3cm");
		
		FlexTable Tab3 = new FlexTable();
		tabPanel.add(Tab3, "Сменить пароль", false);
		Tab3.setSize("100%", "3cm");
		
		FlexTable Tab4 = new FlexTable();
		tabPanel.add(Tab4, "Синхронизация", false);
		Tab4.setSize("100%", "3cm");
	}

}
