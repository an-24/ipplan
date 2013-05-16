package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.CustomerWrapper;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.ValueBoxBase;

public class CustomerBox extends SuggestBox {
	
	private CustomerWrapper customer =  null;

	public CustomerBox() {
		super();
	}

	public CustomerBox(SuggestOracle oracle, ValueBoxBase<String> box,
			SuggestionDisplay suggestDisplay) {
		super(oracle, box, suggestDisplay);
	}

	public CustomerBox(SuggestOracle oracle, ValueBoxBase<String> box) {
		super(oracle, box);
	}

	public CustomerBox(SuggestOracle oracle) {
		super(oracle);
	}

	public CustomerWrapper getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerWrapper customer) {
		this.customer = customer;
	}
}
