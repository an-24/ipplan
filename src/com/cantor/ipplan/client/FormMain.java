package com.cantor.ipplan.client;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.FlexTable;

public class FormMain extends Form {

	public FormMain(Ipplan main, RootPanel root) {
		super(main, root);
		
		VerticalPanel p0 = new VerticalPanel();
		p0.setSpacing(5);
		p0.setStyleName("gwt-Form");
		initWidget(p0);
		p0.setSize("800px", "600px");
		
		HorizontalPanel p1 = new HorizontalPanel();
		p0.add(p1);
		
		TabPanel tabPanel = new TabPanel();
		p0.add(tabPanel);
		tabPanel.setSize("100%", "564px");
		
		FlexTable tab1 = new FlexTable();
		tabPanel.add(tab1, "Главное", false);
		tab1.setSize("100%", "3cm");
	}
		
}
