package com.cantor.ipplan.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

public class RadioButton extends com.google.gwt.user.client.ui.RadioButton {

	public RadioButton(String name) {
		super(name);
		setKeyEvents();
	}
	
	public RadioButton(String name, String label) {
		super(name,label);
		setKeyEvents();
	}

	private void setKeyEvents() {
		DOM.sinkEvents(getElement(), Event.ONKEYDOWN);
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
	
	public Element getLabelElement() {
		return (Element) getElement().getChild(1);
	}

	public Element getInputElement() {
		return (Element) getElement().getChild(0);
	}
}
