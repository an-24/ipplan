package com.cantor.ipplan.server;

import java.util.HashMap;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

import com.cantor.ipplan.db.up.PUser;

public class IpplanSessionListener implements HttpSessionAttributeListener {

	private static HashMap<String,PUser> authSessions = new HashMap();
	
	@Override
	public void attributeAdded(HttpSessionBindingEvent e) {
		if(e.getName().equals("user"));
			authSessions.put(e.getSession().getId(), (PUser) e.getValue());
	}

	@Override
	public void attributeRemoved(HttpSessionBindingEvent e) {
		if(e.getName().equals("user"));
			authSessions.remove(e.getSession().getId());
	}

	@Override
	public void attributeReplaced(HttpSessionBindingEvent e) {
		if(e.getName().equals("user"));
			authSessions.put(e.getSession().getId(), (PUser) e.getValue());
	}
	
	
	public static PUser getAuthUserBySession(String id) {
		return authSessions.get(id);
	}


}
