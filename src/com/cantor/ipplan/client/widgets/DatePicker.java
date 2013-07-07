package com.cantor.ipplan.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.datepicker.client.CalendarModel;
import com.google.gwt.user.datepicker.client.CalendarView;
import com.google.gwt.user.datepicker.client.DefaultCalendarView;
import com.google.gwt.user.datepicker.client.MonthSelector;


public class DatePicker extends com.google.gwt.user.datepicker.client.DatePicker {

    public DatePicker() {
    	this(new DefaultCalendarView());
    }
    
    protected DatePicker(CalendarView view) {
        super(new MonthAndYearSelector(),view,new CalendarModel()) ;
        MonthAndYearSelector monthSelector = (MonthAndYearSelector)	this.getMonthSelector() ;
        monthSelector.setPicker(this) ;
        monthSelector.setModel(this.getModel()) ;
    }

    public void refreshComponents() {
            super.refreshAll() ;
    }

	public static class MonthAndYearSelector extends MonthSelector {

		  private static final String BASE_NAME = "datePicker" ;

		  private PushButton backwards;
		  private PushButton forwards;
		  private PushButton backwardsYear;
		  private PushButton forwardsYear;
		  private Grid grid;
		  private int previousYearColumn = 0;
		  private int previousMonthColumn = 1 ;
		  private int monthColumn = 2 ;
		  private int nextMonthColumn = 3;
		  private int nextYearColumn = 4 ;
		  private CalendarModel model ;
		  private DatePicker picker ;


		public void setModel(CalendarModel model) {
			this.model = model;
		}



		public void setPicker(DatePicker picker) {
			this.picker = picker;
		}



		@Override
		  protected void refresh() {
		    String formattedMonth = getModel().formatCurrentMonth();
		    grid.setText(0, monthColumn, formattedMonth);
		  }

		  @Override
		  protected void setup() {

			class _PushButton extends PushButton{
				@Override
				protected void onAttach() {
					super.onAttach();
					setTabIndex(-1);
				}
			}   
			  
		    backwards = new _PushButton();
		    backwards.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		        addMonths(-1);
		      }
		    });

		    backwards.getUpFace().setHTML("&lsaquo;");
		    backwards.setStyleName(BASE_NAME + "PreviousButton");
		    //backwards.getElement().getParentElement().setAttribute("tabindex", "-1");

		    forwards = new _PushButton();
		    forwards.getUpFace().setHTML("&rsaquo;");
		    forwards.setStyleName(BASE_NAME + "NextButton");
		    forwards.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		        addMonths(+1);
		      }
		    });


		    // Set up backwards year
		    backwardsYear = new _PushButton();
		    backwardsYear.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		        addMonths(-12);
		      }
		    });

		    backwardsYear.getUpFace().setHTML("&laquo;");
		    backwardsYear.setStyleName(BASE_NAME + "PreviousButton");

		    forwardsYear = new _PushButton();
		    forwardsYear.getUpFace().setHTML("&raquo;");
		    forwardsYear.setStyleName(BASE_NAME + "NextButton");
		    forwardsYear.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		        addMonths(+12);
		      }
		    });

		    // Set up grid.
		    grid = new Grid(1, 5);
		    grid.setWidget(0, previousYearColumn, backwardsYear);
		    grid.setWidget(0, previousMonthColumn, backwards);
		    grid.setWidget(0, nextMonthColumn, forwards);
		    grid.setWidget(0, nextYearColumn, forwardsYear);

		    CellFormatter formatter = grid.getCellFormatter();
		    formatter.setStyleName(0, monthColumn, BASE_NAME + "Month");
		    formatter.setWidth(0, previousYearColumn, "1");
		    formatter.setWidth(0, previousMonthColumn, "1");
		    formatter.setWidth(0, monthColumn, "100%");
		    formatter.setWidth(0, nextMonthColumn, "1");
		    formatter.setWidth(0, nextYearColumn, "1");
		    grid.setStyleName(BASE_NAME + "MonthSelector");
		    initWidget(grid);
		  }

		  public void addMonths(int numMonths) {
			  model.shiftCurrentMonth(numMonths);
		      picker.refreshComponents();
		  }


	}
    
}

