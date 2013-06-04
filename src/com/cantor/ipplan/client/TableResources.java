package com.cantor.ipplan.client;

import com.google.gwt.user.cellview.client.CellTable;

public interface TableResources extends CellTable.Resources {

    interface TableStyle extends CellTable.Style {
    }

    @Override
    @Source({ CellTable.Style.DEFAULT_CSS, "TableResources.css" })
    TableStyle cellTableStyle();

}