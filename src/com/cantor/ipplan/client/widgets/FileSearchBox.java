package com.cantor.ipplan.client.widgets;

import java.util.ArrayList;

import com.cantor.ipplan.client.DatabaseServiceAsync;
import com.cantor.ipplan.client.Ipplan;
import com.cantor.ipplan.client.NotifyHandler;
import com.cantor.ipplan.client.OAuth2;
import com.cantor.ipplan.client.OAuth2.EventOnCloseWindow;
import com.cantor.ipplan.shared.FileLink;
import com.cantor.ipplan.shared.FileLinksWrapper;
import com.cantor.ipplan.shared.ImportExportProcessInfo;
import com.cantor.ipplan.shared.SearchInfo;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;

public class FileSearchBox extends SuggestBox {
	
	protected Element btnPlace;
	private int typeDrive;
	private FileLink filelink;

	public FileSearchBox (DatabaseServiceAsync dbservice) {
		super(new FileSearchSuggestOracle(dbservice),new HelperTextBox(), new HelperSuggestionDisplay());
		((HelperTextBox)getValueBox()).box = this;
		((FileSearchSuggestOracle)getSuggestOracle()).box = this;
		this.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				SuggestionImpl sugg = (SuggestionImpl)event.getSelectedItem();
				if(sugg==null) setFilelink(null);else {
					sugg.filelink.typeDrive = getTypeDrive();
					setFilelink(sugg.filelink);
				}
			}
		});
		setStyleName("gwt-CustomerBox");
		final Element div = DOM.createDiv();
		div.setClassName("gwt-SuggestBox container");
		
		addAttachHandler(new Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if(event.isAttached()) {
					int w = getElement().getOffsetWidth();
					com.google.gwt.dom.client.Element parent = getElement().getParentElement();
					parent.appendChild(div);
					div.appendChild(getElement());
					
					btnPlace = DOM.createDiv();
					div.appendChild(btnPlace);
					btnPlace.setTitle("Очистить");
					
					Event.setEventListener(btnPlace, new EventListener() {
						@Override
						public void onBrowserEvent(Event event) {
							clickBoxButton();
						}
					});
					DOM.sinkEvents((com.google.gwt.user.client.Element) btnPlace, Event.ONMOUSEUP);
					refreshShow();
				}
			}
		});
	}

	public FileLink getFilelink() {
		return filelink;
	}

	public void setFilelink(FileLink filelink) {
		this.filelink = filelink;
		if(filelink==null) setText("");else
			   			   setText(filelink.name);
		refreshShow();
	}


	public void setTypeDrive(int typeDrive) {
		this.typeDrive = typeDrive;
	}
	
	public int getTypeDrive() {
		return typeDrive;
	}

	public static <T> void tokenNeeded(int errcode, int typedrive, final T obj, final NotifyHandler<T> notify) {
		if(errcode==ImportExportProcessInfo.TOKEN_NOTFOUND || errcode==ImportExportProcessInfo.TOKEN_EXPIRED) {
			if(typedrive==FileLinksWrapper.PROVIDER_GOOGLE_DRIVE) {
				OAuth2 auth = new OAuth2(Utils.GOOGLE_AUTH_URL, Utils.GOOGLE_CLIENT_ID,
						Utils.GOOGLE_SCOPE_DRIVE, Utils.REDIRECT_URI);
				auth.setState("drive="+typedrive);
				auth.login(new EventOnCloseWindow() {
					@Override
					public void onCloseWindow() {
						// повторить запрос
						if(notify!=null) notify.onNotify(obj);
					}
				});
			}
			
		};
	}
	
	protected void refreshShow() {
		if(filelink!= null) {
			addStyleName("Customer-bind");
			btnPlace.getStyle().setDisplay(Display.INLINE_BLOCK);
		} else {
			removeStyleName("Customer-bind");
			btnPlace.getStyle().setDisplay(Display.NONE);
		}
			
	}

	private void clickBoxButton() {
		setFilelink(null);
	}
	
	public static class FileSearchSuggestOracle extends SuggestOracle {
		private DatabaseServiceAsync dbservice;
		private FileSearchBox box;
		private Timer tminput;

		public FileSearchSuggestOracle(DatabaseServiceAsync dbservice) {
			super();
			this.dbservice = dbservice;
		}

		@Override
		public boolean isDisplayStringHTML(){
			return true;
		}
		
		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			final String newtext = request.getQuery();
			
			if(tminput!=null) tminput.cancel();
			tminput = new Timer() {
			      @Override
			      public void run() {
			    	  dbservice.searchFile(box.getTypeDrive(), newtext, new AsyncCallback<SearchInfo>() {
						
						@Override
						public void onSuccess(SearchInfo result) {
							if(result.error!=0) {
								tokenNeeded(result.error,box.getTypeDrive(), box, new NotifyHandler<FileSearchBox>() {
									@Override
									public void onNotify(FileSearchBox box) {
										box.showSuggestionList();
									}
								});
								return;
							}
								
							ArrayList<Suggestion> list = new ArrayList<Suggestion>();
							for (FileLink fl : result.data) {
								list.add(new SuggestionImpl(fl));
							}
							callback.onSuggestionsReady(request, new Response(list));
						}

						@Override
						public void onFailure(Throwable caught) {
							Ipplan.showError(caught);
						}
					});
			      }
			};
			tminput.schedule(1000);
			
		}

		
	}
	public static class SuggestionImpl implements Suggestion {

		private FileLink filelink;

		public SuggestionImpl(FileLink filelink) {
			this.filelink = filelink;
		}
		
		@Override
		public String getDisplayString() {
			
			StringBuilder sb = new StringBuilder("<div class=\"gwt-FileSearchBox-Suggestion\">");
			
			sb.append("<img src=\""+filelink.iconUri+"\">")
			  .append("<div>")
			  .append(filelink.name);
			
			sb.append("</div>");
			sb.append("</div>");
			return sb.toString();
		}

		@Override
		public String getReplacementString() {
			return filelink.name;
		}
		
	}
	
	static class HelperTextBox extends TextBox {
		public FileSearchBox box;

		HelperTextBox() {
			super();
			addKeyDownHandler(new KeyDownHandler() {
				
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if(event.getNativeKeyCode()==KeyCodes.KEY_ESCAPE) {
						((DefaultSuggestionDisplay)box.getSuggestionDisplay()).hideSuggestions();
					}
				}
			});
		}
	}

	public void clear() {
		setFilelink(null);
	}

}
