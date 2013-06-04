package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.BargaincostsWrapper;
import com.google.gwt.canvas.dom.client.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;
import  com.google.gwt.dom.client.Element;
import  com.google.gwt.dom.client.NativeEvent;

public class FormCost extends Dialog {

	private BargainWrapper bargain;
	private CellTable<BargaincostsWrapper> mainTable;
	private ListDataProvider<BargaincostsWrapper> listProvider;

	public FormCost(BargainWrapper bw) {
		super("Расходы");
		this.bargain = bw;
		
		FlexTable table = getContent();
		table.setHeight("500px");
		table.setWidth("660px");
		HorizontalPanel p = new HorizontalPanel();
		p.setSpacing(5);
		Button btnAdd = new Button("Новый");
		btnAdd.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				listProvider.getList().add((new BargaincostsWrapper()));
				mainTable.redraw();
			}
		});
		
		p.add(btnAdd);
		table.setWidget(0, 0, p);
		table.getCellFormatter().setHeight(0, 0, "30px");

		ProvidesKey<BargaincostsWrapper> prviderkeys = new ProvidesKey<BargaincostsWrapper>(){
			@Override
			public Object getKey(BargaincostsWrapper item) {
				return item.bargaincostsId!=0?item.bargaincostsId:item.hashCode(); //hashCode - temp key 
			}
		}; 
		
		mainTable = new CellTable<BargaincostsWrapper>(10/*,prviderkeys*/);
		
		Column<BargaincostsWrapper,String> c1 = new Column<BargaincostsWrapper,String>(new InplaceEditor(new TextBox(),mainTable)) {
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
		
		Column<BargaincostsWrapper, Number> c2 = new Column<BargaincostsWrapper, Number>(new NumberCell(NumberFormat.getFormat("#,##0.00"))) {
			@Override
			public Number getValue(BargaincostsWrapper object) {
				return (object==null)?null:object.bargaincostsValue/100.0;
			}
		};
		Column<BargaincostsWrapper, Number> c3 = new Column<BargaincostsWrapper, Number>(new NumberCell(NumberFormat.getFormat("#,##0.00"))) {
			@Override
			public Number getValue(BargaincostsWrapper object) {
				return (object==null)?null:object.bargaincostsPayment/100.0;
			}
		};
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
		
		mainTable.addColumn(c1,"Расходная статья");
		mainTable.addColumn(c2,"Всего");
		mainTable.addColumn(c3,"Из низ оплачено");
		mainTable.addColumn(c4,"Комментарий");
		
		mainTable.setColumnWidth(c1, "220px");
		mainTable.setColumnWidth(c2, "80px");
		mainTable.setColumnWidth(c3, "80px");
		
		mainTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		
		mainTable.setWidth("100%");
		table.setWidget(1,0, mainTable);
		table.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
		
		getButtonOk().setText("Сохранить");
		refresh();
	}

	private void refresh() {
		listProvider = Form.prepareGrid(mainTable, bargain.bargaincostses,false);
		mainTable.setRowCount(bargain.bargaincostses.size());
		
	}

}
