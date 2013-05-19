package com.cantor.ipplan.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cantor.ipplan.client.CustomerBox.SuggestionImpl;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;

public class CustomerBox extends SuggestBox {
	
	private DatabaseServiceAsync dbservice;
	private CustomerWrapper customer =  null;
	private Element btnPlace;

	public CustomerBox(DatabaseServiceAsync dbservice) {
		super(new CustomerSuggestOracle(dbservice));
		((CustomerSuggestOracle)getSuggestOracle()).box = this;
		this.dbservice = dbservice;
		this.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				SuggestionImpl sugg = (SuggestionImpl)event.getSelectedItem();
				setCustomer(sugg.customer);
			}
		});
		setStyleName("gwt-CustomerBox");
		final Element div = DOM.createDiv();
		div.setClassName("gwt-SuggestBox");
		
		addAttachHandler(new Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if(event.isAttached()) {
					int w = getElement().getOffsetWidth();
					com.google.gwt.dom.client.Element parent = getElement().getParentElement();
					parent.appendChild(div);
					div.getStyle().setWidth(w, Unit.PX);
					div.appendChild(getElement());
					getElement().getStyle().setWidth(w-22, Unit.PX);
					
					btnPlace = DOM.createDiv();
					div.appendChild(btnPlace);
					
					Event.setEventListener(btnPlace, new EventListener() {
						@Override
						public void onBrowserEvent(Event event) {
							clickBoxButton();
						}
					});
					DOM.sinkEvents((com.google.gwt.user.client.Element) btnPlace, Event.ONMOUSEUP);
					refreshShow();
				}
			}
		});
		
		
	}


	public CustomerWrapper getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerWrapper customer) {
		this.customer = customer;
		refreshShow();
	}
	
	protected void refreshShow() {
		if(customer!=null) {
			removeStyleName("Customer-add");	
			addStyleName("Customer-bind");
			btnPlace.getStyle().setDisplay(Display.BLOCK);
			btnPlace.setTitle("Очистить");
		} else {
			removeStyleName("Customer-add");	
			removeStyleName("Customer-bind");	
			btnPlace.getStyle().setDisplay(Display.NONE);
		}
	}

	private void clickBoxButton() {
		// clean
		if(customer!=null) {
			setCustomer(null);
			setValue("");
		} else {
		// add	
			addCustomer();
		}
	}
	
	private void addCustomer() {
		// TODO Auto-generated method stub
		
	}

	public static class CustomerSuggestOracle extends SuggestOracle {
		private DatabaseServiceAsync dbservice;
		private CustomerBox box;

		public CustomerSuggestOracle(DatabaseServiceAsync dbservice) {
			super();
			this.dbservice = dbservice;
		}

		
		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			if(request.getQuery().length()>3 && box.customer==null) {
				box.addStyleName("Customer-add");
				box.btnPlace.getStyle().setDisplay(Display.BLOCK);
				box.btnPlace.setTitle("Добавить");
			} else {
				box.removeStyleName("Customer-add");
			}
			
			dbservice.findCustomer(request.getQuery(), new AsyncCallback<List<CustomerWrapper>>() {
				
				@Override
				public void onSuccess(List<CustomerWrapper> result) {
					//box.setCustomer(null);
					
					ArrayList<Suggestion> list = new ArrayList<Suggestion>();
					for (CustomerWrapper cw : result) 
						list.add(new SuggestionImpl(cw));
					callback.onSuggestionsReady(request, new Response(list));
				}
				
				@Override
				public void onFailure(Throwable caught) {
					Ipplan.showError(caught);
				}
			});
			
			
		}
		
	}
	
	static class SuggestionImpl implements Suggestion {

		private CustomerWrapper customer;

		public SuggestionImpl(CustomerWrapper customer) {
			this.customer = customer;
		}
		
		@Override
		public String getDisplayString() {
			return customer.customerName;
		}

		@Override
		public String getReplacementString() {
			return customer.customerName;
		}
		
	}
}
