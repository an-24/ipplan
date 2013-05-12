package com.cantor.ipplan.client;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.ValueBoxBase;

public class InputPrompt implements BlurHandler, FocusHandler {

	private String promptText;
	ValueBoxBase input;

	public InputPrompt(String text, ValueBoxBase input) {
		this.promptText = text;
		apply(input);
	}

	public void apply(ValueBoxBase input) {
		this.input = input;
		input.addBlurHandler(this);
		input.addFocusHandler(this);
		showPrompt();
	}
	
	public boolean isEmpty() {
		return promptText.equals(input.getText()) || input.getText().isEmpty();
	} 

	public void onBlur(BlurEvent event) {
		showPrompt();
	}

	public void onFocus(FocusEvent event) {
		if(isEmpty())
			hidePrompt();
	}

	private void showPrompt() {
		if (input.getText().isEmpty()) {
			input.setText(promptText);
			input.addStyleName("gwt-InputPrompt");
		}
	}


	private void hidePrompt() {
		input.setText("");
		input.removeStyleName("gwt-InputPrompt");
	}
}