package com.cantor.ipplan.client;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.client.widgets.CellTable;
import com.cantor.ipplan.client.widgets.CheckBox;
import com.cantor.ipplan.client.widgets.MonthPicker;
import com.cantor.ipplan.client.widgets.RadioButton;
import com.cantor.ipplan.client.widgets.Slider;
import com.cantor.ipplan.client.widgets.CellTable.ChangeCheckListEvent;
import com.cantor.ipplan.client.widgets.Slider.ChangeEvent;
import com.cantor.ipplan.db.ud.PUserIdent;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.cantor.ipplan.client.widgets.HorizontalPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.cantor.ipplan.client.widgets.VerticalPanel;
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
	public MenuItem syncBargainsMenuItem;
	public MenuItem syncAutoBargainsMenuItem;
	public ToggleButton allBtn;


	public FormMain(UserData main, RootPanel root, PUserWrapper usr, int numTab) {
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
		p1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		p0.add(p1);
		
		lUserName = new Label("");
		lUserName.addStyleName("link");
		lUserName.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getDataBaseService().getConfig("IpplanHost", new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						Window.Location.assign(result+"#profile,session="+Cookies.getCookie("sid"));
					}
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
			}
		});
		p1.add(lUserName);
		
		Button btn = new Button("Выйти");
		btn.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				getDataBaseService().getConfig("IpplanHost", new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						Window.Location.assign(result+"#login");
					}
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
			}
		});
		p1.add(btn);
		p1.setCellHorizontalAlignment(btn, HasHorizontalAlignment.ALIGN_RIGHT);
		
		
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
		
		FlexTable tab4 = new TabAnalytical(this);
		tabPanel.add(tab4, "Анализ", false);
		
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
		
		customerTable.createCheckedColumn(new ChangeCheckListEvent<CustomerWrapper>() {
			@Override
			public void onChange(CustomerWrapper object, boolean check) {
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
			FormCustomer.add(dbservice, "", new NotifyHandler<CustomerWrapper>() {
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
		final Dialog dialog = new FormNewBargain("Создание новой сделки", getDataBaseService(),
				new NotifyHandler<BargainWrapper>() {
					@Override
					public void onNotify(BargainWrapper c) {
						tabPanel.add(c);
						tabPanel.selectBargain(c);
					}
				});
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
