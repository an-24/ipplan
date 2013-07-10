package com.cantor.ipplan.client.widgets;

import com.cantor.ipplan.client.NotifyHandler;
import com.cantor.ipplan.shared.FileLinksWrapper;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.FocusWidget;

public class FileBox extends FocusWidget {

	private FileLinksWrapper filelink;
	private NotifyHandler<FileLinksWrapper> removeHandler;
	private AnchorElement eLink;
	private Element divCmd;
	private NotifyHandler<FileBox> attachHandler;

	public FileBox(FileLinksWrapper flink) {
		super(DOM.createDiv());
		setStyleName("gwt-FileBox");
		
		divCmd = DOM.createDiv();
		//divCmd.setInnerHTML("&#160;");
		getElement().appendChild(divCmd);
		Event.setEventListener(divCmd, new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				if(filelink!=null) remove(); attach();
			}
		});
		DOM.sinkEvents((com.google.gwt.user.client.Element) divCmd, Event.ONMOUSEUP);
		
		eLink = DOM.createAnchor().cast();
		eLink.setClassName("link");
		getElement().appendChild(eLink);
		
		setFilelink(flink);
	}

	public void attach() {
		if(attachHandler!=null) attachHandler.onNotify(this);
	}

	public void remove() {
		if(removeHandler!=null) removeHandler.onNotify(filelink);
	}

	public FileLinksWrapper getFilelink() {
		return filelink;
	}

	public void setFilelink(FileLinksWrapper filelink) {
		this.filelink = filelink;
		if(filelink!=null) {
			eLink.setHref(filelink.filelinksUri);
			eLink.setInnerText(filelink.filelinksName);
			divCmd.setClassName("gwt-FileBox-remove");
			divCmd.setTitle("Удалить");
		} else  {
			eLink.setHref("#");
			eLink.setInnerText("");
			divCmd.setClassName("gwt-FileBox-attach");
			divCmd.setTitle("Прикрепить файл");
		}
	}

	public NotifyHandler<FileLinksWrapper> getRemoveHandler() {
		return removeHandler;
	}

	public void setRemoveHandler(NotifyHandler<FileLinksWrapper> removeHandler) {
		this.removeHandler = removeHandler;
	}

	public NotifyHandler<FileBox> getAttachHandler() {
		return attachHandler;
	}

	public void setAttachHandler(NotifyHandler<FileBox> attachHandler) {
		this.attachHandler = attachHandler;
	}
}
