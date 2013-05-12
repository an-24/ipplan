package com.cantor.ipplan.client;

import com.google.gwt.user.client.ui.DoubleBox;

public class CurrencyBox extends DoubleBox {
	
	public CurrencyBox(Integer v) {
		super();
		setValue(v==null?0:v.intValue()/100.0);
		setStyleName("gwt-CurrencyBox");
	}

}
