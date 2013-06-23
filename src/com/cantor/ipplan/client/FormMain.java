package com.cantor.ipplan.client;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.client.CellTable.ChangeCheckListEvent;
import com.cantor.ipplan.client.Slider.ChangeEvent;
import com.cantor.ipplan.db.ud.PUserIdent;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class FormMain extends Form {
	
	protected static final int TAB_MAIN = 0;
	protected static final int TAB_BARGAINS = 1;
	protected static final int TAB_CUSTOMER = 2;
	protected static final int TAB_ANALYTICAL = 3;

	public static FormMain currentForm = null;

	PUserWrapper user;
	private FlexTable currentTab = null;
	private Integer currentTabId = 0;
	
	private Label lUserName;
	private MainTabPanel tabPanel;
	CellTable<BargainWrapper> tableAttention;
	private DatabaseServiceAsync dbservice;
	Button btnCustomerAdd;
	Button btnCustomerDelete;
	CellTable<CustomerWrapper> tableCustomer;
	TextBox tbFindCustomer;
	CellTable<BargainWrapper> tableBargain;
	TextBox tbFindBargain;
	MonthPicker filterBargainDate;
	ToggleButton[] filterBargainStatus;
	CheckBox filterBargainAllUsers;
	MenuItem syncMenuItem;
	MenuItem syncAutoMenuItem;
	Button btnBargainAdd;
	Button btnBargainDelete;
	Button btnBargainRefresh;
	MenuItem exportBargainsMenuItem;
	Button btnCustomerRefresh;
	Label lBargainTotal;
	CellTable.Resources resources;


	public FormMain(Ipplan main, RootPanel root, PUserWrapper usr, int numTab) {
		super(main, root);
		user =usr;
		currentForm = this;
		
		// ресурсы для таблицы
		resources = GWT.create(CellTable.Resources.class);
		
		VerticalPanel p0 = new VerticalPanel();
		p0.setSpacing(5);
		p0.setStyleName("gwt-Form");
		initWidget(p0);
		p0.setSize("800px", "812px");
		
		HorizontalPanel p1 = new HorizontalPanel();
		p0.add(p1);
		
		lUserName = new Label("");
		p1.add(lUserName);
		
		tabPanel = new MainTabPanel();
		p0.add(tabPanel);
		p0.setCellHorizontalAlignment(tabPanel, HasHorizontalAlignment.ALIGN_CENTER);
		tabPanel.setSize("100%", "770px");
		tabPanel.setAnimationEnabled(true);
		
		FlexTable tab1 = new TabMain(this,getDataBaseService());
		tabPanel.add(tab1, "Главное", false);
		
		FlexTable tab2 = new TabBargains(this,getDataBaseService());
		tabPanel.add(tab2, "Сделки", false);
		
		FlexTable tab3 = new TabCustomers(this,getDataBaseService());
		tabPanel.add(tab3, "Клиенты", false);
		
		FlexTable tab4 = new TabAnalytical(this,getDataBaseService());
		tabPanel.add(tab4, "Анализ", false);
		
		Label l0 = new Label(" ");
		tabPanel.add(l0, "...", false);
		l0.setSize("5cm", "3cm");
		
		currentTabId = numTab;
		prepare();
	}

	void makeBargainColumns(CellTable<BargainWrapper> table) {
		Column<BargainWrapper, SafeHtml> c1 = new Column<BargainWrapper, SafeHtml>(new ClickableSafeHtmlCell()) {

			@Override
			public SafeHtml getValue(BargainWrapper object) {
				
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if(object!=null) {
					String s = "<b class=\"linkcell\"><div";
					if(object.getFullName().length()>32) s+=" style=\"text-overflow: ellipsis;display:block;\">";
													else s+=">";
					s+=object.getFullName();
					s+="</div></b>";
					sb.appendHtmlConstant(s);
					sb.appendHtmlConstant("<div class=\"statusbox-in-grid\" style=\"background-color:"+StatusWrapper.getBackgroundColor(object.status.statusId)+";"+
							"color:"+StatusWrapper.getTextColor(object.status.statusId)+";"+
							"\">"+object.status.statusName);
					sb.appendHtmlConstant("</div>");
				}
				return sb.toSafeHtml();
			}
		};
		c1.setFieldUpdater(new FieldUpdater<BargainWrapper, SafeHtml>() {
			@Override
			public void update(int index, BargainWrapper object, SafeHtml value) {
				editBargain(object);
			}
		});
		//c1.setCellStyleNames("linkcell");
		
		Column<BargainWrapper,SafeHtml> c2 = new Column<BargainWrapper, SafeHtml>(new ClickableSafeHtmlCell()) {

			@Override
			public SafeHtml getValue(BargainWrapper object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if(object!=null) {
					String s = "<b class=\"linkcell\"><div";
					if(object.customer.customerName.length()>16) s+=" style=\"text-overflow: ellipsis;display:block;\">";
															else s+=">";
					s+=object.customer.customerName;
					s+="</div></b>";
					sb.appendHtmlConstant(s);
					if(object.customer.customerCompany!=null) {
						sb.appendHtmlConstant("<div>"+object.customer.customerCompany);
						if(object.customer.customerPosition!=null)
							sb.appendHtmlConstant(","+object.customer.customerPosition);
						sb.appendHtmlConstant("</div>");
					}
				}
				return sb.toSafeHtml();
			}
			
		};
		c2.setFieldUpdater(new FieldUpdater<BargainWrapper, SafeHtml>() {
			@Override
			public void update(int index, BargainWrapper object, SafeHtml value) {
				editCustomer(object.customer);
			}
		});
		
		
		
		TextColumn<BargainWrapper> c3 = new TextColumn<BargainWrapper>() {
			@Override
			public String getValue(BargainWrapper object) {
				return (object==null)?"":object.puser.puserId==PUserIdent.USER_ROOT_ID?"":object.puser.getFullName();
			}
		};
		
		Column<BargainWrapper, Number> с4 = new Column<BargainWrapper, Number>(new NumberCell(NumberFormat.getFormat("#,##0.00"))) {
			@Override
			public Number getValue(BargainWrapper object) {
				return (object==null)?null:object.bargainRevenue/100.0;
			}
		};
		с4.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		Column<BargainWrapper, Number> с6 = new Column<BargainWrapper, Number>(new NumberCell(NumberFormat.getFormat("#,##0.00"))) {
			@Override
			public Number getValue(BargainWrapper object) {
				return (object==null)?null:object.getProfit()/100.0;
			}
		};
		с6.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		TextColumn<BargainWrapper> c7 = new TextColumn<BargainWrapper>() {
			
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
		
		table.addColumn(c1,"Сделка");
		table.addColumn(c2,"Клиент");
		table.addColumn(c3,"Сотрудник");
		table.addColumn(с4,"Выручка");
		//table.addColumn(с5,"Расходы");
		table.addColumn(с6,"Прибыль");
		table.addColumn(c7,"");
		
		table.setColumnWidth(c1, "200px");
		table.setColumnWidth(c2, "130px");
		table.setColumnWidth(c3, "80px");
		table.setColumnWidth(с4, "80px");
		//table.setColumnWidth(с5, "80px");
		table.setColumnWidth(с6, "80px");
	}

	void makeCustomerColumns(final CellTable<CustomerWrapper> customerTable) {
		
		Column<CustomerWrapper,SafeHtml> c1 = new Column<CustomerWrapper, SafeHtml>(new ClickableSafeHtmlCell()) {

			@Override
			public SafeHtml getValue(CustomerWrapper object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if(object!=null) {
					sb.appendHtmlConstant("<b class=\"linkcell\"><div>"+object.customerName+"</div></b>");
					if(object.customerCompany!=null) {
						sb.appendHtmlConstant("<div>"+object.customerCompany);
						if(object.customerPosition!=null)
							sb.appendHtmlConstant(","+object.customerPosition);
						sb.appendHtmlConstant("</div>");
					}
				}
				return sb.toSafeHtml();
			}
			
		};
		c1.setFieldUpdater(new FieldUpdater<CustomerWrapper, SafeHtml>() {
			@Override
			public void update(int index, CustomerWrapper object, SafeHtml value) {
				editCustomer(object);
			}
		});
		

		Column<CustomerWrapper,SafeHtml> c2 = new Column<CustomerWrapper, SafeHtml>(new SafeHtmlCell()) {

			@Override
			public SafeHtml getValue(CustomerWrapper object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if(object!=null && object.customerPrimaryEmail!=null) {
					sb.appendHtmlConstant("<a href=\"mailto:"+object.customerPrimaryEmail+"\">"+object.customerPrimaryEmail+"</a>");
				}
				return sb.toSafeHtml();
			}
			
		};

		Column<CustomerWrapper,SafeHtml> c3 = new Column<CustomerWrapper, SafeHtml>(new SafeHtmlCell()) {

			@Override
			public SafeHtml getValue(CustomerWrapper object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if(object!=null && object.customerEmails!=null) {
					String[] emails = object.customerEmails.split(",");
					for (int i = 0; i < emails.length; i++) {
						if(i>0) sb.append(',');
						sb.appendHtmlConstant("<a href=\"mailto:"+emails[i]+"\">"+emails[i]+"</a>");
					}
				}
				return sb.toSafeHtml();
			}
			
		};
				
		Column<CustomerWrapper,SafeHtml> c4 = new Column<CustomerWrapper, SafeHtml>(new SafeHtmlCell()) {

			@Override
			public SafeHtml getValue(CustomerWrapper object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if(object!=null && object.customerPrimaryPhone!=null) {
					sb.appendHtmlConstant(Ipplan.getPhoneLink(object.customerPrimaryPhone));
				}
				return sb.toSafeHtml();
			}
			
		};

		Column<CustomerWrapper,SafeHtml> c5 = new Column<CustomerWrapper, SafeHtml>(new SafeHtmlCell()) {

			@Override
			public SafeHtml getValue(CustomerWrapper object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if(object!=null && object.customerPhones!=null) {
					String[] phones = object.customerPhones.split(",");
					for (int i = 0; i < phones.length; i++) {
						if(i>0) sb.append(',');
						sb.appendHtmlConstant(Ipplan.getPhoneLink(phones[i]));
					}
				}
				return sb.toSafeHtml();
			}
			
		};
		
		customerTable.createCheckedColumn(new ChangeCheckListEvent() {
			@Override
			public void onChange() {
				btnCustomerDelete.setEnabled(customerTable.getCheckedList().size()>0);
			}
		});
		customerTable.addColumn(c1,"Имя");
		customerTable.addColumn(c2,"Основной e-mail");
		customerTable.addColumn(c3,"Дополнительные");
		customerTable.addColumn(c4,"Основной телефон");
		customerTable.addColumn(c5,"Другие телефоны");
		
		customerTable.setColumnWidth(c1, "200px");
		
	}

	protected void editCustomer(final CustomerWrapper c) {
		if(c!=null) 
			// edit	
			FormCustomer.edit(dbservice, c, new NotifyHandler<CustomerWrapper>() {
				@Override
				public void onNotify(CustomerWrapper newc) {
					List<CustomerWrapper> list = tableCustomer.getProvider().getList();
					list.set(list.indexOf(c),newc);
					int r = tableCustomer.getVisibleItems().indexOf(newc);
					if(r>=0) tableCustomer.redrawRow(r);
					toast(tableCustomer, "Клиент был успешно изменен");
				}
			}); else
			// add	
			FormCustomer.add(dbservice, new NotifyHandler<CustomerWrapper>() {
				@Override
				public void onNotify(CustomerWrapper newc) {
					tableCustomer.getProvider().getList().add(newc);
					// TODO найти страницу и на нее встать
					
					toast(tableCustomer, "Клиент "+newc.customerName+" был успешно добавлен");
				}
			});
	}

	protected void editBargain(BargainWrapper b) {
		// поиск в окрытых вкладках
		int tabidx = tabPanel.find(b.bargainId);
		if(tabidx>=0) {
			tabPanel.selectBargain(b.bargainId);
			tabPanel.getFormBargain(tabidx).selectHeadVersion();
			return;
		}
		
		DatabaseServiceAsync db = getDataBaseService();
		db.editBargain(b.bargainId, new AsyncCallback<BargainWrapper>() {
			
			@Override
			public void onSuccess(BargainWrapper result) {
				tabPanel.add(result);
				tabPanel.selectBargain(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
	}

	protected void addNewBargain() throws Exception {
		
		Label l;
		final long weekTime = 7 * 24 * 60 * 60 * 1000; // 7 d * 24 h * 60 min * 60 s * 1000 millis
		
		final Dialog dialog = new Dialog("Создание новой сделки");
		FlexTable table = dialog.getContent();
		//table.setWidget(0,0,new Label("Наименование сделки"));
		final TextBox tbBargainName = new TextBox();
		tbBargainName.getElement().setAttribute("placeholder", "Введите наименование сделки");
		tbBargainName.setWidth("400px");
		table.setWidget(0, 0, tbBargainName);
		tbBargainName.setName("bargainName");
		tbBargainName.getElement().setAttribute("autocomplete", "on");
		table.getFlexCellFormatter().setColSpan(0, 0, 2);

		final RadioButton rb1 = new RadioButton("status","Начать с продажи");
		table.setWidget(1, 0, rb1);
		table.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
		final RadioButton rb2 = new RadioButton("status","Осталось только исполнить");
		table.setWidget(1, 1, rb2);
		table.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
		rb1.setValue(true);

		VerticalPanel p1 = new VerticalPanel();
		l = new Label("Начать");
		p1.add(l);
		l.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		final DateBox dbstart = new DateBox();
		dbstart.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
		dbstart.setValue(new Date());
		p1.add(dbstart);
		p1.setWidth("100%");
		table.setWidget(2, 0, p1);
		p1.setCellHorizontalAlignment(dbstart, HasHorizontalAlignment.ALIGN_CENTER);

		
		VerticalPanel p2 = new VerticalPanel();
		l = new Label("Закончить");
		p2.add(l);
		l.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		final DateBox dbfinish = new DateBox();
		dbfinish.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
		p2.add(dbfinish);
		p2.setWidth("100%");
		table.setWidget(2, 1, p2);
		p2.setCellHorizontalAlignment(dbfinish, HasHorizontalAlignment.ALIGN_CENTER);
		
		table.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
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
		s.setChangePositionEvent(new ChangeEvent() {
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
		table.setWidget(3, 0, p3);
		table.getFlexCellFormatter().setColSpan(3, 0, 2);
		table.getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.getCellFormatter().getElement(3, 0).getStyle().setPaddingTop(0, Unit.PX);
		table.getCellFormatter().getElement(2, 0).getStyle().setPaddingBottom(0, Unit.PX);
		table.getCellFormatter().getElement(2, 1).getStyle().setPaddingBottom(0, Unit.PX);
		
		
		dialog.getButtonOk().setText("Создать");
		dialog.setButtonOkClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.cancel();
				dialog.resetErrors();
				if(tbBargainName.getText().isEmpty()) {
					dialog.showError(tbBargainName, "Наименование сделки не может быть пустым");
					return;
				}
				if(dbstart.getValue().after(dbfinish.getValue())) {
					dialog.showError(dbstart, "Дата начала должна быть меньше даты окончания");
					return;
				}
				
				DatabaseServiceAsync db = getDataBaseService();
				db.newBargain(tbBargainName.getText(), rb1.getValue()?StatusWrapper.PRIMARY_CONTACT:StatusWrapper.EXECUTION,
						dbstart.getValue(),dbfinish.getValue(),
					new AsyncCallback<BargainWrapper>() {
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
			}
		});
			
		dialog.setFirstFocusedWidget(tbBargainName);
		dialog.center();
		
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

				switch (currentTabId) {
				case TAB_MAIN:
					((TabMain)currentTab).refresh();
					break;
				// Сделки
				case TAB_BARGAINS:
					break;
				// Клиенты
				case TAB_CUSTOMER:
					if(tableCustomer.getProvider()==null) ((TabCustomers)currentTab).refreshCustomers();
													 else tableCustomer.redraw();
					break;
				case TAB_ANALYTICAL:
					break;

				default:
					break;
				}
				
				History.newItem("main."+currentTabId, false);
			}
		});
		
		startRecoveryEditBargain();
		
		tabPanel.getTabBar().selectTab(currentTabId);
		
		// получим все статусы
		StatusWrapper.requestStatusesOnServer(getDataBaseService());
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
}
