package com.cantor.ipplan.shared;

import com.google.gwt.json.client.JSONObject;

@SuppressWarnings("serial")
public class FileLink  implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {
	public String name;
	public String uri;
	public int typeDrive;
	public String iconUri;

	public FileLink(JSONObject jslink) {
		name = jslink.get("name").isString().stringValue();
		uri = jslink.get("uri").isString().stringValue();
		iconUri = jslink.get("iconUri").isString().stringValue();
		typeDrive = (int) jslink.get("typeDrive").isNumber().doubleValue();
	}

	public FileLink() {
	}
}
