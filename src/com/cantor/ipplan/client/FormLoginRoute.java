package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.client.History;

public class FormLoginRoute extends Form {

	public static void route(PUserWrapper user) {
		if(!user.canEnterService()) {
			History.newItem("profile");
		} else {
			FormProfile.enterService();
		}
	}
}
