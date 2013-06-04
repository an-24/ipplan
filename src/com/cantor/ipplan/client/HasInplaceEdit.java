package com.cantor.ipplan.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public interface HasInplaceEdit {
	public Widget wrapElement(Element e);
	public boolean setEditValue(Object value);
	public Object getEditValue();
}
