package com.cantor.ipplan.shared;

import java.util.Date;

import com.cantor.ipplan.client.ClonableObject;


@SuppressWarnings("serial")
public class CustomerWrapper implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable, Cloneable {
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
	public int customerVisible;

    public CustomerWrapper copy() {
    	CustomerWrapper wrap = new CustomerWrapper();
    	wrap.customerId = customerId;
    	wrap.customerName = customerName;
    	wrap.customerLookupKey = customerLookupKey;
    	wrap.customerPrimaryEmail = customerPrimaryEmail;
    	wrap.customerEmails = customerEmails;
    	wrap.customerPrimaryPhone = customerPrimaryPhone;
    	wrap.customerPhones = customerPhones;
    	wrap.customerCompany = customerCompany;
    	wrap.customerBirthday = customerBirthday;
    	wrap.customerPosition = customerPosition;
    	wrap.customerLastupdate = customerLastupdate;
    	wrap.customerVisible = customerVisible; 
    	return wrap;
    }
	
}
