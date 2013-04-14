package com.cantor.ipplan.client;
import com.cantor.ipplan.db.up.PUser;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Label;


public class FormProfile extends Form {

	private PUser user;

	public FormProfile(RootPanel root, PUser user) {
		super(root);
		this.user = user;
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setStyleName("gwt-Form");
		initWidget(verticalPanel);
		verticalPanel.setSize("634px", "513px");
		
		Label label = new Label("Профиль пользователя");
		label.setStyleName("gwt-FormCaption");
		verticalPanel.add(label);
	}

}
