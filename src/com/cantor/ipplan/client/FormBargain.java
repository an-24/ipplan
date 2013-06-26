package com.cantor.ipplan.client;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.cantor.ipplan.client.CellTable.ChangeCheckListEvent;
import com.cantor.ipplan.client.StatusBox.StatusChangeEventListiner;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
import com.cantor.ipplan.shared.TaskWrapper;
import com.cantor.ipplan.shared.TasktypeWrapper;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

@SuppressWarnings("rawtypes")
public class FormBargain extends FlexTable implements ValueChangeHandler{

	private DatabaseServiceAsync dbservice;
	
	private BargainWrapper bargain;
	private Button btnPrev;
	private Button btnNext;
	private Button btnSave;
	
	private DateBox dbStart;
	private DateBox dbFinish;
	private Label lAttention;
	private StatusBox eStatus;
	private CustomerBox eCustomer;
	
	private CurrencyBox eRevenue;
	private NumberLabel<Double> lDeltaRevenue;
	private CurrencyBox ePrePayment;
	private NumberLabel<Double> lPaymentCost;
	private NumberLabel<Double> lDeltaMargin;
	private NumberLabel<Double> lDeltaCosts;
	private CurrencyBox eFine;
	private NumberLabel<Double> lDeltaFine;
	private NumberLabel<Double> lTax;
	private NumberLabel<Double> lDeltaTax;
	private NumberLabel<Double> lMargin;
	private NumberLabel<Double> lProfit;
	private NumberLabel<Double> lDeltaProfit;
	private MainTabPanel tabPanel;
	//private int index;
	private Label lTitle;
	private List<Integer> errorList = new ArrayList<Integer>();
	private Label lVersion;
	private Label lDateCreated;

	private Button btnCosts;

	private Label lAttentionPrePayment;

	private VerticalPanel pCustomer;

	private int loadCounter = 0;

	private CellTable<TaskWrapper> tableTasks;

	public FormBargain(final BargainWrapper b) {
		super();
		setCellPadding(4);
		//setSize("740px", "");
		getColumnFormatter().setWidth(0, "450px");
		getColumnFormatter().setWidth(1, "272px");
		//getElement().getStyle().setTableLayout(TableLayout.FIXED);
		setStyleName("FormBargain");
		addStyleName("tableBorderCollapse");

		
		Label l;
		VerticalPanel p;
		
		lTitle = new Label();
		lTitle.setStyleName("gwt-FormCaption");
		setWidget(0, 0, lTitle);
		getFlexCellFormatter().setColSpan(0, 0, 2);
		getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);

		
        // BARGAIN -----------------
		SimplePanel spBargain = new SimplePanel();
		spBargain.setStyleName("simplebox");
		setWidget(1, 0, spBargain);
		spBargain.setWidget(new BargainFragment());
		

		//TASK ----------
		SimplePanel sp = new SimplePanel();
		sp.setStyleName("simplebox");
		sp.setWidth("256px");
		//getFlexCellFormatter().setRowSpan(1, 2, 13);
		setWidget(1, 1, sp);
		
