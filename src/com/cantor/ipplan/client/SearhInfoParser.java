package com.cantor.ipplan.client;

import java.util.HashSet;

import com.cantor.ipplan.shared.FileLink;
import com.cantor.ipplan.shared.SearchInfo;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public class SearhInfoParser {
	
	public static SearchInfo parse(JSONObject rest) {
		SearchInfo si = new SearchInfo();
		si.error = (int) rest.get("error").isNumber().doubleValue();
		JSONValue jsdata = rest.get("data");
		if(jsdata==null) si.data = null; else {
			si.data = new HashSet<FileLink>();
			JSONArray jsset = jsdata.isArray();
			for (int i = 0, len = jsset.size(); i < len; i++) {
				JSONObject jslink = jsset.get(i).isObject();
				FileLink fl = parseFileLink(jslink);
				si.data.add(fl);
			}
		}	
		return si;
		
	}

	private static FileLink parseFileLink(JSONObject jslink) {
		FileLink fl = new FileLink();
		fl.name = jslink.get("name").isString().stringValue();
		fl.uri = jslink.get("uri").isString().stringValue();
		fl.iconUri = jslink.get("iconUri").isString().stringValue();
		fl.typeDrive = (int) jslink.get("typeDrive").isNumber().doubleValue();
		return fl;
	}

}
