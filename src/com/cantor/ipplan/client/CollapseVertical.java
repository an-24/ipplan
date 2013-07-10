package com.cantor.ipplan.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Widget;

public class CollapseVertical extends Animation {

	private Widget widget;
	private int endHeight;
	private int startHeight;


	public CollapseVertical(Widget w, int endHeight) {
		super();
		widget = w;
		this.endHeight = endHeight;
	}


	@Override
	protected void onStart() {
		startHeight = widget.getOffsetHeight();
		super.onStart();
	}
	
	@Override
	protected void onUpdate(double progress) {
		widget.setHeight(endHeight+(startHeight-endHeight)*(1-progress)+"px");
	}
	
	@Override
	protected void onComplete() {
	    super.onComplete();
	    if(endHeight==0) widget.setVisible(false);
	}
	

}
