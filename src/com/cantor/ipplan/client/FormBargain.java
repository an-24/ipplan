package com.cantor.ipplan.client;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

@SuppressWarnings("rawtypes")
public class FormBargain extends FlexTable implements ValueChangeHandler{

	public static final int EDIT_VIEW = 0;
	public static final int EDIT_MODE = 1;
	
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
	
	private int mode = EDIT_VIEW;
	private CurrencyBox eRevenue;
	private NumberLabel<Double> lRevenueDelta;
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
	private ContractBox eContract;
	private MainTabPanel tabPanel;
	private int index;
	private Label lTitle;
	private List<Integer> errorList = new ArrayList<Integer>();
	private Label lVersion;
	private Label lDateCreated;

	public FormBargain(BargainWrapper b) {
		super();
		setCellPadding(4);
		setSize("600px", "");
		setStyleName("FormBargain");
		addStyleName("tableBorderCollapse");
		this.bargain = b;
		
		Label l;
		VerticalPanel p;
		
		lTitle = new Label(getTitle());
		lTitle.setStyleName("gwt-FormCaption");
		setWidget(0, 0, lTitle);
		getFlexCellFormatter().setColSpan(0, 0, 3);
		getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		btnPrev = new Button("<");
		btnPrev.setText("<");
		setWidget(1, 0, btnPrev);
		
		p = new VerticalPanel();
		p.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		lVersion = new Label("Версия "+(bargain.bargainVer+1));
		lVersion.addStyleName("gwt-FormSubCaption");
		p.add(lVersion);
		
		lDateCreated = new Label(DateTimeFormat.getMediumDateFormat().format(bargain.bargainCreated));
		p.add(lDateCreated);
		
		setWidget(1, 1, p);
		getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);

		btnNext = new Button(">");
		setWidget(1, 2, btnNext);
		getCellFormatter().setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		
		dbStart = new DateBox();
		setWidget(2, 0, dbStart);
		getCellFormatter().setWidth(2, 0, "160px");
		dbStart.setWidth("113px");
		dbStart.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
		dbStart.setValue(bargain.bargainStart);
		dbStart.setEnabled(false);
		
