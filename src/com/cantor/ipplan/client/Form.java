package com.cantor.ipplan.client;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.cantor.ipplan.shared.SortedColumn;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class Form extends Composite {
	private static final int SHEDULE_LOCK_CONTROL = 1000;
	private FocusWidget firstFocusedWidget = null;
	private RootPanel root = null;
	private Ipplan main;

	public Form() {
		startLockControl();
	}
	
	public Form(Ipplan main,RootPanel root) {
		this();
		this.root = root;
		this.main = main;
	}
	
	public Ipplan getMain() {
		return main;
	}
	
	public RootPanel getRoot() {
		return root;
	}
	
	public FocusWidget getFirstFocusedWidget() {
		return firstFocusedWidget;
	}

	public void setFirstFocusedWidget(FocusWidget focused) {
		this.firstFocusedWidget = focused;
	}

	public void show() {
		if(firstFocusedWidget!=null)
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			public void execute() {
				firstFocusedWidget.setFocus(true);
			}
		});	
		root.clear();
		root.add(this);
		this.getElement().getStyle().setCursor(Cursor.DEFAULT);
	}
	
	public static <T> void prepareGrid(DataGrid<T> grid, Collection<T> c) {
	    ListDataProvider<T> dataProvider = new ListDataProvider<T>();
	    dataProvider.addDataDisplay(grid);
		dataProvider.getList().addAll(c);
		setSortingColumns(grid, dataProvider.getList());
	}
	
	public static <T> void setSortingColumns(DataGrid<T> grid, List<T> list) {
	    ListHandler<T> columnSortHandler = new ListHandler<T>(list);
		for (int i = 0; i < grid.getColumnCount(); i++) {
			final Column<T, ?> c = grid.getColumn(i);
			c.setSortable(true);
			Cell<?> cell = c.getCell();
			Comparator<T> cmpr = null;
			if(c instanceof SortedColumn) {
				cmpr = new Comparator<T>(){
					@Override
					public int compare(T o1, T o2) {
			            if (o1 == o2) {
				              return 0;
				            }
				            // Compare the name columns.
				            if (o1 != null) {
				            	Comparable s1 = (Comparable) ((SortedColumn)c).getSortedValue(o1);
				            	Comparable s2 = (Comparable)((SortedColumn)c).getSortedValue(o2);
				            	return (o2 != null) ? s1.compareTo(s2) : 1;
				            }
				            return -1;
					}
				};
			} else
			if(cell instanceof TextCell) {
				cmpr = new Comparator<T>(){
					@Override
					public int compare(T o1, T o2) {
			            if (o1 == o2) {
				              return 0;
				            }
				            // Compare the name columns.
				            if (o1 != null) {
				            	String s1 = (String)c.getValue(o1);
				            	String s2 = (String)c.getValue(o2);
				            	return (o2 != null) ? s1.compareToIgnoreCase(s2) : 1;
				            }
				            return -1;
					}
				};
			} else
			cmpr = new Comparator<T>(){
					@Override
					public int compare(T o1, T o2) {
			            if (o1 == o2) {
				              return 0;
				            }
				            if (o1 != null) {
				            	Comparable s1 = (Comparable) c.getValue(o1);
				            	Comparable s2 = (Comparable)c.getValue(o2);
				            	return (o2 != null) ? s1.compareTo(s2) : 1;
				            }
				            return -1;
					}
			};
				
			if(cmpr!=null) columnSortHandler.setComparator(c,cmpr); 
		}
		grid.addColumnSortHandler(columnSortHandler);
	}

	static public FocusWidget getFirstFocusedWidget(FlexTable table) {
	    for (int row = 0; row < table.getRowCount(); row++) {
	      for (int col = 0; col < table.getCellCount(row); col++) {
	        Widget w = table.getWidget(row, col);
	        if (w instanceof FocusWidget ) {
	          return (FocusWidget) w;
	        }
	      }
	    };
		return null;
	};
	
	protected boolean validate() {
		return true;
	}

	protected void lockControl() {
	}

	private void startLockControl() {
		Timer t = new Timer() {
	      public void run() {
	    	  lockControl();
	      }
	    };
	    t.scheduleRepeating(SHEDULE_LOCK_CONTROL);
	}

}
