package com.cantor.ipplan.client;

import com.cantor.ipplan.shared.StatusWrapper;
import com.google.gwt.dom.client.Style.Clear;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.FocusWidget;


public class StatusBox extends FocusWidget implements HasValueChangeHandlers<StatusWrapper>{

	private StatusWrapper status;
	private StatusWrapper oldStatus;
	private Element divStatusName;
	private Element divNext;
	private StatusChangeEventListiner changeListiner;
	private Element divPause;
	private boolean locked = false;

	public StatusBox(StatusWrapper status) {
		super(DOM.createDiv());

		
		divPause = DOM.createDiv();
		divPause.setClassName("gwt-StatusBox-pause");
		getElement().appendChild(divPause);
		divPause.setTitle("Приостановить");
		Event.setEventListener(divPause, new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				if(!locked) pause(); else undo();
			}
		});
		DOM.sinkEvents((com.google.gwt.user.client.Element) divPause, Event.ONMOUSEUP);
		
		divStatusName = DOM.createDiv();
		divStatusName.setInnerText("<неизвестен>");
		getElement().appendChild(divStatusName);
		
		divNext = DOM.createDiv();
		divNext.setClassName("gwt-StatusBox-next");
		getElement().appendChild(divNext);
		Event.setEventListener(divNext, new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				next();
			}
		});
		DOM.sinkEvents((com.google.gwt.user.client.Element) divNext, Event.ONMOUSEUP);

		Element div = DOM.createDiv();
		div.getStyle().setClear(Clear.BOTH);
		div.getStyle().setFloat( com.google.gwt.dom.client.Style.Float.NONE);
		getElement().appendChild(div);
		
		setStyleName("gwt-StatusBox");
	
		this.setStatus(status); 
	}
	
	protected void undo() {
		if(locked) {
			lock(false);
			setStatus(oldStatus);
		}
	}

	public void setChangeListiner(StatusChangeEventListiner changeListiner) {
		this.changeListiner = changeListiner;
	}

	public void pause() {
		if(changeListiner!=null)
			changeListiner.onPause(status);
	}

	public void next() {
		if(changeListiner!=null && status!=null)
			changeListiner.onNext(status);
	}

	public StatusWrapper getStatus() {
		return status;
	}

	public void setStatus(StatusWrapper status) {
		if(!equalStatus(status)) {
			this.oldStatus = this.status; 
			this.status = status;
			ValueChangeEvent.fire(this, this.status);
			refreshStatus();
		}
	}

	public void refreshStatus() {
		setFinish(false);
		getElement().removeClassName("Attention3");
		getElement().removeClassName("Attention2");
		getElement().removeClassName("gwt-StatusBox-suspend");
		
		if(status!=null) {
			divStatusName.setInnerText(status.statusName);
			int[] newState =  StatusWrapper.getNextState(status.statusId,true);
			if(newState.length>1) {
				String t = "Установить следующий статус. Возможны варианты: ";
				for (int i = 0, len = newState.length; i < len; i++) {
					StatusWrapper st = StatusWrapper.getStatus(newState[i]);
					t+=st.statusName;
					if(i<len-1) t+=",";
				}
				divNext.setTitle(t);
			} else
			if(newState.length==1) {
				divNext.setTitle(StatusWrapper.getStatus(newState[0]).statusName);
			} else
				if(!isLocked()) setFinish(true);
			// attention
			if(this.status.statusId==StatusWrapper.CLOSE_FAIL)
				getElement().addClassName("Attention3");
			if(this.status.statusId==StatusWrapper.SUSPENDED) {
				getElement().addClassName("Attention2");
				if(!isLocked()) getElement().addClassName("gwt-StatusBox-suspend");
			}
			// продажи приостановить Нельзя!
			if(this.status.statusId<StatusWrapper.EXECUTION) {
				divPause.getStyle().setVisibility(Visibility.HIDDEN);
			} else
				divPause.getStyle().setVisibility(Visibility.VISIBLE);
			
			
		} else  {
			divStatusName.setInnerText("<неизвестен>");
			divNext.setTitle("");
		}
	}

	public void lock(boolean v) {
		this.locked = v;
		if(v) {
			getElement().addClassName("gwt-StatusBox-lock");			
			divPause.setTitle("Отменить");
		} else {
			getElement().removeClassName("gwt-StatusBox-lock");			
			divPause.setTitle("Приостановить");
		}
	}
	
	public void setFinish(boolean v) {
		if(v) {
			getElement().addClassName("gwt-StatusBox-finish");			
		} else {
			getElement().removeClassName("gwt-StatusBox-finish");			
		}
	}
	

	public boolean isLocked() {
		return this.locked;
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<StatusWrapper> handler) {
	    return addHandler(handler, ValueChangeEvent.getType());
	}

	
	private boolean equalStatus(StatusWrapper status) {
		int id1 = this.status==null?0:this.status.statusId;
		int id2 = status==null?0:status.statusId;
		return (id1==id2); 		
	}
	
	
	public interface StatusChangeEventListiner {
		public void onNext(StatusWrapper oldStatus);
		public void onPause(StatusWrapper oldStatus);
	}






}
