package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.ContractWrapper;
import com.google.gwt.user.client.ui.InlineHTML;

public class ContractBox extends InlineHTML {
	ContractWrapper contract;
	
	public ContractBox(ContractWrapper contract) {
		super(contract==null?"<договор>":"?");
		this.contract = contract;  
		setStyleName("link");
	}

	public ContractWrapper getContract() {
		return contract;
	}

	public void setContract(ContractWrapper contract) {
		this.contract = contract;
	}
	
	

}
