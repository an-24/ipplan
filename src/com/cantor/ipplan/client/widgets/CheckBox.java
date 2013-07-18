package com.cantor.ipplan.client.widgets;

import com.cantor.ipplan.client.UserAgent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

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
		DOM.sinkEvents(getElement(), Event.ONKEYDOWN);
		addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode() == 32) 
				setValue(!(Boolean)getValue());
			}
		});
		if(UserAgent.isIEBrowser())
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    if(getValue()) addStyleName(getStylePrimaryName()+"-checked");
		    		      else removeStyleName(getStylePrimaryName()+"-checked");
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
	
	@Override
	public void setValue(Boolean value) {
	    super.setValue(value);
		if(UserAgent.isIEBrowser())
	    if(value) addStyleName(getStylePrimaryName()+"-checked");
	    	else removeStyleName(getStylePrimaryName()+"-checked");
	}
}
