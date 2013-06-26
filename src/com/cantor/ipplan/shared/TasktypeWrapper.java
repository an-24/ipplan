package com.cantor.ipplan.shared;

@SuppressWarnings("serial")
public class TasktypeWrapper  implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {
	public static final int TT_EMAIL = 1;
	public static final int TT_CAL = 2;
	public static final int TT_PREPAREDOC = 3;
	public static final int TT_SENDDOC = 4;
	public static final int TT_MEETING = 5;
	public static final int TT_PRODUCT = 6;
	public static final int TT_OTHER = 7;
	public int tasktypeId;
	public String tasktypeName;
	
	public static String backgroundUrlFromType(int tasktypeId) {
		String baseurl = "url(resources/images/sprite-004276.png) ";
		switch (tasktypeId) {
		case TT_EMAIL: return baseurl+"no-repeat -14px 0px";
		case TT_CAL: return baseurl+"no-repeat -96px 0px";
		case TT_PREPAREDOC: return baseurl+"no-repeat -41px 0px";
		case TT_SENDDOC: return baseurl+"no-repeat -55px 0px";
		case TT_MEETING: return baseurl+"no-repeat -83px 0px";
		case TT_PRODUCT: return baseurl+"no-repeat -69px 0px";
		default:
			return "transparent";
		}
	}
}
