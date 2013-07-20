package com.cantor.ipplan.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ImportExportProcessInfo  implements Serializable,com.google.gwt.user.client.rpc.IsSerializable  {
	
	public static final int TOKEN_EXPIRED = -1;
	public static final int TOKEN_NOTFOUND = -2; 

	private int importAllCount = 0;
	private int importInsert = 0;
	private int importUpdate = 0;
	
	private int exportAllCount = 0;
	private int exportInsert = 0;
	private int exportUpdate = 0;
	
	private int error = 0;
	
	public ImportExportProcessInfo() {
		
	}

	public ImportExportProcessInfo(int error) {
		this.error = error;
	}
	

	public int getError() {
		return error;
	}

	public int getImportAllCount() {
		return importAllCount;
	}

	public void setImportAllCount(int importAllCount) {
		this.importAllCount = importAllCount;
	}

	public int getImportInsert() {
		return importInsert;
	}

	public void setImportInsert(int importInsert) {
		this.importInsert = importInsert;
	}

	public int getImportUpdate() {
		return importUpdate;
	}

	public void setImportUpdate(int importUpdate) {
		this.importUpdate = importUpdate;
	}

	public int getExportAllCount() {
		return exportAllCount;
	}

	public void setExportAllCount(int exportAllCount) {
		this.exportAllCount = exportAllCount;
	}

	public int getExportInsert() {
		return exportInsert;
	}

	public void setExportInsert(int exportInsert) {
		this.exportInsert = exportInsert;
	}

	public int getExportUpdate() {
		return exportUpdate;
	}

	public void setExportUpdate(int exportUpdate) {
		this.exportUpdate = exportUpdate;
	}

}
