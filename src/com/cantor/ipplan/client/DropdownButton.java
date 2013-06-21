package com.cantor.ipplan.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

public class DropdownButton extends Button {

	private MenuBar menu = new MenuBar(true);
	private PopupPanel popup = new PopupPanel(true);
	private boolean showmenu = false;
	
	public DropdownButton(String txt) {
		super();
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<div>"+txt+"<span class=\"gwt-DropdownButton-dropbtn\">"+"</span></div>");
		setHTML(builder.toSafeHtml());
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(!showmenu) dropdown();else closeup();
			}
		});
		menu.setVisible(true);
		popup.add(menu);
		popup.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				// нажатие на кнопку при открытом меню должно его закрывать
				// чтобы срабатывало после обработки click
				Timer t = new Timer() {
					public void run() {
						showmenu = false;
				    }
				};
				t.schedule(100);
			}
		});
	}

	public void closeup() {
		popup.hide();
		showmenu = false;
	}

	public void dropdown() {
		popup.showRelativeTo(this);
		showmenu = true;
	}

	public MenuBar getMenu() {
		return menu;
	}
	

}
