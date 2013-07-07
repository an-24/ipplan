package com.cantor.ipplan.client;

import java.util.Date;

import com.cantor.ipplan.client.widgets.CurrencyBox;
import com.cantor.ipplan.client.widgets.RadioButton;
import com.cantor.ipplan.client.widgets.Slider;
import com.cantor.ipplan.client.widgets.TextBox;
import com.cantor.ipplan.client.widgets.VerticalPanel;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.datepicker.client.DateBox;

public class FormNewBargain extends Dialog {

	private DatabaseServiceAsync dbservice;
	final long weekTime = 7 * 24 * 60 * 60 * 1000; // 7 d * 24 h * 60 min * 60 s * 1000 millis
	private CurrencyBox tbRevenue;
	
	public FormNewBargain(String caption, DatabaseServiceAsync db,final NotifyHandler<BargainWrapper> ok) throws Exception {
		super(caption);
		this.dbservice = db;
		
		int row = 0;
		FlexTable table = this.getContent();
		final TextBox tbBargainName = new TextBox();
		tbBargainName.getElement().setAttribute("placeholder", "Введите наименование сделки");
		tbBargainName.setWidth("400px");
		table.setWidget(row, 0, tbBargainName);
		tbBargainName.setName("bargainName");
		tbBargainName.getElement().setAttribute("autocomplete", "on");
		table.getFlexCellFormatter().setColSpan(row, 0, 2);

		row++;
		
		table.setWidget(row, 0, new Label("Планируемая выручка"));
		tbRevenue = new CurrencyBox();
		table.setWidget(row, 1, tbRevenue);
		table.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		
		row++;
		
		final RadioButton rb1 = new RadioButton("status","Начать с продажи");
		table.setWidget(row, 0, rb1);
		table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
		final RadioButton rb2 = new RadioButton("status","Осталось только исполнить");
		table.setWidget(row, 1, rb2);
		table.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_CENTER);
		rb1.setValue(true);

		row++;
		
		VerticalPanel p1 = new VerticalPanel();
		Label l = new Label("Начать");
		p1.add(l);
		l.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		final DateBox dbstart = new DateBox();
		dbstart.setFormat(Ipplan.DEFAULT_DATE_FORMAT);
		dbstart.setValue(new Date());
		p1.add(dbstart);
		p1.setWidth("100%");
		table.setWidget(row, 0, p1);
		p1.setCellHorizontalAlignment(dbstart, HasHorizontalAlignment.ALIGN_CENTER);

		
		VerticalPanel p2 = new VerticalPanel();
		l = new Label("Закончить");
		p2.add(l);
		l.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		final DateBox dbfinish = new DateBox();
		dbfinish.setFormat(Ipplan.DEFAULT_DATE_FORMAT);
		p2.add(dbfinish);
		p2.setWidth("100%");
		table.setWidget(row, 1, p2);
		p2.setCellHorizontalAlignment(dbfinish, HasHorizontalAlignment.ALIGN_CENTER);
		
		table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
		row++;
		
		VerticalPanel p3 = new VerticalPanel();
		p3.setSpacing(6);
		Slider s = new Slider();
		s.setWidth("300px");
		s.setMin(1., "1 неделя");
		s.setMax(12., "3 месяца");
		s.setValues(new Double[]{2.0,3.0,4.0,5.,6.,7.,8.,9.,10.,11.}, 
				    new String[]{"2 недели","3 недели","месяц", "1 месяц, 1 неделя","1 месяц, 2 недели","1 месяц, 3 недели", "2 месяца",
							     "2 месяца, 1 неделя", "2 месяца, 2 недели", "2 месяца, 3 недели"});
		final Label lduration = new Label("");
		p3.add(lduration);
		lduration.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		s.setChangePositionEvent(new Slider.ChangeEvent() {
			@Override
			public void onChangePosition(double value, String label) {
				lduration.setText(label);
				Date start = dbstart.getValue();
				dbfinish.setValue(new Date(start.getTime()+Math.round(value)*weekTime));
			}

			@Override
			public void onDragPosition(double value, String label) {
				lduration.setText(label);
			}
		});
		s.setPosition(s.getMin()+2);
		p3.add(s);
		p3.setCellHorizontalAlignment(s, HasHorizontalAlignment.ALIGN_CENTER);
		table.setWidget(row, 0, p3);
		table.getFlexCellFormatter().setColSpan(row, 0, 2);
		table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.getCellFormatter().getElement(row, 0).getStyle().setPaddingTop(0, Unit.PX);
		table.getCellFormatter().getElement(row, 0).getStyle().setPaddingBottom(0, Unit.PX);
		//table.getCellFormatter().getElement(row, 1).getStyle().setPaddingBottom(0, Unit.PX);
		
		
		this.getButtonOk().setText("Создать");
		this.setButtonOkClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				cancel();
				resetErrors();
				if(tbBargainName.getText().isEmpty()) {
					showError(tbBargainName, "Наименование сделки не может быть пустым");
					return;
				}
				if(tbRevenue.getValue()==null) {
					showError(tbRevenue, "Необходимо определить планируемую выручку");
					return;
				}
				
				if(dbstart.getValue().after(dbfinish.getValue())) {
					showError(dbstart, "Дата начала должна быть меньше даты окончания");
					return;
				}
				
				dbservice.newBargain(tbBargainName.getText(), rb1.getValue()?StatusWrapper.PRIMARY_CONTACT:StatusWrapper.EXECUTION,
						dbstart.getValue(),dbfinish.getValue(),
					new AsyncCallback<BargainWrapper>() {
					@Override
					public void onSuccess(BargainWrapper result) {
						result.bargainRevenue = tbRevenue.getValue();
						ok.onNotify(result);
						hide();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
			}
		});
			
		this.setFirstFocusedWidget(tbBargainName);
	}

}
