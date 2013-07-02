package com.cantor.ipplan.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.cantor.ipplan.client.DatabaseServiceAsync;
import com.cantor.ipplan.client.HasInplaceEdit;
import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.shared.CostsWrapper;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
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
import com.google.gwt.user.client.ui.Widget;

public class CostItemBox extends SuggestBox implements HasBlurHandlers,HasInplaceEdit {

	
	private CostsWrapper cost =  null;
	private Element btnPlace;
	private DatabaseServiceAsync dbservice;
	private Element outerDiv;

	public CostItemBox(DatabaseServiceAsync dbservice) {
		this(dbservice, new TextBox());
	}
    

	public CostItemBox(DatabaseServiceAsync dbservice, TextBox textBox) {
		super(new CustomerSuggestOracle(dbservice),textBox, new HelperSuggestionDisplay());
		this.dbservice = dbservice;
		((CustomerSuggestOracle)getSuggestOracle()).box = this;
		this.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				SuggestionImpl sugg = (SuggestionImpl)event.getSelectedItem();
				setCost(sugg.cost);
			}
		});
		getElement().setClassName("gwt-InnerBox");
		outerDiv = DOM.createDiv();
		setStyleName("gwt-SuggestBox container");
		
		addAttachHandler(new Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if(event.isAttached()) {
					int w = getElement().getOffsetWidth();
					com.google.gwt.dom.client.Element parent = getElement().getParentElement();
					parent.appendChild(outerDiv);
					//outerDiv.getStyle().setWidth(w, Unit.PX);
					outerDiv.appendChild(getElement());
					
					btnPlace = DOM.createDiv();
					btnPlace.setClassName("gwt-InnerBox-Button");
					outerDiv.appendChild(btnPlace);
					
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
	
	@SuppressWarnings("deprecation")
	protected void delegateEvent(Widget target, GwtEvent<?> event) {
		// костыль, от того, что SuggestBox не вызывает StopPropagation 
		if(event instanceof KeyDownEvent && isSuggestionListShowing()) {
			NativeEvent ne = ((KeyDownEvent)event).getNativeEvent();
			int keycode = ne.getKeyCode();
			if(keycode==KeyCodes.KEY_DOWN || keycode==KeyCodes.KEY_UP ||
			   keycode==KeyCodes.KEY_PAGEDOWN || keycode==KeyCodes.KEY_PAGEUP) {
				ne.stopPropagation();
				ne.preventDefault();
			}
			return;
		} 
		super.delegateEvent(target, event);
	}
	
	public Element getStyleElement() {
		return outerDiv;
	}


	public CostsWrapper getCost() {
		return cost;
	}

	public void setCost(CostsWrapper cost) {
		this.cost = cost;
		if(cost==null) setValue("",true); else setValue(cost.costsName,true);
		refreshShow();
	}
	
	protected void refreshShow() {
		if(cost!=null) {
			getElement().removeClassName("Customer-add");
			getElement().addClassName("Customer-bind");
			btnPlace.getStyle().setDisplay(Display.BLOCK);
			btnPlace.setTitle("Очистить");
		} else {
			getElement().removeClassName("Customer-add");
			getElement().removeClassName("Customer-bind");
			btnPlace.getStyle().setDisplay(Display.NONE);
		}
	}

	private void clickBoxButton() {
		// clean
		if(cost!=null) {
			setCost(null);
		} else {
		// add	
			addCost();
		}
	}
	
	private void addCost() {
		// TODO Auto-generated method stub
		
	}

	public static class CustomerSuggestOracle extends SuggestOracle {
		private DatabaseServiceAsync dbservice;
		private CostItemBox box;
		private Timer tminput;

		public CustomerSuggestOracle(DatabaseServiceAsync dbservice) {
			super();
			this.dbservice = dbservice;
		}

		
		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			
			final String newtext = request.getQuery();
			
			box.cost = null;
			box.refreshShow();
			
			if(newtext.length()>3) {
				box.getElement().addClassName("Customer-add");
				box.btnPlace.getStyle().setDisplay(Display.BLOCK);
				box.btnPlace.setTitle("Добавить");
			} else {
				box.getElement().removeClassName("Customer-add");
			}
			
			if(tminput!=null) tminput.cancel();
			tminput = new Timer() {
			      @Override
			      public void run() {
						dbservice.findCost(newtext, new AsyncCallback<List<CostsWrapper>>() {
							@Override
							public void onSuccess(List<CostsWrapper> result) {
								ArrayList<Suggestion> list = new ArrayList<Suggestion>();
								for (CostsWrapper cw : result) 
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

		private CostsWrapper cost;

		public SuggestionImpl(CostsWrapper c) {
			this.cost = c;
		}
		
		@Override
		public String getDisplayString() {
			return cost.costsName;
		}

		@Override
		public String getReplacementString() {
			return cost.costsName;
		}
	}

	@Override
	public HandlerRegistration addBlurHandler(BlurHandler handler) {
		return getValueBox().addBlurHandler(handler);
	}


	@Override
	public Widget wrapElement(com.google.gwt.dom.client.Element e) {
	    assert Document.get().getBody().isOrHasChild(e);

	    class HelperTextBox extends TextBox {
	    	public HelperTextBox(com.google.gwt.dom.client.Element e) {
	    		super(e);
	    	}
	    }
	    CostItemBox box = new CostItemBox(dbservice, new HelperTextBox(e));
	    box.onAttach();
	    return box;
	}


	@Override
	public boolean setEditValue(Object value) {
		setCost((CostsWrapper) value);
		return true;
	}


	@Override
	public Object getEditValue() {
		return getCost();
	}
	
}
