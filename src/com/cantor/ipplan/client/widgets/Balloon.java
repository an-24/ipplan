package com.cantor.ipplan.client.widgets;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Duration;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class Balloon extends PopupPanel {

	private boolean shouldHide = true;
	private final int DURATION = 1200;
	private final  int HIDE_DELAY  = 4000;

	public Balloon(String baloonText, boolean shouldHide) {
		super(true);
		this.shouldHide = shouldHide;
		setAutoHideEnabled(true);
		setAnimationEnabled(true);
		setStyleName("");
		HTML text = new HTML("<div class=\"baloonPanel\">"+baloonText+"</div>");
		setWidget(text);
	}
	
	public void show() {
		BaloonAnimation showBaloon = new BaloonAnimation();
		showBaloon.run(DURATION);
		super.show();
	
		if (shouldHide)
		{
			BaloonAnimation hideAnim = new BaloonAnimation(false);
			hideAnim.run(DURATION, Duration.currentTimeMillis() + HIDE_DELAY);
			Timer t = new Timer() {
				@Override
				public void run() {
					Balloon.this.hide();
				}
			};
			t.schedule(HIDE_DELAY + DURATION);
		}
	}
	
	public void show(final Widget w) {
        addAttachHandler(new Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if(event.isAttached()) {
			        int left = w.getAbsoluteLeft() + 10;
			        int top = w.getAbsoluteTop() - getOffsetHeight();
			        setPopupPosition(left, top);
				}
			}
		});
        show();
	}
	
	class BaloonAnimation extends Animation {
		boolean show = true;
	
		BaloonAnimation(boolean show) {
			super();
			this.show = show;
		}
	
		public BaloonAnimation() {
			this(true);
		}
	
		@Override
		protected void onUpdate(double progress) {
			double opacityValue = progress;
			if (!show) {
				opacityValue = 1.0 - progress;
			}
			Balloon.this.getElement().getStyle().setOpacity(
					opacityValue);
		}
	}
}