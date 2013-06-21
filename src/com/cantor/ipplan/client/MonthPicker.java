package com.cantor.ipplan.client;

import java.util.Date;

import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.user.datepicker.client.CalendarView;

@SuppressWarnings("deprecation")
public class MonthPicker extends DatePicker implements HasFocus {

	private int month;
	private int year;


	public MonthPicker() {
		this(new Date());
	}

	public MonthPicker(Date date) {
		super(new MonthCalendarView());
		month = date.getMonth();
		year = date.getYear();
		setStyleName("gwt-MonthPicker");
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public void setMonth(int year, int month) {
		this.month = month;
		this.year = year;
		refreshAll();
	}

	public void setMonth(Date date) {
		this.month = date.getMonth();
		refreshAll();
	}
	
	public Date getStartDate() {
		return new Date(year,month,1);
	}

	public Date getFinishDate() {
		Date date = new Date(year,month,1);
		CalendarUtil.addMonthsToDate(date, 1);
		CalendarUtil.addDaysToDate(date, -1);
		return date;
	}
	
    public void refreshComponents() {
        super.refreshAll();
        Date date = getModel().getCurrentMonth();
		month = date.getMonth();
		year = date.getYear();
    }
	
	static class MonthCalendarView extends CalendarView {
		

		@Override
		public void addStyleToDate(String styleName, Date date) {
		}

		@Override
		public Date getFirstDate() {
			return new Date();
		}

		@Override
		public Date getLastDate() {
			return new Date();
		}

		@Override
		public boolean isDateEnabled(Date date) {
			return true;
		}

		@Override
		public void removeStyleFromDate(String styleName, Date date) {
		}

		@Override
		public void setEnabledOnDate(boolean enabled, Date date) {
		}

		@Override
		protected void refresh() {
		}

		@Override
		protected void setup() {
		    initWidget(new Label());
		}
		
	}

	@Override
	public int getTabIndex() {
		return this.getElement().getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
	}

	@Override
	public void setFocus(boolean focused) {
		if(focused) this.getElement().focus(); 
			  else this.getElement().blur();
		
	}

	@Override
	public void setTabIndex(int index) {
		this.getElement().setTabIndex(index);
		
	}

	@Override
	public void addFocusListener(FocusListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFocusListener(FocusListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addKeyboardListener(KeyboardListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeKeyboardListener(KeyboardListener listener) {
		// TODO Auto-generated method stub
		
	}



}
