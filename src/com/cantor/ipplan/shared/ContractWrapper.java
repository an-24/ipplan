package com.cantor.ipplan.shared;

import com.cantor.ipplan.client.ClonableObject;

@SuppressWarnings("serial")
public class ContractWrapper  extends ClonableObject implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable, Cloneable {

    public ContractWrapper clone() {
    	return (ContractWrapper) super.clone(new ContractWrapper());
    }

}
