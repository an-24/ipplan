package com.cantor.ipplan.client;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

public class CheckBox extends com.google.gwt.user.client.ui.CheckBox {


	public CheckBox() {
		super();
		setKeyEvents();
	}

	public CheckBox(String label) {
		super(label);
		setKeyEvents();
	}
	
	
	
	private void setKeyEvents() {
		addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode() == 32) 
				setValue(!(Boolean)getValue());
			}
		});
	}

	@Override
	public int getTabIndex() {
		return getElement().getTabIndex();
	}
	
	@Override
	public void setTabIndex(int index) {
		getElement().setTabIndex(index);
    }
	
}
