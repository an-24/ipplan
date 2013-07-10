package com.cantor.ipplan.shared;

@SuppressWarnings("serial")
public class FileLinksWrapper implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable, Cloneable {

	public static final int PROVIDER_GOOGLE_DRIVE = 1;
	public static final int PROVIDER_DROPBOX = 2;

	public int filelinksId;
	public int providerId;
	public String filelinksName;
	public String filelinksUri;
	
	public FileLinksWrapper(){
	}

	public FileLinksWrapper(FileLink flink){
		providerId = flink.typeDrive;
		filelinksName = flink.name;
		filelinksUri = flink.uri;
	}
	
    public FileLinksWrapper copy() {
    	FileLinksWrapper wrap = new FileLinksWrapper();
    	wrap.filelinksId = filelinksId;
    	wrap.providerId = providerId;
    	wrap.filelinksName = filelinksName;
    	wrap.filelinksUri = filelinksUri;
    	return wrap;
    }
	
}
