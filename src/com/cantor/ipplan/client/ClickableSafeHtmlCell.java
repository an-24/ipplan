package com.cantor.ipplan.client;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

class ClickableSafeHtmlCell extends AbstractCell<SafeHtml> {
	
	ClickableSafeHtmlCell() {
		super(CLICK, KEYDOWN);
	}
	
	@Override
	public void onBrowserEvent(Context context, Element parent, SafeHtml value,
	      NativeEvent event, ValueUpdater<SafeHtml> valueUpdater) {
	    super.onBrowserEvent(context, parent, value, event, valueUpdater);
	    if (CLICK.equals(event.getType())) {
	      onEnterKeyDown(context, parent, value, event, valueUpdater);
	    }
	}
	@Override
	protected void onEnterKeyDown(Context context, Element parent, SafeHtml value,
	      NativeEvent event, ValueUpdater<SafeHtml> valueUpdater) {
	    if (valueUpdater != null) {
	      valueUpdater.update(value);
	    }
	}
	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			SafeHtml value, SafeHtmlBuilder sb) {
	    if (value != null) {
	        sb.append(value);
	      }
	}
}