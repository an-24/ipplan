package com.cantor.ipplan.client.widgets;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;


public class HorizontalPanel extends AbstractPanel {


	public HorizontalPanel() {
		super();
		setDisplayCell("table-cell");
	}
	
	@Override
	protected Element createAlignedDiv() {
	    Element td = DOM.createDiv();
		DOM.setStyleAttribute(td, "display", getDisplayCell());
		DOM.setStyleAttribute(td, "textAlign", horzAlign.getTextAlignString());
		DOM.setStyleAttribute(td, "verticalAlign", vertAlign.getVerticalAlignString());
	    return td;
	}
	
	@Override
	public void setCellHorizontalAlignment(Widget w, HorizontalAlignmentConstant align) {
		super.setCellHorizontalAlignment(w,align);
    }
		  
	@Override
	public void setCellHorizontalAlignment(IsWidget w, HorizontalAlignmentConstant align) {
		super.setCellHorizontalAlignment(w,align);
	}
	
	@Override
	public void setCellVerticalAlignment(Widget w, HasVerticalAlignment.VerticalAlignmentConstant align) {
		super.setCellVerticalAlignment(w,align);
	}
	  
	@Override
	public void setCellVerticalAlignment(IsWidget w, VerticalAlignmentConstant align) {
		super.setCellVerticalAlignment(w,align);
	}

}
