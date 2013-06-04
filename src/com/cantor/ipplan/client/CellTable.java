package com.cantor.ipplan.client;

import static com.google.gwt.dom.client.BrowserEvents.BLUR;
import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SelectionModel.AbstractSelectionModel;
import com.google.gwt.view.client.SetSelectionModel;

public class CellTable<T> extends com.google.gwt.user.cellview.client.CellTable<T> {

	public static final int START_EDIT_KEY = 113;
	private ListDataProvider<T> provider;
	private T currentValue;
	protected boolean alwaysShowEditor = true;

	public CellTable(int pageSize) {
		this(pageSize,null);
	}
	
	public CellTable(int pageSize, ProvidesKey<T> keyProvider) {
		super(pageSize, (CellTable.Resources)GWT.create(TableResources.class), keyProvider);
		getElement().setAttribute("tabindex", "0");
		//setStyleName("gwt-CellTable");
		
		final RecordSelectionModel smodel = new RecordSelectionModel(new CanSelection<T>() {
			@Override
			public boolean isCanSelection(T oldItem, T newItem) {
				boolean ok = post();
				if(ok) setEditorMode(false);
				return ok;
			}
		});
		
		smodel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				// т.к. в HasDataPresenter вызывается ensurePendingState
				// нужно запустить через finally
			    ScheduledCommand pendingEditMode = new ScheduledCommand() {
			        @Override
			        public void execute() {
						currentValue = smodel.getSelectedObject();
						if(alwaysShowEditor) setEditorMode(true);
			        }
			    };
			    Scheduler.get().scheduleFinally(pendingEditMode);
			}
		});
		setSelectionModel(smodel,new SelectionEventManager());
	}
	
	public void setEditorMode(boolean b) {
		for (int i = 0, len = getColumnCount(); i < len; i++) {
			Cell<?> cell = getColumn(i).getCell();
			if(cell instanceof InplaceEditor) {
				((InplaceEditor)cell).setEditMode(b);
			}
		}
	}
	
	public boolean isEditorMode() {
		for (int i = 0, len = getColumnCount(); i < len; i++) {
			Cell<?> cell = getColumn(i).getCell();
			if(cell instanceof InplaceEditor) {
				if(((InplaceEditor)cell).isEditing()) return true;
			}
		}
		return false;
	}
	
	public InplaceEditor<?> getFocusedEditor() {
		for (int i = 0, len = getColumnCount(); i < len; i++) {
			Cell<?> cell = getColumn(i).getCell();
			if(cell instanceof InplaceEditor) {
				InplaceEditor<?> ed = (InplaceEditor<?>)cell;
				if(ed.isEditing() && ed.isFocused()) return ed; 
			}
		}
		return null;
	}

	public InplaceEditor<?> getFirstEditor() {
		for (int i = 0, len = getColumnCount(); i < len; i++) {
			Cell<?> cell = getColumn(i).getCell();
			if(cell instanceof InplaceEditor) {
				return (InplaceEditor<?>)cell;
			}
		}
		return null;
	}
	
	public boolean post() {
		for (int i = 0, len = getColumnCount(); i < len; i++) {
			Cell<?> cell = getColumn(i).getCell();
			if(cell instanceof InplaceEditor) {
				if(!((InplaceEditor<?>)cell).commit()) return false;
			}	
		}
		return true;
	}

	public void setDataProvider(ListDataProvider<T> provider) {
		this.provider = provider;
	}
	
	public ListDataProvider<T> getDataProvider() {
		return provider;
	}
	
	public void setValues(List<? extends T> values) {
		setRowData(0,values);
	}

	public List<? extends T> getValues() {
		return provider.getList();
	}
	
	public T next(T v) {
		return getValue(v,1);
	}

	public T next() {
		return next(currentValue);
	}

	public T next(boolean onlyVisible) {
		if(onlyVisible) return getVisibleValue(currentValue,1);
		return next(currentValue);
	}
	

	public T prior(T v) {
		return getValue(v,-1);
	}

	public T prior() {
		return prior(currentValue);
	}

	public T prior(boolean onlyVisible) {
		if(onlyVisible) return getVisibleValue(currentValue,-1);
		return prior(currentValue);
	}
	
	public T getValue(T v, int delta) {
		return getValue(v,delta,getValues());
	}

	public T getVisibleValue(T v, int delta) {
		return getValue(v,delta,getVisibleItems());
	}
	
	public T getValue(T v, int delta, List<? extends T> values) {
		if(values==null) return null;
		if(v==null)
			if(values.size()>0) return values.get(0);
						   else return null;
		int idx = values.indexOf(v);
		if(idx<0 || (idx+delta)<0 || (idx+delta)>=values.size() ) return null;
		return values.get(idx+delta);
	}

	public T getValue(int delta) {
		return getValue(currentValue,delta);
	}

	public T getCurrentValue() {
		return currentValue;
	}
	
	public int getCurrentRowOnPage() {
		List<T> list = getVisibleItems();
		for (int i = 0, len = list.size(); i < len; i++) {
			if(list.get(i)==currentValue) return i;
		}
		return -1;
	}

	public void setCurrentValue(T currentValue) {
		this.currentValue = currentValue;
	}

	protected void onBrowserEvent2(Event event) {
    	String type = event.getType();
    	if(KEYDOWN.equals(type)) {
    		T n = null;
    		final int code = event.getKeyCode();
			switch (code) {
    		case CellTable.START_EDIT_KEY:
    			setEditorMode(true);
    			n = currentValue;
    			break;
			case KeyCodes.KEY_UP:
				n = prior(true);
				if(n==null && hasPriorPage()) {
					priorPage();
					n = prior();
				}
				break;
			case KeyCodes.KEY_DOWN:
				n = next(true);
				if(n==null && hasNextPage()) {
					nextPage();
					n = next();
				}
				break;
			case KeyCodes.KEY_PAGEUP:
				if(hasPriorPage()) {
					int row = getCurrentRowOnPage();
					priorPage();
					// getVisibleItems уже сменился
					List<T> vitems = getVisibleItems();
					n = vitems.get(row);
				} else {
					int row = getCurrentRowOnPage();
					if(row>0) n = getVisibleItems().get(0);
				}
				break;
			case KeyCodes.KEY_PAGEDOWN:
				if(hasNextPage()) {
					int row = getCurrentRowOnPage();
					nextPage();
					// getVisibleItems уже сменился
					List<T> vitems = getVisibleItems();
					if(row>=vitems.size()) n = vitems.get(vitems.size()-1);
									  else n = vitems.get(row);
					//next(vitems.get(vitems.size()-1));
				} else {
					int row = getCurrentRowOnPage();
					List<T> vitems = getVisibleItems();
					if(row<vitems.size()-1) n = vitems.get(vitems.size()-1);
				}
				break;
				
			default:
				break;
			}
			if(n!=null) {
				final InplaceEditor<?> curred = getFocusedEditor();
				getSelectionModel().setSelected(n, true);
				// в очередь, т.к. onChangeSelection срабатывает через ScheduledCommand 
			    ScheduledCommand pendingEditMode = new ScheduledCommand() {
			        @Override
			        public void execute() {
			        	InplaceEditor<?> editor = curred!=null?curred:
			        		code==CellTable.START_EDIT_KEY?getFirstEditor():null; 
						if(editor!=null) {
							Column<T, ?> col = editor.getColumn();
							((InplaceEditor<?>) col.getCell()).setFocus();
						}
			        }
			      };
			    Scheduler.get().scheduleFinally(pendingEditMode);
			    event.preventDefault();
			    event.stopPropagation();
			}	
    	} else
    	if(BLUR.equals(type)) {
    		new Timer() {
				@Override
				public void run() {
					if(isEditorMode()) {
			    		Element e = Form.getActiveElement(getElement());
			    		if(e==null || !Form.isHasChild(getElement(),e)) {
			    			if(post()) setEditorMode(false);
			    		}
					}
				}
    			
    		}.schedule(0);
    	};
		super.onBrowserEvent2(event);
	}
	
	public boolean resetSelection() {
		Element e = Form.getActiveElement(getElement());
		if(e==null || !Form.isHasChild(getElement(),e)) {
			if(post()) {
				setEditorMode(false);
				((RecordSelectionModel)getSelectionModel()).clear();
				return true;
			}
		}
		return false;
	}
    
	private void nextPage() {
		Range range = getVisibleRange();
	    setPageStart(range.getStart() + range.getLength());
	}
	
	private boolean hasNextPage() {
        if (!isRowCountExact()) return true;
        Range range = getVisibleRange();
		return range.getStart() + range.getLength() < getRowCount();
	}
	
	private void priorPage() {
		Range range = getVisibleRange();
	    setPageStart(range.getStart() - range.getLength());
	}
	
	protected boolean hasPriorPage() {
	    return getPageStart() > 0 && getRowCount() > 0;
	}

	interface CanSelection<T> {
		public boolean isCanSelection(T oldItem, T newItem);
	}
	
	class SelectionEventManager extends DefaultSelectionEventManager<T> {

		protected SelectionEventManager() {
			super(null);
		};
	    
		@Override
		public void onCellPreview(CellPreviewEvent<T> event) {
		    // Early exit if selection is already handled or we are editing.
		    if (/*event.isCellEditing() ||*/ event.isSelectionHandled()) {
		      return;
		    }

		    // Early exit if we do not have a SelectionModel.
		    HasData<T> display = event.getDisplay();
		    SelectionModel<? super T> selectionModel = display.getSelectionModel();
		    if (selectionModel == null) {
		      return;
		    }

		    // Check for user defined actions.
		    SelectAction action = SelectAction.DEFAULT;

		    // Handle the event based on the SelectionModel type.
		    if (selectionModel instanceof MultiSelectionModel<?>) {
		      // Add shift key support for MultiSelectionModel.
		      handleMultiSelectionEvent(event, action,
		          (MultiSelectionModel<? super T>) selectionModel);
		    } else {
		    	// Use the standard handler.
		    	//handleSelectionEvent(event, action, selectionModel);
		        String type = event.getNativeEvent().getType();
		        if (CLICK.equals(type)) {
		        	selectionModel.setSelected(event.getValue(), true);
		        	if(!((RecordSelectionModel)selectionModel).isChanged()) {
			        	CellTable table = (CellTable) event.getDisplay();
			        	if(!table.isEditorMode()) {
			        		currentValue = event.getValue();
			        		table.setEditorMode(true);
			        	}	
		        	}
		        }
		    }
	    }
		

		
	}

    // copy SingleSelectionModel  
    class RecordSelectionModel extends AbstractSelectionModel<T> implements SetSelectionModel<T> {

    	  private Object curKey;
    	  private T curSelection;

    	  private boolean newSelected;
    	  private T newSelectedItem = null;
    	  private boolean newSelectedPending;
    	  
    	  private CanSelection canSelect =  null;
		  private boolean changed = false;

    	  public RecordSelectionModel(ProvidesKey<T> keyProvider, CanSelection<T> canSelect) {
    	    super(keyProvider);
    	    this.canSelect = canSelect;
    	  }

    	  public boolean isChanged() {
			return changed;
		}

		public RecordSelectionModel(CanSelection<T> canSelect) {
      	    this(null,canSelect);
      	  }
    	  
    	  @Override
    	  public void clear() {
    	    setSelected(getSelectedObject(), false);
    	  }

    	  public T getSelectedObject() {
    	    resolveChanges();
    	    return curSelection;
    	  }

    	  @Override
    	  public Set<T> getSelectedSet() {
    	    Set<T> set = new HashSet<T>();
    	    if (curSelection != null) {
    	      set.add(curSelection);
    	    }
    	    return set;
    	  }

    	  @Override
    	  public boolean isSelected(T item) {
    	    resolveChanges();
    	    if (curSelection == null || curKey == null || item == null) {
    	      return false;
    	    }
    	    return curKey.equals(getKey(item));
    	  }

    	  @Override
    	  public void setSelected(T item, boolean selected) {
    	    // If we are deselecting an item that isn't actually selected, ignore it.
    	    if (!selected) {
    	      Object oldKey = newSelectedPending ? getKey(newSelectedItem) : curKey;
    	      Object newKey = getKey(item);
    	      if (!equalsOrBothNull(oldKey, newKey)) {
    	        return;
    	      }
    	    }
    	    newSelectedItem = item;
    	    newSelected = selected;
    	    newSelectedPending = true;
    	    resolveChanges();
    	    //scheduleSelectionChangeEvent();
    	  }

    	  @Override
    	  protected void fireSelectionChangeEvent() {
    	    if (isEventScheduled()) {
    	      setEventCancelled(true);
    	    }
    	    resolveChanges();
    	  }

    	  private boolean equalsOrBothNull(Object a, Object b) {
    	    return (a == null) ? (b == null) : a.equals(b);
    	  }

    	  private void resolveChanges() {
    	    if (!newSelectedPending) {
    	      return;
    	    }

    	    Object key = getKey(newSelectedItem);
    	    boolean sameKey = equalsOrBothNull(curKey, key);
    	    changed = false;
    	    if (newSelected) {
    	      changed = !sameKey;
    	      
    	      if(changed && canSelect!=null)
    	    	  if(!canSelect.isCanSelection(curSelection, newSelectedItem)) return;
    	      
    	      curSelection = newSelectedItem;
    	      curKey = key;
    	      
    	    } else if (sameKey) {
    	      changed = true;
    	      curSelection = null;
    	      curKey = null;
    	    }

    	    newSelectedItem = null;
    	    newSelectedPending = false;

    	    // Fire a selection change event.
    	    if (changed) {
    	      SelectionChangeEvent.fire(this);
    	    };
    	  }
    }


}
