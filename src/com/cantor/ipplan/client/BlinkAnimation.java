package com.cantor.ipplan.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Widget;

public class BlinkAnimation extends Animation {

	private int startHeight;
	private Widget widget;
	private NotifyHandler<Widget> completeNotify;
	private NotifyHandler<Widget> hideNotify;
	private NotifyHandler<Widget> startNotify;
	private boolean hideOn;

	public BlinkAnimation(Widget w, int startHeight) {
		this(w,startHeight,null);
	}
	
	public BlinkAnimation(Widget w, int startHeight, NotifyHandler<Widget> complete) {
		super();
		widget = w;
		this.startHeight = startHeight;
		completeNotify = complete;
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		hideOn = true;
		if(startNotify!=null) startNotify.onNotify(widget);
	}
	
	@Override
	protected void onUpdate(double progress) {
		
		if(progress<=0.5) {
			if(startHeight>0)
				widget.setHeight(startHeight*2*(0.5-progress)+"px");
		} else {
			if(hideOn) {
				hideOn = false;
				if(hideNotify!=null) hideNotify.onNotify(widget);
			}
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


	public NotifyHandler<Widget> getCompleteNotify() {
		return completeNotify;
	}


	public void setCompleteNotify(NotifyHandler<Widget> completeNotify) {
		this.completeNotify = completeNotify;
	}


	public NotifyHandler<Widget> getHideNotify() {
		return hideNotify;
	}


	public void setHideNotify(NotifyHandler<Widget> hideNotify) {
		this.hideNotify = hideNotify;
	}

	public int getStartHeight() {
		return startHeight;
	}

	public void setStartHeight(int startHeight) {
		this.startHeight = startHeight;
	}

	public NotifyHandler<Widget> getStartNotify() {
		return startNotify;
	}

	public void setStartNotify(NotifyHandler<Widget> startNotify) {
		this.startNotify = startNotify;
	}
}
