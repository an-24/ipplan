package com.cantor.ipplan.shared;


@SuppressWarnings("serial")
public class CustomerWrapper  implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {
	public int customerId;
	public String customerName;
	public String customerLookupKey;
	
	public CustomerWrapper copy() {
		CustomerWrapper wrap = new CustomerWrapper(); 
		wrap.customerId = customerId; 
		wrap.customerName = customerName; 
		wrap.customerLookupKey = customerLookupKey; 
		return wrap;
	}
}
