package com.cantor.ipplan.client;

import java.util.Date;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
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
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;

public class FormLogin extends Form {

	private TextBox tbLogin;
	private PasswordTextBox tbPassword;
	private FlexTable flexTable;
	private int rowError =  -1;

	public FormLogin(Ipplan main, RootPanel root) {
		super(main,root);
		final FormPanel form = new FormPanel();
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);
		form.setWidth("600px");
		form.setAction("/login");
		
		initWidget(form);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		form.setWidget(verticalPanel);
		verticalPanel.setSize("598px", "316px");
		setStyleName("gwt-Form");
		
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		verticalPanel.add(horizontalPanel);
		horizontalPanel.setWidth("100%");
		
		Label l1 = new Label("Вход в систему");
		horizontalPanel.add(l1);
		l1.setSize("328px", "100%");
		l1.setStyleName("gwt-FormCaption");
		
		InlineHyperlink inlineHyperlink = new InlineHyperlink("Зарегистрироваться", false, "register");
		horizontalPanel.add(inlineHyperlink);
		horizontalPanel.setCellHorizontalAlignment(inlineHyperlink, HasHorizontalAlignment.ALIGN_RIGHT);
		horizontalPanel.setCellVerticalAlignment(inlineHyperlink, HasVerticalAlignment.ALIGN_MIDDLE);
		
		flexTable = new FlexTable();
		flexTable.setCellSpacing(4);
		flexTable.setCellPadding(10);
		verticalPanel.add(flexTable);
		flexTable.setSize("100%", "195px");
		
		Label l2 = new Label("* Электронная почта или имя");
		flexTable.setWidget(0, 0, l2);
		l2.setWidth("190px");
		
		tbLogin = new TextBox();
		tbLogin.setName("userid");
		tbLogin.setMaxLength(320);
		flexTable.setWidget(0, 1, tbLogin);
		tbLogin.setWidth("331px");
		
		String remlogin = Cookies.getCookie("userid");
		if(remlogin!=null)
			tbLogin.setText(remlogin);
		
		Label l3 = new Label("* Пароль");
		flexTable.setWidget(1, 0, l3);
		
		tbPassword = new PasswordTextBox();
		tbPassword.setName("password");
		flexTable.setWidget(1, 1, tbPassword);
		flexTable.getCellFormatter().setHeight(1, 1, "");
		tbPassword.setWidth("184px");
		
		final CheckBox cb1 = new CheckBox("Запомнить меня");
		cb1.setName("rememberme");
		if(remlogin!=null)
			cb1.setValue(true);
		
		flexTable.setWidget(2, 1, cb1);
		flexTable.getCellFormatter().setHeight(2, 1, "");
		flexTable.getCellFormatter().setVerticalAlignment(2, 1, HasVerticalAlignment.ALIGN_TOP);
		flexTable.getCellFormatter().setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		flexTable.getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		Button button = new Button("Войти");
		button.getElement().setAttribute("type", "submit");
		button.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				//form.submit();
			}
		});
		
		form.addSubmitHandler(new SubmitHandler() {
			public void onSubmit(SubmitEvent event) {
				event.cancel();
				
				if(cb1.getValue()!=null && cb1.getValue()) {
					Date now = new Date();
					long nowLong = now.getTime();
					nowLong = nowLong + (1000 * 60 * 60 * 24 * 7);//seven days
					now.setTime(nowLong);
					Cookies.setCookie("userid", tbLogin.getText(), now);
				}
				
				if(!validate()) return;
				
				LoginServiceAsync service = GWT.create(LoginService.class);
				service.login(tbLogin.getText(), tbPassword.getText(), "WBC", new AsyncCallback<PUserWrapper>() {
					
					public void onSuccess(PUserWrapper result) {
						if(result==null) {
							showError(3, "Введен неверный пароль или имя или адрес электронной почты.");
						} else {
							FormProfile f = new FormProfile(getMain(), FormLogin.this.getRoot(),result);
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
		
		Hyperlink hlRegister = new Hyperlink("Забыли пароль?", false, "remember");
		flexTable.setWidget(4, 0, hlRegister);
		flexTable.getFlexCellFormatter().setColSpan(4, 0, 2);
		flexTable.getCellFormatter().setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_CENTER);
		flexTable.getCellFormatter().setVerticalAlignment(4, 0, HasVerticalAlignment.ALIGN_TOP);

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
