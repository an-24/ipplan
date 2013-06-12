package com.cantor.ipplan.client;

import java.util.ArrayList;
import java.util.List;

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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;

public class CustomerBox extends SuggestBox {
	
	private CustomerWrapper customer =  null;
	private Element btnPlace;

	public CustomerBox(DatabaseServiceAsync dbservice) {
		super(new CustomerSuggestOracle(dbservice),new TextBox(), new HelperSuggestionDisplay());
		((CustomerSuggestOracle)getSuggestOracle()).box = this;
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
		if(customer==null) setValue("",true); else setValue(customer.customerName,true);
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
		private Timer tminput;

		public CustomerSuggestOracle(DatabaseServiceAsync dbservice) {
			super();
			this.dbservice = dbservice;
		}

		
		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			
			final String newtext = request.getQuery();
			
			box.customer = null;
			box.refreshShow();
			
			if(newtext.length()>3) {
				box.addStyleName("Customer-add");
				box.btnPlace.getStyle().setDisplay(Display.BLOCK);
				box.btnPlace.setTitle("Добавить");
			} else {
				box.removeStyleName("Customer-add");
			}
			
			if(tminput!=null) tminput.cancel();
			tminput = new Timer() {
			      @Override
			      public void run() {
						dbservice.findCustomer(newtext, new AsyncCallback<List<CustomerWrapper>>() {
							@Override
							public void onSuccess(List<CustomerWrapper> result) {
								
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
			};
			tminput.schedule(1000);
			
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
