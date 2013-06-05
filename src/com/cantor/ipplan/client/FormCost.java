package com.cantor.ipplan.client;


import java.util.List;

import com.cantor.ipplan.client.CellTable.ChangeCheckListEvent;
import com.cantor.ipplan.client.InplaceEditor.DisplayValueFormatter;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.BargaincostsWrapper;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.Event;

public class FormCost extends Dialog {

	private BargainWrapper bargain;
	private CellTable<BargaincostsWrapper> mainTable;
	private ListDataProvider<BargaincostsWrapper> listProvider;
	private ClickHandler okExternalHandler;
	//private List<T> checkedList = new ArrayList<T>();

	public FormCost(BargainWrapper bw, ClickHandler ok) {
		super("Расходы");
		this.bargain = bw;
		this.okExternalHandler = ok;
		
		FlexTable table = getContent();
		table.setHeight("500px");
		table.setWidth("660px");
		table.setCellSpacing(2);
		table.setCellPadding(2);
		HorizontalPanel p = new HorizontalPanel();
		p.setSpacing(5);
		Button btnAdd = new Button("Новый");
		btnAdd.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final BargaincostsWrapper cost = new BargaincostsWrapper();
				listProvider.getList().add(cost);
				mainTable.redraw();
				// mainTable еще не обновила свое состояние
				new Timer() {
					@Override
					public void run() {
						mainTable.edit(cost);
					}
				}.schedule(0);
			}
		});
		final Button btnDelete = new Button("Удалить");
		btnDelete.setEnabled(false);
		btnDelete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mainTable.resetSelection(true);
				for (BargaincostsWrapper c : mainTable.getCheckedList()) {
					mainTable.delete(c);
				}
				mainTable.getCheckedList().clear();
				mainTable.redraw();
				btnDelete.setEnabled(false);
			}
		});
		
		p.add(btnAdd);
		p.add(btnDelete);
		table.setWidget(0, 0, p);
		table.getFlexCellFormatter().setHeight(0, 0, "20px");

		mainTable = new CellTable<BargaincostsWrapper>(10);
		
		TextBox tb = new TextBox();
		tb.getElement().setAttribute("placeholder", "Введите расходную статью");
		Column<BargaincostsWrapper,String> c1 = new Column<BargaincostsWrapper,String>(new InplaceEditor(tb,mainTable)) {
			@Override
			public String getValue(BargaincostsWrapper object) {
				return (object==null || object.cost==null)?"":object.cost.costsName;
			}
		};
		c1.setFieldUpdater(new FieldUpdater<BargaincostsWrapper, String>() {
			@Override
			public void update(int index, BargaincostsWrapper object, String value) {
				//TODO
			}
		});
		
		final NumberFormat currencyFormat = NumberFormat.getFormat("#,##0.00");
		
		DisplayValueFormatter<Integer> currFormater = new InplaceEditor.DisplayValueFormatter<Integer>(){
			@Override
			public String format(Integer value) {
				if(value==null) return "";
				return currencyFormat.format(value.intValue()/100.0);
			}
		};
		
		Column<BargaincostsWrapper, Integer> c2 = new Column<BargaincostsWrapper, Integer>(new InplaceEditor(new CurrencyBox(),mainTable,currFormater)) {
			@Override
			public Integer getValue(BargaincostsWrapper object) {
				return (object==null)?null:object.bargaincostsValue;
			}
		};
		c2.setFieldUpdater(new FieldUpdater<BargaincostsWrapper, Integer>() {
			@Override
			public void update(int index, BargaincostsWrapper object, Integer value) {
				object.bargaincostsValue = value;
			}
		});
		Column<BargaincostsWrapper, Integer> c3 = new Column<BargaincostsWrapper, Integer>(new InplaceEditor(new CurrencyBox(),mainTable,currFormater))  {
			@Override
			public Integer getValue(BargaincostsWrapper object) {
				return (object==null)?null:object.bargaincostsPayment;
			}
		};
		c3.setFieldUpdater(new FieldUpdater<BargaincostsWrapper, Integer>() {
			@Override
			public void update(int index, BargaincostsWrapper object, Integer value) {
				object.bargaincostsPayment = value;
			}
		});
		Column<BargaincostsWrapper,String> c4 = new Column<BargaincostsWrapper,String>(new InplaceEditor(new TextBox(),mainTable)) {
			@Override
			public String getValue(BargaincostsWrapper object) {
				return (object==null || object.bargaincostsNote==null)?"":object.bargaincostsNote;
			}
		};
		c4.setFieldUpdater(new FieldUpdater<BargaincostsWrapper, String>() {
			@Override
			public void update(int index, BargaincostsWrapper object, String value) {
				object.bargaincostsNote = value;
			}
		});
		
		mainTable.createCheckedColumn(new ChangeCheckListEvent() {
			@Override
			public void onChange() {
				btnDelete.setEnabled(mainTable.getCheckedList().size()>0);
			}
		});
		mainTable.addColumn(c1,"Расходная статья");
		mainTable.addColumn(c2,"Всего");
		mainTable.addColumn(c3,"Из низ оплачено");
		mainTable.addColumn(c4,"Комментарий");
		
		mainTable.setColumnWidth(c1, "200px");
		mainTable.setColumnWidth(c2, "110px");
		mainTable.setColumnWidth(c3, "110px");
		
		c2.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		c3.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		mainTable.setWidth("100%");
		
		SimplePager pager = new GridPager();
		pager.setDisplay(mainTable);
		table.setWidget(1,0, pager);
		table.getFlexCellFormatter().setHeight(1, 0, "20px");
		table.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
		table.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		
		table.setWidget(2,0, mainTable);
		table.getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
		
		getButtonOk().setText("Сохранить");
		setButtonOkClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(validate()) {
					bargain.bargaincostses.clear();
					bargain.bargaincostses.addAll(mainTable.getValues());
					if(okExternalHandler!=null) okExternalHandler.onClick(event);
				}
			}
		});
		
		mainTable.setDataChangeEvent(new DataChangeEvent<BargaincostsWrapper>() {
			
			@Override
			public boolean onBeforePost() {
				return true;
			}
			
			@Override
			public void onAfterPost() {
				List<BargaincostsWrapper> list = mainTable.getValues();
				int t1 =0,t2 =0;
				for (BargaincostsWrapper c : list) {
					t1+=c.bargaincostsValue;
					t2+=c.bargaincostsPayment;
					
				}
				bargain.bargainCosts = t1;
				bargain.bargainPaymentCosts = t2;
				showTotals();
			}

			@Override
			public boolean onBeforeDelete(BargaincostsWrapper c) {
				return true;
			}

			@Override
			public void onAfterDelete(BargaincostsWrapper c) {
				bargain.bargainCosts-=c.bargaincostsValue;
				bargain.bargainPaymentCosts -=c.bargaincostsPayment;
				showTotals();
			}
		});
		
		refresh();
	}

	protected void showTotals() {
		// TODO Auto-generated method stub
		
	}

	protected boolean validate() {
		// TODO Auto-generated method stub
		return true;
	}

	private void refresh() {
		listProvider = Form.prepareGrid(mainTable, bargain.bargaincostses,false);
		mainTable.setRowCount(bargain.bargaincostses.size());
		showTotals();
	}
	
}
