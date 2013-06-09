package com.cantor.ipplan.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cantor.ipplan.shared.SortedColumn;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class Form extends Composite {
	private static final int SHEDULE_LOCK_CONTROL = 1000;
	private FocusWidget firstFocusedWidget = null;
	private RootPanel root = null;
	private Ipplan main;
	private Map<FlexTable,List<Integer>> errorList = new HashMap<FlexTable, List<Integer>>();

	public Form() {
		startLockControl();
	}
    
	protected void onLoad() {
		// для выравнивания по центру
		// корректируем размеры в formContainer
		Element container = getElement().getOwnerDocument().getElementById("formContainer");
		container.getStyle().setMarginLeft(-container.getOffsetWidth()/2, Unit.PX);
		container.getStyle().setMarginTop(-container.getOffsetHeight()/2, Unit.PX);
		// удаляем лоадер
		DOM.getElementById("apploader").removeFromParent();
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
	
	public static <T> ListDataProvider<T> prepareGrid(AbstractCellTable<T> grid, Collection<T> c) {
		return prepareGrid(grid, c, true);
	}

	public static <T> ListDataProvider<T> prepareGrid(AbstractCellTable<T> grid, Collection<T> c, boolean sorting) {
	    ListDataProvider<T> dataProvider = new ListDataProvider<T>();
	    dataProvider.addDataDisplay(grid);
		dataProvider.getList().addAll(c);
		if(sorting) setSortingColumns(grid, dataProvider.getList());
		grid.setEmptyTableWidget(new Label("Записи отсутствуют"));
		if(grid instanceof CellTable) {
			((CellTable<T>)grid).setDataProvider(dataProvider);
		}
		return dataProvider;
		/*
		display: table-cell;
		vertical-align: middle;
		height: 100px;
		*/
	}

	public static <T> void setSortingColumns(AbstractCellTable<T> grid, List<T> list) {
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
	        if (w instanceof FocusWidget && ((FocusWidget) w).isEnabled()) {
	          return (FocusWidget) w;
	        }
	      }
	    };
		return null;
	};
	
	public static native Element getActiveElement(Element element) /*-{
	   return element.ownerDocument.activeElement;
	}-*/; 	
	
	public static native boolean isHasChild(Element parent, Element child)/*-{
		while (child != null) {
			if (parent==child) {
				return true;
			}
			child = child.parentNode;
		}
		return false;		
	}-*/;
	
	protected boolean validate() {
		return true;
	}

	protected void lockControl() {
	}

	static public void toast(Widget w, String message) {
		Balloon b = new Balloon(message, true);
		b.show(w);
	}
	
	private void startLockControl() {
		Timer t = new Timer() {
	      public void run() {
	    	  lockControl();
	      }
	    };
	    t.scheduleRepeating(SHEDULE_LOCK_CONTROL);
	}
	
	public void showError(FlexTable table, Widget w,String message) {
		for (int r = 0, len = table.getRowCount(); r < len; r++) 
			for (int c = 0, len1 = table.getCellCount(r); c < len1; c++) { 
				if(w==table.getWidget(r, c))
					showError(table,r+1,message);
		}
	}

	private void showError(FlexTable table, int beforeRow,String message) {
		int rowError = table.insertRow(beforeRow);
		Label l = new Label(message);
		l.setStyleName("errorHint");
		table.getCellFormatter().setHorizontalAlignment(rowError, 0, HasHorizontalAlignment.ALIGN_CENTER);
		table.getCellFormatter().setVerticalAlignment(rowError, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		table.setWidget(rowError, 0, l);
		table.getFlexCellFormatter().setColSpan(rowError, 0, 3);
		List<Integer> le = errorList.get(table);
		if(le==null) {
			le = new ArrayList<Integer>();
			errorList.put(table,le);
		}
		le.add(rowError);
	}

	public void resetErrors() {
		for (FlexTable table : errorList.keySet()) { 
			int offs = 0;
			for (int row : errorList.get(table) ) { 
				table.removeRow(row+offs);
				offs--;
			}
		}	
		errorList.clear();
	}

}
