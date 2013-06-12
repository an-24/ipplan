package com.cantor.ipplan.client;

import java.util.Collection;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestBox.SuggestionCallback;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class HelperSuggestionDisplay extends DefaultSuggestionDisplay {
	HelperSuggestionDisplay() {
		super();
	}
    public PopupPanel getPopupPanel() {
        return super.getPopupPanel();
    }
    protected void showSuggestions(final SuggestBox suggestBox,
            Collection<? extends Suggestion> suggestions,
            boolean isDisplayStringHTML, boolean isAutoSelectEnabled,
            final SuggestionCallback callback) {
    	super.showSuggestions(suggestBox, suggestions, isDisplayStringHTML, isAutoSelectEnabled, callback);
		// hack
		PopupPanel popPanel = getPopupPanel();
		popPanel.getElement().getStyle().setOverflowY(Overflow.AUTO);
		popPanel.getElement().getStyle().setOverflowX(Overflow.HIDDEN);
    }
}
