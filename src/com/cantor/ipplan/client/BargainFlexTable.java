package com.cantor.ipplan.client;


import java.util.Date;

import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.InlineHTML;

@SuppressWarnings("rawtypes")
public class BargainFlexTable extends FlexTable implements ValueChangeHandler{

	public static final int EDIT_VIEW = 0;
	public static final int EDIT_MODE = 1;
	
	private BargainWrapper bargain;
	private Button btnPrev;
	private Button btnNext;
	private Button button;
	private Label l1;
	private DateBox dbStart;
	private DateBox dbFinish;
	private Label lAttention;
	private InlineHTML eStatus;
	private InlineHTML eCustomer;
	
	private int mode = EDIT_VIEW;
	private CurrencyBox eRevenue;
	private NumberLabel<Double> lRevenueDelta;

	public BargainFlexTable(BargainWrapper b) {
		super();
		setCellPadding(4);
		setSize("600px", "");
		setStyleName("FormBargain");
		addStyleName("tableBorderCollapse");
		this.bargain = b;
		
		Label l;
		VerticalPanel p;
		
		l = new Label(getTitle());
		l.setStyleName("gwt-FormCaption");
		setWidget(0, 0, l);
		getFlexCellFormatter().setColSpan(0, 0, 3);
		getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		btnPrev = new Button("<");
		btnPrev.setText("<");
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
		
		dbStart = new DateBox();
		setWidget(2, 0, dbStart);
		getCellFormatter().setWidth(2, 0, "160px");
		dbStart.setWidth("113px");
		dbStart.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
		dbStart.setValue(bargain.bargainStart);
		dbStart.setEnabled(false);
		dbStart.addValueChangeHandler(this);
		
		button = new Button("Календарь");
		setWidget(2, 1, button);
		getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
		dbFinish = new DateBox();
		setWidget(2, 2, dbFinish);
		getCellFormatter().setWidth(2, 2, "160px");
		dbFinish.setWidth("113px");
		dbFinish.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
		dbFinish.setValue(bargain.bargainFinish);
		dbFinish.setEnabled(false);
		dbFinish.addValueChangeHandler(this);
		getCellFormatter().setHorizontalAlignment(2, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		
		l1 = new Label("Статус: ");
		setWidget(3, 0, l1);
		getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		
		eStatus = new InlineHTML("Исполнение");
		eStatus.setStyleName("link");
		setWidget(3, 1, eStatus);
		eStatus.setText(bargain.status.statusName);
		eStatus.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(mode==EDIT_MODE) showStatusForm();
			}
		});
		
		lAttention = new Label("");
		setWidget(3, 2, lAttention);
		getCellFormatter().setVerticalAlignment(3, 2, HasVerticalAlignment.ALIGN_MIDDLE);
		getCellFormatter().setHorizontalAlignment(3, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		if(bargain.attention!=null) {
			lAttention.setText(bargain.attention.message);
			lAttention.setStyleName("Attention"+bargain.attention.type);
		} else
			lAttention.setText("Осталось "+Utils.getDuration(new Date(), bargain.bargainFinish));
		
		l = new Label("Заказчик:");
		setWidget(4, 0, l);
		getCellFormatter().setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		getCellFormatter().setVerticalAlignment(4, 0, HasVerticalAlignment.ALIGN_TOP);
		
		eCustomer = new InlineHTML("Исполнение");
		eCustomer.setStyleName("link");
		setWidget(4, 1, eCustomer);
		getCellFormatter().setVerticalAlignment(4, 1, HasVerticalAlignment.ALIGN_TOP);
		if(bargain.customer == null) eCustomer.setText("<необходимо определить>"); 
							    else eCustomer.setText(bargain.customer.customerName);
			
		eCustomer.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(mode==EDIT_MODE) showCustomer();
			}
		});
		getCellFormatter().setHeight(4,0,"36px");
		
		getCellFormatter().setStyleName(5, 0, "grayBorder");
		getCellFormatter().setStyleName(5, 1, "grayBorder");
		getCellFormatter().setStyleName(5, 2, "grayBorder");

		getCellFormatter().setHeight(5,0,"36px");
		
		l = new Label("Выручка");
		setWidget(5, 0, l);

		eRevenue = newNumberEdit(bargain.bargainRevenue);
		setWidget(5, 1, eRevenue);
		
		lRevenueDelta = newDeltaNumberLabel();
		setWidget(5, 2, lRevenueDelta);
		
		lockControl();
	}
	
	protected void showCustomer() {
		// TODO Auto-generated method stub
		
	}

	private void lockControl() {
		btnPrev.setEnabled(bargain.bargainVer>0);
		btnNext.setEnabled(!bargain.isNew());
		dbStart.setEnabled(mode==EDIT_MODE);
		dbFinish.setEnabled(mode==EDIT_MODE);
		eRevenue.setEnabled(mode==EDIT_MODE);
	}

	public String getTitle() {
		String s = bargain.getFullName();
		if(this.bargain.isDirty()) s = "* "+s;
		return s;
	}
	
	public BargainWrapper getBargain() {
		return bargain; 
	}

	public void setMode(int newmode) {
		if(mode!=newmode) {
			mode = newmode;
			lockControl();
		}
	}


	protected void showStatusForm() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onValueChange(ValueChangeEvent event) {
		if(mode==EDIT_MODE) {
			bargain.modify();
			//TODO поменять title
		}	
		
		
	}

	private CurrencyBox newNumberEdit(Integer value) {
		CurrencyBox db = new CurrencyBox(value);
		return db;
	}

	private NumberLabel<Double> newDeltaNumberLabel() {
		return new NumberLabel<Double>(NumberFormat.getFormat("(#,##0.00)"));
	}

}
