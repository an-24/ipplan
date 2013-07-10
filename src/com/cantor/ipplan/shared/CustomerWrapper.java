package com.cantor.ipplan.shared;

import java.util.Date;
import com.cantor.ipplan.client.Ipplan;


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
	public String customerPhoto;

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
    	wrap.customerPhoto =customerPhoto;
    	return wrap;
    }

	public String getPrimaryEmail(boolean linked) {
		String s = (customerPrimaryEmail!=null)?customerPrimaryEmail:
					(customerEmails!=null)?customerEmails.split(",")[0]:null;
		if(s!=null && linked) s = "<a href=\"mailto:"+s+"\">"+s+"</a>";	
		return s;	
	}

	public String getPrimaryPhone(boolean linked) {
		String s = (customerPrimaryPhone!=null)?customerPrimaryPhone:
			(customerPhones!=null)?customerPhones.split(",")[0]:null;
		if(s!=null && linked) s = Ipplan.getPhoneLink(s);	
		return s;	
	}
	
	public String getEmails(boolean linked) {
		if(customerPrimaryEmail!=null && !linked) return customerEmails;
		if(customerEmails==null) return null;
		// first стал Primary
		String s="";
		String[] ems = customerEmails.split(",");
		for (int i = (customerPrimaryEmail!=null)?0:1; i < ems.length; i++) {
			if(i>1) s+=',';
			if(linked) s+="<a href=\"mailto:"+ems[i]+"\">"+ems[i]+"</a>";
				  else s+=ems[i];
		}
		return s.isEmpty()?null:s;
	}

	public String getPhones(boolean linked) {
		if(customerPrimaryPhone!=null && !linked) return customerPhones;
		if(customerPhones==null) return null;
		// first стал Primary
		String s="";
		String[] ems = customerPhones.split(",");
		for (int i = (customerPrimaryPhone!=null)?0:1; i < ems.length; i++) {
			if(i>1) s+=',';
			if(linked) s+=Ipplan.getPhoneLink(ems[i]);
				  else s+=ems[i];
		}
		return s.isEmpty()?null:s;
	}
}