		VerticalPanel vp =  new VerticalPanel();
		l = new Label("Задачи");
		l.addStyleName("gwt-FormSubCaption");
		l.getElement().getStyle().setPaddingBottom(2, Unit.PX);
		vp.setWidth("100%");
		vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		vp.add(l);
		Label ledit = new Label("Новая");
		ledit.addStyleName("link");
		//ledit.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		ledit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				FormTask.addTask(dbservice, bargain, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						TaskWrapper tw = ((FormTask)event.getSource()).getTask();
						bargain.tasks.add(tw);
						List<TaskWrapper> list = tableTasks.getProvider().getList();
						list.add(tw);
						if(tw.taskExecuted==1) setTaskExecuted(tw, true);
						prepareTaskData();
						tableTasks.redraw();
						onValueChange(new BargainChangeEvent(bargain, tableTasks));
					}
				});
			}
		});
		vp.add(ledit);
		sp.setWidget(vp);
		
		tableTasks = new CellTable<TaskWrapper>(Integer.MAX_VALUE, (CellTable.Resources)GWT.create(TaskTableResources.class));
		tableTasks.setSelectionModel(null);
		tableTasks.setWidth("100%");
		Style style = tableTasks.getElement().getStyle();
		style.setPaddingTop(10, Unit.PX);
		makeTaskCells();
		vp.add(tableTasks);
		
		// Buttons -----------------------------

		HorizontalPanel ph = new HorizontalPanel();
		ph.setSpacing(10);
		ph.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		btnSave = new Button("Сохранить");
		btnSave.addStyleName("mainCommand");
		ph.add(btnSave);
		btnSave.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				save(false);
			}
		});
		Button btn  = new Button("Сохранить и Закрыть");
		ph.add(btn);
		btn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				save(true);
			}
		});
		btn  = new Button("Закрыть");
		ph.add(btn);
		btn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				close();
			}
		});
		btn  = new Button("Отменить изменения");
		ph.add(btn);
		btn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(bargain.isDirty())
					Ipplan.showContinueConfirmation("В сделке обнаружены изменения. Если продолжить они будут потеряны. Продолжить?", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Ipplan.getActiveDialog().hide();
							reload();
						}
					}); 
				else reload();
						
			}
		});
		
		setWidget(2, 0, ph);
		getFlexCellFormatter().setColSpan(2, 0, 2);
		getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);
		getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_MIDDLE);

		new Timer(){
			@Override
			public void run() {
				loadBargain(b);
				lockControl();
			}
		}.schedule(0);
		// это нужно чтобы форма добавилась во вкладки
		// а вообще это должно происходить в loadBargain 
		bargain = b;
	}

	protected void reload() {
		dbservice.editBargain(bargain.bargainId, new AsyncCallback<BargainWrapper>() {
			@Override
			public void onSuccess(BargainWrapper result) {
				if(result!=null) loadBargain(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
		
	}

	private void prepareTaskData() {
		// раставляем checks
		List<TaskWrapper> list = tableTasks.getProvider().getList();
		for (TaskWrapper tw : list) {
			if(tw.taskExecuted!=0) tableTasks.getCheckedList().add(tw);
		}
		// сортировка
		Collections.sort(list, new Comparator<TaskWrapper>(){
			@Override
			public int compare(TaskWrapper o1, TaskWrapper o2) {
				return o1.taskDeadline.compareTo(o2.taskDeadline);
			}
		});
		
	}

	private void makeTaskCells() {
		final Date currentDate = new Date(); 
		
		Column<TaskWrapper, Boolean> c0 = tableTasks.createCheckedColumn(new ChangeCheckListEvent<TaskWrapper>() {
			@Override
			public void onChange(TaskWrapper object, boolean check) {
				setTaskExecuted(object, check);
				tableTasks.redraw();
				onValueChange(new BargainChangeEvent(bargain, tableTasks));
			}
		},false);
		c0.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		
		Column<TaskWrapper, SafeHtml> c1 = new Column<TaskWrapper, SafeHtml>(new ClickableSafeHtmlCell()) {

			@Override
			public SafeHtml getValue(TaskWrapper object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if(object!=null) {
					StringBuilder s = new StringBuilder();
					boolean outofdate = object.taskDeadline.before(currentDate);
					s.append("<div class=\"task-list-item");
					if(object.taskExecuted!=0) s.append(" executed");
					if(outofdate) s.append(" outofdate");
					s.append("\">");
					s.append("<div class=\"name link\">");
					if(object.taskStart!=null && !outofdate && object.taskStart.before(currentDate)) s.append("<div class=\"start\"></div>");
					s.append(object.taskName).append("</div>");
					s.append("<div class=\"deadline\">");
					if(object.tasktype.tasktypeId<TasktypeWrapper.TT_OTHER) {
						s.append("<div class=\"start\" style=\"background:"+TasktypeWrapper.backgroundUrlFromType(object.tasktype.tasktypeId));
						s.append("\"></div>");
					}	
					s.append("до ").append(Ipplan.ALTERNATE_DATETIME_FORMAT.format(null,object.taskDeadline)).append("</div>");
					if(object.taskPlace!=null)
						s.append("<div class=\"place\">").append(object.taskPlace).append("</div>");
					
					s.append("</div>");
					sb.appendHtmlConstant(s.toString());
				}
				return sb.toSafeHtml();
			}
		};
		c1.setFieldUpdater(new FieldUpdater<TaskWrapper, SafeHtml>() {
			@Override
			public void update(int index, final TaskWrapper object, SafeHtml value) {
				FormTask.editTask(dbservice, bargain, object, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						TaskWrapper tw = ((FormTask)event.getSource()).getTask();
						List<TaskWrapper> list = tableTasks.getProvider().getList();
						bargain.tasks.remove(object);
						bargain.tasks.add(tw);
						list.set(list.indexOf(object), tw);
						if(tw.taskExecuted==1 && tw.taskExecuted!=object.taskExecuted)
							setTaskExecuted(tw, tw.taskExecuted!=0);
						prepareTaskData();
						tableTasks.redraw();
						onValueChange(new BargainChangeEvent(bargain, tableTasks));
					}
				});
			};
		});
		c1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		

		Column<TaskWrapper, SafeHtml> c2 = new Column<TaskWrapper, SafeHtml>(new ClickableSafeHtmlCell()) {

			@Override
			public SafeHtml getValue(TaskWrapper object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if(object!=null) {
					StringBuilder s = new StringBuilder();
					s.append("<div class=\"delete-icon\"></div>");
					sb.appendHtmlConstant(s.toString());
				}
				return sb.toSafeHtml();
			}
		};
		c2.setFieldUpdater(new FieldUpdater<TaskWrapper, SafeHtml>() {
			@Override
			public void update(int index, final TaskWrapper object, SafeHtml value) {
				List<TaskWrapper> list = tableTasks.getProvider().getList();
				list.remove(object);
				bargain.tasks.remove(object);
				tableTasks.redraw();
				onValueChange(new BargainChangeEvent(bargain, tableTasks));
			};
		});
		c2.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		c2.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		
		tableTasks.addColumn(c1);
		tableTasks.addColumn(c2);
		
		tableTasks.setColumnWidth(c2, "22px");
	}

	private String getTotalCostDisplay() {
		return bargain.bargainCosts==null?"<нет>":NumberFormat.getFormat("#,##0.00").format(bargain.bargainCosts/100.0);
	}

	/**
	 * Когда tab полностью включен в TabPanel. Все необходимые структуры  подготовлены 
	 */
	public void init() {
		dbStart.addValueChangeHandler(this);
		dbFinish.addValueChangeHandler(this);
		eRevenue.addValueChangeHandler(this);
		ePrePayment.addValueChangeHandler(this);
		eFine.addValueChangeHandler(this);
		eCustomer.addValueChangeHandler(this);
		eStatus.addValueChangeHandler(this);
		
		initButtonClose();
	}
	
	protected void close() {
		if(bargain.isDirty()) {
			Ipplan.showSaveConfirmation("В сделке произошли изменения. Сохранить?", new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					Ipplan.getActiveDialog().hide();
					save(true);
				}
			}, new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					Ipplan.getActiveDialog().hide();
					dropBargain();
				}
			});
			
		} else {
			dropBargain();
		}
	}

	protected void save(final boolean closed) {
		save(closed,null);
	}
	
	protected void save(final boolean closed, final NotifyHandler<BargainWrapper> external) {
		
		resetErrors();
		validate();
		if(errorList.size()>0) return;
		formFieldToBargain(bargain);
		
		DatabaseServiceAsync db = getDataBaseService();
		db.saveBargain(bargain, closed, new AsyncCallback<BargainWrapper>() {
			
			@Override
			public void onSuccess(BargainWrapper result) {
				boolean newVerCreated = bargain.bargainVer!=result.bargainVer;
				BargainWrapper savebargain = bargain;
				boolean insertMode = bargain.isNew();
				
				if(closed) {
					String message = "Сделка \""+getTitle()+"\" сохранена";
					Form.toast(FormBargain.this, message);
					tabPanel.remove(FormBargain.this);
					History.back();
				} else { 
					bargain.saveCompleted();
					String message = "Сделка \""+getTitle()+"\" сохранена";
					if(newVerCreated)
						message = "Создана новая версия "+(result.bargainVer+1)+" сделки "+getTitle(); 
					Form.toast(FormBargain.this.btnSave, message);
					loadBargain(result);					
				};
				if(external!=null) external.onNotify(result);
				
				// системная нотификация
				if(insertMode || newVerCreated) {
					if(newVerCreated) SystemNotify.getDeleteNotify().notify(savebargain);
					SystemNotify.getInsertNotify().notify(result);
				} else SystemNotify.getUpdateNotify().notify(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
		
	};

	private void loadBargain(BargainWrapper result) {
		loadCounter++;
		bargain = result;
		refreshTitle();				
		bargainToFormField(bargain);
		if(eStatus.isLocked()) eStatus.lock(false);
		eStatus.refreshStatus();
		// task
		Form.prepareGrid(tableTasks, bargain.tasks,false);
		tableTasks.setRowCount(bargain.tasks.size());
		prepareTaskData();
		setAttention();
		loadCounter--;
	}

	protected void bargainToFormField(BargainWrapper bw) {
		lTitle.setText(bargain.getFullName());
		lVersion.setText("Версия "+(bw.bargainVer+1));
		lDateCreated.setText(DateTimeFormat.getMediumDateFormat().format(bw.bargainCreated));
		dbStart.setValue(bw.bargainStart);
		dbFinish.setValue(bw.bargainFinish);
		eStatus.setStatus(bw.status);
		eCustomer.setCustomer(bw.customer);
		showInfoCustomer();
		// TODO ссылки на документы
		//eContract.setContract(bw.contract);
		eRevenue.setValue(bw.bargainRevenue);
		ePrePayment.setValue(bw.bargainPrepayment);
		btnCosts.setText(getTotalCostDisplay());
		lPaymentCost.setValue(bargain.bargainPaymentCosts==null?0:bargain.bargainPaymentCosts/100.0);
		eFine.setValue(bw.bargainFine);
		lMargin.setValue(bargain.getMargin()/100.0);
		lTax.setValue(bargain.bargainTax/100.0);
		lProfit.setValue(bargain.getProfit()/100.0);
		// дельты
		if(bw.bargainVer>0) {
			lDeltaRevenue.setValue((bw.bargainRevenue-bw.rootBargain.bargainRevenue)/100.0);
			lDeltaCosts.setValue((intValue(bw.bargainCosts)-intValue(bw.rootBargain.bargainCosts))/100.0);
			lDeltaMargin.setValue((bw.getMargin()-bw.rootBargain.getMargin())/100.0);
			lDeltaFine.setValue((intValue(bw.bargainFine)-intValue(bw.rootBargain.bargainFine))/100.0);
			lDeltaTax.setValue((bw.bargainTax-bw.rootBargain.bargainTax)/100.0);
			lDeltaProfit.setValue((bw.getProfit()-bw.rootBargain.getProfit())/100.0);
		}	
		
		// tasks
		tableTasks.setRowCount(bargain.tasks.size());
	}
	
	private int intValue(Integer v) {
		return (v==null)?0:v;
	}

	private void formFieldToBargain(BargainWrapper bw) {
		bw.customer = eCustomer.getCustomer();
		// TODO ссылки на документы
		bw.contract = null; 
		bw.status = eStatus.getStatus();
		bw.bargainStart = dbStart.getValue();
		bw.bargainFinish=dbFinish.getValue();
		bw.bargainRevenue = eRevenue.getValue();
		bw.bargainPrepayment = ePrePayment.getValue();
		bw.bargainFine = eFine.getValue();
	}

	private void validate() {
		int offs =0;
		// ! обязательно по порядку сверху вниз
		if(dbFinish.getValue().getTime() - dbStart.getValue().getTime()<0) {
			showError(3,"Дата окончания не может быть меньше даты начала");
			offs++;
		}	
		if(eCustomer.getCustomer()==null) {
			showError(5+offs,"Необходимо определить клиента");
			offs++;
		}	
		if(eRevenue.getValue()==null) {
			showError(6+offs,"Необходимо определить плановую выручку");
			offs++;
		}
		
	}

	private void dropBargain() {
		DatabaseServiceAsync db = getDataBaseService();
		db.dropTemporalyBargain(bargain.bargainId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				tabPanel.remove(FormBargain.this);
				History.back();
			}
			@Override
			public void onFailure(Throwable caught) {
				// since
			}
		});
	}

	private void setAttention() {
		if(bargain.getMargin()<0) lMargin.addStyleName("Attention3");
			else lMargin.removeStyleName("Attention3");
		if(bargain.getProfit()<0) lProfit.addStyleName("Attention3"); 
			else lProfit.removeStyleName("Attention3");
		if(bargain.attention!=null) {
			lAttention.setText(bargain.attention.message);
			lAttention.setStyleName("Attention"+bargain.attention.type);
		} else { 
			lAttention.setText("Осталось "+Utils.getDuration(new Date(), bargain.bargainFinish));
			lAttention.removeStyleName("Attention1");
			lAttention.removeStyleName("Attention2");
			lAttention.removeStyleName("Attention3");
		}
		int prePay = bargain.bargainPrepayment==null?0:bargain.bargainPrepayment;
		int cost = bargain.bargainPaymentCosts==null?0:bargain.bargainPaymentCosts;
		if(cost>prePay) {
			lAttentionPrePayment.setText("(аванс не покрывает расходы)");
			lAttentionPrePayment.addStyleName("Attention3");
		} else {
			lAttentionPrePayment.setText("");
			lAttentionPrePayment.removeStyleName("Attention3");
		}
			
	}

	private void lockControl() {
		btnPrev.setEnabled(bargain.bargainVer>0);
		btnNext.setEnabled(!bargain.isNew());
		dbStart.setEnabled(bargain.isNew());
		dbFinish.setEnabled(true);
		eRevenue.setEnabled(true);
	}

	public String getTitle() {
		String s = bargain.getFullName();
		if(this.bargain.isDirty()) s = "* "+s;
		return s;
	}
	
	public BargainWrapper getBargain() {
		return bargain; 
	}

	protected void showStatusForm(int[] newState) {
		final Dialog dialog = new Dialog("Выберите следующий статус");
		FlexTable table = dialog.getContent();
		final HashMap<RadioButton,StatusWrapper> list = new HashMap<RadioButton,StatusWrapper>();
		int j = 0;
		for (int i = 0, len = newState.length; i < len; i++) {
			StatusWrapper st = StatusWrapper.getStatus(newState[i]);
			if(st.statusId!=StatusWrapper.SUSPENDED) {
				RadioButton rb = new RadioButton("gr", st.statusName);
				
				if(st.statusId==StatusWrapper.CLOSE_FAIL)
					rb.getLabelElement().addClassName("Attention3");
				
				list.put(rb,st);
				table.setWidget(j, 0, rb);
				j++;
			}  
		}
		dialog.getButtonOk().setText("Установить");
		dialog.setButtonOkClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.cancel();
				for (RadioButton rb : list.keySet()) {
					if(rb.getValue()) {
						eStatus.lock(true);
						eStatus.setStatus(list.get(rb));
						dialog.hide();
						return;
					}
				}
				dialog.showError(list.size(), "Необходимо выбрать статус");
			}
		});
		dialog.center();
	}

	@Override
	public void onValueChange(ValueChangeEvent event) {
		resetErrors();
		
		if(loadCounter>0) return;
		
		bargain.modify();
		
		// обновляем в объекте
		if(event.getSource()==dbStart) bargain.bargainStart = dbStart.getValue();
		if(event.getSource()==dbFinish) {
			bargain.bargainFinish = dbFinish.getValue();
			bargain.attention = bargain.calcAttention();
		}
		if(event.getSource()==eRevenue) bargain.bargainRevenue = eRevenue.getValue();
		if(event.getSource()==ePrePayment) bargain.bargainPrepayment = ePrePayment.getValue();
		if(event.getSource()==eFine) bargain.bargainFine = eFine.getValue();
		if(event.getSource()==eRevenue || event.getSource()==eFine || event.getSource()==btnCosts)
			bargain.bargainTax = bargain.calcTax(); 
		
		// обновляем на форме
		if(event.getSource()==dbFinish)
			setAttention();
		
		if(event.getSource()==eRevenue) {
			lMargin.setValue(bargain.getMargin()/100.0);
			lTax.setValue(bargain.bargainTax/100.0);
			lProfit.setValue(bargain.getProfit()/100.0);
			setAttention();
		};
		if(event.getSource()==eFine) {
			lTax.setValue(bargain.bargainTax/100.0);
			lProfit.setValue(bargain.getProfit()/100.0);
			setAttention();
		};
		if(event.getSource()==btnCosts) {
			lMargin.setValue(bargain.getMargin()/100.0);
			lTax.setValue(bargain.bargainTax/100.0);
			lProfit.setValue(bargain.getProfit()/100.0);
			setAttention();
		};
		
		refreshTitle();
	}

	private void showInfoCustomer() {
		CustomerWrapper customer = eCustomer.getCustomer();
		while(pCustomer.getWidgetCount()>1) 
			pCustomer.remove(pCustomer.getWidgetCount()-1);
		
		if(customer!=null) {
			StringBuilder sb = new StringBuilder("<div class=\"customer-info\">");
			
			sb.append("<img src=\"");
			if(customer.customerPhoto==null) sb.append("resources/images/noname.png");
										else sb.append(customer.customerPhoto);
			sb.append("\">");
			
			sb.append("<div>");
			if(customer.customerCompany!=null) {
				sb.append("<div>").append(customer.customerCompany);
				if(customer.customerPosition!=null) 
					sb.append(',').append(customer.customerPosition);
			    sb.append("</div>");
			}
			
			String email = customer.getPrimaryEmail(true);
			if(email!=null) {
				sb.append("<div class=\"email\">").append(email);
				String s = customer.getEmails(true);
				if(s!=null) sb.append(" ("+s+")");
			    sb.append("</div>");
			}
			String phone = customer.getPrimaryPhone(true);  
			if(phone!=null) {
				sb.append("<div class=\"phone\">").append(phone);
				String s = customer.getPhones(true);
				if(s!=null) sb.append(" ("+s+")");
			    sb.append("</div>");
			}
			
			sb.append("</div>");
			sb.append("</div>");
			
			pCustomer.add(new HTML(sb.toString()));
			Label ledit = new Label("Изменить");
			ledit.addStyleName("link");
			ledit.getElement().getStyle().setFloat(Style.Float.RIGHT);
			ledit.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					FormCustomer.edit(dbservice, eCustomer.getCustomer(), new NotifyHandler<CustomerWrapper>() {
						@Override
						public void onNotify(CustomerWrapper c) {
							eCustomer.setCustomer(c);
							showInfoCustomer();
						}
					});
				}
			});
			pCustomer.add(ledit);
			
		}
	}

	private void refreshTitle() {
		tabPanel.getTabBar().setTabHTML(getIndex(), makeHTMLTab());
		initButtonClose();
		lTitle.setText(getTitle());
	}


	private void initButtonClose() {
		TableElement bar = TableElement.as(tabPanel.getTabBar().getElement());
		TableCellElement cell = TableCellElement.as(TableRowElement.as(bar.getRows().getItem(0)).getCells().getItem(getIndex()+1));
		NodeList<Element> ndl = cell.getElementsByTagName("span");
		if(ndl.getLength()>0) {
			Element btn = ndl.getItem(0);
			if(btn.getId().equals("closebtn")) {
				Event.setEventListener(btn, new EventListener() {
					@Override
					public void onBrowserEvent(Event event) {
						event.preventDefault();
						event.stopPropagation();
						close();
					}
				});
				DOM.sinkEvents((com.google.gwt.user.client.Element) btn, Event.ONMOUSEUP);
				btn.setTitle("Закрыть");
			}
		}
	}

	private NumberLabel<Double> newDeltaNumberLabel(int value) {
		return new NumberLabel<Double>(NumberFormat.getFormat("(#,##0.00)"));
	}

	private NumberLabel<Double> newNumberLabel() {
		NumberLabel<Double> l = new NumberLabel<Double>(NumberFormat.getFormat("#,##0.00"));
		l.setStyleName("gwt-CurrencyLabel");
		return l; 
	}

	private NumberLabel<Double> newNumberLabel(Integer v) {
		NumberLabel<Double> l = newNumberLabel();
		l.setValue(v!=null?v/100.0:0);
		return l; 
	}

	public void setOwner(MainTabPanel panel) {
		this.tabPanel = panel;
	}

	public int getIndex() {
		return tabPanel.find(bargain.bargainId);
	}

	public String makeHTMLTab() {
		return "<div class=\"gwt-Label\" style=\"white-space: nowrap;\">"+getTitle()+"<span id=\"closebtn\" class=\"gwt-TabBarItem-CloseBtn\"></span></div>";
	}

	private DatabaseServiceAsync getDataBaseService() {
		if(dbservice!=null) return dbservice; 
		dbservice = GWT.create(DatabaseService.class);
		return dbservice;
	}

	public void showError(int beforeRow,String message) {
		int rowError = insertRow(beforeRow);
		Label l = new Label(message);
		l.setStyleName("errorHint");
		getCellFormatter().setHorizontalAlignment(rowError, 0, HasHorizontalAlignment.ALIGN_CENTER);
		getCellFormatter().setVerticalAlignment(rowError, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		setWidget(rowError, 0, l);
		getFlexCellFormatter().setColSpan(rowError, 0, 3);
		errorList.add(rowError);
	}

	private void resetErrors() {
		int offs = 0;
		for (int row : errorList) { 
			removeRow(row+offs);
			offs--;
		}	
		errorList.clear();
	}

	private void setTaskExecuted(TaskWrapper task, boolean value) {
		if(value && task.afterStatus!=null) {
			bargain.status = task.afterStatus;
			eStatus.setStatus(bargain.status);
			eStatus.lock(true);
		}	
		task.taskExecuted = value?1:0;
	}

	class BargainChangeEvent extends ValueChangeEvent<BargainWrapper> {
		protected BargainChangeEvent(BargainWrapper value, Object source) {
			super(value);
			setSource(source);
		}
	};
	
	public class BargainFragment extends FlexTable {

		BargainFragment() {
			addStyleName("tableBorderCollapse");
			
			int col = 0, row = 0;
			
			btnPrev = new Button("<");
			btnPrev.setText("<");
			btnPrev.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final BargainWrapper savebargain = bargain; 
					if(bargain.isDirty()) {
						Ipplan.showSaveConfirmation("В сделке произошли изменения. Сохранить?", new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								Ipplan.getActiveDialog().hide();
								save(false,new NotifyHandler<BargainWrapper>() {
									@Override
									public void onNotify(BargainWrapper c) {
										loadBargain(savebargain);
										btnPrev.click();
									}
								});
							}
						}, new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								Ipplan.getActiveDialog().hide();
								bargain.cancel();
								btnPrev.click();
							}
						});
						return;
					}
					dbservice.prevBargainVersion(bargain.bargainId, new AsyncCallback<BargainWrapper>() {
						
						@Override
						public void onSuccess(BargainWrapper result) {
							if(result!=null) loadBargain(result);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							Ipplan.showError(caught);
							
						}
					});
				}
			});
			setWidget(row, col, btnPrev);
			
			VerticalPanel p = new VerticalPanel();
			p.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			
			lVersion = new Label("Версия ");
			lVersion.addStyleName("gwt-FormSubCaption");
			p.add(lVersion);
			
			lDateCreated = new Label();
			p.add(lDateCreated);
			
			setWidget(row, col+1, p);
			getCellFormatter().setHorizontalAlignment(row, col+1, HasHorizontalAlignment.ALIGN_CENTER);

			btnNext = new Button(">");
			btnNext.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final BargainWrapper savebargain = bargain; 
					if(bargain.isDirty()) {
						Ipplan.showSaveConfirmation("В сделке произошли изменения. Сохранить?", new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								Ipplan.getActiveDialog().hide();
								save(false,new NotifyHandler<BargainWrapper>() {
									@Override
									public void onNotify(BargainWrapper c) {
										loadBargain(savebargain);
										btnNext.click();
									}
								});
							}
						}, new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								Ipplan.getActiveDialog().hide();
								bargain.cancel();
								btnNext.click();
							}
						});
						return;
					}
					dbservice.nextBargainVersion(bargain.bargainId, new AsyncCallback<BargainWrapper>() {
						
						@Override
						public void onSuccess(BargainWrapper result) {
							if(result!=null) loadBargain(result);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							Ipplan.showError(caught);
							
						}
					});
				}
			});
			setWidget(row, col+2, btnNext);
			getCellFormatter().setHorizontalAlignment(row, col+2, HasHorizontalAlignment.ALIGN_RIGHT);
			
			row++;
			
			dbStart = new DateBox();
			setWidget(row, col, dbStart);
			dbStart.setWidth("113px");
			dbStart.setFormat(Ipplan.DEFAULT_DATE_FORMAT);
			dbStart.setEnabled(false);
			
			dbFinish = new DateBox();
			setWidget(row, col+2, dbFinish);
			dbFinish.setWidth("113px");
			dbFinish.setFormat(Ipplan.DEFAULT_DATE_FORMAT);
			dbFinish.setEnabled(false);
			getCellFormatter().setHorizontalAlignment(row, col+2, HasHorizontalAlignment.ALIGN_RIGHT);
			
			HorizontalPanel ph = new HorizontalPanel();
			ph.setSpacing(5);
			ph.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			ph.add(new Label("Статус:"));
			
			eStatus = new StatusBox(null);
			eStatus.setChangeListiner(new StatusChangeEventListiner() {
				
				@Override
				public void onPause(StatusWrapper oldStatus) {
					eStatus.lock(true);
					eStatus.setStatus(StatusWrapper.getPauseStatus());
				}
				
				@Override
				public void onNext(StatusWrapper oldStatus) {
					int[] newState =  StatusWrapper.getNextState(oldStatus.statusId,false);
					if(newState.length==1) {
						eStatus.lock(true);
						eStatus.setStatus(StatusWrapper.getStatus(newState[0])); 
					}
				    else showStatusForm(newState);
				}
			});
			ph.add(eStatus);
			setWidget(row, col+1, ph);
			
			row++;
			
			lAttention = new Label("");
			setWidget(row, col+1, lAttention);
			getCellFormatter().setVerticalAlignment(row, col+1, HasVerticalAlignment.ALIGN_MIDDLE);
			getCellFormatter().setHorizontalAlignment(row, col+1, HasHorizontalAlignment.ALIGN_RIGHT);
			getFlexCellFormatter().setColSpan(row, col+1, 2);

			pCustomer = new VerticalPanel();
			pCustomer.addStyleName("bpad10");
			pCustomer.addStyleName("tpad10");
			//p.getElement().getStyle().setMargin(-10., Unit.PX);
			
			eCustomer = new CustomerBox(getDataBaseService());
			eCustomer.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
				@Override
				public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
					showInfoCustomer();
				}
			});		
			pCustomer.add(eCustomer);		
			eCustomer.getElement().setAttribute("placeholder", "введите имя клиента");
			eCustomer.setWidth("100%");
			
			row++;
			
			pCustomer.setWidth("100%");
			setWidget(row, col+0, pCustomer);
			getFlexCellFormatter().setColSpan(row, col+0, 3);

			row++;
			
			getCellFormatter().setStyleName(row, col+0, "grayBorder");
			getCellFormatter().setStyleName(row, col+1, "grayBorder");
			getCellFormatter().setStyleName(row, col+2, "grayBorder");

			getCellFormatter().setHeight(row,col+0,"36px");
			
			Label l = new Label("Выручка");
			setWidget(row, col+0, l);

			eRevenue = new CurrencyBox();
			eRevenue.setWidth("130px");
			setWidget(row, col+1, eRevenue);
			
			lDeltaRevenue = newDeltaNumberLabel(0); 
			setWidget(row, col+2, lDeltaRevenue);

			row++;
			
			l = new Label("Аванс");
			setWidget(row, col+0, l);

			ePrePayment = new CurrencyBox();
			ePrePayment.setWidth("130px");
			ePrePayment.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					bargain.bargainPrepayment = ePrePayment.getValue(); 
					setAttention();
				}
			});
			setWidget(row, col+1, ePrePayment);
			
			row++;

			l = new Label("Расходы,");
			setWidget(row, col+0, l);

			btnCosts = new Button();
			btnCosts.addClickHandler(new ClickHandler() {
				private FormCost formCost;

				@Override
				public void onClick(ClickEvent event) {
					formCost = new FormCost(bargain, dbservice, new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							BargainWrapper nb = formCost.getBargain();
							bargain.bargaincostses.clear();
							bargain.bargaincostses.addAll(nb.bargaincostses);
							bargain.bargainPaymentCosts = nb.bargainPaymentCosts;
							bargain.bargainCosts = nb.bargainCosts;
							
							btnCosts.setText(getTotalCostDisplay());
							lPaymentCost.setValue(bargain.bargainPaymentCosts==null?0:bargain.bargainPaymentCosts/100.0);
							
							onValueChange(new BargainChangeEvent(bargain, btnCosts));
						}
					});
					formCost.center();
				}
			});
			setWidget(row, col+1, btnCosts);
			btnCosts.setWidth("140px");
			
			lDeltaCosts = newDeltaNumberLabel(0); 
			setWidget(row, col+2, lDeltaCosts);
			
			row++;

			l = new Label("из них оплачено");
			setWidget(row, col+0, l);
			
			lPaymentCost = newNumberLabel();
			lPaymentCost.setWidth("130px");
			setWidget(row, col+1, lPaymentCost);
			
			lAttentionPrePayment = new Label("");
			setWidget(row, col+2, lAttentionPrePayment);
			
			row++;
			
			l = new Label("Маржа");
			setWidget(row, col+0, l);
			
			lMargin = newNumberLabel();
			lMargin.setWidth("130px");
			setWidget(row, col+1, lMargin);

			lDeltaMargin = newDeltaNumberLabel(0); 
			setWidget(row, col+2, lDeltaMargin);
			
			row++;
			
			l = new Label("Штрафы, пени");
			setWidget(row, col+0, l);
			
			eFine = new CurrencyBox();
			eFine.setWidth("130px");
			setWidget(row, col+1, eFine);

			lDeltaFine = newDeltaNumberLabel(0);
			setWidget(row, col+2, lDeltaFine);
			
			row++;
			
			l = new Label("Налог");
			setWidget(row, col+0, l);
			
			lTax = newNumberLabel();
			lTax.setWidth("130px");
			setWidget(row, col+1, lTax);
			
			lDeltaTax = newDeltaNumberLabel(0);
			setWidget(row, col+2, lDeltaTax);
			
			row++;
			
			getCellFormatter().setStyleName(row, col+0, "grayBorder");
			getCellFormatter().setStyleName(row, col+1, "grayBorder");
			getCellFormatter().setStyleName(row, col+2, "grayBorder");
			
			row++;
			
			l = new Label("Прибыль");
			setWidget(row, col+0, l);
			l.addStyleName("bold-text");
			
			lProfit = newNumberLabel();
			lProfit.setWidth("130px");
			setWidget(row, col+1, lProfit);
			lProfit.addStyleName("bold-text");
			
			lDeltaProfit = newDeltaNumberLabel(0);
			setWidget(row, col+2, lDeltaProfit);
			lDeltaProfit.addStyleName("bold-text");
			
		}
	}
	
	public interface TaskTableResources extends TableResources {
	    @Override
	    @Source({ CellTable.Style.DEFAULT_CSS, "TableResources.css","TaskTableResources.css" })
	    TableStyle cellTableStyle();
	}
}
