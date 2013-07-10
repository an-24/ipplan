package com.cantor.ipplan.shared;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

@SuppressWarnings("serial")
public class SearchInfo  implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {
	public int error;
	public Set<FileLink> data; 

	public SearchInfo(JSONObject rest) {
		error = (int) rest.get("error").isNumber().doubleValue();
		JSONValue jsdata = rest.get("data");
		if(jsdata==null) data = null; else {
			data = new HashSet<FileLink>();
			JSONArray jsset = jsdata.isArray();
			for (int i = 0, len = jsset.size(); i < len; i++) {
				JSONObject jslink = jsset.get(i).isObject();
				data.add(new FileLink(jslink));
			}
		}	
	} 
	
	public SearchInfo() {
		data = new HashSet<FileLink>();
	}
	
	public SearchInfo(int err) {
		error = err;
	}
}
