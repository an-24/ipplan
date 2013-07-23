package com.cantor.ipplan.client;

import static com.google.gwt.dom.client.BrowserEvents.BLUR;
import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;
import static com.google.gwt.dom.client.BrowserEvents.KEYUP;

import com.cantor.ipplan.client.widgets.CellTable;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.view.client.Range;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.event.dom.client.HasBlurHandlers;

@SuppressWarnings("rawtypes")
public class InplaceEditor<C> extends AbstractEditableCell<C,InplaceEditor.ViewData> {

	private SimpleSafeHtmlRenderer renderer;
	private Widget widget;
	private CellTable owner;
	public ViewData currentViewEdit;
	private DisplayValueFormatter<C> formatter;

	public InplaceEditor(Widget editor, CellTable owner) {
	    this(editor,owner,null);
	}

	public InplaceEditor(Widget editor, CellTable owner, DisplayValueFormatter<C> formatter) {
	    super(CLICK, KEYUP, KEYDOWN, BLUR);
	    widget = editor;
		((Focusable)widget).setTabIndex(0);
	    this.renderer = SimpleSafeHtmlRenderer.getInstance();
	    this.owner = owner;
	    this.formatter = formatter;
	}
	
	static class ViewData<C> {

	    private boolean isEditing;
	    private Widget editor;

	    private C original;
	    private C value;
		public Object key;
		public ValueUpdater<C> updater;

	    public ViewData(C v) {
	      this.original = v;
	      this.value = v;
	      this.isEditing = true;
	    }

	    @Override
	    public boolean equals(Object o) {
	      if (o == null) {
	        return false;
	      }
	      ViewData vd = (ViewData) o;
	      return equalsOrBothNull(original, vd.original)
	          && equalsOrBothNull(value, vd.value) && isEditing == vd.isEditing;
	    }

	    @Override
	    public int hashCode() {
	      return original.hashCode() + value.hashCode()
	          + Boolean.valueOf(isEditing).hashCode() * 29;
	    }

	    public void setEditing(boolean mode) {
	      this.isEditing = mode;
          original = value;
	    }

	    private boolean equalsOrBothNull(Object o1, Object o2) {
	      return (o1 == null) ? o2 == null : o1.equals(o2);
	    }
	    
		private void prepareEditor() {
			if(editor instanceof ValueBoxBase)
				((ValueBoxBase)editor).selectAll();
			((Focusable)editor).setFocus(true);
		}
		
	}
	
	
	@Override
	public boolean isEditing(Context context, Element parent, C value) {
	    ViewData viewData = getViewData(context.getKey());
	    return viewData == null ? false : viewData.isEditing;
	}

	public boolean isEditing() {
	    return currentViewEdit == null ? false : currentViewEdit.isEditing;
	}
	

	@Override
	public void render(Context context, C value, SafeHtmlBuilder sb) {
		ViewData viewData;
		if(context!=null) {
			Object key = context.getKey();
			viewData = getViewData(key);
		} else
			viewData = currentViewEdit;

		String toRender = "";
		if(value!=null) 
			toRender = formatter==null?value.toString():formatter.format(value); 
		
		
	    if (viewData != null) {
	      if (viewData.isEditing) {
	    	  com.google.gwt.dom.client.Element e = this.widget.getElement();
	    	  sb.appendHtmlConstant(e.getPropertyString("outerHTML"));
	        return;
	      } else {
	    	  C v = (C) viewData.value;
	    	  if(v!=null)
	    		  toRender = formatter==null?v.toString():formatter.format(v);
	      }  
	    }

	    if (toRender != null && toRender.trim().length() > 0) {
	      sb.append(renderer.render(toRender));
	    } else 
	    	sb.appendHtmlConstant("\u00A0");
	}
	
	
	@Override
	public void onBrowserEvent(Context context, Element parent, C value,
	      NativeEvent event, ValueUpdater<C> updater) {
	    final Object key = context.getKey();
	    ViewData viewData = getViewData(key);
	    if (viewData != null && viewData.isEditing) {
	    	editEvent(context, parent, value, viewData, event, updater);
	    } else {
	    	String type = event.getType();
	    	int keyCode = event.getKeyCode();
	    	//boolean editPressed = KEYUP.equals(type) && keyCode == 113;
	    	if (CLICK.equals(type)/* || editPressed*/) {
	    		// запустим процесс первода в фокус
			    ScheduledCommand pendingEditMode = new ScheduledCommand() {
			        @Override
			        public void execute() {
			    		getViewData(key).prepareEditor();
			        }
			      };
			    Scheduler.get().scheduleFinally(pendingEditMode);
	    	}
	    }
	}

