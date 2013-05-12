package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.BargainWrapper;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainTabPanel extends TabPanel {
	
	public BargainFlexTable add(BargainWrapper bw) {
		BargainFlexTable bft = new BargainFlexTable(bw);
		super.add(bft,bft.getTitle());
		return bft;
	}

	public void selectBargain(int bargainId) {
		for (int i = 0,len = getWidgetCount(); i < len; i++) {
			Widget w = getWidget(i);
			if(w instanceof BargainFlexTable) {
				BargainWrapper b = ((BargainFlexTable)w).getBargain();
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
			if(w instanceof BargainFlexTable) {
				BargainWrapper b = ((BargainFlexTable)w).getBargain();
				if(b==bw) {
					getTabBar().selectTab(i);
					return;
				}
			}
		}
	}

}
