package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.text.client.IntegerRenderer;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.CaptionPanel;


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
		lblUser.setText(user.getFullName());
		p3.add(lblUser);
		p3.setCellVerticalAlignment(lblUser, HasVerticalAlignment.ALIGN_MIDDLE);
		
		Label l2 = new Label(", последний раз был: ");
		p3.add(l2);
		p3.setCellVerticalAlignment(l2, HasVerticalAlignment.ALIGN_MIDDLE);
		
		Label lbLastaccess = new Label(DateTimeFormat.getMediumDateTimeFormat().format(user.puserLastaccess));
		p3.add(lbLastaccess);
		p3.setCellVerticalAlignment(lbLastaccess, HasVerticalAlignment.ALIGN_MIDDLE);
		
		user.puserLock=1;
		user.puserLockReason = "просрочена оплата";
		
		CaptionPanel pMessage = new CaptionPanel("Вам сообщение");
		pMessage.setStyleName("messageToUser");
		p0.add(pMessage);

		if(user.puserLock!=0) {
			HTML htm = new HTML("Аккаунт временно заблокирован. Причина: "+user.puserLockReason);
			pMessage.add(htm);
		} else
			pMessage.setVisible(false);
		
		VerticalPanel p2 = new VerticalPanel();
		p0.add(p2);
		p2.setSize("100%", "45px");
		
		Button btnOpenDB = new Button("Открыть базу данных");
		btnOpenDB.setEnabled(user.puserLock!=0);
		p2.add(btnOpenDB);
		btnOpenDB.setWidth("162px");
		p2.setCellVerticalAlignment(btnOpenDB, HasVerticalAlignment.ALIGN_MIDDLE);
		p2.setCellHorizontalAlignment(btnOpenDB, HasHorizontalAlignment.ALIGN_CENTER);
		
		TabPanel tabPanel = new TabPanel();
		tabPanel.setAnimationEnabled(true);
		p0.add(tabPanel);
		tabPanel.setSize("100%", "434px");
		
		FlexTable Tabl1 = new FlexTable();
		Tabl1.setCellSpacing(4);
		Tabl1.setCellPadding(10);
		tabPanel.add(Tabl1, "Общие сведения", false);
		Tabl1.setSize("100%", "3cm");
		
		Label label = new Label("Дата создания");
		Tabl1.setWidget(0, 0, label);
		
		Label lDateCreated = new Label(DateTimeFormat.getMediumDateFormat().format(user.puserCreated));
		lDateCreated.setStyleName("gwt-TextBox");
		Tabl1.setWidget(0, 1, lDateCreated);
		lDateCreated.setWidth("132px");
		
		Label l3 = new Label("Имя пользователя");
		Tabl1.setWidget(1, 0, l3);
		
		TextBox tbName = new TextBox();
		Tabl1.setWidget(1, 1, tbName);
		tbName.setWidth("300px");
		tbName.setText(user.puserLogin);
		
		Label l4 = new Label("* Адрес электронной почты");
		Tabl1.setWidget(2, 0, l4);
		
		TextBox tbEmail = new TextBox();
		Tabl1.setWidget(2, 1, tbEmail);
		tbEmail.setWidth("300px");
		tbEmail.setText(user.puserEmail);
		
		CheckBox cbBoss = new CheckBox("Босс-аккаунт");
		Tabl1.setWidget(3, 1, cbBoss);
		cbBoss.setChecked(user.puserBoss!=0);
		
		Label l5 = new Label("Вам подчиняются");
		Tabl1.setWidget(4, 0, l5);
		
		ListBox lbChildren = new ListBox();
		Tabl1.setWidget(4, 1, lbChildren);
		lbChildren.setWidth("300px");
		lbChildren.setVisibleItemCount(5);
		if(user.children!=null) 
		for (PUserWrapper child : user.children) {
			lbChildren.addItem(child.getFullName());
		}
		
		
		Label l6 = new Label("Вы подчинены");
		Tabl1.setWidget(5, 0, l6);
		
		Label lOwner = new Label(" ");
		lOwner.setStyleName("gwt-TextBox");
		Tabl1.setWidget(5, 1, lOwner);
		lOwner.setWidth("234px");
		if(user.owner==null) lOwner.setText("никому"); else
			lOwner.setText(user.getFullName());
		
		Button btnSave = new Button("Сохранить изменения");
		Tabl1.setWidget(6, 0, btnSave);
		Tabl1.getFlexCellFormatter().setColSpan(6, 0, 2);
		Tabl1.getCellFormatter().setHorizontalAlignment(6, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		FlexTable Tab2 = new FlexTable();
		tabPanel.add(Tab2, "Оплата сервиса", false);
		Tab2.setSize("100%", "3cm");
		
		FlexTable Tab3 = new FlexTable();
		tabPanel.add(Tab3, "Сменить пароль", false);
		Tab3.setSize("100%", "3cm");
		
		FlexTable Tab4 = new FlexTable();
		tabPanel.add(Tab4, "Синхронизация", false);
		Tab4.setSize("100%", "3cm");
		
		tabPanel.getTabBar().selectTab(0);
	}

}
