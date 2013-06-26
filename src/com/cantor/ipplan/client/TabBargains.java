package com.cantor.ipplan.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.cantor.ipplan.client.CellTable.ChangeCheckListEvent;
import com.cantor.ipplan.client.OAuth2.EventOnCloseWindow;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.ImportExportProcessInfo;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.core.client.Scheduler;

public class TabBargains extends FlexTable {

	private FormMain form;
	private DatabaseServiceAsync dbservice;

	public TabBargains(FormMain form, DatabaseServiceAsync dbservice) {
		super();
		this.form = form;
		this.dbservice = dbservice;
		setSize("100%", "3cm");
		init();
	}

	private void init() {
		int row = 0;
		form.filterBargainStatus = new ToggleButton[]{new ToggleButton("в работе"),new ToggleButton("выполненные"),
				 new ToggleButton("просроченные"),new ToggleButton("несогласованные"),new ToggleButton("все")};
		form.allBtn =  form.filterBargainStatus[form.filterBargainStatus.length-1];
		
		HorizontalPanel p = new HorizontalPanel();
		p.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		p.setSpacing(10);
		form.btnBargainAdd = new Button("Новая");
		form.btnBargainAdd.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				try {
					form.addNewBargain();
				} catch (Exception e) {
					Ipplan.error(e);
				}
			}
		});
		p.add(form.btnBargainAdd);
		
		form.btnBargainRefresh = new Button("Обновить");
		form.btnBargainRefresh.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refreshBargain();
			}
		});
		p.add(form.btnBargainRefresh);
		
		form.btnBargainDelete = new Button("Удалить");
		form.btnBargainDelete.setEnabled(false);
		form.btnBargainDelete.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final List<BargainWrapper> list = form.tableBargain.getCheckedList();
				dbservice.deleteBargain(list, new AsyncCallback<Void>() {
					
					@Override
					public void onSuccess(Void result) {
						List<BargainWrapper> all = form.tableBargain.getProvider().getList();
						for (BargainWrapper cw : list) {
							SystemNotify.getDeleteNotify().notify(cw);							
							all.remove(cw);
						}
						list.clear();
						form.tableBargain.redraw();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
						
					}
				});
			}
		});
		p.add(form.btnBargainDelete);

		final DropdownButton cmd = new DropdownButton("Еще");
		form.exportBargainsMenuItem = new MenuItem("Экспортировать список",new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				// TODO Export to xls
				cmd.closeup();
			}
		});
		cmd.getMenu().addItem(form.exportBargainsMenuItem);
		
		cmd.getMenu().addSeparator();
		
		form.syncBargainsMenuItem = new MenuItem("Синхронизировать прямо сейчас календари сделок",new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				syncCalendarsDirectly();
				cmd.closeup();
			}
		});
		cmd.getMenu().addItem(form.syncBargainsMenuItem);

		form.syncAutoBargainsMenuItem = new MenuItem("Автоматическая синхронизация [нет]",new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				// TODO sync
				cmd.closeup();
			}
		});
		cmd.getMenu().addItem(form.syncAutoBargainsMenuItem);
		
		
		p.add(cmd);
		
		setWidget(row, 0, p);
		
		row++;
		
		HorizontalPanel th = new HorizontalPanel();
		th.setSpacing(5);
		th.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		th.add(new Label("C начала года до конца месяца "));
		
		form.filterBargainDate = new MonthPicker();
		form.filterBargainDate.setTabIndex(0);
		th.add(form.filterBargainDate);
		form.filterBargainAllUsers = new CheckBox("Сделки подчиненных");
		th.add(form.filterBargainAllUsers);
		setWidget(row, 0, th);
		getFlexCellFormatter().setColSpan(row, 0, 2);
		row++;
		
		th = new HorizontalPanel();
		ClickHandler groupclick = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToggleButton btn = (ToggleButton) event.getSource();
				if(btn==form.allBtn) {
					for (int i = 0; i < form.filterBargainStatus.length; i++) 
						form.filterBargainStatus[i].setDown(false);
					form.allBtn.setDown(true);
				} else
					form.allBtn.setDown(false);
			}
		};
		form.filterBargainStatus[0].setDown(true);
		
		for (int i = 0; i < form.filterBargainStatus.length; i++) {
			th.add(form.filterBargainStatus[i]);
			form.filterBargainStatus[i].addClickHandler(groupclick);
		}
		setWidget(row, 1, th);
		getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);

		HorizontalPanel ph = new HorizontalPanel();
		ph.setSpacing(5);
		ph.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		ph.add(new Label("Встречаются слова"));
		
		form.tbFindBargain = new TextBox();
		// не больше 120-2 иначе Jaybird не умеет обрезать параметры и FB валится с ошибкой
		// arithmetic exception, numeric overflow, or string truncation string right truncation
		form.tbFindBargain.setMaxLength(110);
		form.tbFindBargain.setWidth("231px");
		form.tbFindBargain.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeEvent().getKeyCode()==KeyCodes.KEY_ENTER)
					refreshBargain();
			}
		});
		ph.add(form.tbFindBargain);
		setWidget(row, 0, ph);
		
		getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
		//tab2.getFlexCellFormatter().setVerticalAlignment(row,0, HasVerticalAlignment.ALIGN_MIDDLE);
		getFlexCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		
		row++;
		
		
		form.tableBargain = new CellTable<BargainWrapper>(13);
		form.tableBargain.setSelectionModel(null);
		form.tableBargain.setWidth("100%");

		GridPager pager = new GridPager();
		pager.setDisplay(form.tableBargain);
		setWidget(row,1, pager);
		getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		
		form.lBargainTotal = new Label();
		setWidget(row,0, form.lBargainTotal);

		row++;
		
		form.tableBargain.createCheckedColumn(new ChangeCheckListEvent<BargainWrapper>() {
			@Override
			public void onChange(BargainWrapper object, boolean check) {
				form.btnBargainDelete.setEnabled(form.tableBargain.getCheckedList().size()>0);
			}
		});
		
		form.makeBargainColumns(form.tableBargain);
		
		getFlexCellFormatter().setColSpan(row, 0, 2);
		setWidget(row, 0, form.tableBargain);
		
		// not open
		Form.prepareGrid(form.tableBargain, new ArrayList<BargainWrapper>(),true);
		form.tableBargain.setRowCount(0);
		showBargainTotals();
	}
	
	protected void syncCalendarsDirectly() {
		dbservice.syncCalendar(new AsyncCallback<ImportExportProcessInfo>() {
			
			@Override
			public void onSuccess(ImportExportProcessInfo result) {
				// получение token
				if(result.getError()==ImportExportProcessInfo.TOKEN_NOTFOUND) {
					OAuth2 auth = new OAuth2(Utils.GOOGLE_AUTH_URL, Utils.GOOGLE_CLIENT_ID,
							Utils.GOOGLE_SCOPE, Utils.REDIRECT_URI);
					auth.login(new EventOnCloseWindow() {
						@Override
						public void onCloseWindow() {
							form.syncBargainsMenuItem.getScheduledCommand().execute(); 
						}
					});
				} else
				// обовление token
				if(result.getError()==ImportExportProcessInfo.TOKEN_EXPIRED) {
					dbservice.refreshGoogleToken(new AsyncCallback<Void>() {
						
						@Override
						public void onSuccess(Void result) {
							form.syncBargainsMenuItem.getScheduledCommand().execute(); //!attention, its's recursion
						}
						
						@Override
						public void onFailure(Throwable e) {
							Ipplan.showError(e);
						}
					});
				} else {
					refreshBargain();	
					Form.toast(form.tableBargain, "Синхронизация окончена. При импорте обработано "+result.getImportAllCount()+
							   " записей , из них новых - "+result.getImportInsert()+". При экспорте обработано "+result.getExportAllCount()+
							   " записей , из них новых - "+result.getExportInsert()+"."
					      );
				};
			};
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
	}

	private void showBargainTotals(){
		List<BargainWrapper> list = form.tableBargain.getProvider().getList();
		int total = 0;
		for (BargainWrapper bw : list) {
			total+= bw.bargainRevenue;
		}
		form.lBargainTotal.setText("Всего "+list.size()+" сделок на общую сумму "+NumberFormat.getFormat("#,##0.00").format(total/100.0));
	}
	
	private boolean[] getFilterBargainStatuses() {
		boolean[] stats = new boolean[4];
		for (int i = 0; i < form.filterBargainStatus.length-1; i++) 
			stats[i] = form.filterBargainStatus[i].isDown();
		return stats;
	}

	protected void startBargain(String text, Date date, boolean allUser, boolean[] stats) {
		dbservice.findBargain(text, date, allUser, stats, new AsyncCallback<List<BargainWrapper>>() {
			
			@Override
			public void onSuccess(List<BargainWrapper> result) {
				Form.prepareGrid(form.tableBargain , result,true);
				form.tableBargain.setRowCount(result.size());
				showBargainTotals();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
		
	}

	private void refreshBargain() {
		startBargain(form.tbFindBargain.getText(),form.filterBargainDate.getFinishDate(),
				form.filterBargainAllUsers.getValue(), (form.allBtn.isDown()?null:getFilterBargainStatuses()));
	}
}
