package com.cantor.ipplan.client;

import java.text.DateFormat;

import com.cantor.ipplan.shared.BargainWrapper;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class BargainFlexTable extends FlexTable {

	private BargainWrapper bargain;
	private Button btnPrev;
	private Button btnNext;

	public BargainFlexTable(BargainWrapper b) {
		super();
		this.bargain = b;
		Label l;
		VerticalPanel p;
		
		l = new Label(getTitle());
		l.setStyleName("gwt-FormCaption");
		setWidget(0, 0, l);
		getFlexCellFormatter().setColSpan(0, 0, 3);
		getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		btnPrev = new Button("<");
		setWidget(1, 0, btnPrev);
		
		p = new VerticalPanel();
		p.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		l = new Label("Версия "+(bargain.bargainVer+1));
		l.addStyleName("gwt-FormSubCaption");
		p.add(l);
		l = new Label(DateTimeFormat.getMediumDateFormat().format(bargain.bargainCreated));
		p.add(l);
		
		setWidget(1, 1, p);
		getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);

		btnNext = new Button(">");
		setWidget(1, 2, btnNext);
		getCellFormatter().setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		
		lockControl();
	}
	
	private void lockControl() {
		btnPrev.setEnabled(bargain.bargainVer>0);
		btnNext.setEnabled(!bargain.isNew());
	}

	public String getTitle() {
		String s = bargain.getFullName();
		if(this.bargain.isDirty()) s = "* "+s;
		return s;
	}
	
	public BargainWrapper getBargain() {
		return bargain; 
	}

}
