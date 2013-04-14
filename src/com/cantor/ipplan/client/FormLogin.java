package com.cantor.ipplan.client;

import javax.validation.ValidatorFactory;

import com.cantor.ipplan.db.up.PUser;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.validation.client.impl.Validation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.SuggestBox;

public class FormLogin extends Form {
	
	private TextBox tbLogin;
	private PasswordTextBox tbPassword;
	private FlexTable flexTable;
	private int rowError =  -1;

	public FormLogin(RootPanel root) {
		super(root);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		initWidget(verticalPanel);
		verticalPanel.setHeight("316px");
		
		Label l1 = new Label("Вход в систему");
		l1.setStyleName("gwt-FormCaption");
		verticalPanel.add(l1);
		
		flexTable = new FlexTable();
		flexTable.setCellSpacing(4);
		flexTable.setCellPadding(10);
		verticalPanel.add(flexTable);
		flexTable.setSize("100%", "195px");
		
		Label l2 = new Label("* Электронная почта или имя");
		flexTable.setWidget(0, 0, l2);
		l2.setWidth("190px");
		
		tbLogin = new TextBox();
		tbLogin.setName("edtLogin");
		tbLogin.setMaxLength(320);
		flexTable.setWidget(0, 1, tbLogin);
		tbLogin.setWidth("331px");
		
		Label l3 = new Label("* Пароль");
		flexTable.setWidget(1, 0, l3);
		
		tbPassword = new PasswordTextBox();
		tbPassword.setName("edtPassword");
		flexTable.setWidget(1, 1, tbPassword);
		flexTable.getCellFormatter().setHeight(1, 1, "");
		tbPassword.setWidth("184px");
		
		CheckBox cb1 = new CheckBox("Запомнить меня");
		flexTable.setWidget(2, 1, cb1);
		flexTable.getCellFormatter().setHeight(2, 1, "");
		flexTable.getCellFormatter().setVerticalAlignment(2, 1, HasVerticalAlignment.ALIGN_TOP);
		flexTable.getCellFormatter().setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		flexTable.getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		Button button = new Button("Войти");
		button.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				
				if(!validate()) return;
				
				LoginServiceAsync service = GWT.create(LoginService.class);
				service.login(tbLogin.getText(), tbPassword.getText(), new AsyncCallback<PUser>() {
					
					public void onSuccess(PUser result) {
						if(result==null) {
							showError(3, "Введен неверный пароль или имя или адрес электронной почты.");
						} else {
							FormProfile f = new FormProfile(FormLogin.this.getRoot(),result);
							f.show();
						}
					}
					
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
				
			}
		});
		
		flexTable.setWidget(3, 0, button);
		button.setWidth("167px");
		verticalPanel.setCellVerticalAlignment(button, HasVerticalAlignment.ALIGN_MIDDLE);
		verticalPanel.setCellHorizontalAlignment(button, HasHorizontalAlignment.ALIGN_CENTER);
		flexTable.getFlexCellFormatter().setColSpan(3, 0, 2);
		
		Hyperlink hlRegister = new Hyperlink("Забыли пароль?", false, "register");
		flexTable.setWidget(4, 0, hlRegister);
		flexTable.getFlexCellFormatter().setColSpan(4, 0, 2);
		flexTable.getCellFormatter().setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_CENTER);
		flexTable.getCellFormatter().setVerticalAlignment(4, 0, HasVerticalAlignment.ALIGN_TOP);
		setStyleName("gwt-Form");
		
		setFirstFocusedWidget(tbLogin);
	}

	protected boolean validate() {
		if(tbLogin.getText().isEmpty()) {
			showError(1, "Имя пользователя или электронный адрес не может быть пустым");
			return false;
		}
		if(tbPassword.getText().isEmpty()) {
			showError(2, "Пароль пользователя не может быть пустым");
			return false;
		}
		return true;
	}

	public void showError(int beforeRow,String message) {
		if(rowError>=0) flexTable.removeRow(rowError);
		rowError = flexTable.insertRow(beforeRow);
		Label l = new Label(message);
		l.setStyleName("serverResponseLabelError");
		flexTable.getCellFormatter().setHorizontalAlignment(rowError, 0, HasHorizontalAlignment.ALIGN_CENTER);
		flexTable.getCellFormatter().setVerticalAlignment(rowError, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		flexTable.setWidget(rowError, 0, l);
		flexTable.getFlexCellFormatter().setColSpan(rowError, 0, 2);
	}

}
