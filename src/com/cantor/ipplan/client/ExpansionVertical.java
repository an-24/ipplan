package com.cantor.ipplan.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Widget;

public class ExpansionVertical extends Animation {

	private Widget widget;
	private int endHeight;

	public ExpansionVertical(Widget w, int endHeight) {
		super();
		widget = w;
		this.endHeight = endHeight;
	}
			
	
	protected void onStart() {
		super.onStart();
		widget.setVisible(true);
	    widget.getElement().getStyle().setOpacity(0);
	}
	
	@Override
	protected void onUpdate(double progress) {
		widget.setHeight(endHeight*progress+"px");
	    widget.getElement().getStyle().setOpacity(progress);
	}

	@Override
	protected void onComplete() {
	    super.onComplete();
	    widget.getElement().getStyle().setOpacity(1);
	}

}
