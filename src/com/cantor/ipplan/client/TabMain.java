package com.cantor.ipplan.client;

import java.util.List;

import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.client.ui.SimplePanel;

public class TabMain extends FlexTable {
	
	private FormMain form;
	private DatabaseServiceAsync dbservice;
	private boolean dirty = true;

	public TabMain(FormMain form,DatabaseServiceAsync dbservice) {
		super();
		this.form = form;
		this.dbservice = dbservice;
		setSize("100%", "3cm");
		init();
		// слушатели изменений
		NotifyHandler<BargainWrapper> notify = new NotifyHandler<BargainWrapper>() {
			@Override
			public void onNotify(BargainWrapper c) {
				setDirty();
			}
		};
		SystemNotify.getDeleteNotify().registerNotify(BargainWrapper.class, notify);
		SystemNotify.getInsertNotify().registerNotify(BargainWrapper.class, notify);
		SystemNotify.getUpdateNotify().registerNotify(BargainWrapper.class, notify);
		
	}

	private void init() {
		Button btnNew = new Button("Создать новую сделку");
		btnNew.addStyleName("mainCommand");
		btnNew.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				try {
					form.addNew();
				} catch (Exception e) {
					Ipplan.error(e);
				}
			}
		});
		setWidget(0, 0, btnNew);
		getCellFormatter().setHeight(0, 0, "70px");
		getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
		Button btnNewFromPattern = new Button("Создать по шаблону");
		setWidget(0, 1, btnNewFromPattern);
		
		Button btnNewFromSample = new Button("Создать по образцу");
		setWidget(0, 2, btnNewFromSample);
		getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
		
		form.lCaption = new Label("Всего в работе");
		setWidget(1, 0, form.lCaption);
		form.lCaption.setHeight("");
		form.lCaption.addStyleName("bold-text");
		
		form.tableStats = new FlexTable();
		form.tableStats.setCellSpacing(5);
		form.tableStats.setCellPadding(5);
		setWidget(2, 0, form.tableStats);
		form.tableStats.setWidth("100%");
		form.tableStats.addStyleName("tableBorderCollapse");

		getFlexCellFormatter().setColSpan(2, 0, 3);
		

		Label l11 = new Label("на общую сумму");
		form.tableStats.setWidget(0, 0, l11);
		form.tableStats.getCellFormatter().setWidth(0, 0, "300px");
		
		form.lRevenue = newNumberLabel();
		form.tableStats.setWidget(0, 1, form.lRevenue);
		
		form.lRevenueDelta = newDeltaNumberLabel();
		form.tableStats.setWidget(0, 2, form.lRevenueDelta);
		
		Label l3 = new Label("Авансы");
		form.tableStats.setWidget(1, 0, l3);
		
		form.lPrePayment = newNumberLabel();
		form.tableStats.setWidget(1, 1, form.lPrePayment);
		
		Label l4 = new Label("Маржа");
		form.tableStats.setWidget(2, 0, l4);
		
		form.lMargin = newNumberLabel();
		form.tableStats.setWidget(2, 1, form.lMargin);
		
		form.lMarginDelta = newDeltaNumberLabel();
		form.tableStats.setWidget(2, 2, form.lMarginDelta);
		
		Label l5 = new Label("Налог");
		form.tableStats.setWidget(3, 0, l5);
		
		form.lTax = newNumberLabel();
		form.tableStats.setWidget(3, 1, form.lTax);
		
		form.lTaxDelta = newDeltaNumberLabel();
		form.tableStats.setWidget(3, 2, form.lTaxDelta);
		
		Label l6 = new Label("Прибыль");
		form.tableStats.setWidget(4, 0, l6);
		l6.addStyleName("bold-text");
		
		
		form.lProfit = newNumberLabel();
		form.tableStats.setWidget(4, 1, form.lProfit);
		form.lProfit.setStyleName("gwt-CurrencyLabel");
		form.lProfit.addStyleName("bold-text");
		//form.lProfit.addStyleName("auto-width");
		
		form.lProfitDelta = newDeltaNumberLabel();
		form.tableStats.setWidget(4, 2, form.lProfitDelta);
		form.lProfitDelta.addStyleName("bold-text");
		
		
		form.tableStats.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		form.tableStats.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		form.tableStats.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_MIDDLE);
		form.tableStats.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		form.tableStats.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		form.tableStats.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		form.tableStats.getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		form.tableStats.getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		form.tableStats.getCellFormatter().setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		form.tableStats.getCellFormatter().setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		
		form.tableStats.getCellFormatter().setStyleName(4, 0, "redBorder");
		form.tableStats.getCellFormatter().setStyleName(4, 1, "redBorder");
		form.tableStats.getCellFormatter().setStyleName(4, 2, "redBorder");
		form.tableStats.getCellFormatter().setStyleName(0, 0, "grayBorder");
		form.tableStats.getCellFormatter().setStyleName(0, 1, "grayBorder");
		form.tableStats.getCellFormatter().setStyleName(0, 2, "grayBorder");
		
		setProfitLoading(form.tableStats);
		
		Label l10 = new Label("Требуют срочного вмешательства");
		setWidget(3, 0, l10);
		getCellFormatter().setHeight(3, 0, "50px");
		l10.addStyleName("bold-text");
		
		SimplePanel simplePanel = new SimplePanel();
		setWidget(4, 0, simplePanel);
		simplePanel.setSize("100%", "200px");
		
		form.tableAttention = new CellTable<BargainWrapper>(5);
		form.tableAttention.setSelectionModel(null);

		simplePanel.setWidget(form.tableAttention);
		form.tableAttention.setSize("100%", "");
		
		form.makeBargainColumns(form.tableAttention);
		
		getFlexCellFormatter().setColSpan(4, 0, 3);
		getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_BOTTOM);
		getCellFormatter().setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_BOTTOM);
	}

	private NumberLabel<Double> newNumberLabel() {
		return new NumberLabel<Double>(NumberFormat.getFormat("#,##0.00"));
	}

	private NumberLabel<Double> newDeltaNumberLabel() {
		return new NumberLabel<Double>(NumberFormat.getFormat("(#,##0.00)"));
	}

	private void setProfitLoading(FlexTable tableStats) {
		tableStats.setWidget(4, 1, form.loading);
		tableStats.getCellFormatter().setWidth(4, 1, "200px");
		tableStats.getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_LEFT);
	}

	private void startAttention() {
		dbservice.attention(new AsyncCallback<List<BargainWrapper>>() {
			
			@Override
			public void onSuccess(List<BargainWrapper> result) {
				Form.prepareGrid(form.tableAttention, result,false);
				form.tableAttention.setRowCount(result.size());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
	}
	
	private void startTotals() {
		dbservice.getTotals(new AsyncCallback<BargainTotals[]>() {
			
			@Override
			public void onSuccess(BargainTotals[] result) {
				form.lCaption.setText("Всего в работе "+result[0].getCount()+" "+Utils.getNumberPadeg(new String[]{"сделка","сделки","сделок"},
						result[0].getCount()));
				form.lRevenue.setValue(result[0].getRevenue()/100.0);
				form.lPrePayment.setValue(result[0].getPrepayment()/100.0);
				form.lMargin.setValue(result[0].getMargin()/100.0);
				form.lTax.setValue(result[0].getTax()/100.0);
				form.lProfit.setValue(result[0].getProfit()/100.0);

				form.lRevenueDelta.setValue((result[0].getRevenue()-result[1].getRevenue())/100.0);
				form.lMarginDelta.setValue((result[0].getMargin() - result[1].getMargin())/100.0);
				form.lTaxDelta.setValue((result[0].getTax()-result[1].getTax())/100.0);
				form.lProfitDelta.setValue((result[0].getProfit()-result[1].getProfit())/100.0);

				if(form.lRevenueDelta.getValue()<0)
					form.lRevenueDelta.addStyleName("Attention3"); else
						if(form.lRevenueDelta.getValue()>0) 
							form.lRevenueDelta.addStyleName("Attention1"); 
				if(form.lMarginDelta.getValue()<0)
					form.lMarginDelta.addStyleName("Attention3"); else
						if(form.lMarginDelta.getValue()>0) 
							form.lMarginDelta.addStyleName("Attention1"); 
				if(form.lTaxDelta.getValue()>0)
					form.lTaxDelta.addStyleName("Attention3"); else
						if(form.lTaxDelta.getValue()<0) 
							form.lTaxDelta.addStyleName("Attention1"); 
				if(form.lProfitDelta.getValue()<0)
					form.lProfitDelta.addStyleName("Attention3"); else
						if(form.lProfitDelta.getValue()>0) 
							form.lProfitDelta.addStyleName("Attention1"); 
				
				form.tableStats.setWidget(4, 1, form.lProfit);
				form.tableStats.getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_RIGHT);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
	}
	
	public void setDirty() {
		this.dirty = true;
	}


	public void refresh() {
		if(!dirty) return;
		startAttention();
		startTotals();
		dirty = false;
	}

}
