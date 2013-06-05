package com.cantor.ipplan.client;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent.Handler;

public class GridPager extends SimplePager {

	public GridPager() {
		super();
	}
	@Override
	public void setDisplay(HasRows display) {
	    super.setDisplay(display);
	    if(display!=null)
	    	display.addRowCountChangeHandler(new PagerHandler());
	}

 
	protected String createText() {
	    NumberFormat formatter = NumberFormat.getFormat("#,###");
	    HasRows display = getDisplay();
	    Range range = display.getVisibleRange();
	    int pageStart = range.getStart() + 1;
	    int pageSize = range.getLength();
	    int dataSize = display.getRowCount();
	    int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
	    endIndex = Math.max(pageStart, endIndex);
	    boolean exact = display.isRowCountExact();
	    return formatter.format(pageStart) + "-" + formatter.format(endIndex)
	        + (exact ? " из " : " более ") + formatter.format(dataSize);
	}
	  
	public class PagerHandler implements Handler {

		@Override
		public void onRowCountChange(RowCountChangeEvent event) {
			onRangeOrRowCountChanged();
		}

	}
}
