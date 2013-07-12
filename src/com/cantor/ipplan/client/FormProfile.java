package com.cantor.ipplan.client;

import java.util.Date;

import com.cantor.ipplan.client.widgets.CellTable;
import com.cantor.ipplan.client.widgets.CheckBox;
import com.cantor.ipplan.client.widgets.ComboBox;
import com.cantor.ipplan.client.widgets.GridPager;
import com.cantor.ipplan.client.widgets.HorizontalPanel;
import com.cantor.ipplan.client.widgets.RadioButton;
import com.cantor.ipplan.client.widgets.VerticalPanel;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.PaymentsWrapper;
import com.cantor.ipplan.shared.SyncWrapper;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;


public class FormProfile extends Form {

	private TextBox tbEmail;
	private TextBox tbName;
	private FlexTable Tabl1;
	private FlexTable currentTab;
	private TabPanel tabPanel;
	private Button btnSave;
	private Button btnAdd;
	private Button btnDelete;
	private ListBox lbChildren;
	private PUserWrapper user;
	private ComboBox cbTarif;
	private CellTable<SyncWrapper> syncGrid;
	private CellTable<PaymentsWrapper> payGrid;

	//private LoginServiceAsync loginService = null;
	private ProfileServiceAsync profService = null;
	
	
	public FormProfile(Ipplan main, RootPanel root, PUserWrapper usr) {
		super(main, root);
		this.user = usr;
		
		VerticalPanel p0 = new VerticalPanel();
		p0.setStyleName("gwt-Form");
		initWidget(p0);
		p0.setSize("634px", "513px");
		
		HorizontalPanel p1 = new HorizontalPanel();
		p1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		p0.add(p1);
		p1.setWidth("629px");
		
		Label l1 = new Label("Профиль пользователя");
		p1.add(l1);
		l1.setWidth("325px");
		l1.setStyleName("gwt-FormCaption");
		
		Button btnExit = new Button("Выйти");
		btnExit.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				LoginServiceAsync loginService = GWT.create(LoginService.class);
				loginService.logout(new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						com.google.gwt.user.client.Window.Location.reload();
					}
					public void onFailure(Throwable caught) {
					}
				});
			}
		});
		p1.add(btnExit);
		p1.setCellHorizontalAlignment(btnExit, HasHorizontalAlignment.ALIGN_RIGHT);
		
		HorizontalPanel p3 = new HorizontalPanel();
		p0.add(p3);
		p3.setHeight("");
		
		Label lblUser = new Label("user23");
		lblUser.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		lblUser.addStyleName("lpad10");
		lblUser.addStyleName("bold-text");
		lblUser.setWordWrap(false);
		lblUser.setText(user.getFullName());
		p3.add(lblUser);
		p3.setCellVerticalAlignment(lblUser, HasVerticalAlignment.ALIGN_MIDDLE);
		
		Label l2 = new Label(", последний раз был: ");
		p3.add(l2);
		p3.setCellVerticalAlignment(l2, HasVerticalAlignment.ALIGN_MIDDLE);
		
		@SuppressWarnings("deprecation")
		Label lbLastaccess = new Label(DateTimeFormat.getMediumDateTimeFormat().format(user.puserLastaccess));
		p3.add(lbLastaccess);
		p3.setCellVerticalAlignment(lbLastaccess, HasVerticalAlignment.ALIGN_MIDDLE);
		
		CaptionPanel pMessage = new CaptionPanel("Вам сообщение");
		pMessage.setStyleName("messageToUser");
		p0.add(pMessage);

		if(user.puserLock!=0) {
			HTML htm = new HTML("Аккаунт временно заблокирован. Причина: "+user.puserLockReason);
			htm.addStyleName("serverResponseLabelError");
			pMessage.add(htm);
		} else
			pMessage.setVisible(false);
		

		String sTmp = "Открыть базу данных";
		if(user.isDatabaseCreateNeeded()) sTmp = "Создать базу данных";
		Button btnOpenDB = new Button(sTmp);
		btnOpenDB.addStyleName("mainCommand");
		
		btnOpenDB.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				enterService();
			}
		});
		btnOpenDB.setEnabled(user.canEnterService());
		
		p0.add(btnOpenDB, "table-cell");
		p0.getCellElement(btnOpenDB).getStyle().setPaddingTop(6, Unit.PX);
		p0.getCellElement(btnOpenDB).getStyle().setPaddingBottom(6, Unit.PX);
		p0.setCellVerticalAlignment(btnOpenDB, HasVerticalAlignment.ALIGN_MIDDLE);
		p0.setCellHorizontalAlignment(btnOpenDB, HasHorizontalAlignment.ALIGN_CENTER);
		
		tabPanel = new TabPanel();
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
				int tabId = event.getSelectedItem();
				currentTab = (FlexTable) tabPanel.getWidget(tabId);
				FocusWidget w = getFirstFocusedWidget(currentTab);
				if(w!=null) w.setFocus(true);
				if(tabId==3) syncGrid.redraw();
				if(tabId==1) payGrid.redraw();
			}
		});
		tabPanel.setAnimationEnabled(true);
		p0.add(tabPanel);
		tabPanel.setSize("100%", "434px");
		
		Tabl1 = new FlexTable();
		Tabl1.setCellSpacing(4);
		Tabl1.setCellPadding(10);
		tabPanel.add(Tabl1, "Общие сведения", false);
		Tabl1.setSize("100%", "3cm");
		
		Label label = new Label("Дата создания");
		Tabl1.setWidget(0, 0, label);
		
		@SuppressWarnings("deprecation")
		Label lDateCreated = new Label(DateTimeFormat.getMediumDateFormat().format(user.puserCreated));
		lDateCreated.setStyleName("gwt-TextBox");
		Tabl1.setWidget(0, 1, lDateCreated);
		lDateCreated.setWidth("132px");
		
		Label l3 = new Label("Имя пользователя");
		Tabl1.setWidget(1, 0, l3);
		
		tbName = new TextBox();
		Tabl1.setWidget(1, 1, tbName);
		tbName.setWidth("300px");
		tbName.setText(user.puserLogin);
		
		Label l4 = new Label("* Адрес электронной почты");
		Tabl1.setWidget(2, 0, l4);
		
		tbEmail = new TextBox();
		Tabl1.setWidget(2, 1, tbEmail);
		tbEmail.setWidth("300px");
		tbEmail.setText(user.puserEmail);
		
		Label l31 = new Label("Вид налогооблажения");
		Tabl1.setWidget(3, 0, l31);
		
		HorizontalPanel p30 = new HorizontalPanel();
		Tabl1.setWidget(3, 1, p30);
		p30.setWidth("100%");
		Tabl1.getCellFormatter().setWidth(3, 1, "");
		
		final RadioButton rbTaxType1 = new RadioButton("taxtype", "По ставке 6% от дохода");
		rbTaxType1.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(user.puserTaxtype!=1)
				 toast(rbTaxType1,"Изменение коснется только новых сделок");
			}
		});
		p30.add(rbTaxType1);
		rbTaxType1.setValue(user.puserTaxtype==1);
		
		final RadioButton rbTaxType2 = new RadioButton("taxtype", "По ставке 15% от доходов, уменьшенных на величину расходов");
		rbTaxType2.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(user.puserTaxtype!=2)
				 toast(rbTaxType2,"Изменение коснется только новых сделок");
			}
		});
		p30.add(rbTaxType2);
		rbTaxType2.setWidth("155px");
		rbTaxType2.setValue(user.puserTaxtype==2);
		
		Label l9 = new Label("Тарифный план");
		Tabl1.setWidget(4, 0, l9);
		
		cbTarif = new ComboBox();
		cbTarif.addItem("Минимальный");
		cbTarif.addItem("Стандартный");
		cbTarif.setSelectedIndex(user.puserTarif);
		Tabl1.setWidget(4, 1, cbTarif);
		cbTarif.setWidth("310px");
		
		final CheckBox cbBoss = new CheckBox("Босс-аккаунт");
		cbBoss.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				btnAdd.setEnabled(cbBoss.getValue());
				btnDelete.setEnabled(cbBoss.getValue() && lbChildren.getItemCount()>0);
			}
		});
		cbBoss.setValue(user.puserBoss!=0);
		Tabl1.setWidget(5, 1, cbBoss);
		
		
		Label l5 = new Label("Вам подчиняются");
		Tabl1.setWidget(6, 0, l5);
		
		HorizontalPanel p6 = new HorizontalPanel();
		Tabl1.setWidget(6, 1, p6);
		
		lbChildren = new ListBox();
		p6.add(lbChildren);
		lbChildren.setWidth("300px");
		lbChildren.setVisibleItemCount(5);

		if(user.children!=null) 
		for (PUserWrapper child : user.children) {
			lbChildren.addItem(child.getFullName());
		}
		
		VerticalPanel p7 = new VerticalPanel();
		p7.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		p7.setStyleName("lpad10");
		p6.add(p7);
		p7.setHeight("77px");
		
		btnAdd = new Button("Добавить");
		btnAdd.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				showAddChildDialog();
			}
		});
		p7.add(btnAdd);
		btnAdd.setWidth("100%");
		btnAdd.setEnabled(user.puserBoss!=0);
		
		btnDelete = new Button("Удалить");
		btnDelete.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int idx = lbChildren.getSelectedIndex();
				if(idx>=0) {
					PUserWrapper duser = (PUserWrapper) user.children.toArray()[idx];
					if(duser.tempflag)
						Ipplan.showError(new Exception("Этому пользователю уже отправлено приглашение. Ожидается его решение.")); else
						{
							lbChildren.removeItem(idx);
							user.children.remove(duser);
						}
				}
			}
		});
		p7.add(btnDelete);
		btnDelete.setWidth("100%");
		btnDelete.setEnabled(user.puserBoss!=0 && user.children!=null && user.children.size()>0);
		
		
		Label l6 = new Label("Вы подчинены");
		Tabl1.setWidget(7, 0, l6);

		HorizontalPanel p11 = new HorizontalPanel();
		p11.setSpacing(5);
		Tabl1.setWidget(7, 1, p11);
		
		Label lOwner = new Label(" ");
		p11.add(lOwner);
		p11.setCellVerticalAlignment(lOwner, HasVerticalAlignment.ALIGN_MIDDLE);
		lOwner.setStyleName("gwt-TextBox");
		lOwner.setWidth("212px");

		VerticalPanel p12 = new VerticalPanel();
		p12.setSpacing(2);
		p11.add(p12);
		p12.setHeight("");

		if(user.owner==null) lOwner.setText("никому"); else
			lOwner.setText(user.owner.getFullName());
		
		if(user.lastSystemMessage!=null) {
			lOwner.setText("Приглашение от босса "+user.lastSystemMessage.sender.getFullName());
		} else
			p12.setVisible(false);
		
		
		final RadioButton rbOwnerOk = new RadioButton("groupOwner", "Принять приглашение");
		p12.add(rbOwnerOk);
		
		final RadioButton rbOwnerCancel = new RadioButton("groupOwner", "Отказаться");
		p12.add(rbOwnerCancel);
		
		
		btnSave = new Button("Сохранить изменения");
		btnSave.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				resetErrors();
				if(!validateTab1()) return;
				PUserWrapper u = new PUserWrapper();
				u.puserLogin = tbName.getText();
				u.puserEmail = tbEmail.getText();
				u.puserTaxtype = rbTaxType1.getValue()?1:2;
				u.puserBoss = cbBoss.getValue()?1:0;
				u.puserTarif =cbTarif.getSelectedIndex();
				u.children.addAll(user.children);
				u.lastSystemMessage = FormProfile.this.user.lastSystemMessage;
				if(rbOwnerOk.getValue()) 
					u.owner = FormProfile.this.user.lastSystemMessage.sender;
				
				if(profService==null) profService = GWT.create(ProfileService.class);
				profService.setUserData(u, rbOwnerOk.getValue()?1:rbOwnerCancel.getValue()?-1:0,new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						toast(btnSave,"Общие данные успешно изменены");
						getMain().refreshForm(FormProfile.class,null);
					}
					
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
			}
		});
		
		Tabl1.setWidget(8, 0, btnSave);
		Tabl1.getFlexCellFormatter().setColSpan(8, 0, 2);
		Tabl1.getCellFormatter().setHorizontalAlignment(8, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		setFirstFocusedWidget(tbName);

		FlexTable Tab2 = new FlexTable();
		tabPanel.add(Tab2, "Оплата сервиса", false);
		Tab2.setSize("100%", "3cm");
		
		HorizontalPanel p33 = new HorizontalPanel();
		p33.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		p33.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		p33.setSpacing(5);
		Tab2.setWidget(0, 0, p33);
		p33.setSize("100%", "51px");
		
		Button btnPay1 = new Button("Заплатить за 1 месяц");
		p33.add(btnPay1);
		
		Button btnPay3 = new Button("Заплатить за 3 месяца");
		p33.add(btnPay3);
		
		Button button = new Button("Заплатить за полгода");
		p33.add(button);
		
		GridPager pagerPayTop = new GridPager();
		Tab2.setWidget(1, 0, pagerPayTop);
		
		payGrid = new CellTable<PaymentsWrapper>(15);
		pagerPayTop.setDisplay(payGrid);
		Tab2.setWidget(2, 0, payGrid);
		Tab2.getCellFormatter().setHeight(2, 0, "");
		Tab2.getCellFormatter().setWidth(2, 0, "");
		payGrid.setSize("100%", "400px");
		
		TextColumn<PaymentsWrapper> c3 = new PeriodColumn<PaymentsWrapper,Integer>() {
			@Override
			public String getValue(PaymentsWrapper object) {
				return (object==null)?"":object.getPeriod();
			}

			@Override
			public Integer getSortedValue(PaymentsWrapper object) {
				return object.getPeriodNumber();
			}
		};
		c3.setDefaultSortAscending(false);
		
		Column<PaymentsWrapper, Date> c4 = new Column<PaymentsWrapper, Date>(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy"))) {
			@Override
			public Date getValue(PaymentsWrapper object) {
				return (object==null)?null:object.getPaymentsDate();
			}
		};
		
		Column<PaymentsWrapper, Number> с5 = new Column<PaymentsWrapper, Number>(new NumberCell(NumberFormat.getFormat("#,##0.00"))) {
			@Override
			public Number getValue(PaymentsWrapper object) {
				return (object==null)?null:object.getPaymentsSumma()/100.0;
			}
		};
		с5.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		payGrid.addColumn(c3, "Период");
		payGrid.addColumn(c4, "Дата оплаты");
		payGrid.addColumn(с5, "Сумма");

		prepareGrid(payGrid, user.paymentses);
		Tab2.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		
		GridPager pagerPayBottom = new GridPager();
		pagerPayBottom.setDisplay(payGrid);
		Tab2.setWidget(3, 0, pagerPayBottom);
		Tab2.getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		payGrid.getColumnSortList().push(c3);
		
		
		FlexTable Tab3 = new FlexTable();
		Tab3.setCellSpacing(4);
		Tab3.setCellPadding(10);
		tabPanel.add(Tab3, "Сменить пароль", false);
		Tab3.setSize("100%", "3cm");
		
		Label l20 = new Label("Новый пароль");
		Tab3.setWidget(0, 0, l20);
		
		final PasswordTextBox tbNewPassword = new PasswordTextBox();
		Tab3.setWidget(0, 1, tbNewPassword);
		tbNewPassword.setWidth("300px");
		
		Label l21 = new Label("Повторите пароль");
		Tab3.setWidget(1, 0, l21);
		
		final PasswordTextBox tbRepeatPassword = new PasswordTextBox();
		Tab3.setWidget(1, 1, tbRepeatPassword);
		tbRepeatPassword.setWidth("300px");
		
		final Button btnSavePassword = new Button("Сохранить изменения");
		btnSavePassword.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				resetErrors();
				if(tbNewPassword.getText().length()<7) {
					showError(currentTab, tbNewPassword, "Пароль слишком короткий. Требуется более 6 знаков");
					return;
				};	
				if(!tbNewPassword.getText().equals(tbRepeatPassword.getText())) {
					showError(currentTab, tbRepeatPassword, "Вам не удалось повторить пароль");
					return;
				};
				LoginServiceAsync loginService = GWT.create(LoginService.class);
				loginService.changePassword(tbNewPassword.getText(), new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						tbNewPassword.setText("");
						tbRepeatPassword.setText("");
						toast(btnSavePassword,"Пароль успешно изменен");
					}
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
			}
		});
		Tab3.setWidget(2, 0, btnSavePassword);
		Tab3.getFlexCellFormatter().setColSpan(2, 0, 2);
		Tab3.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		FlexTable Tab4 = new FlexTable();
		tabPanel.add(Tab4, "Синхронизация", false);
		Tab4.setSize("100%", "3cm");
		
		GridPager pagerSyncTop = new GridPager();
		Tab4.setWidget(0, 0, pagerSyncTop);
		
		syncGrid = new CellTable<SyncWrapper>(15);
		pagerSyncTop.setDisplay(syncGrid);
		Tab4.setWidget(1, 0, syncGrid);
		syncGrid.setSize("100%", "400px");
		
		TextColumn<SyncWrapper> c1 = new TextColumn<SyncWrapper>() {
			@Override
			public String getValue(SyncWrapper object) {
				return (object==null)?"":object.getImei();
			}
		};
		c1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		c1.setSortable(true);
		syncGrid.setColumnWidth(c1, "200px");
		
		@SuppressWarnings("deprecation")
		Column<SyncWrapper, Date> c2 = new Column<SyncWrapper, Date>(new DateCell(DateTimeFormat.getMediumDateTimeFormat())) {
			@Override
			public Date getValue(SyncWrapper object) {
				return (object==null)?null:object.getLast();
			}
		};
		c2.setSortable(true);
		

		syncGrid.addColumn(c1, "Устройство");
		syncGrid.addColumn(c2, "Дата и время синхронизации");
		
		prepareGrid(syncGrid, user.syncs);
		c2.setDefaultSortAscending(false);
	    syncGrid.getColumnSortList().push(c2);
	    Tab4.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
	    
	    GridPager pagerSyncBottom = new GridPager();
	    pagerSyncBottom.setDisplay(syncGrid);
	    Tab4.setWidget(2, 0, pagerSyncBottom);
	    Tab4.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		
		
		
