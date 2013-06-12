package com.cantor.ipplan.server;

import java.net.URL;
import java.util.List;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.util.AuthenticationException;

public class ContactsImport {
	
	private ContactsService service;

	public ContactsImport(String user, String password) throws AuthenticationException {
	    service = new ContactsService("IpplanSyncGoogle");
	    service.setUserCredentials(user, password);
	}
	
	public List<ContactEntry> getAllEntrys() throws Exception{
		URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
		//ContactFeed resultFeed = service.getFeed(feedUrl, ContactFeed.class);
		Query q = new Query(feedUrl);
		q.setMaxResults(Integer.MAX_VALUE);
		ContactFeed resultFeed = service.query(q, ContactFeed.class);
		return resultFeed.getEntries();
	}

}
