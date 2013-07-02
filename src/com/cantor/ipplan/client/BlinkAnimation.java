package com.cantor.ipplan.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Widget;

public class BlinkAnimation extends Animation {

	private int startHeight;
	private Widget widget;
	private NotifyHandler<Widget> completeNotify;

	public BlinkAnimation(Widget w, int startHeight, NotifyHandler<Widget> complete) {
		super();
		widget = w;
		this.startHeight = startHeight;
		completeNotify = complete;
	}
	
	
	@Override
	protected void onUpdate(double progress) {
		
		if(progress<=0.5) {
			if(startHeight>0)
				widget.setHeight(startHeight*2*(0.5-progress)+"px");
		} else {
			widget.setHeight(startHeight*2*(progress-0.5)+"px");
		}

	}

	@Override
	protected void onComplete() {
		super.onComplete();
		if(startHeight==0)
			widget.setVisible(true);
		if(completeNotify!=null) completeNotify.onNotify(widget);
	}
}
