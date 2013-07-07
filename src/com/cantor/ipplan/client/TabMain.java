package com.cantor.ipplan.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cantor.ipplan.client.widgets.CellTable;
import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.cantor.ipplan.client.widgets.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class TabMain extends InplaceForm {
	
	protected static final int SALES_BOX_MAXWIDTH = 160;
	private FormMain form;
	private DatabaseServiceAsync dbservice;
	private boolean dirty = true;

	private Label lWorkCaption;
	private NumberLabel<Double> lRevenue;
	private NumberLabel<Double> lRevenueDelta;
	private NumberLabel<Double> lPrePayment;
	private NumberLabel<Double> lMargin;
	private NumberLabel<Double> lMarginDelta;
	private NumberLabel<Double> lTax;
	private NumberLabel<Double> lTaxDelta;
	private NumberLabel<Double> lProfit;
	private NumberLabel<Double> lProfitDelta;
	private FlexTable tableWorkStats;
	private Label lSalesCaption;
	private FlexTable tableSalesStats;
	private SimplePanel divPrimaryContact;
	private SimplePanel divTalk;
	private SimplePanel divDecMake;
	private Widget divAgreement;
	private SimplePanel divExecution;

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

		HorizontalPanel ph = new HorizontalPanel();
		ph.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		ph.setSpacing(5);
		Button btnNew = new Button("Создать новую сделку");
		btnNew.addStyleName("mainCommand");
		btnNew.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				try {
					form.addNewBargain();
				} catch (Exception e) {
					Ipplan.error(e);
				}
			}
		});
		ph.add(btnNew);
		
		Button btnNewFromPattern = new Button("Создать по шаблону");
		ph.add(btnNewFromPattern);
		
		Button btnNewFromSample = new Button("Создать по образцу");
		ph.add(btnNewFromSample);
		
		getFlexCellFormatter().setColSpan(0, 0, 3);
		setWidget(0, 0, ph);
		getCellFormatter().setHeight(0, 0, "70px");
		getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		
		//getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
		
		initWorkStats();
		initSalesStats();
		
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

	private void initSalesStats() {
		getFlexCellFormatter().setColSpan(2, 1, 2);
		
		lSalesCaption = new Label("Ход продаж");
		setWidget(1, 1, lSalesCaption);
		lSalesCaption.addStyleName("bold-text");
		
		
		tableSalesStats = new FlexTable();
		tableSalesStats.setCellSpacing(5);
		tableSalesStats.setCellPadding(5);
		tableSalesStats.setWidth("350px");
		tableSalesStats.addStyleName("tableBorderCollapse");
		setWidget(2, 1, tableSalesStats);
		
		//PRIMARY_CONTACT = 1;
		tableSalesStats.setWidget(0, 0, new Label("Первичный контакт"));
		divPrimaryContact = createStatusBox(StatusWrapper.PRIMARY_CONTACT);
		tableSalesStats.setWidget(0, 1, divPrimaryContact);
		tableSalesStats.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		//TALK = 10;
		tableSalesStats.setWidget(1, 0, new Label("Переговоры"));
		divTalk = createStatusBox(StatusWrapper.TALK);
		tableSalesStats.setWidget(1, 1, divTalk);
		tableSalesStats.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
		//DECISION_MAKING = 20;
		tableSalesStats.setWidget(2, 0, new Label("Принимают решение"));
		divDecMake = createStatusBox(StatusWrapper.DECISION_MAKING);
		tableSalesStats.setWidget(2, 1, divDecMake);
		tableSalesStats.getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_CENTER);
		//RECONCILIATION_AGREEMENT = 30;
		tableSalesStats.setWidget(3, 0, new Label("Согласование договора"));
		divAgreement = createStatusBox(StatusWrapper.RECONCILIATION_AGREEMENT);
		tableSalesStats.setWidget(3, 1, divAgreement);
		tableSalesStats.getCellFormatter().setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_CENTER);
		// -> EXECUTION
		tableSalesStats.setWidget(4, 0, new Label("Заключено сделок"));
		divExecution = createStatusBox(StatusWrapper.EXECUTION);
		divExecution.getElement().getStyle().setMarginTop(7, Unit.PX);
		divExecution.getElement().getStyle().setMarginBottom(7, Unit.PX);
		tableSalesStats.setWidget(4, 1, divExecution);
		tableSalesStats.getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
		tableSalesStats.getCellFormatter().setStyleName(4, 0, "redBorder");
		tableSalesStats.getCellFormatter().setStyleName(4, 1, "redBorder");
		tableSalesStats.getCellFormatter().setStyleName(0, 0, "grayBorder");
		tableSalesStats.getCellFormatter().setStyleName(0, 1, "grayBorder");
		
		tableSalesStats.setWidget(4, 1, new Image(form.resources.cellTableLoading()));
	}

	private SimplePanel createStatusBox(int stat) {
		SimplePanel box = new SimplePanel();
		box.setStyleName("statusbox");
		box.setWidth("0px");
		box.getElement().getStyle().setBackgroundColor(StatusWrapper.getBackgroundColor(stat));
		return box;
	}

	private void initWorkStats() {
		lWorkCaption = new Label("Ход работ");
		setWidget(1, 0, lWorkCaption);
		lWorkCaption.addStyleName("bold-text");
		
		tableWorkStats = new FlexTable();
		tableWorkStats.setCellSpacing(5);
		tableWorkStats.setCellPadding(5);
		setWidget(2, 0, tableWorkStats);
		//tableWorkStats.setWidth("380px");
		tableWorkStats.addStyleName("tableBorderCollapse");

		Label l11 = new Label("на общую сумму");
		tableWorkStats.setWidget(0, 0, l11);
		tableWorkStats.getCellFormatter().setWidth(0, 0, "220px");
		
		lRevenue = newNumberLabel();
		tableWorkStats.setWidget(0, 1, lRevenue);
		
		lRevenueDelta = newDeltaNumberLabel();
		tableWorkStats.setWidget(0, 2, lRevenueDelta);
		
		Label l3 = new Label("Авансы");
		tableWorkStats.setWidget(1, 0, l3);
		
		lPrePayment = newNumberLabel();
		tableWorkStats.setWidget(1, 1, lPrePayment);
		
		Label l4 = new Label("Маржа");
		tableWorkStats.setWidget(2, 0, l4);
		
		lMargin = newNumberLabel();
		tableWorkStats.setWidget(2, 1, lMargin);
		
		lMarginDelta = newDeltaNumberLabel();
		tableWorkStats.setWidget(2, 2, lMarginDelta);
		
		Label l5 = new Label("Налог");
		tableWorkStats.setWidget(3, 0, l5);
		
		lTax = newNumberLabel();
		tableWorkStats.setWidget(3, 1, lTax);
		
		lTaxDelta = newDeltaNumberLabel();
		tableWorkStats.setWidget(3, 2, lTaxDelta);
		
		Label l6 = new Label("Прибыль");
		tableWorkStats.setWidget(4, 0, l6);
		l6.addStyleName("bold-text");
		
		
		lProfit = newNumberLabel();
		tableWorkStats.setWidget(4, 1, lProfit);
		lProfit.setStyleName("gwt-CurrencyLabel");
		lProfit.addStyleName("bold-text");
		//form.lProfit.addStyleName("auto-width");
		
		lProfitDelta = newDeltaNumberLabel();
		tableWorkStats.setWidget(4, 2, lProfitDelta);
		lProfitDelta.addStyleName("bold-text");
		
		
		tableWorkStats.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		tableWorkStats.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		tableWorkStats.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_MIDDLE);
		tableWorkStats.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		tableWorkStats.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		tableWorkStats.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		tableWorkStats.getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		tableWorkStats.getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		tableWorkStats.getCellFormatter().setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		tableWorkStats.getCellFormatter().setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		
		tableWorkStats.getCellFormatter().setStyleName(4, 0, "redBorder");
		tableWorkStats.getCellFormatter().setStyleName(4, 1, "redBorder");
		tableWorkStats.getCellFormatter().setStyleName(4, 2, "redBorder");
		tableWorkStats.getCellFormatter().setStyleName(0, 0, "grayBorder");
		tableWorkStats.getCellFormatter().setStyleName(0, 1, "grayBorder");
		tableWorkStats.getCellFormatter().setStyleName(0, 2, "grayBorder");
		
		tableWorkStats.setWidget(4, 1, new Image(form.resources.cellTableLoading()));
		//table.getCellFormatter().setWidth(4, 1, "100px");
		tableWorkStats.getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_LEFT);
	}

	private NumberLabel<Double> newNumberLabel() {
		return new NumberLabel<Double>(NumberFormat.getFormat("#,##0.00"));
	}

	private NumberLabel<Double> newDeltaNumberLabel() {
		return new NumberLabel<Double>(NumberFormat.getFormat("(#,##0.00)"));
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
				int bcount = result[0].getCount();
				if(bcount>0)
					lWorkCaption.setText("Ход работ ("+bcount+" "+Utils.getNumberPadeg(new String[]{"сделка","сделки","сделок"},bcount)+")");
				
				lRevenue.setValue(result[0].getRevenue()/100.0);
				lPrePayment.setValue(result[0].getPrepayment()/100.0);
				lMargin.setValue(result[0].getMargin()/100.0);
				lTax.setValue(result[0].getTax()/100.0);
				lProfit.setValue(result[0].getProfit()/100.0);

				lRevenueDelta.setValue((result[0].getRevenue()-result[1].getRevenue())/100.0);
				lMarginDelta.setValue((result[0].getMargin() - result[1].getMargin())/100.0);
				lTaxDelta.setValue((result[0].getTax()-result[1].getTax())/100.0);
				lProfitDelta.setValue((result[0].getProfit()-result[1].getProfit())/100.0);

				if(lRevenueDelta.getValue()<0)
					lRevenueDelta.addStyleName("Attention3"); else
						if(lRevenueDelta.getValue()>0) 
							lRevenueDelta.addStyleName("Attention1"); 
				if(lMarginDelta.getValue()<0)
					lMarginDelta.addStyleName("Attention3"); else
						if(lMarginDelta.getValue()>0) 
							lMarginDelta.addStyleName("Attention1"); 
				if(lTaxDelta.getValue()>0)
					lTaxDelta.addStyleName("Attention3"); else
						if(lTaxDelta.getValue()<0) 
							lTaxDelta.addStyleName("Attention1"); 
				if(lProfitDelta.getValue()<0)
					lProfitDelta.addStyleName("Attention3"); else
						if(lProfitDelta.getValue()>0) 
							lProfitDelta.addStyleName("Attention1"); 
				
				tableWorkStats.setWidget(4, 1, lProfit);
				tableWorkStats.getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_RIGHT);
				// начиная с 2 - ход продаж
				Map<Integer,BargainTotals> list =  new HashMap<Integer,BargainTotals>();
				int cmax = 0;
				for (int i = 2; i < result.length; i++) {
					list.put(result[i].getStatusId(), result[i]);
					cmax = Math.max(result[i].getCount(), cmax);
				}
				// заполняем
				int call = 0;
				BargainTotals bt;
				bt = list.get(StatusWrapper.PRIMARY_CONTACT);
				if(bt!=null) {
					int c =bt.getCount();
					divPrimaryContact.setWidth(String.valueOf(c*1.0/cmax*SALES_BOX_MAXWIDTH)+"px");
					divPrimaryContact.setVisible(true);
					((Label)tableSalesStats.getWidget(0, 0)).setText("Первичный контакт ("+c+")");
					call+=c;
				} else
					divPrimaryContact.setVisible(false);
				bt = list.get(StatusWrapper.TALK);
				if(bt!=null) {
					int c =bt.getCount();
					divTalk.setWidth(String.valueOf(c*1.0/cmax*SALES_BOX_MAXWIDTH)+"px");
					divTalk.setVisible(true);
					((Label)tableSalesStats.getWidget(1, 0)).setText("Переговоры ("+c+")");
					call+=c;
				} else
					divTalk.setVisible(false);
				bt = list.get(StatusWrapper.DECISION_MAKING);
				if(bt!=null) {
					int c =bt.getCount();
					divDecMake.setWidth(String.valueOf(c*1.0/cmax*SALES_BOX_MAXWIDTH)+"px");
					divDecMake.setVisible(true);
					((Label)tableSalesStats.getWidget(2, 0)).setText("Принимают решение ("+c+")");
					call+=c;
				} else
					divDecMake.setVisible(false);
				bt = list.get(StatusWrapper.RECONCILIATION_AGREEMENT);
				if(bt!=null) {
					int c =bt.getCount();
					divAgreement.setWidth(String.valueOf(c*1.0/cmax*SALES_BOX_MAXWIDTH)+"px");
					divAgreement.setVisible(true);
					((Label)tableSalesStats.getWidget(3, 0)).setText("Согласование договора ("+c+")");
					call+=c;
				} else
					divAgreement.setVisible(false);
				bt = list.get(StatusWrapper.EXECUTION);
				if(bt!=null && bt.getCount()>0 && cmax>0) {
					divExecution.setWidth(String.valueOf(bt.getCount()*1.0/cmax*SALES_BOX_MAXWIDTH)+"px");
					divExecution.setVisible(true);
					((Label)tableSalesStats.getWidget(4, 0)).setText("Заключено сделок ("+bt.getCount()+")");
				} else
					divExecution.setVisible(false);
				tableSalesStats.setWidget(4, 1, divExecution);
					
				lSalesCaption.setText("Ход продаж ("+call+" "+Utils.getNumberPadeg(new String[]{"сделка","сделки","сделок"},call)+")");
				
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