//		syncGrid.setRowCount(user.syncs.size(),true);
		
		tabPanel.getTabBar().selectTab(0);
	}

	protected void showAddChildDialog() {
		final Dialog dialog = new Dialog("Добавить подчиненного");
		FlexTable table = dialog.getContent();
		
		table.setWidget(0,0,new Label("Имя подчиненного"));
		final TextBox tbChildName = new TextBox();
		tbChildName.setWidth("200px");
		table.setWidget(0, 1, tbChildName);
		tbChildName.getElement().setAttribute("autocomplete", "on");
		tbChildName.setName("userName");
		
		Label l = new Label("* Адрес электронной почты подчиненного");
		l.setWidth("150px");
		table.setWidget(1,0,l);
		final TextBox tbChildEmail = new TextBox();
		tbChildEmail.setWidth("200px");
		table.setWidget(1, 1, tbChildEmail);
		tbChildEmail.getElement().setAttribute("autocomplete", "on");
		tbChildEmail.setName("email");
		
		dialog.setButtonOkClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialog.resetErrors();
				if(tbChildEmail.getText().isEmpty()) {
					dialog.cancel();
					dialog.showError(tbChildEmail, "Адрес электронной почты подчиненного не может быть пустым");
					return;
				}
				if (tbChildEmail.getText().equalsIgnoreCase(FormProfile.this.user.puserEmail)) {
					dialog.cancel();
					dialog.showError(tbChildEmail, "Нельзя быть подчиненным у самого себя");
					return;
				}
				if(profService==null) profService = GWT.create(ProfileService.class);
				dialog.cancel(); // дальше идут асинхронные проверки, поэтому окно не скрываем
				profService.checkUser(tbChildName.getText(), tbChildEmail.getText(), new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if(!result) { 
							dialog.showError(tbChildName, "Такой пользователь отсутствует");
							return;
						};
						if(findChildren(tbChildEmail.getText())!=null) { 
							dialog.showError(tbChildName, "Такой пользователь уже добавлен в список");
							return;
						};
						lbChildren.addItem(tbChildName.getText()+"("+tbChildEmail.getText()+")");
						PUserWrapper u = new PUserWrapper();
						u.puserEmail = tbChildEmail.getText();
						u.puserLogin = tbChildName.getText();
						user.children.add(u);
						dialog.hide();
						toast(lbChildren,"Пользователю "+u.getFullName()+" будет отправлено приглашение."+
						            " Он должен будет подтвердить, что готов быть Вашим подчиненным.");
					}

					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
			}
		});

		dialog.setFirstFocusedWidget(tbChildName);
		dialog.center();
	}

	protected boolean validateTab1() {
		if(tbEmail.getText().isEmpty()) {
			showError(currentTab, tbEmail, "Электронный адрес не может быть пустым");
			return false;
		}
		return true;
	}

	protected void lockControl() {
		btnDelete.setEnabled(lbChildren.getItemCount()>0 && lbChildren.getSelectedIndex()>=0);
	}

	private PUserWrapper findChildren(String text) {
		for (PUserWrapper u : user.children) {
			if(u.puserEmail.equalsIgnoreCase(text))
				return u;
		}
		return null;
	}

	public static void enterService() {
		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.openDatabase(new AsyncCallback<String>() {
			public void onSuccess(String result) {
				Window.Location.assign(result);
			}
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
	};
	
};
