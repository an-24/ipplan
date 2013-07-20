package com.cantor.ipplan.server;

import java.net.URLConnection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;

import com.cantor.ipplan.shared.FileLink;
import com.cantor.ipplan.shared.FileLinksWrapper;
import com.cantor.ipplan.shared.ImportExportProcessInfo;
import com.cantor.ipplan.shared.SearchInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.io.Files;
import com.google.gson.Gson;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;

@SuppressWarnings("serial")

public class UploadFile extends UploadAction {


	private OAuthToken token;

	public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
		int typedrive = -1;
		for (FileItem item : sessionFiles) {
		    if (item.isFormField() && "tdrive".equals(item.getFieldName())) {
		    	typedrive = new Integer(item.getString());
		    	break;
		    }
		}
		if(typedrive<0) 
			throw new UploadActionException("Не указан тип диска");
		
		SearchInfo si = getTokenDrive(request, typedrive);
		if(si!= null) 
			return new Gson().toJson(si);
		
		si = new SearchInfo();
		
		for (FileItem item : sessionFiles) 
		if(!item.isFormField()) {
			FileLink flnk = new FileLink();
			flnk.name =	new java.io.File(item.getName()).getName();
			flnk.typeDrive = typedrive;
			// отправляем в drive
			switch (typedrive) {
				case FileLinksWrapper.PROVIDER_GOOGLE_DRIVE:
					insertToGDr(item,flnk);
					break;
				case FileLinksWrapper.PROVIDER_DROPBOX:
					insertToDBx(item,flnk);
					break;
				default:
					throw new UploadActionException("Неизвестный тип диска");
			};	
			
			si.data.add(flnk);
		}
		
		removeSessionFileItems(request);
		
		
		return new Gson().toJson(si);
	}

	private void insertToDBx(FileItem item, FileLink flnk) {
		// TODO Auto-generated method stub
		
	}

	private void insertToGDr(FileItem item, FileLink flnk) throws UploadActionException {
		GoogleCredential credential = new GoogleCredential();
		credential.setAccessToken(token.getValue());
		Drive service = new Drive.Builder(new NetHttpTransport(),new JacksonFactory(),credential)
				.setApplicationName("Ipplan")
				.build();

        String mimetype = URLConnection.getFileNameMap().getContentTypeFor(flnk.name);
		
		File body = new File();
		body.setTitle(flnk.name);
        body.setMimeType(mimetype);
		
		try {
			java.io.File file = java.io.File.createTempFile("upload-", "."+Files.getFileExtension(flnk.name));
	        item.write(file);
	        FileContent mediaContent = new FileContent(mimetype,file);

	        File drfile = service.files().insert(body, mediaContent).execute();
	        
	        flnk.uri = drfile.getWebContentLink();
	        flnk.iconUri = drfile.getIconLink();
		} catch (Exception e) {
			throw new UploadActionException(e);
		}
		
	}

	private SearchInfo getTokenDrive(HttpServletRequest request, int typedrive) {
		DatabaseServiceImpl dbservive = new DatabaseServiceImpl(request.getSession());
		token = dbservive.getTokenDrive(typedrive);
		if(!token.exists())
			return new SearchInfo(ImportExportProcessInfo.TOKEN_NOTFOUND);
		
		if(token.isExpired()) 
			if(token.canRefresh())
				return new SearchInfo(ImportExportProcessInfo.TOKEN_EXPIRED); else
				return new SearchInfo(ImportExportProcessInfo.TOKEN_NOTFOUND);
		return null;
	}

}