	public boolean commit() {
		if(!isEditing()) return true;
		try {
			Object value = ((HasInplaceEdit)currentViewEdit.editor).getEditValue();
			currentViewEdit.updater.update(value);
			currentViewEdit.value = value;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isFocused() {
		return currentViewEdit!=null && currentViewEdit.editor!=null && currentViewEdit.editor.isVisible() &&
			   hasFocus(currentViewEdit.editor.getElement());
	};

	public void setFocus() {
		if(currentViewEdit!=null) currentViewEdit.prepareEditor();
		
	}
	
	public void setEditMode(boolean v) {
		if (isEditing()!=v)
		if(v) {
			int colidx = getColumnIndex();
			final Object record = owner.getCurrentValue();
			final Column col = owner.getColumn(colidx);
			final Context context =  
				new Context(owner.getCurrentRowOnPage()+owner.getPageStart(),
						    colidx,owner.getValueKey(record));
			ValueUpdater updater = new ValueUpdater<C>() {
		        @Override
		        public void update(C value) {
		        	// внимание context.getKey(), в случае использования keyProvider булет
		        	// возврашать не record
		        	FieldUpdater upd = col.getFieldUpdater();
		        	if(upd!=null) upd.update(context.getIndex(), record, value);
		        }
		    };
		    Element elRow = owner.getRowElement(owner.getCurrentRowOnPage());
			Element div = (Element) ((Element)elRow.getChild(colidx)).getFirstChild();
			// start 
			startEdit(context,div,(C) col.getValue(record),updater);
		} else {
			currentViewEdit.setEditing(false);
			int colidx = getColumnIndex();
			int row = owner.getCurrentRowOnPage();
			// при генерации следующей стрв=аницы, данных уже нет
			if(row>=0) {
			    Element elRow = owner.getRowElement(row);
				Element div = (Element) ((Element)elRow.getChild(colidx)).getFirstChild();
				div.getStyle().setOverflow(Overflow.HIDDEN);
				setValue(null,div, (C) currentViewEdit.value);
			}
		}
	}
	
	public Column getColumn() {
		return owner.getColumn(getColumnIndex());
	}

	
	private void startEdit(Context context, Element parent,C value, ValueUpdater<C> updater) {
		Object key = context.getKey();
		ViewData viewData = getViewData(key);
        if (viewData == null) {
	        viewData = new ViewData<C>(value);
	        viewData.key = key;
	        viewData.updater = updater;
	        setViewData(key, viewData);
	    };

	    viewData.setEditing(true);
    	currentViewEdit = viewData;

		setValue(context,parent,(C) viewData.value);
		viewData.editor = wrap(parent.getFirstChildElement());
		if(!(viewData.editor instanceof SuggestBox))
			viewData.editor.addStyleName("gwt-InplaceEditor");
		parent.getStyle().setMarginRight(10, Unit.PX);
		parent.getStyle().setOverflow(Overflow.VISIBLE);
		
		
		
		((HasBlurHandlers)viewData.editor).addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(final BlurEvent event) {
	    		new Timer() {
					@Override
					public void run() {
						owner.resetSelection(false);
					}
	    			
	    		}.schedule(0);
			}
		});
		((HasInplaceEdit)viewData.editor).setEditValue(value);
	}
	
	private int getColumnIndex() {
		for (int i = 0, len =owner.getColumnCount(); i < len; i++) {
			Column col = owner.getColumn(i);
			Cell<?> cell = col.getCell();
			if(cell==this) return i;
		}
		return -1;
	}
	
	private native boolean hasFocus(Element element) /*-{
	   return element.ownerDocument.activeElement == element;
	}-*/; 	
/*
	private void startEditRow(final Context context, Element parent) {
		Element elRow = owner.getRowElement(getRowByChildElement(parent));
		for (int i = 0, len =owner.getColumnCount(); i < len; i++) {
			final Column col = owner.getColumn(i);
			Cell<?> cell = col.getCell();
			if(cell!=this)
				if(cell instanceof InplaceEditor) {
					Element div = (Element) ((Element)elRow.getChild(i)).getFirstChild();
					
					ValueUpdater update = new ValueUpdater<C>() {
				        @Override
				        public void update(C value) {
				        	// внимание context.getKey(), в случае использования keyProvider булет
				        	// возврашать не record
				        	col.getFieldUpdater().update(context.getIndex(), context.getKey(), value);
				        }
				      };
					((InplaceEditor<?>)cell).startEdit(context, div, null,false,update);
				}	
		}
	}
*/	

	private int getRowByChildElement(Element child) {
		Element td = child.getParentElement();
		Range range = owner.getVisibleRange();
		for (int i = range.getStart(), len = range.getLength(); i < len; i++) {
			TableRowElement r = owner.getRowElement(i);
			NodeList<TableCellElement> cells = r.getCells();
			for (int j = 0, len1 = cells.getLength(); j < len1; j++) {
				if(cells.getItem(j)==td) return i;
			}
		};
		return -1;
	}

	private void editEvent(Context context,final Element parent, C value, ViewData viewData, NativeEvent event,
			ValueUpdater<C> updater) {
	    String type = event.getType();
	    if(CLICK.equalsIgnoreCase(type) && viewData.editor!=null) {
	    	if(!hasFocus(viewData.editor.getElement()))
	    		viewData.prepareEditor();
	    };
	};
	
	private void cancel(ViewData viewData) {
		((HasInplaceEdit)viewData.editor).setEditValue(viewData.original);
	}

	private void commit(ViewData viewData, ValueUpdater<C> updater) {
		updater.update((C) ((HasInplaceEdit)viewData.editor).getEditValue());
	}

	private Widget wrap(Element el) {
		return (Widget) ((HasInplaceEdit)widget).wrapElement(el);
	}

	public interface DisplayValueFormatter<C> {
		public String format(C value);
	}


}
