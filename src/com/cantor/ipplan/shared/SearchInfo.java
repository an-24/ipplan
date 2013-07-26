package com.cantor.ipplan.shared;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class SearchInfo  implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {
	public int error;
	public Set<FileLink> data; 

	public SearchInfo() {
		data = new HashSet<FileLink>();
	}
	
	public SearchInfo(int err) {
		error = err;
	}
	
}
