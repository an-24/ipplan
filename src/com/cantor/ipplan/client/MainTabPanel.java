package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.BargainWrapper;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TabBar.Tab;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainTabPanel extends TabPanel {
	
	public FormBargain add(BargainWrapper bw) {
		final FormBargain bft = new FormBargain(bw);
		bft.setOwner(this);
		bft.setIndex(getTabBar().getTabCount());
		super.add(bft,bft.makeHTMLTab(),true);
		new Timer(){
			@Override
			public void run() {
				bft.init();
			}
		}.schedule(0);
		return bft;
	}

	public void selectBargain(int bargainId) {
		for (int i = 0,len = getWidgetCount(); i < len; i++) {
			Widget w = getWidget(i);
			if(w instanceof FormBargain) {
				BargainWrapper b = ((FormBargain)w).getBargain();
				if(b.bargainId==bargainId) {
					getTabBar().selectTab(i);
					return;
				}
			}
		}
	}

	public void selectBargain(BargainWrapper bw) {
		for (int i = 0,len = getWidgetCount(); i < len; i++) {
			Widget w = getWidget(i);
			if(w instanceof FormBargain) {
				BargainWrapper b = ((FormBargain)w).getBargain();
				if(b==bw) {
					getTabBar().selectTab(i);
					return;
				}
			}
		}
	}

}
