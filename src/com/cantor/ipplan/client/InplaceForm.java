package com.cantor.ipplan.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class InplaceForm extends FlexTable {

	private Map<FlexTable,List<Integer>> errorList = new HashMap<FlexTable, List<Integer>>();

	public void showError(FlexTable table, Widget w,String message) {
		for (int r = 0, len = table.getRowCount(); r < len; r++) 
			for (int c = 0, len1 = table.getCellCount(r); c < len1; c++) { 
				if(w==table.getWidget(r, c))
					showError(table,r+1,message);
		}
	}
	
	public void showError(FlexTable table, int beforeRow,String message) {
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
	
	public boolean existErrors() {
		return errorList.size()>0;
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
