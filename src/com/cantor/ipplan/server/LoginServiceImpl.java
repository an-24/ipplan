package com.cantor.ipplan.server;

import javax.servlet.http.HttpSession;

import com.cantor.ipplan.client.LoginService;
import com.cantor.ipplan.db.up.PUser;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet  implements LoginService {

	@Override
	public PUser login(String nameOrEmail, String pswd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PUser isLogged() {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		if (sess.isNew()) return null;
		return (PUser) sess.getAttribute("user");
	}

}
