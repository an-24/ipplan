package com.cantor.ipplan.client;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.db.ud.PUserIdent;
import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.cantor.ipplan.client.Slider;

public class FormMain extends Form {
	
	public static FormMain currentForm = null;

	private PUserWrapper user;
	private FlexTable currentTab = null;
	private Integer currentTabId = 0;
	
	private Label lUserName;
	private MainTabPanel tabPanel;
	private CellTable<BargainWrapper> tableAttention;
	private DatabaseServiceAsync dbservice;
	private Image loading;
	private NumberLabel<Double> lRevenue;
	private NumberLabel<Double> lRevenueDelta;
	private NumberLabel<Double> lPrePayment;
	private NumberLabel<Double> lMargin;
	private NumberLabel<Double> lMarginDelta;
	private NumberLabel<Double> lTax;
	private NumberLabel<Double> lTaxDelta;
	private NumberLabel<Double> lProfit;
	private NumberLabel<Double> lProfitDelta;
	private FlexTable tableStats;
	private Label lCaption;

	public FormMain(Ipplan main, RootPanel root, PUserWrapper usr, int numTab) {
		super(main, root);
		user =usr;
		currentForm = this;
		
		// ресурсы для таблицы
		CellTable.Resources resources = GWT.create(CellTable.Resources.class);
		loading = new Image(resources.cellTableLoading());
		
		
		VerticalPanel p0 = new VerticalPanel();
		p0.setSpacing(5);
		p0.setStyleName("gwt-Form");
		initWidget(p0);
		p0.setSize("800px", "600px");
		
		HorizontalPanel p1 = new HorizontalPanel();
		p0.add(p1);
		
		lUserName = new Label("");
		p1.add(lUserName);
		
		tabPanel = new MainTabPanel();
		p0.add(tabPanel);
		tabPanel.setSize("100%", "564px");
		tabPanel.setAnimationEnabled(true);
		
		FlexTable tab1 = new FlexTable();
		tabPanel.add(tab1, "Главное", false);
		tab1.setSize("100%", "3cm");
		
		Button btnNew = new Button("Создать новую сделку");
		btnNew.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addNew();
			}
		});
		tab1.setWidget(0, 0, btnNew);
		tab1.getCellFormatter().setHeight(0, 0, "70px");
		tab1.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		tab1.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
		Button btnNewFromPattern = new Button("Создать по шаблону");
		tab1.setWidget(0, 1, btnNewFromPattern);
		
		Button btnNewFromSample = new Button("Создать по образцу");
		tab1.setWidget(0, 2, btnNewFromSample);
		tab1.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
		
		lCaption = new Label("Всего в работе");
		tab1.setWidget(1, 0, lCaption);
		lCaption.setHeight("");
		lCaption.addStyleName("bold-text");
		
		tableStats = new FlexTable();
		tableStats.setCellSpacing(5);
		tableStats.setCellPadding(5);
		tab1.setWidget(2, 0, tableStats);
		tableStats.setWidth("100%");
		tableStats.addStyleName("tableBorderCollapse");

		tab1.getFlexCellFormatter().setColSpan(2, 0, 3);
		

		Label l11 = new Label("на общую сумму");
		tableStats.setWidget(0, 0, l11);
		tableStats.getCellFormatter().setWidth(0, 0, "300px");
		
		lRevenue = newNumberLabel();
		tableStats.setWidget(0, 1, lRevenue);
		
		lRevenueDelta = newDeltaNumberLabel();
		tableStats.setWidget(0, 2, lRevenueDelta);
		
		Label l3 = new Label("Авансы");
		tableStats.setWidget(1, 0, l3);
		
		lPrePayment = newNumberLabel();
		tableStats.setWidget(1, 1, lPrePayment);
		
		Label l4 = new Label("Маржа");
		tableStats.setWidget(2, 0, l4);
		
		lMargin = newNumberLabel();
		tableStats.setWidget(2, 1, lMargin);
		
		lMarginDelta = newDeltaNumberLabel();
		tableStats.setWidget(2, 2, lMarginDelta);
		
		Label l5 = new Label("Налог");
		tableStats.setWidget(3, 0, l5);
		
		lTax = newNumberLabel();
		tableStats.setWidget(3, 1, lTax);
		
		lTaxDelta = newDeltaNumberLabel();
		tableStats.setWidget(3, 2, lTaxDelta);
		
		Label l6 = new Label("Прибыль");
		tableStats.setWidget(4, 0, l6);
		l6.addStyleName("bold-text");
		
		
		lProfit = newNumberLabel();
		tableStats.setWidget(4, 1, lProfit);
		lProfit.addStyleName("bold-text");
		
		lProfitDelta = newDeltaNumberLabel();
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
		
		tableStats.getCellFormatter().setStyleName(4, 0, "redBorder");
		tableStats.getCellFormatter().setStyleName(4, 1, "redBorder");
		tableStats.getCellFormatter().setStyleName(4, 2, "redBorder");
		tableStats.getCellFormatter().setStyleName(0, 0, "grayBorder");
		tableStats.getCellFormatter().setStyleName(0, 1, "grayBorder");
		tableStats.getCellFormatter().setStyleName(0, 2, "grayBorder");
		
		setProfitLoading(tableStats);
		
		Label l10 = new Label("Требуют срочного вмешательства");
		tab1.setWidget(3, 0, l10);
		tab1.getCellFormatter().setHeight(3, 0, "50px");
		l10.addStyleName("bold-text");
		
		SimplePanel simplePanel = new SimplePanel();
		tab1.setWidget(4, 0, simplePanel);
		simplePanel.setSize("100%", "200px");
		
		tableAttention = new CellTable<BargainWrapper>();
		simplePanel.setWidget(tableAttention);
		tableAttention.setSize("100%", "");
		
		Column<BargainWrapper, String> c1 = new Column<BargainWrapper, String>(new ClickableTextCell()) {

			@Override
			public String getValue(BargainWrapper object) {
				return (object==null)?"":object.getFullName();
			}
		};
		c1.setFieldUpdater(new FieldUpdater<BargainWrapper, String>() {
			@Override
			public void update(int index, BargainWrapper object, String value) {
				Window.alert("jump");
			}
		});
		c1.setCellStyleNames("linkcell");
		TextColumn<BargainWrapper> c2 = new TextColumn<BargainWrapper>() {
			@Override
			public String getValue(BargainWrapper object) {
				return (object==null)?"":object.puser.puserId==PUserIdent.USER_ROOT_ID?"":object.puser.getFullName();
			}
		};
		
		Column<BargainWrapper, Number> с3 = new Column<BargainWrapper, Number>(new NumberCell(NumberFormat.getFormat("#,##0.00"))) {
			@Override
			public Number getValue(BargainWrapper object) {
				return (object==null)?null:object.bargainRevenue/100.0;
			}
		};
		с3.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		TextColumn<BargainWrapper> c4 = new TextColumn<BargainWrapper>() {
			
			@Override
            public String getCellStyleNames(Context context, BargainWrapper  object) {
				if(object.attention==null) return super.getCellStyleNames(context, object);
				switch (object.attention.type) {
				case 1: return "Attention1";
				case 2: return "Attention2";
				case 3: return "Attention3";
				default:
					return super.getCellStyleNames(context, object);
				}
            }  
			
			@Override
			public String getValue(BargainWrapper object) {
				return (object.attention!=null)?object.attention.message:"";
			}
		};
		
		tableAttention.addColumn(c1,"Сделка");
		tableAttention.addColumn(c2,"Сотрудник");
		tableAttention.addColumn(с3,"Выручка");
		tableAttention.addColumn(c4,"");
		tab1.getFlexCellFormatter().setColSpan(4, 0, 3);
		tab1.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_BOTTOM);
		tab1.getCellFormatter().setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_BOTTOM);
		
		FlexTable tab2 = new FlexTable();
		tabPanel.add(tab2, "Сделки", false);
		tab2.setSize("100%", "3cm");
		
		Button btnNewButton = new Button("New button");
		tab2.setWidget(0, 0, btnNewButton);
		
		Slider slider = new Slider();
		tab2.setWidget(0, 1, slider);
		slider.setSize("286px", "");
		slider.setPosition(5.);
		
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

	protected void addNew() {
		
		final Dialog dialog = new Dialog("Создание новой сделки");
		FlexTable table = dialog.getContent();
		table.setWidget(0,0,new Label("Наименование сделки"));
		final TextBox tbBargainName = new TextBox();
		tbBargainName.setWidth("200px");
		table.setWidget(0, 1, tbBargainName);
		tbBargainName.setName("bargainName");
		tbBargainName.getElement().setAttribute("autocomplete", "on");

		RadioButton rb1 = new RadioButton("status","Начать с продажи");
		table.setWidget(1, 0, rb1);
		table.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
		RadioButton rb2 = new RadioButton("status","Осталось только исполнить");
		table.setWidget(1, 1, rb2);
		table.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
		rb1.setValue(true);
		
		table.setWidget(2, 0, new Label("Начать"));
		table.setWidget(2, 1, new Label("Закончить"));
		table.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
		DateBox dbstart = new DateBox();
		dbstart.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
		dbstart.setValue(new Date());
		table.setWidget(3, 0, dbstart);
		table.getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_CENTER);
		DateBox dbfinish = new DateBox();
		dbfinish.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
		table.setWidget(3, 1, dbfinish);
		table.getCellFormatter().setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
		dialog.getButtonOk().setText("Создать");
		dialog.setButtonOkClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.resetErrors();
				if(tbBargainName.getText().isEmpty()) {
					dialog.showError(1, "Наименование сделки не может быть пустым");
					return;
				}
				DatabaseServiceAsync db = getDataBaseService();
				db.newBargain(tbBargainName.getText(), 1, new AsyncCallback<BargainWrapper>() {
					@Override
					public void onSuccess(BargainWrapper result) {
						tabPanel.add(result);
						tabPanel.selectBargain(result);
						dialog.hide();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
				dialog.cancel();
			}
		});
			
		dialog.setFirstFocusedWidget(tbBargainName);
		dialog.center();
		
	}

	private void setProfitLoading(FlexTable tableStats) {
		tableStats.setWidget(4, 1, loading);
		tableStats.getCellFormatter().setWidth(4, 1, "200px");
		tableStats.getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_LEFT);
	}

	private NumberLabel<Double> newNumberLabel() {
		return new NumberLabel<Double>(NumberFormat.getFormat("#,##0.00"));
	}

	private NumberLabel<Double> newDeltaNumberLabel() {
		return new NumberLabel<Double>(NumberFormat.getFormat("(#,##0.00)"));
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
		
		startAttention();
		startTotals();
		startRecoveryEditBargain();
		
		tabPanel.getTabBar().selectTab(currentTabId);
	}


	public void selectTab(int numTab) {
		if(numTab<tabPanel.getTabBar().getTabCount() && numTab>=0)
			tabPanel.getTabBar().selectTab(numTab);
	}
	
	private DatabaseServiceAsync getDataBaseService() {
		if(dbservice!=null) return dbservice; 
		dbservice = GWT.create(DatabaseService.class);
		return dbservice;
	}

	private void startAttention() {
		DatabaseServiceAsync db = getDataBaseService();
		db.attention(new AsyncCallback<List<BargainWrapper>>() {
			
			@Override
			public void onSuccess(List<BargainWrapper> result) {
				prepareGrid(tableAttention, result,false);
				tableAttention.setRowCount(result.size());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
	}

	private void startRecoveryEditBargain() {
		DatabaseServiceAsync db = getDataBaseService();
		db.getTemporalyBargains(new AsyncCallback<List<BargainWrapper>>() {
			
			@Override
			public void onSuccess(List<BargainWrapper> result) {
				for (BargainWrapper bw : result) {
					tabPanel.add(bw);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// молчим
			}
		});
	}
	
	private void startTotals() {
		DatabaseServiceAsync db = getDataBaseService();
		db.getTotals(new AsyncCallback<BargainTotals[]>() {
			
			@Override
			public void onSuccess(BargainTotals[] result) {
				lCaption.setText("Всего в работе "+result[0].getCount()+" "+Utils.getNumberPadeg(new String[]{"сделка","сделки","сделок"},
						result[0].getCount()));
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
				
				tableStats.setWidget(4, 1, lProfit);
				tableStats.getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_RIGHT);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
	}

		
}
