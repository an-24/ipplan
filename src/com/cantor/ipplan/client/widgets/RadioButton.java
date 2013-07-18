package com.cantor.ipplan.client.widgets;

import com.cantor.ipplan.client.UserAgent;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
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

	public RadioButton(String name, SafeHtml label) {
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
		if(UserAgent.isIEBrowser())
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dropChecked(Document.get());
			    if(getValue()) addStyleName(getStylePrimaryName()+"-checked");
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

	public void click() {
		setValue(true);
		this.fireEvent(new GwtEvent<ClickHandler>(){

			@Override
			protected void dispatch(ClickHandler handler) {
				handler.onClick(null);
			}

			@Override
			public com.google.gwt.event.shared.GwtEvent.Type<ClickHandler> getAssociatedType() {
				return ClickEvent.getType();
			}
			
		});
		
	}

	@Override
	public void setValue(Boolean value) {
	    super.setValue(value);
		if(UserAgent.isIEBrowser()) {
			dropChecked(Document.get());
	    	if(value) addStyleName(getStylePrimaryName()+"-checked");
		}	
	}
	
	private void dropChecked(Node root){
		NodeList<Node> ndl = root.getChildNodes();
		for (int i = 0, len =ndl.getLength(); i < len; i++) {
			Node nd = ndl.getItem(i);
			if(nd.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element)nd;
				String name = el.getAttribute("name");
				if(name!=null && name.equals(this.getName())) {
					Element span = el.getParentElement();
					span.removeClassName(getStylePrimaryName()+"-checked");
				}	
				if(el.hasChildNodes())
					dropChecked(el);
			}
		}	
			
	}
}
