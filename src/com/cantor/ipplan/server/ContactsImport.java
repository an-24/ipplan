package com.cantor.ipplan.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;

public class ContactsImport {
	
	public static final int NO_AUTH_TOKEN = 1;
	
	private ContactsService service;
	private int lasterr = 0;
	private OAuthToken token;

	public ContactsImport(OAuthToken token) throws AuthenticationException {
		this.token = token;
	    service = new ContactsService("IpplanSyncGoogle");
	    service.setAuthSubToken(token.getValue());
	}
	
	public List<ContactEntry> getAllEntrys() throws Exception{
		return getAllEntrys(null);
	}

	public List<ContactEntry> getAllEntrys(Date lastSync) throws Exception{
		lasterr = 0;
		URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
		Query q = new Query(feedUrl);
		q.setMaxResults(Integer.MAX_VALUE);
		if(lastSync!=null) 
			q.setUpdatedMin(new DateTime(lastSync));
		
		ContactFeed resultFeed = null;
		try {
			resultFeed = service.query(q, ContactFeed.class);
			return resultFeed.getEntries();
		} catch (Exception e) {
			if(!new OAuthService().validateToken(token));
				lasterr = NO_AUTH_TOKEN;
		}
		// return empty list
		return new ArrayList<ContactEntry>();
	}
	
	public int getLastError() {
		return this.lasterr;
	}
	
	public ContactsService getService() {
		return service;
	}

}
