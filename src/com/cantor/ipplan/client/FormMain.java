package com.cantor.ipplan.client;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.client.CellTable.ChangeCheckListEvent;
import com.cantor.ipplan.client.OAuth2.EventOnCloseWindow;
import com.cantor.ipplan.client.Slider.ChangeEvent;
import com.cantor.ipplan.db.ud.PUserIdent;
import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.ImportExportProcessInfo;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class FormMain extends Form {
	
	private final static String USER_AGENT = getUserAgent();
	private final static boolean USER_AGENT_IPHONE = USER_AGENT.indexOf("iPhone")>=0;
	
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
	private HTML linkAutoSync;

	private Button btnSync;

	private Button btnCustomerAdd;

	private Button btnCustomerDelete;

	private CellTable<CustomerWrapper> tableCustomer;

	private TextBox tbFindCustomer;

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
		p0.setCellHorizontalAlignment(tabPanel, HasHorizontalAlignment.ALIGN_CENTER);
		tabPanel.setSize("100%", "564px");
		tabPanel.setAnimationEnabled(true);
		
		FlexTable tab1 = new FlexTable();
		tabPanel.add(tab1, "Главное", false);
		tab1.setSize("100%", "3cm");
		
		Button btnNew = new Button("Создать новую сделку");
		btnNew.addStyleName("mainCommand");
		btnNew.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				try {
					addNew();
				} catch (Exception e) {
					Ipplan.error(e);
				}
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
		lProfit.setStyleName("gwt-CurrencyLabel");
		lProfit.addStyleName("bold-text");
		lProfit.addStyleName("auto-width");
		
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
		
		tableAttention = new CellTable<BargainWrapper>(5);
		tableAttention.setSelectionModel(null);

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
				edit(object);
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
		
		tableAttention.setColumnWidth(c1, "300px");
		tableAttention.setColumnWidth(c2, "150px");
		tableAttention.setColumnWidth(с3, "80px");
		
		tab1.getFlexCellFormatter().setColSpan(4, 0, 3);
		tab1.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_BOTTOM);
		tab1.getCellFormatter().setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_BOTTOM);
		
		FlexTable tab2 = new FlexTable();
		tabPanel.add(tab2, "Сделки", false);
		tab2.setSize("100%", "3cm");
		
		FlexTable tab3 = new FlexTable();
		tabPanel.add(tab3, "Клиенты", false);
		tab3.setSize("100%", "3cm");

		
		HorizontalPanel p;
		p = new HorizontalPanel();
		p.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		p.setSpacing(10);
		btnSync = new Button("Синхронизировать прямо сейчас");
		p.add(btnSync);
		btnSync.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				syncContactsdDrectly();
			}
		});
		linkAutoSync = new HTML(getContactAutoSyncCaption());
		linkAutoSync.setStyleName("link");
		linkAutoSync.getElement().getStyle().setTextAlign(TextAlign.CENTER);
		linkAutoSync.getElement().getStyle().setBorderWidth(0, Unit.PX);
		p.add(linkAutoSync);
		linkAutoSync.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				syncContactsdAuto();
			}
		});
		
		tab3.setWidget(0, 1, p);

		p = new HorizontalPanel();
		p.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		p.setSpacing(10);
		btnCustomerAdd = new Button("Новый");
		btnCustomerAdd.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				editCustomer(null);
			}
		});
		p.add(btnCustomerAdd);
		btnCustomerDelete = new Button("Удалить");
		btnCustomerDelete.setEnabled(false);
		btnCustomerDelete.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final List<CustomerWrapper> list = tableCustomer.getCheckedList();
				dbservice.deleteCustomer(list, new AsyncCallback<Void>() {
					
					@Override
					public void onSuccess(Void result) {
						List<CustomerWrapper> all = tableCustomer.getProvider().getList();
						for (CustomerWrapper cw : list) all.remove(cw);
						list.clear();
						tableCustomer.redraw();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
						
					}
				});
			}
		});
		p.add(btnCustomerDelete);
		tab3.setWidget(0, 0, p);
		tab3.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
		tab3.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		
		p = new HorizontalPanel();
		tbFindCustomer = new TextBox();
		tbFindCustomer.setWidth("300px");
		tbFindCustomer.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeEvent().getKeyCode()==KeyCodes.KEY_ENTER) 
					startCustomers(tbFindCustomer.getText());
			}
		});
		p.add(tbFindCustomer);
		Button btn = new Button("Найти");
		btn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				startCustomers(tbFindCustomer.getText());
			}
		});
		p.add(btn);
		p.setSpacing(2);
		tab3.setWidget(1, 0, p);
		
		tableCustomer = new CellTable<CustomerWrapper>(10);
		tableCustomer.setSelectionModel(null);
		tableCustomer.setWidth("100%");
		
		setColumnCustomerTable(tableCustomer);

		SimplePager pager = new GridPager();
		pager.setDisplay(tableCustomer);
		tab3.setWidget(1,1, pager);
		tab3.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		
		tab3.getFlexCellFormatter().setColSpan(2, 0, 2);
		tab3.setWidget(2,0, tableCustomer);
		tab3.getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
		
		
		
		FlexTable tab4 = new FlexTable();
		tabPanel.add(tab4, "Анализ", false);
		tab4.setSize("100%", "3cm");
		
		Label l0 = new Label(" ");
		tabPanel.add(l0, "...", false);
		l0.setSize("5cm", "3cm");
		
		currentTabId = numTab;
		prepare();
	}
	
	private String getPhoneLink(String phstr) {
		String phone,phonedisplay;
		String tolink = "callto:";
		if(USER_AGENT_IPHONE) tolink = "tel:";
		phonedisplay = phstr;
		String[] phonecomp = phstr.split(":");
		if(phonecomp.length>1) {
			phone = phonecomp[1].replaceAll("\\s|[()-]","");
		} else {
			phone = phonecomp[0].replaceAll("\\s|[()-]","");
		}
		return "<a href=\""+tolink+phone+"\">"+phonedisplay+"</a>";
	}
	
	class ClickableSafeHtmlCell extends AbstractCell<SafeHtml> {
		
		ClickableSafeHtmlCell() {
			super(CLICK, KEYDOWN);
		}
		
		@Override
		public void onBrowserEvent(Context context, Element parent, SafeHtml value,
		      NativeEvent event, ValueUpdater<SafeHtml> valueUpdater) {
		    super.onBrowserEvent(context, parent, value, event, valueUpdater);
		    if (CLICK.equals(event.getType())) {
		      onEnterKeyDown(context, parent, value, event, valueUpdater);
		    }
		}
		@Override
		protected void onEnterKeyDown(Context context, Element parent, SafeHtml value,
		      NativeEvent event, ValueUpdater<SafeHtml> valueUpdater) {
		    if (valueUpdater != null) {
		      valueUpdater.update(value);
		    }
		}
		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context,
				SafeHtml value, SafeHtmlBuilder sb) {
		    if (value != null) {
		        sb.append(value);
		      }
		}
	}
	
	private void setColumnCustomerTable(final CellTable<CustomerWrapper> customerTable) {
		
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
					sb.appendHtmlConstant(getPhoneLink(object.customerPrimaryPhone));
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
						sb.appendHtmlConstant(getPhoneLink(phones[i]));
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
		final FormCustomer form = new FormCustomer(c);
		
		if(c==null) form.setExternalHandler(
			new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					dbservice.addCustomer(form.getCustomer(), new AsyncCallback<CustomerWrapper>() {
						@Override
						public void onSuccess(CustomerWrapper result) {
							tableCustomer.getProvider().getList().add(result);
							// TODO найти страницу и на нее встать
							
							toast(tableCustomer, "Клиент "+result.customerName+" был успешно добавлен");
						};
						@Override
						public void onFailure(Throwable e) {
							Ipplan.showError(e);
							
						}
					});
					
				}
			}); else form.setExternalHandler(
				new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					dbservice.updateCustomer(form.getCustomer(), new AsyncCallback<Void>() {
						
						@Override
						public void onSuccess(Void result) {
							List<CustomerWrapper> list = tableCustomer.getProvider().getList();
							list.set(list.indexOf(c),form.getCustomer());
							int r = tableCustomer.getVisibleItems().indexOf(form.getCustomer());
							if(r>=0) tableCustomer.redrawRow(r);
							toast(tableCustomer, "Клиент был успешно изменен");
						}
						
						@Override
						public void onFailure(Throwable e) {
							Ipplan.showError(e);
						}
					});
					
				}
			});
		
		form.center();
	}

	private String getContactAutoSyncCaption() {
		String s;
		switch (user.puserContactSyncDuration) {
			case 0:
				s = "[нет]"; 
				break;
			case 30*60:
				s = "[раз в полчаса]"; 
				break;
			case 60*60:
				s = "[раз в час]"; 
				break;
			case 24*60*60:
				s = "[раз в сутки]"; 
				break;
			default:
				s = "[раз в "+user.puserContactSyncDuration/60+" минут]";
				break;
		}
		return "Автоматическая<br>синхронизация<br>"+s;
	}

	protected void edit(BargainWrapper b) {
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
				FormBargain bft = tabPanel.add(result);
				tabPanel.selectBargain(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
	}

	protected void addNew() throws Exception {
		
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
						FormBargain bft = tabPanel.add(result);
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

				
				// Сделки
				//if(currentTabId==1) bargainGrid.redraw();
				
				// Клиенты
				if(currentTabId==2) {
					if(tableCustomer.getProvider()==null) refreshCustomers();
							else tableCustomer.redraw();
				}
				
				History.newItem("main."+currentTabId, false);
			}
		});
		
		startAttention();
		startTotals();
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
	
	private void startCustomers(String query) {
		DatabaseServiceAsync db = getDataBaseService();
		db.findCustomer(query, new AsyncCallback<List<CustomerWrapper>>() {
			
			@Override
			public void onSuccess(List<CustomerWrapper> result) {
				prepareGrid(tableCustomer, result,true);
				tableCustomer.setRowCount(result.size());
			}
			
			@Override
			public void onFailure(Throwable e) {
				Ipplan.showError(e);
				
			}
		});
	}
	

	private void startRecoveryEditBargain() {
		DatabaseServiceAsync db = getDataBaseService();
		db.getTemporalyBargains(new AsyncCallback<List<BargainWrapper>>() {
			
			@Override
			public void onSuccess(List<BargainWrapper> result) {
				for (BargainWrapper bw : result) {
					FormBargain bft = tabPanel.add(bw);
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

	private void syncContactsdAuto() {
		final Dialog dialog = new Dialog("Выберите вариант синхронизации");
		final FlexTable table = dialog.getContent();
		
		int duration = user.puserContactSyncDuration;
		
		RadioButton rb;
		rb = new RadioButton("gr", "Не синхронизировать");
		rb.setValue(duration==0);
		final RadioButton rb0 = rb;
		table.setWidget(0, 0, rb);
		rb = new RadioButton("gr", "Раз в полчаса");
		rb.setValue(duration==30*60);
		table.setWidget(1, 0, rb);
		rb = new RadioButton("gr", "Раз в час");
		rb.setValue(duration==60*60);
		table.setWidget(2, 0, rb);
		rb = new RadioButton("gr", "Раз в сутки");
		rb.setValue(duration==24*60*60);
		table.setWidget(3, 0, rb);
		
		dialog.getButtonOk().setText("Установить");
		dialog.setButtonOkClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.cancel();
				
				// запрос разрешения
				if(!rb0.getValue()) {
					OAuth2 auth = new OAuth2(Utils.GOOGLE_AUTH_URL, Utils.GOOGLE_CLIENT_ID,
							Utils.GOOGLE_SCOPE, Utils.REDIRECT_URI);
					auth.loginOffline(new EventOnCloseWindow() {
						@Override
						public void onCloseWindow() {
							for (int i = 1, len = table.getRowCount(); i < len; i++) {
								RadioButton rb = (RadioButton) table.getWidget(i,0);
								if(rb.getValue()) {
									final int durationClass = i;
									dbservice.setContactsAutoSync(i, new AsyncCallback<Void>() {
										
										@Override
										public void onSuccess(Void result) {
											user.puserContactSyncDuration = 0;
											switch (durationClass) {
												case 1: //полчаса 
													user.puserContactSyncDuration = 30*60;
												break;
												case 2: //час 
													user.puserContactSyncDuration = 60*60;
												break;
												case 3: //сутки 
													user.puserContactSyncDuration = 24*60*60;
												break;
											}
											linkAutoSync.setHTML(getContactAutoSyncCaption());
										}
										
										@Override
										public void onFailure(Throwable e) {
											Ipplan.showError(e);
										}
									});
									dialog.hide();
									break;
								}
							}
						}
					});
				} else
				dbservice.setContactsAutoSync(0, new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						user.puserContactSyncDuration = 0;
						linkAutoSync.setHTML(getContactAutoSyncCaption());
						dialog.hide();
					}
					@Override
					public void onFailure(Throwable e) {
						Ipplan.showError(e);
					}
				});
			}
		});
		dialog.center();
	}

	private void syncContactsdDrectly() {
		dbservice.syncContacts(new AsyncCallback<ImportExportProcessInfo>() {
			
			@Override
			public void onSuccess(ImportExportProcessInfo result) {
				// получение token
				if(result.getError()==ImportExportProcessInfo.TOKEN_NOTFOUND) {
					OAuth2 auth = new OAuth2(Utils.GOOGLE_AUTH_URL, Utils.GOOGLE_CLIENT_ID,
							Utils.GOOGLE_SCOPE, Utils.REDIRECT_URI);
					auth.login(new EventOnCloseWindow() {
						@Override
						public void onCloseWindow() {
							btnSync.click();
						}
					});
				} else
				// обовление token
				if(result.getError()==ImportExportProcessInfo.TOKEN_EXPIRED) {
					dbservice.refreshGoogleToken(new AsyncCallback<Void>() {
						
						@Override
						public void onSuccess(Void result) {
							btnSync.click(); //!attention, its's recursion
						}
						
						@Override
						public void onFailure(Throwable e) {
							Ipplan.showError(e);
						}
					});
				} else {
					refreshCustomers();	
					toast(btnSync, "Синхронизация окончена. При импорте обработано "+result.getImportAllCount()+
							   " записей , из них новых - "+result.getImportInsert()+". При экспорте обработано "+result.getExportAllCount()+
							   " записей , из них новых - "+result.getExportInsert()+"."
					      );
				}
			}
			@Override
			public void onFailure(Throwable e) {
				Ipplan.showError(e);
			}
		});
	}

	private void refreshCustomers() {
		startCustomers(tbFindCustomer.getText());
	}
		
}
