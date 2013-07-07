package com.cantor.ipplan.client.widgets;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractPanel extends ComplexPanel  implements HasAlignment {

	protected HorizontalAlignmentConstant horzAlign = ALIGN_DEFAULT;
	protected VerticalAlignmentConstant vertAlign = ALIGN_TOP;
	private int spacing = 0;
	private Element rootDiv;
	
	public AbstractPanel() {
		setElement(getRoot());
	}

	private Element getRoot() {
		if(rootDiv==null) {
			rootDiv = DOM.createDiv();
			DOM.setStyleAttribute(rootDiv, "display", "table");
		}
		return rootDiv;
	}

	@Override
	  public void add(Widget w) {
	    Element td = createAlignedDiv();
	    DOM.appendChild(getRoot(), td);
	    add(w, td);
	  }

	  public HorizontalAlignmentConstant getHorizontalAlignment() {
	    return horzAlign;
	  }

	  public VerticalAlignmentConstant getVerticalAlignment() {
	    return vertAlign;
	  }

	  public void insert(IsWidget w, int beforeIndex) {
	    insert(asWidgetOrNull(w), beforeIndex);
	  }

	  public void insert(Widget w, int beforeIndex) {
	    checkIndexBoundsForInsertion(beforeIndex);

	    /*
	     * The case where we reinsert an already existing child is tricky.
	     * 
	     * For the WIDGET, it ultimately removes first and inserts second, so we
	     * have to adjust the index within ComplexPanel.insert(). But for the DOM,
	     * we insert first and remove second, which means we DON'T need to adjust
	     * the index.
	     */
	    Element td = createAlignedDiv();
	    DOM.insertChild(getRoot(), td, beforeIndex);
	    insert(w, td, beforeIndex, false);
	  }

	  @Override
	  public boolean remove(Widget w) {
	    // Get the TD to be removed, before calling super.remove(), because
	    // super.remove() will detach the child widget's element from its parent.
	    Element td = DOM.getParent(w.getElement());
	    boolean removed = super.remove(w);
	    if (removed) {
	      DOM.removeChild(getRoot(), td);
	    }
	    return removed;
	  }

	  /**
	   * Sets the default horizontal alignment to be used for widgets added to this
	   * panel. It only applies to widgets added after this property is set.
	   * 
	   * @see HasHorizontalAlignment#setHorizontalAlignment(HasHorizontalAlignment.HorizontalAlignmentConstant)
	   */
	  public void setHorizontalAlignment(HorizontalAlignmentConstant align) {
	    horzAlign = align;
	  }

	  /**
	   * Sets the default vertical alignment to be used for widgets added to this
	   * panel. It only applies to widgets added after this property is set.
	   * 
	   * @see HasVerticalAlignment#setVerticalAlignment(HasVerticalAlignment.VerticalAlignmentConstant)
	   */
	  public void setVerticalAlignment(VerticalAlignmentConstant align) {
	    vertAlign = align;
	  }

	  
	  Element getWidgetTd(Widget w) {
		    if (w.getParent() != this) {
		      return null;
		    }
		    return DOM.getParent(w.getElement());
	  }
	  
	  /**
	   * <b>Affected Elements:</b>
	   * <ul>
	   * <li>-# = the cell at the given index.</li>
	   * </ul>
	   * 
	   * @see UIObject#onEnsureDebugId(String)
	   */
	  @Override
	  protected void onEnsureDebugId(String baseID) {
	    super.onEnsureDebugId(baseID);
	    int numChildren = getWidgetCount();
	    for (int i = 0; i < numChildren; i++) {
	      ensureDebugId(getWidgetTd(getWidget(i)), baseID, "" + i);
	    }
	  }

	abstract protected Element createAlignedDiv(); 

	public int getSpacing() {
		return spacing;
	}

	public void setSpacing(int spacing) {
		this.spacing = spacing;
		DOM.setStyleAttribute(getRoot(), "borderSpacing", spacing+"px");
		if(spacing>0) 
			DOM.setStyleAttribute(getRoot(), "borderCollapse", "separate");
	}
	
	protected void setCellHorizontalAlignment(Element td,
		      HorizontalAlignmentConstant align) {
		DOM.setStyleAttribute(td, "textAlign", align.getTextAlignString());
	}

	protected void setCellVerticalAlignment(Element td,
		      VerticalAlignmentConstant align) {
		DOM.setStyleAttribute(td, "verticalAlign", align.getVerticalAlignString());
	}
	  /**
	   * Sets the horizontal alignment of the given widget within its cell.
	   * 
	   * @param w the widget whose horizontal alignment is to be set
	   * @param align the widget's horizontal alignment, as defined in
	   *          {@link HasHorizontalAlignment}.
	   */

	protected void setCellHorizontalAlignment(Widget w,
	      HorizontalAlignmentConstant align) {
	    Element td = getWidgetTd(w);
	    if (td != null) {
	      setCellHorizontalAlignment(td, align);
	    }
	  }
	  
	  /**
	   * Overloaded version for IsWidget.
	   * 
	   * @see #setCellHorizontalAlignment(Widget,HasHorizontalAlignment.HorizontalAlignmentConstant)
	   */
	protected void setCellHorizontalAlignment(IsWidget w,
	      HorizontalAlignmentConstant align) {
	    this.setCellHorizontalAlignment(w.asWidget(), align);
	}

	  /**
	   * Sets the vertical alignment of the given widget within its cell.
	   * 
	   * @param w the widget whose vertical alignment is to be set
	   * @param align the widget's vertical alignment, as defined in
	   *          {@link HasVerticalAlignment}.
	   */
	protected void setCellVerticalAlignment(Widget w, HasVerticalAlignment.VerticalAlignmentConstant align) {
	    Element td = getWidgetTd(w);
	    if (td != null) {
	      setCellVerticalAlignment(td, align);
	    }
	}
	  
	  /**
	   * Overloaded version for IsWidget.
	   * 
	   * @see #setCellVerticalAlignment(Widget,HasVerticalAlignment.VerticalAlignmentConstant)
	   */
	protected void setCellVerticalAlignment(IsWidget w, VerticalAlignmentConstant align) {
	    this.setCellVerticalAlignment(w.asWidget(),align);
	}

	  /**
	   * Sets the width of the border to be applied to all cells in this panel. This
	   * is particularly useful when debugging layouts, in that it allows you to see
	   * explicitly the cells that contain this panel's children.
	   * 
	   * @param width the width of the panel's cell borders, in pixels
	   */
	public void setBorderWidth(int width) {
	    DOM.setElementProperty(getRoot(), "border", "" + width);
	}

	  /**
	   * Sets the height of the cell associated with the given widget, related to
	   * the panel as a whole.
	   * 
	   * @param w the widget whose cell height is to be set
	   * @param height the cell's height, in CSS units
	   */
	public void setCellHeight(Widget w, String height) {
	    Element td = getWidgetTd(w);
	    if (td != null) {
	      td.setPropertyString("height", height);
	    }
	}
	  
	  /**
	   * Overloaded version for IsWidget.
	   * 
	   * @see #setCellHeight(Widget,String)
	   */
	public void setCellHeight(IsWidget w, String height) {
	    this.setCellHeight(w.asWidget(), height);
	}

	  /**
	   * Sets the width of the cell associated with the given widget, related to the
	   * panel as a whole.
	   * 
	   * @param w the widget whose cell width is to be set
	   * @param width the cell's width, in CSS units
	   */
	  public void setCellWidth(Widget w, String width) {
	    Element td = getWidgetTd(w);
	    if (td != null) {
	      td.setPropertyString("width", width);
	    }
	  }
	  
	  /**
	   * Overloaded version for IsWidget.
	   * 
	   * @see #setCellWidth(Widget,String)
	   */
	  public void setCellWidth(IsWidget w, String width) {
	    this.setCellWidth(w.asWidget(), width);
	  }
}
