package com.cantor.ipplan.client.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.cantor.ipplan.client.widgets.Slider.PointValue;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;

public class Slider extends SimplePanel{

	private Double min = 0.;
	private Double max = 10.;
	private List<PointValue> values = new ArrayList<PointValue>();
	
	private Element handler;
	private boolean draging;
	private int offsX; 
	private boolean mouseDownFlag;
	
	private ChangeEvent changePositionEvent;
	private String lmin;
	private String lmax;

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
				int x = event.getRelativeX(Slider.this.handler);
				if(x>0 && x<handler.getClientWidth()) {
					mouseDownFlag = true;
					event.preventDefault();
				};
			}
	    }, MouseDownEvent.getType());
	    
	    addDomHandler(new MouseUpHandler(){

			@Override
			public void onMouseUp(MouseUpEvent event) {
				int x = event.getX();
				if(draging) stopDraging(x); else
					setPositionPx(x);
				mouseDownFlag = false;
			}
	    	
	    }, MouseUpEvent.getType());
	    
	    addDomHandler(new MouseMoveHandler(){

			@Override
			public void onMouseMove(MouseMoveEvent event) {
				int x = event.getX();
				if(draging) {
					drag(x);
					event.stopPropagation();
				} else
					if(mouseDownFlag) {
						startDraging(event.getRelativeX(Slider.this.handler));
						mouseDownFlag = false;
						event.stopPropagation();
					}
			}
	    	
	    }, MouseMoveEvent.getType());
	}
	
	protected void startDraging(int offs) {
		draging = true; 
		offsX = offs;
		DOM.setCapture(getElement());
	}

	protected void stopDraging(int x) {
		draging = false;
		DOM.releaseCapture(getElement());
		setPositionPx(x-offsX);
	}

	protected void cancelDraging() {
		draging = false; 
	}
	
	protected void drag(int x) {
		if(x<0) {
			handler.getStyle().setLeft(0,Unit.PCT);
			x = 0;
		} else
		if(x>getOffsetWidth()) {
			handler.getStyle().setLeft(100,Unit.PCT);
			x = getOffsetWidth();
		} else
		handler.getStyle().setLeft(x-offsX,Unit.PX);

		if(changePositionEvent!=null) {
			double percent = (x+0.0)/getOffsetWidth();
			PointValue pv = oncoming(min+(max-min)*percent);
			changePositionEvent.onDragPosition(pv.value, pv.label);
		}
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min, String label) {
		this.min = min;
		this.lmin = label;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max, String label) {
		this.max = max;
		this.lmax = label;
	}

	public HashMap<String, Double> getValues() {
		HashMap<String, Double> hm = new HashMap<String, Double>();
		for (PointValue pv : values) {
			hm.put(pv.label, pv.value);
		}
		return hm;
	}

	public void setValues(Double[] value,String[] label) throws Exception {
		for (int i = 0, len = value.length; i < len; i++) {
			setValue(value[i],label[i]);
		}
		Collections.sort(values);
	}
	
	private void setValue(Double value,String label) throws Exception {
		if(value>max || value<min) 
			throw new Exception("Invalid value "+value);
		values.add(new PointValue(value, label));
	}
	
	public void setPosition(Double value) {
		if(value>max || value<min) return;
		PointValue pv = oncoming(value);
		handler.getStyle().setLeft(100*(pv.value-min)/(max-min),Unit.PCT);
		if(changePositionEvent!=null)
			changePositionEvent.onChangePosition(pv.value, pv.label);
	}

	private void setPositionPx(int x) {
		if(x<0) {
			setPosition(min);
			return;
		}
		if(x>getOffsetWidth()) {
			setPosition(max);
			return;
		}
		double percent = (x+0.0)/getOffsetWidth();
		setPosition(min+(max-min)*percent);
	}

	private PointValue oncoming(double value) {
		if(values.size()==0) return new PointValue(value);
		//nearest
		PointValue prev = new PointValue(min,lmin);
		for (int i = 0, len = values.size(); i < len; i++) {
			PointValue v = values.get(i);
			if(value>=prev.value && value<=v.value) {
				double mid = (v.value + prev.value)/2.0;
				if(value>mid) return v; else return prev;
			}
			prev = v;
		}
		double mid = (max + prev.value)/2.0;
		if(value>mid) return new PointValue(max,lmax); else return prev;
	}
	
	class PointValue implements Comparable<PointValue> {
		double value;
		String label;
		
		public PointValue(double v, String l) {
			super();
			this.value = v;
			this.label = l;
		}

		public PointValue(double v) {
			super();
			this.value = v;
		}

		@Override
		public int compareTo(PointValue o) {
			if (value<o.value) return -1; else
				if (value>o.value) return 1; else return 0;
		}
	}
	
	public interface ChangeEvent{
		void onChangePosition(double value, String label);
		void onDragPosition(double value, String label);
	}

	public ChangeEvent getChangePositionEvent() {
		return changePositionEvent;
	}

	public void setChangePositionEvent(ChangeEvent changePositionEvent) {
		this.changePositionEvent = changePositionEvent;
	}

}
