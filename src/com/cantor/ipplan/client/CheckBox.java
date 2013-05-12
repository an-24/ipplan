package com.cantor.ipplan.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
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
		// получим labelElement
		/*
		Node labelElement = getElement().getChild(1);
		//labelElement.setTabIndex(-1); // нужно для того, чтобы обрабатывались нажатия клавишь
		*/
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
