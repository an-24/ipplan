package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.CustomerWrapper;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.datepicker.client.DateBox;

@SuppressWarnings("rawtypes")
public class FormCustomer extends Dialog  implements ValueChangeHandler {

	private CustomerWrapper customer;
	private TextBox tbName;
	private TextBox tbPrimaryEmail;
	private TextBox tbCompany;
	private TextBox tbPosition;
	private TextBox tbEmails;
	private TextBox tbPrimaryPhone;
	private TextBox tbPhones;
	private DateBox tbBirthday;
	private ClickHandler okExternalHandler;
	private Image photo;

	public FormCustomer(CustomerWrapper customer) {
		super(customer==null?"Новый клиент":"Изменение данных о клиенте \""+customer.customerName+"\"");
		this.getButtonOk().setText("Сохранить");
		FlexTable table = getContent();
		HorizontalPanel ph;
		int startCol = 1;

		photo = new Image();
		photo.setSize("120px", "120px");
		table.getFlexCellFormatter().setRowSpan(0, 0, 10);
		table.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
		table.setWidget(0, 0, photo);
		
		table.setWidget(0, startCol, new Label("Имя"));
		tbName = new TextBox();
		tbName.setWidth("400px");
		tbName.getElement().setAttribute("placeholder", "Рекомендуем вводить в порядке <Фамилия> <Имя> [Отчество]");
		tbName.addValueChangeHandler(this);
		table.setWidget(0, startCol+1, tbName);
		
		startCol=0;
		table.setWidget(1, startCol, new Label("Дата рождения"));
		tbBirthday = new DateBox(new DatePicker(),null,Ipplan.DEFAULT_DATE_FORMAT);
		table.setWidget(1, startCol+1, tbBirthday);

		table.setWidget(2, startCol, new Label("Представляет юридическое лицо"));
		ph = new HorizontalPanel();
		ph.setSpacing(4);
		ph.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		tbCompany = new TextBox();
		tbCompany.setWidth("158px");
		ph.add(tbCompany);
		ph.add(new Label(" в должности"));
		tbPosition = new TextBox();
		ph.add(tbPosition);
		table.setWidget(2, startCol+1, ph);
		
		table.setWidget(3, startCol, new Label("Основной e-mail"));
		tbPrimaryEmail = new TextBox();
		tbPrimaryEmail.setWidth("240px");
		tbPrimaryEmail.addValueChangeHandler(this);
		table.setWidget(3, startCol+1, tbPrimaryEmail);
		
		table.setWidget(4, startCol, new Label("Дополнительные e-mail"));
		tbEmails = new TextBox();
		tbEmails.setWidth("400px");
		tbEmails.getElement().setAttribute("placeholder", "Можно ввести несколько адресов с разделителем <,> или <пробел>");
		table.setWidget(4, startCol+1, tbEmails);
		
		table.setWidget(5, startCol, new Label("Основной телефон"));
		tbPrimaryPhone = new TextBox();
		tbPrimaryPhone.setWidth("240px");
		table.setWidget(5, startCol+1, tbPrimaryPhone);
		
		table.setWidget(6, startCol, new Label("Дополнительные номера телефонов"));
		tbPhones = new TextBox();
		tbPhones.setWidth("400px");
		tbPhones.getElement().setAttribute("placeholder", "Можно ввести несколько номеров с разделителем <,> или <пробел>");
		table.setWidget(6, startCol+1, tbPhones);
		

		if(customer!=null) {
			this.customer = customer.copy();
			toEditFields();
		} else {
			this.customer = new CustomerWrapper();
			photo.setUrl("resources/images/noname.png");
		}

		this.setButtonOkClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				resetErrors();
				cancel();
				if(validate()) {
					fromEditFields();
					if(okExternalHandler!=null) okExternalHandler.onClick(event);
					hide();
				}
			}
		});
		
		setFirstFocusedWidget(tbName);
	}
	
	public void setName(String n) {
		tbName.setText(n);
	}

	protected boolean validate() {
		if(tbName.getText().isEmpty()) {
			showError(tbName, "Имя не может быть пустым");
			return false;
		}
		return true;
	}

	protected void fromEditFields() {
		customer.customerName = tbName.getText();
		customer.customerBirthday = tbBirthday.getValue();
		customer.customerCompany = nulleable(tbCompany.getText());
		customer.customerPosition = nulleable(tbPosition.getText());
		customer.customerPrimaryEmail = nulleable(tbPrimaryEmail.getText());
		customer.customerEmails = nulleable(tbEmails.getText());
		customer.customerPrimaryPhone = nulleable(tbPrimaryPhone.getText());
		customer.customerPhones = nulleable(tbPhones.getText());
	}

	private void toEditFields() {
		tbName.setText(customer.customerName);
		tbBirthday.setValue(customer.customerBirthday);
		tbCompany.setText(customer.customerCompany);
		tbPosition.setText(customer.customerPosition);
		tbPrimaryEmail.setText(customer.customerPrimaryEmail);
		tbEmails.setText(customer.customerEmails);
		tbPrimaryPhone.setText(customer.customerPrimaryPhone);
		tbPhones.setText(customer.customerPhones);
		if(customer.customerPhoto==null) 
			photo.setUrl("resources/images/noname.png");
	}
	
	private String nulleable(String t){
		return (t!=null)?(t.isEmpty())?null:t:t;
	}

	@Override
	public void onValueChange(ValueChangeEvent event) {
		resetErrors();
	}

	public CustomerWrapper getCustomer() {
		return customer;
	}

	public void setExternalHandler(ClickHandler okExternalHandler) {
		this.okExternalHandler = okExternalHandler;
	}
	
	public static void edit(final DatabaseServiceAsync dbservice, final CustomerWrapper c, final NotifyHandler<CustomerWrapper> competed) {
		final FormCustomer form = new FormCustomer(c);
		form.setExternalHandler(
				new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					dbservice.updateCustomer(form.getCustomer(), new AsyncCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							if(competed!=null) competed.onNotify(form.getCustomer());
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

	public static void add(final DatabaseServiceAsync dbservice, final NotifyHandler<CustomerWrapper> competed) {
		final FormCustomer form = new FormCustomer(null);
		form.setExternalHandler(
				new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					dbservice.addCustomer(form.getCustomer(), new AsyncCallback<CustomerWrapper>() {
						@Override
						public void onSuccess(CustomerWrapper result) {
							if(competed!=null) competed.onNotify(result);
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

}