		Button button = new Button("Календарь");
		setWidget(2, 1, button);
		getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
		dbFinish = new DateBox();
		setWidget(2, 2, dbFinish);
		getCellFormatter().setWidth(2, 2, "160px");
		dbFinish.setWidth("113px");
		dbFinish.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
		dbFinish.setValue(bargain.bargainFinish);
		dbFinish.setEnabled(false);
		getCellFormatter().setHorizontalAlignment(2, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		
		l = new Label("Статус: ");
		setWidget(3, 0, l);
		getCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		
		eStatus = new StatusBox(bargain.status);
		setWidget(3, 1, eStatus);
		eStatus.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(mode==EDIT_MODE) showStatusForm();
			}
		});
		
		lAttention = new Label("");
		setWidget(3, 2, lAttention);
		getCellFormatter().setVerticalAlignment(3, 2, HasVerticalAlignment.ALIGN_MIDDLE);
		getCellFormatter().setHorizontalAlignment(3, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		
		l = new Label("Заказчик:");
		l.addStyleName("tpad10");
		setWidget(4, 0, l);
		getCellFormatter().setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		getCellFormatter().setVerticalAlignment(4, 0, HasVerticalAlignment.ALIGN_TOP);
		getCellFormatter().setHeight(4,0,"36px");

		p = new VerticalPanel();
		p.addStyleName("bpad10");
		
		eCustomer = new CustomerBox(getDataBaseService());
		p.add(eCustomer);		
		eCustomer.setCustomer(bargain.customer);
		eCustomer.getElement().setAttribute("placeholder", "введите имя клиента");
		eCustomer.setWidth("415px");
		
		eContract = new ContractBox(bargain.contract);
		p.add(eContract);

		setWidget(4, 1, p);
		getFlexCellFormatter().setColSpan(4, 1, 2);
		getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		getCellFormatter().setVerticalAlignment(4, 1, HasVerticalAlignment.ALIGN_MIDDLE);
		
		getCellFormatter().setStyleName(5, 0, "grayBorder");
		getCellFormatter().setStyleName(5, 1, "grayBorder");
		getCellFormatter().setStyleName(5, 2, "grayBorder");

		getCellFormatter().setHeight(5,0,"36px");
		
		l = new Label("Выручка");
		setWidget(5, 0, l);

		eRevenue = new CurrencyBox(bargain.bargainRevenue);
		eRevenue.setWidth("130px");
		setWidget(5, 1, eRevenue);
		
		lRevenueDelta = newDeltaNumberLabel(0); //TODO
		setWidget(5, 2, lRevenueDelta);

		l = new Label("Аванс");
		setWidget(6, 0, l);

		ePrePayment = new CurrencyBox(bargain.bargainPrepayment);
		ePrePayment.setWidth("130px");
		setWidget(6, 1, ePrePayment);

		l = new Label("Расходы,");
		setWidget(7, 0, l);

		Button btn = new Button(bargain.bargainCosts==null?"<нет>":NumberFormat.getFormat("#,##0.00").format(bargain.bargainCosts/100.0));
		setWidget(7, 1, btn);
		btn.setWidth("140px");
		
		lDeltaCosts = newDeltaNumberLabel(0); //TODO
		setWidget(7, 2, lDeltaCosts);
		

		l = new Label("из них оплачено");
		setWidget(8, 0, l);
		
		lPaymentCost = newNumberLabel(bargain.bargainPaymentCosts);
		lPaymentCost.setWidth("130px");
		setWidget(8, 1, lPaymentCost);
		
		if(bargain.bargainPaymentCosts!=null && bargain.bargainPaymentCosts>bargain.bargainPrepayment) {
			l = new Label("(аванс не покрывает расходы)");
			l.setStyleName("Attention3");
			setWidget(8, 3, l);
			
		};
		
		l = new Label("Маржа");
		setWidget(9, 0, l);
		
		lMargin = newNumberLabel(bargain.getMargin());
		lMargin.setWidth("130px");
		setWidget(9, 1, lMargin);

		lDeltaMargin = newDeltaNumberLabel(0); //TODO
		setWidget(9, 2, lDeltaMargin);
		
		l = new Label("Штрафы, пени");
		setWidget(10, 0, l);
		
		eFine = new CurrencyBox(bargain.bargainFine);
		eFine.setWidth("130px");
		setWidget(10, 1, eFine);

		lDeltaFine = newDeltaNumberLabel(0); //TODO
		setWidget(10, 2, lDeltaFine);
		
		
		l = new Label("Налог");
		setWidget(11, 0, l);
		
		lTax = newNumberLabel(bargain.bargainTax);
		lTax.setWidth("130px");
		setWidget(11, 1, lTax);
		
		lDeltaTax = newDeltaNumberLabel(0); //TODO
		setWidget(11, 2, lDeltaTax);
		
		getCellFormatter().setStyleName(12, 0, "grayBorder");
		getCellFormatter().setStyleName(12, 1, "grayBorder");
		getCellFormatter().setStyleName(12, 2, "grayBorder");
		
		l = new Label("Прибыль");
		setWidget(13, 0, l);
		l.addStyleName("bold-text");
		
		lProfit = newNumberLabel(bargain.getProfit());
		lProfit.setWidth("130px");
		setWidget(13, 1, lProfit);
		lProfit.addStyleName("bold-text");
		
		lDeltaProfit = newDeltaNumberLabel(0); //TODO
		setWidget(13, 2, lDeltaProfit);
		lDeltaProfit.addStyleName("bold-text");


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
		btn  = new Button("Сохранить и Закрыть");
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
		
		setWidget(14, 0, ph);
		getFlexCellFormatter().setColSpan(14, 0, 3);
		getCellFormatter().setHorizontalAlignment(14, 0, HasHorizontalAlignment.ALIGN_CENTER);
		getCellFormatter().setVerticalAlignment(14, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		
		
		setAttention();
		lockControl();

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
		
		resetErrors();
		validate();
		if(errorList.size()>0) return;
		formFieldToBargain(bargain);
		
		DatabaseServiceAsync db = getDataBaseService();
		db.saveBargain(bargain, closed, new AsyncCallback<BargainWrapper>() {
			
			@Override
			public void onSuccess(BargainWrapper result) {
				bargain.saveCompleted();
				refreshTitle();				
				String message = "Сделка \""+getTitle()+"\" сохранена";
				if(closed) {
					Form.toast(FormBargain.this, message);
					tabPanel.remove(FormBargain.this);
					History.back();
				} else { 
					Form.toast(FormBargain.this.btnSave, message);
					bargainToFormField(result);
					bargain = result;
				};	
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		});
		
	};

	protected void bargainToFormField(BargainWrapper bw) {
		lTitle.setText(bargain.getFullName());
		lVersion.setText("Версия "+(bw.bargainVer+1));
		lDateCreated.setText(DateTimeFormat.getMediumDateFormat().format(bw.bargainCreated));
		dbStart.setValue(bw.bargainStart);
		dbFinish.setValue(bw.bargainFinish);
		eStatus.setStatus(bw.status);
		eCustomer.setCustomer(bw.customer);
		eContract.setContract(bw.contract);
		eRevenue.setValue(bw.bargainRevenue);
		ePrePayment.setValue(bw.bargainPrepayment);
		eFine.setValue(bw.bargainFine);
	}

	private void formFieldToBargain(BargainWrapper bw) {
		bw.customer = eCustomer.getCustomer();
		bw.contract = eContract.getContract();
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
			
	}

	private void lockControl() {
		btnPrev.setEnabled(bargain.bargainVer>0);
		btnNext.setEnabled(!bargain.isNew());
		dbStart.setEnabled(mode==EDIT_MODE);
		dbFinish.setEnabled(mode==EDIT_MODE);
		eRevenue.setEnabled(mode==EDIT_MODE);
	}

	public String getTitle() {
		String s = bargain.getFullName();
		if(this.bargain.isDirty()) s = "* "+s;
		return s;
	}
	
	public BargainWrapper getBargain() {
		return bargain; 
	}

	public void setMode(int newmode) {
		if(mode!=newmode) {
			mode = newmode;
			lockControl();
		}
	}


	protected void showStatusForm() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onValueChange(ValueChangeEvent event) {
		if(mode==EDIT_MODE) {
			resetErrors();
			
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
			if(event.getSource()==eRevenue || event.getSource()==eFine)
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
			refreshTitle();
		}	
		
		
	}

	private void refreshTitle() {
		tabPanel.getTabBar().setTabHTML(index, makeHTMLTab());
		initButtonClose();
		lTitle.setText(getTitle());
	}


	private void initButtonClose() {
		TableElement bar = TableElement.as(tabPanel.getTabBar().getElement());
		TableCellElement cell = TableCellElement.as(TableRowElement.as(bar.getRows().getItem(0)).getCells().getItem(index+1));
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

	public void setIndex(int idx) {
		this.index = idx;
		
	}

	public int getIndex() {
		return index;
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
	
	
}
