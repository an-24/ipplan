package com.cantor.ipplan.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.ScrollPanel;

public class ScrollAnimation extends Animation {

	private ScrollPanel scroll;
	private int deltaX;
	private int deltaY;
	private int startX;
	private int startY;

	public ScrollAnimation(ScrollPanel scroll, int dPosX, int dPosY) {
		super();
		this.scroll = scroll;
		this.deltaX = dPosX;
		this.deltaY = dPosY;
		startX = scroll.getHorizontalScrollPosition();
		startY = scroll.getVerticalScrollPosition();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onUpdate(double progress) {
		if(deltaX!=0) {
			scroll.setHorizontalScrollPosition(startX+(int)Math.round(deltaX*progress));
		}
		if(deltaY!=0) {
			scroll.setVerticalScrollPosition(startY+(int)Math.round(deltaY*progress));
		}
	}

}
