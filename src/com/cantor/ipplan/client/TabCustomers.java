package com.cantor.ipplan.client;

import java.util.List;

import com.cantor.ipplan.client.OAuth2.EventOnCloseWindow;
import com.cantor.ipplan.client.widgets.CellTable;
import com.cantor.ipplan.client.widgets.DropdownButton;
import com.cantor.ipplan.client.widgets.GridPager;
import com.cantor.ipplan.client.widgets.RadioButton;
import com.cantor.ipplan.client.widgets.TextBox;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.ImportExportProcessInfo;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.cantor.ipplan.client.widgets.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;

public class TabCustomers extends FlexTable {

	private FormMain form;
	private DatabaseServiceAsync dbservice;
	private PUserWrapper user;
	
	public TabCustomers(FormMain form, DatabaseServiceAsync dbservice) {
		super();
		this.form = form;
		this.user = form.user;
		this.dbservice = dbservice;
		setSize("100%", "3cm");
		init();
	}

	private void init() {
		HorizontalPanel p;
		p = new HorizontalPanel();
		p.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		p.setSpacing(10);
		
		setWidget(0, 1, p);

		p = new HorizontalPanel();
		p.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		p.setSpacing(10);
		form.btnCustomerAdd = new Button("Новый");
		form.btnCustomerAdd.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				form.editCustomer(null);
			}
		});
		p.add(form.btnCustomerAdd);
		
		form.btnCustomerRefresh = new Button("Обновить");
		form.btnCustomerRefresh.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				startCustomers(form.tbFindCustomer.getText());
			}
		});
		p.add(form.btnCustomerRefresh);
		
		form.btnCustomerDelete = new Button("Удалить");
		form.btnCustomerDelete.setEnabled(false);
		form.btnCustomerDelete.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final List<CustomerWrapper> list = form.tableCustomer.getCheckedList();
				dbservice.deleteCustomer(list, new AsyncCallback<Void>() {
					
					@Override
					public void onSuccess(Void result) {
						List<CustomerWrapper> all = form.tableCustomer.getProvider().getList();
						for (CustomerWrapper cw : list) all.remove(cw);
						list.clear();
						form.tableCustomer.redraw();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
						
					}
				});
			}
		});
		p.add(form.btnCustomerDelete);
		
		final DropdownButton cmd = new DropdownButton("Еще");
		form.syncMenuItem = new MenuItem("Синхронизировать прямо сейчас",new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				syncContactsdDrectly();
				cmd.closeup();
			}
		});
		cmd.getMenu().addItem(form.syncMenuItem);
		form.syncAutoMenuItem = new MenuItem(getContactAutoSyncCaption(),new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				syncContactsdAuto();
				cmd.closeup();
			}
		});
		cmd.getMenu().addItem(form.syncAutoMenuItem);
		
		p.add(cmd);
		
		setWidget(0, 0, p);
		getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
		getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		
		p = new HorizontalPanel();
		p.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		p.setSpacing(5);
		
		p.add(new Label("Встречаются слова"));
		form.tbFindCustomer = new TextBox();
		form.tbFindCustomer.setWidth("300px");
		// не больше 120-2 иначе Jaybird не умеет обрезать параметры и FB валится с ошибкой
		// arithmetic exception, numeric overflow, or string truncation string right truncation
		form.tbFindCustomer.setMaxLength(110);  
		form.tbFindCustomer.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeEvent().getKeyCode()==KeyCodes.KEY_ENTER) 
					startCustomers(form.tbFindCustomer.getText());
			}
		});
		p.add(form.tbFindCustomer);
		
		setWidget(1, 0, p);
		
		form.tableCustomer = new CellTable<CustomerWrapper>(15);
		form.tableCustomer.setSelectionModel(null);
		form.tableCustomer.setWidth("100%");
		
		form.makeCustomerColumns(form.tableCustomer);

		GridPager pager = new GridPager();
		pager.setDisplay(form.tableCustomer);
		setWidget(1,1, pager);
		getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		
		getFlexCellFormatter().setColSpan(2, 0, 2);
		setWidget(2,0, form.tableCustomer);
		getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
	}

	private void startCustomers(String query) {
		dbservice.findCustomer(query, new AsyncCallback<List<CustomerWrapper>>() {
			
			@Override
			public void onSuccess(List<CustomerWrapper> result) {
				Form.prepareGrid(form.tableCustomer, result,true);
				form.tableCustomer.setRowCount(result.size());
			}
			
			@Override
			public void onFailure(Throwable e) {
				Ipplan.showError(e);
				
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
											form.syncAutoMenuItem.setText(getContactAutoSyncCaption());
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
						form.syncAutoMenuItem.setText(getContactAutoSyncCaption());
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
							form.syncMenuItem.getScheduledCommand().execute(); 
						}
					});
				} else
				// обовление token
				if(result.getError()==ImportExportProcessInfo.TOKEN_EXPIRED) {
					dbservice.refreshGoogleToken(new AsyncCallback<Void>() {
						
						@Override
						public void onSuccess(Void result) {
							form.syncMenuItem.getScheduledCommand().execute(); //!attention, its's recursion
						}
						
						@Override
						public void onFailure(Throwable e) {
							Ipplan.showError(e);
						}
					});
				} else {
					refreshCustomers();	
					Form.toast(form.tableCustomer, "Синхронизация окончена. При импорте обработано "+result.getImportAllCount()+
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

	public void refreshCustomers() {
		startCustomers(form.tbFindCustomer.getText());
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
		return "Автоматическая синхронизация "+s;
	}
}
