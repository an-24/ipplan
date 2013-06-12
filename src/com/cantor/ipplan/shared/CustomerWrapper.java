package com.cantor.ipplan.shared;

import java.util.Date;

import com.cantor.ipplan.client.ClonableObject;


@SuppressWarnings("serial")
public class CustomerWrapper extends ClonableObject  implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable, Cloneable {
	public int customerId;
	public String customerName;
	public String customerLookupKey;
	public String customerPrimaryEmail;
	public String customerEmails;
	public String customerPrimaryPhone;
	public String customerPhones;
	public String customerCompany;
	public Date customerBirthday;
	public String customerPosition;
	public Date customerLastupdate;

    public CustomerWrapper clone() {
    	return (CustomerWrapper) super.clone(new CustomerWrapper());    	
    }
	
}
