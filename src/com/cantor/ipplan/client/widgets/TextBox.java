package com.cantor.ipplan.client.widgets;

import com.cantor.ipplan.client.HasInplaceEdit;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class TextBox extends com.google.gwt.user.client.ui.TextBox implements HasInplaceEdit {

	public TextBox() {
		super();
	}

	public TextBox(Element element) {
		super(element);
	}
	
	@Override
	public Widget wrapElement(Element e) {
	    assert Document.get().getBody().isOrHasChild(e);
	    TextBox textBox = new TextBox(e);
	    textBox.onAttach();
	    return textBox;
	}

	@Override
	public boolean setEditValue(Object value) {
		setText(value.toString());
		return true;
	}

	@Override
	public Object getEditValue() {
		return getText();
	}


}
