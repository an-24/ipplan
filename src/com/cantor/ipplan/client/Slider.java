package com.cantor.ipplan.client;

import java.util.HashMap;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;

public class Slider extends SimplePanel{

	private Double min = 0.;
	private Double max = 10.;
	private HashMap<Double,String> values = new HashMap<Double, String>();
	
	private Element handler; 
	
	public Slider() {
		super();
	    setStyleName("gwt-Slider");
	    addStyleName("gwt-Slider-Horizontal");
	    handler = DOM.createAnchor();
	    Element div = getElement().appendChild(DOM.createDiv());
	    div.appendChild(handler);
	    addDomHandler(new MouseDownHandler(){

			@Override
			public void onMouseDown(MouseDownEvent event) {
				// TODO Auto-generated method stub
				
			}
	    	
	    }, MouseDownEvent.getType());	    
	}
	
	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public HashMap<Double, String> getValues() {
		return values;
	}
	
	public void setValue(Double value,String label) throws Exception {
		if(value>max || value<min) 
			throw new Exception("Invalid value "+value);
		values.put(value, label);
	}
	
	public void setPosition(Double value) {
		if(value>max || value<min) return; 
		handler.getStyle().setLeft(100*value/(max-min),Unit.PCT);
	}


}
