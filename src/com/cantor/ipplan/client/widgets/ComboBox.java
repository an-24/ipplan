package com.cantor.ipplan.client.widgets;


import java.util.ArrayList;
import java.util.List;

import com.cantor.ipplan.client.UserAgent;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Text;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.i18n.shared.BidiFormatter;
import com.google.gwt.i18n.shared.DirectionEstimator;
import com.google.gwt.i18n.shared.HasDirectionEstimator;
import com.google.gwt.i18n.shared.WordCountDirectionEstimator;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.ListenerWrapper;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SourcesChangeEvents;

@SuppressWarnings("deprecation")
public class ComboBox extends FocusWidget implements SourcesChangeEvents,
		HasChangeHandlers, HasName, HasDirectionEstimator {

	public static final DirectionEstimator DEFAULT_DIRECTION_ESTIMATOR = WordCountDirectionEstimator
			.get();

	private static final String BIDI_ATTR_NAME = "bidiwrapped";
	private static final int INSERT_AT_END = -1;


	private DirectionEstimator estimator;
	private InputElement selectElement;
	private Element btnElement;
	private ComboPopupList popupList;
	private boolean lostFocusLocked = false;
	
	private List<Option> list = new ArrayList<Option>();

	private int selectedIndex = -1;

	private int visibleCount;

	private boolean isMultiple = false;

	private Text textElement;
	
	/**
	 * Creates an empty list box in single selection mode.
	 */
	public ComboBox() {
		this(false);
		setStyleName("gwt-ComboBox");
	}

	/**
	 * Creates an empty list box. The preferred way to enable multiple
	 * selections is to use this constructor rather than
	 * {@link #setMultipleSelect(boolean)}.
	 * 
	 * @param isMultipleSelect
	 *            specifies if multiple selection is enabled
	 */
	public ComboBox(boolean isMultipleSelect) {
		super(createElement(isMultipleSelect));
		isMultiple = isMultipleSelect;
		btnElement = getElement().getChild(0).cast();
		selectElement = getElement().getChild(1).cast();
		textElement = getElement().getChild(2).cast();
		

		Event.setEventListener(btnElement, new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				event.preventDefault();
				event.stopPropagation();
				dropDown();
			}
		});
		DOM.sinkEvents((com.google.gwt.user.client.Element) btnElement, Event.ONMOUSEDOWN);
		
		
		addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				event.preventDefault();
				event.stopPropagation();
				dropDown();
			}
		});
		
		addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if (isDropDown() && !lostFocusLocked)
					hideDropDown();
			}
		});
		
		addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode()==KeyCodes.KEY_DOWN) 
					dropDown();
			}
		});

		setStyleName("gwt-ComboBox");
	}
	
	/*	
	protected ComboBox(Element element) {
		super(element);
		selectElement = SelectElement.as(element);
	}
*/
	
	/**
	 * Creates a ListBox widget that wraps an existing &lt;select&gt; element.
	 * 
	 * This element must already be attached to the document. If the element is
	 * removed from the document, you must call
	 * {@link RootPanel#detachNow(Widget)}.
	 * 
	 * @param element
	 *            the element to be wrapped
	 * @return list box
	 */
	public static ComboBox wrap(Element element) {
		// Assert that the element is attached.
		assert Document.get().getBody().isOrHasChild(element);
		
		throw new RuntimeException("wrap not supported");
//TODO
//		ComboBox listBox = new ComboBox(element);
/*
		// Mark it attached and remember it for cleanup.
		listBox.onAttach();
		RootPanel.detachOnWindowClose(listBox);

		return listBox;
*/		
	}

	public void dropDown() {
		if (!isDropDown()) {
			popupList = new ComboPopupList(true);
			popupList.setStyleName("gwt-ComboBox-popup");
			popupList.addCloseHandler(new CloseHandler<PopupPanel>() {
				@Override
				public void onClose(CloseEvent<PopupPanel> event) {
					popupList = null;
				}
			});
			popupList.addAttachHandler(new Handler() {
				@Override
				public void onAttachOrDetach(AttachEvent event) {
					if (event.isAttached()) {
						int left = getAbsoluteLeft();
						int top = getAbsoluteTop() + getOffsetHeight() - 1;

						if (top + popupList.getOffsetHeight() > Window
								.getClientHeight() + Window.getScrollTop())
							top = getAbsoluteTop()
									- popupList.getOffsetHeight() + 1;

						popupList.getElement().getStyle()
								.setWidth(getOffsetWidth(), Unit.PX);
						popupList.setPopupPosition(left, top);

						lostFocusLocked = true;
						popupList.getContentElement().focus();
					}
				}
			});
			fillPopupList();
			popupList.show();
			popupList.getElement().getStyle().setOverflowY(Overflow.SCROLL);
			popupList.getElement().getStyle().setOverflowX(Overflow.HIDDEN);
		}
	}

	public void hideDropDown() {
		if (isDropDown()) {
			popupList.hide();
		}
	}

	private void fillPopupList() {
		EventListener click = new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				if(event.getTypeInt()==Event.ONMOUSEDOWN) {
					int id = new Integer(event.getCurrentTarget().getId());
					setSelectedIndex(id);
					hideDropDown();
					Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
						public void execute() {
							setFocus(true);
						}
					});	
				};
			}
		};
		final Element el = popupList.getContentElement();
		el.setTabIndex(-1);
		Event.setEventListener(el, new EventListener() {
			
			@Override
			public void onBrowserEvent(Event event) {
				if(event.getTypeInt()==Event.ONKEYDOWN) {
					int code = event.getKeyCode();
					if(code==KeyCodes.KEY_DOWN || code==KeyCodes.KEY_UP ||
					   code==KeyCodes.KEY_LEFT || code==KeyCodes.KEY_RIGHT) {
						int idx = getSelectedIndex();
						Element div = getItemElement(idx);
						
						if((code==KeyCodes.KEY_DOWN || code==KeyCodes.KEY_RIGHT) && idx<getItemCount()-1) {
							setSelectedIndex(idx+1);
							div.removeClassName("gwt-ComboBox-item-selected");
							div = div.getNextSiblingElement();
							div.addClassName("gwt-ComboBox-item-selected");
						} else 
							if((code==KeyCodes.KEY_UP || code==KeyCodes.KEY_LEFT) && idx>0) {
								setSelectedIndex(idx-1);
								div.removeClassName("gwt-ComboBox-item-selected");
								div = div.getPreviousSiblingElement();
								div.addClassName("gwt-ComboBox-item-selected");
							}
						event.stopPropagation();
						event.preventDefault();
					} else
						if(code==KeyCodes.KEY_ESCAPE || code==KeyCodes.KEY_ENTER) {
							hideDropDown();
							setFocus(true);
						}
						
				} else 
				if(event.getTypeInt()==Event.ONFOCUS) {
					lostFocusLocked = false;
				}
	
			}
		});
		DOM.sinkEvents((com.google.gwt.user.client.Element) el,Event.ONKEYDOWN | Event.ONFOCUS);
		
		
		int selidx = getSelectedIndex();
		for (int i = 0, len = getItemCount(); i < len; i++) {
			Element div = DOM.createDiv();
			div.setClassName("gwt-ComboBox-item");
			div.setInnerText(getItemText(i));
			div.setId(String.valueOf(i));
			Event.setEventListener(div, click);
			DOM.sinkEvents((com.google.gwt.user.client.Element) div,Event.ONCLICK | Event.ONMOUSEDOWN);
			if(i==selidx)
				div.addClassName("gwt-ComboBox-item-selected");
			el.appendChild(div);
		}
		
	}

	protected Element getItemElement(int id) {
		Element root = popupList.getContentElement();
		NodeList<Node> nodes = root.getChildNodes();
		for (int i = 0, len =root.getChildCount(); i < len; i++) {
			if(nodes.getItem(i) instanceof Element) {
				Element e = (Element) nodes.getItem(i);
				if(Integer.valueOf(e.getId())==id) return e;
			} 
		}
		return null;
	}

	public boolean isDropDown() {
		return popupList != null;
	}

	/**
	 * This constructor may be used by subclasses to explicitly use an existing
	 * element. This element must be a &lt;select&gt; element.
	 * 
	 * @param element
	 *            the element to be used
	 */
	static private Element createElement(boolean isMultipleSelect) {
		Element div = DOM.createDiv();
		Element select = Document.get().createHiddenInputElement();
		Element btnPlace = DOM.createDiv();
		btnPlace.setClassName("gwt-ComboBox-button");
		div.appendChild(btnPlace);
		Element btn = DOM.createDiv();
		btn.setClassName("gwt-ComboBox-button-img");
		btnPlace.appendChild(btn);
		div.appendChild(select);
		div.appendChild(Document.get().createTextNode(""));
		return div;
	}

	public HandlerRegistration addChangeHandler(ChangeHandler handler) {
		return addDomHandler(handler, ChangeEvent.getType());
	}

	/**
	 * @deprecated Use {@link #addChangeHandler} instead
	 */
	@Deprecated
	public void addChangeListener(ChangeListener listener) {
		ListenerWrapper.WrappedChangeListener.add(this, listener);
	}

	/**
	 * Adds an item to the list box. This method has the same effect as
	 * 
	 * <pre>
	 * addItem(item, item)
	 * </pre>
	 * 
	 * @param item
	 *            the text of the item to be added
	 */
	public void addItem(String item) {
		insertItem(item, INSERT_AT_END);
	}

	/**
	 * Adds an item to the list box, specifying its direction. This method has
	 * the same effect as
	 * 
	 * <pre>
	 * addItem(item, dir, item)
	 * </pre>
	 * 
	 * @param item
	 *            the text of the item to be added
	 * @param dir
	 *            the item's direction
	 */
	public void addItem(String item, Direction dir) {
		insertItem(item, dir, INSERT_AT_END);
	}

	/**
	 * Adds an item to the list box, specifying an initial value for the item.
	 * 
	 * @param item
	 *            the text of the item to be added
	 * @param value
	 *            the item's value, to be submitted if it is part of a
	 *            {@link FormPanel}; cannot be <code>null</code>
	 */
	public void addItem(String item, String value) {
		insertItem(item, value, INSERT_AT_END);
	}

	/**
	 * Adds an item to the list box, specifying its direction and an initial
	 * value for the item.
	 * 
	 * @param item
	 *            the text of the item to be added
	 * @param dir
	 *            the item's direction
	 * @param value
	 *            the item's value, to be submitted if it is part of a
	 *            {@link FormPanel}; cannot be <code>null</code>
	 */
	public void addItem(String item, Direction dir, String value) {
		insertItem(item, dir, value, INSERT_AT_END);
	}

	/**
	 * Removes all items from the list box.
	 */
	public void clear() {
		getSelectElement().setValue("");
	}

	public DirectionEstimator getDirectionEstimator() {
		return estimator;
	}

	/**
	 * Gets the number of items present in the list box.
	 * 
	 * @return the number of items
	 */
	public int getItemCount() {
		return list.size();
	}

	/**
	 * Gets the text associated with the item at the specified index.
	 * 
	 * @param index
	 *            the index of the item whose text is to be retrieved
	 * @return the text associated with the item
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range
	 */
	public String getItemText(int index) {
		checkIndex(index);
		return getOptionText(list.get(index));
	}

	public String getName() {
		return getSelectElement().getName();
	}

	/**
	 * Gets the currently-selected item. If multiple items are selected, this
	 * method will return the first selected item ({@link #isItemSelected(int)}
	 * can be used to query individual items).
	 * 
	 * @return the selected index, or <code>-1</code> if none is selected
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * Gets the value associated with the item at a given index.
	 * 
	 * @param index
	 *            the index of the item to be retrieved
	 * @return the item's associated value
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range
	 */
	public String getValue(int index) {
		checkIndex(index);
		return list.get(index).value;
	}

	/**
	 * Gets the number of items that are visible. If only one item is visible,
	 * then the box will be displayed as a drop-down list.
	 * 
	 * @return the visible item count
	 */
	public int getVisibleItemCount() {
		return visibleCount;
	}

	/**
	 * Inserts an item into the list box. Has the same effect as
	 * 
	 * <pre>
	 * insertItem(item, item, index)
	 * </pre>
	 * 
	 * @param item
	 *            the text of the item to be inserted
	 * @param index
	 *            the index at which to insert it
	 */
	public void insertItem(String item, int index) {
		insertItem(item, item, index);
	}

	/**
	 * Inserts an item into the list box, specifying its direction. Has the same
	 * effect as
	 * 
	 * <pre>
	 * insertItem(item, dir, item, index)
	 * </pre>
	 * 
	 * @param item
	 *            the text of the item to be inserted
	 * @param dir
	 *            the item's direction
	 * @param index
	 *            the index at which to insert it
	 */
	public void insertItem(String item, Direction dir, int index) {
		insertItem(item, dir, item, index);
	}

	/**
	 * Inserts an item into the list box, specifying an initial value for the
	 * item. Has the same effect as
	 * 
	 * <pre>
	 * insertItem(item, null, value, index)
	 * </pre>
	 * 
	 * @param item
	 *            the text of the item to be inserted
	 * @param value
	 *            the item's value, to be submitted if it is part of a
	 *            {@link FormPanel}.
	 * @param index
	 *            the index at which to insert it
	 */
	public void insertItem(String item, String value, int index) {
		insertItem(item, null, value, index);
	}

	/**
	 * Inserts an item into the list box, specifying its direction and an
	 * initial value for the item. If the index is less than zero, or greater
	 * than or equal to the length of the list, then the item will be appended
	 * to the end of the list.
	 * 
	 * @param item
	 *            the text of the item to be inserted
	 * @param dir
	 *            the item's direction. If {@code null}, the item is displayed
	 *            in the widget's overall direction, or, if a direction
	 *            estimator has been set, in the item's estimated direction.
	 * @param value
	 *            the item's value, to be submitted if it is part of a
	 *            {@link FormPanel}.
	 * @param index
	 *            the index at which to insert it
	 */
	public void insertItem(String item, Direction dir, String value, int index) {
		
		Option option = new Option();
		option.text = item;
		option.value = value;

		int itemCount = list.size();
		if (index < 0 || index > itemCount) {
			index = itemCount;
		}
		if (index == itemCount) {
			list.add(option);
		} else {
			list.add(index, option);
		}
	}

	/**
	 * Determines whether an individual list item is selected.
	 * 
	 * @param index
	 *            the index of the item to be tested
	 * @return <code>true</code> if the item is selected
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range
	 */
	public boolean isItemSelected(int index) {
		checkIndex(index);
		return selectedIndex == index;
	}

	/**
	 * Gets whether this list allows multiple selection.
	 * 
	 * @return <code>true</code> if multiple selection is allowed
	 */
	public boolean isMultipleSelect() {
		return isMultiple ;
	}

	/**
	 * @deprecated Use the {@link HandlerRegistration#removeHandler} method on
	 *             the object returned by {@link #addChangeHandler} instead
	 */
	@Deprecated
	public void removeChangeListener(ChangeListener listener) {
		ListenerWrapper.WrappedChangeListener.remove(this, listener);
	}

	/**
	 * Removes the item at the specified index.
	 * 
	 * @param index
	 *            the index of the item to be removed
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range
	 */
	public void removeItem(int index) {
		checkIndex(index);
		list.remove(index);
	}

	/**
	 * {@inheritDoc} See note at
	 * {@link #setDirectionEstimator(com.google.gwt.i18n.shared.DirectionEstimator)}
	 * .
	 */
	public void setDirectionEstimator(boolean enabled) {
		setDirectionEstimator(enabled ? DEFAULT_DIRECTION_ESTIMATOR : null);
	}

	/**
	 * {@inheritDoc} Note: this does not affect the direction of
	 * already-existing content.
	 */
	public void setDirectionEstimator(DirectionEstimator directionEstimator) {
		estimator = directionEstimator;
	}

	/**
	 * Sets whether an individual list item is selected.
	 * 
	 * <p>
	 * Note that setting the selection programmatically does <em>not</em> cause
	 * the {@link ChangeHandler#onChange(ChangeEvent)} event to be fired.
	 * </p>
	 * 
	 * @param index
	 *            the index of the item to be selected or unselected
	 * @param selected
	 *            <code>true</code> to select the item
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range
	 */
	public void setItemSelected(int index, boolean selected) {
		checkIndex(index);
		if(!isMultiple) selectedIndex = selected?index:-1; 
			//TODO else
	}

	/**
	 * Sets the text associated with the item at a given index.
	 * 
	 * @param index
	 *            the index of the item to be set
	 * @param text
	 *            the item's new text
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range
	 */
	public void setItemText(int index, String text) {
		setItemText(index, text, null);
	}

	public void setItemText(int index, String text, Direction dir) {
		checkIndex(index);
		if (text == null) {
			throw new NullPointerException(
					"Cannot set an option to have null text");
		}
		list.get(index).text = text;
	}

	@Deprecated
	public void setMultipleSelect(boolean multiple) {
		isMultiple = multiple;
	}

	public void setName(String name) {
		getSelectElement().setName(name);
	}

	public void setSelectedIndex(int index) {
		selectedIndex = index;
		if(selectedIndex<0)	clear();else {
			checkIndex(selectedIndex);
			Option opt = list.get(selectedIndex);
			getSelectElement().setValue(opt.value);
			textElement.setData(opt.text);
		}
	}

	public void setValue(int index, String value) {
		checkIndex(index);
		list.get(index).value = value;
	}

	public void setVisibleItemCount(int visibleItems) {
		visibleCount = visibleItems;
	}

	protected String getOptionText(Option option) {
		return option.text;
	}

	@Override
	protected void onEnsureDebugId(String baseID) {
		super.onEnsureDebugId(baseID);

		// Set the id of each option
		int numItems = getItemCount();
		for (int i = 0; i < numItems; i++) {
			ensureDebugId(getSelectElement(), baseID, "item" + i);
		}
	}


	private void checkIndex(int index) {
		if (index < 0 || index >= getItemCount()) {
			throw new IndexOutOfBoundsException();
		}
	}

	private InputElement getSelectElement() {
		return selectElement;
	}
	
	public int valueIndexOf(String value) {
		int i = 0;
		for (Option opt : list) {
			if(value.equals(opt.value)) return i;
			i++;
		}
		return -1;
	}
	
	class ComboPopupList extends PopupPanel {

		public ComboPopupList(boolean b) {
			super(b);
		}

		public Element getContentElement() {
			return getContainerElement();
		}
		
		
	}

	class Option {
		String text;
		String value;
		String label;
		boolean disabled = false;
	}
}
