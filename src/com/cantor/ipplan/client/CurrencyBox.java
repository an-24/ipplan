package com.cantor.ipplan.client;


import java.text.ParseException;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.ui.ValueBox;

public class CurrencyBox extends ValueBox<Integer> {
	
	static final private NumberFormat displayFormat = NumberFormat.getFormat("#,##0.00");
	static final private String decimalSep = LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator();

	public CurrencyBox() {
	    super(Document.get().createTextInputElement(), new CurrencyRenderer(), new CurrencyParser());
	}
	
	public CurrencyBox(Integer v) {
		this();
		setStyleName("gwt-CurrencyBox");
		setValue(v);
		
		addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				setValue(getValue());
			}
		});
		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				setText(CurrencyRenderer.simpleRender(getValue())); 
			}
		});
	}
	

	  static public class CurrencyRenderer extends AbstractRenderer<Integer> {
		  @Override
		  public String render(Integer object) {
			  if (object == null) return "";
			  return displayFormat.format(object/100.0);
		  }
		  
		  public static String simpleRender(Integer object) {
			  if (object == null) return "";
			  String intVal =	new Integer(object.intValue()/100).toString();
			  String fracVal = new Integer(object.intValue()%100).toString();
			  return intVal+decimalSep+fracVal; 
		  }

		  
	  };
	  
	  static public class CurrencyParser implements Parser<Integer> {

		@Override
		public Integer parse(CharSequence text) throws ParseException {
		    if (text==null || "".equals(text.toString())) {
		        return null;
		    }

		    try {
		    	// comma change
		    	text = text.toString().replace(',', decimalSep.charAt(0));
		    	// dot change
		    	text = text.toString().replace('.', decimalSep.charAt(0));
		    	
		    	return new Long(Math.round(displayFormat.parse(text.toString())*100)).intValue();
		    } catch (NumberFormatException e) {
		        throw new ParseException(e.getMessage(), 0);
		    }
		}
		  
	  };
	  
}
