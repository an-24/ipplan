package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.cantor.ipplan.shared.BargaincostsWrapper;

public class FormMain extends Form {

	private PUserWrapper user;
	private FlexTable currentTab = null;
	private Integer currentTabId = 0;
	
	private Label lUserName;
	private TabPanel tabPanel;
	private CellTable<BargainWrapper> tableAttention;

	public FormMain(Ipplan main, RootPanel root, PUserWrapper usr, int numTab) {
		super(main, root);
		user =usr;
		
		VerticalPanel p0 = new VerticalPanel();
		p0.setSpacing(5);
		p0.setStyleName("gwt-Form");
		initWidget(p0);
		p0.setSize("800px", "600px");
		
		HorizontalPanel p1 = new HorizontalPanel();
		p0.add(p1);
		
		lUserName = new Label("");
		p1.add(lUserName);
		
		tabPanel = new TabPanel();
		p0.add(tabPanel);
		tabPanel.setSize("100%", "564px");
		tabPanel.setAnimationEnabled(true);
		
		FlexTable tab1 = new FlexTable();
		tab1.setCellPadding(5);
		tab1.setCellSpacing(5);
		tabPanel.add(tab1, "Главное", false);
		tab1.setSize("100%", "3cm");
		
		Button btnNew = new Button("Создать новую сделку");
		tab1.setWidget(0, 0, btnNew);
		tab1.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		tab1.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
		Button btnNewFromPattern = new Button("New button");
		btnNewFromPattern.setText("Создать по шаблону");
		tab1.setWidget(0, 1, btnNewFromPattern);
		
		Button btnNewFromSample = new Button("Создать по образцу");
		tab1.setWidget(0, 2, btnNewFromSample);
		tab1.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
		
		Label l2 = new Label("Всего в работе");
		tab1.setWidget(1, 0, l2);
		l2.setHeight("20px");
		l2.addStyleName("bold-text");
		
		FlexTable tableStats = new FlexTable();
		tableStats.setCellPadding(5);
		tab1.setWidget(2, 0, tableStats);
		tableStats.setWidth("100%");
		tableStats.addStyleName("tableBorderCollapse");

		tab1.getFlexCellFormatter().setColSpan(2, 0, 3);
		

		Label lCount = new Label("0 сделок на общую сумму");
		tableStats.setWidget(0, 0, lCount);
		tableStats.getCellFormatter().setWidth(0, 0, "300px");
		
		NumberLabel<Double> lRevenue = newNumberLabel();
		tableStats.setWidget(0, 1, lRevenue);
		
		NumberLabel<Double> lRevenueDelta = newNumberLabel();
		tableStats.setWidget(0, 2, lRevenueDelta);
		
		Label l3 = new Label("Авансы");
		tableStats.setWidget(1, 0, l3);
		
		NumberLabel<Double> lPrePayment = newNumberLabel();
		tableStats.setWidget(1, 1, lPrePayment);
		
		Label l4 = new Label("Маржа");
		tableStats.setWidget(2, 0, l4);
		
		NumberLabel<Double> lMargin = newNumberLabel();
		tableStats.setWidget(2, 1, lMargin);
		
		NumberLabel<Double> llMarginDelta = newNumberLabel();
		tableStats.setWidget(2, 2, llMarginDelta);
		
		Label l5 = new Label("Налог");
		tableStats.setWidget(3, 0, l5);
		
		NumberLabel<Double> lTax = newNumberLabel();
		tableStats.setWidget(3, 1, lTax);
		
		NumberLabel<Double> lTaxDelta = newNumberLabel();
		tableStats.setWidget(3, 2, lTaxDelta);
		
		Label l6 = new Label("Прибыль");
		tableStats.setWidget(4, 0, l6);
		l6.addStyleName("bold-text");
		
		NumberLabel<Double> lProfit = newNumberLabel();
		tableStats.setWidget(4, 1, lProfit);
		lProfit.addStyleName("bold-text");
		
		NumberLabel<Double> lProfitDelta = newNumberLabel();
		tableStats.setWidget(4, 2, lProfitDelta);
		lProfitDelta.addStyleName("bold-text");
		
		
		tableStats.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		tableStats.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		tableStats.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_MIDDLE);
		tableStats.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		tableStats.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		tableStats.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		tableStats.getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		tableStats.getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		tableStats.getCellFormatter().setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		tableStats.getCellFormatter().setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		tableStats.getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		
		tableStats.getCellFormatter().setStyleName(4, 0, "ProfitBorder");
		tableStats.getCellFormatter().setStyleName(4, 1, "ProfitBorder");
		tableStats.getCellFormatter().setStyleName(4, 2, "ProfitBorder");
		
		Label l10 = new Label("Требуют срочного вмешательства");
		tab1.setWidget(3, 0, l10);
		l10.addStyleName("bold-text");
		
		tableAttention = new CellTable<BargainWrapper>();
		tab1.setWidget(4, 0, tableAttention);
		tableAttention.setSize("100%", "280px");
		tab1.getFlexCellFormatter().setColSpan(4, 0, 3);
		
		FlexTable tab2 = new FlexTable();
		tabPanel.add(tab2, "Сделки", false);
		tab2.setSize("100%", "3cm");
		
		FlexTable tab3 = new FlexTable();
		tabPanel.add(tab3, "Клиенты", false);
		tab3.setSize("100%", "3cm");
		
		FlexTable tab4 = new FlexTable();
		tabPanel.add(tab4, "Анализ", false);
		tab4.setSize("100%", "3cm");
		
		Label l0 = new Label(" ");
		tabPanel.add(l0, "...", false);
		l0.setSize("5cm", "3cm");
		
		currentTabId = numTab;
		prepare();
	}

	private NumberLabel<Double> newNumberLabel() {
		return new NumberLabel<Double>(NumberFormat.getFormat("#,##0.00"));
	}

	private void prepare() {
		lUserName.setText(user.getFullName());
		
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {

			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				currentTabId = event.getSelectedItem();
				currentTab = (FlexTable) tabPanel.getWidget(currentTabId);
				FocusWidget w = getFirstFocusedWidget(currentTab);
				if(w!=null) w.setFocus(true);
				//if(tabId==1) bargainGrid.redraw();
				History.newItem("main."+currentTabId, false);
			}
		});
		
		
		TextColumn<BargainWrapper> c1 = new TextColumn<BargainWrapper>() {
			@Override
			public String getValue(BargainWrapper object) {
				return (object==null)?"":object.getFullName();
			}
		};
		
		Column<BargainWrapper, Number> с2 = new Column<BargainWrapper, Number>(new NumberCell(NumberFormat.getFormat("#,##0.00"))) {
			@Override
			public Number getValue(BargainWrapper object) {
				return (object==null)?null:object.bargainRevenue/100.0;
			}
		};
		с2.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		TextColumn<BargainWrapper> c3 = new TextColumn<BargainWrapper>() {
			@Override
			public String getValue(BargainWrapper object) {
				return "резолюция";
			}
		};
		
		tableAttention.addColumn(c1);
		tableAttention.addColumn(с2);
		tableAttention.addColumn(c3);
		
		tabPanel.getTabBar().selectTab(currentTabId);
	}
		
}
