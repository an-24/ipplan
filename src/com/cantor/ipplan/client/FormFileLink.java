package com.cantor.ipplan.client;

import gwtupload.client.BaseUploadStatus;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.OnStartUploaderHandler;
import gwtupload.client.IUploader.UploaderConstants;
import gwtupload.client.SingleUploader;

import com.cantor.ipplan.client.widgets.FileSearchBox;
import com.cantor.ipplan.client.widgets.RadioButton;
import com.cantor.ipplan.client.widgets.VerticalPanel;
import com.cantor.ipplan.shared.FileLink;
import com.cantor.ipplan.shared.FileLinksWrapper;
import com.cantor.ipplan.shared.SearchInfo;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class FormFileLink extends Dialog {
	
	private ToggleButton btnGD;
	private ToggleButton btnDBx;
	private RadioButton rbFileExists;
	private RadioButton rbNewFile;
	private VerticalPanel hvFile;
	private VerticalPanel hvExistFile;
	private FileSearchBox tbFileSearch;
	protected ClickHandler okExternalHandler;
	private VerticalPanel vpExistFile;

	public FormFileLink(DatabaseServiceAsync dbservice) {
		super("Прикрепить файл");
		FlexTable table = getContent();
		int row = 0;
		VerticalPanel vp;
		
		table.setWidget(row, 0, new Label("Место размещения файла"));
		
		row++;
		
		Element div;
		Image img;
		img = new Image("resources/images/google-drive.png");
		img.setSize("100%", "90px");
		btnGD = new ToggleButton(img, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resetErrors();
				tbFileSearch.clear();
				btnGD.setDown(true);
				btnDBx.setDown(false);
				tbFileSearch.setTypeDrive(FileLinksWrapper.PROVIDER_GOOGLE_DRIVE);
			}
		});
		div = DOM.createDiv();
		div.setInnerHTML("Google Drive");
		btnGD.getElement().appendChild(div);
		
		btnGD.setSize("100px", "100px");
		table.setWidget(row, 0, btnGD);
		table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		img = new Image("resources/images/dropbox.png");
		img.setSize("100%", "90px");
		btnDBx = new ToggleButton(img, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resetErrors();
				tbFileSearch.clear();
				btnGD.setDown(false);
				btnDBx.setDown(true);
				tbFileSearch.setTypeDrive(FileLinksWrapper.PROVIDER_DROPBOX);
			}
		}); 
		div = DOM.createDiv();
		div.setInnerHTML("Dropbox");
		btnDBx.getElement().appendChild(div);
		btnDBx.setSize("100px", "100px");
		table.setWidget(row, 1, btnDBx);
		table.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_CENTER);
		
		row++;
		
		vpExistFile = new VerticalPanel();
		
		rbFileExists = new RadioButton("g1","Файл уже там находится");
		rbFileExists.setValue(true);
		rbFileExists.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				new CollapseVertical(hvFile,0).run(700);
				new ExpansionVertical(hvExistFile,40).run(700);
			}
		});
		vpExistFile.add(rbFileExists);

		hvExistFile = new VerticalPanel();
		hvExistFile.setWidth("100%");
		hvExistFile.getElement().getStyle().setOverflow(Overflow.HIDDEN);

		tbFileSearch = new FileSearchBox(dbservice);
		tbFileSearch.setWidth("279px");
		tbFileSearch.getElement().setAttribute("placeholder", "введите имя искомого файла");
		hvExistFile.add(tbFileSearch);
		vpExistFile.add(hvExistFile);
		
		table.setWidget(row, 0, vpExistFile);
		table.getFlexCellFormatter().setColSpan(row, 0, 2);

		row++;

		vp = new VerticalPanel();
		
		rbNewFile = new RadioButton("g1","Файл необходимо загрузить");
		rbNewFile.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resetErrors();
				new CollapseVertical(hvExistFile,0).run(700);
				new ExpansionVertical(hvFile,65).run(700);
			}
		});
		vp.add(rbNewFile);
		
		hvFile = new VerticalPanel();
		hvFile.setWidth("100%");
		hvFile.getElement().getStyle().setOverflow(Overflow.HIDDEN);
		
		final SingleUploader btnUpload = new SingleUploader(FileInputType.BUTTON, new UploadStatus());
		btnUpload.setServletPath("main/upload");
		final Hidden tbTypeDrive = new Hidden("tdrive");
		btnUpload.add(tbTypeDrive);
		btnUpload.setAutoSubmit(true);
		Widget w = btnUpload.getFileInput().getWidget();
		w.setSize("309px", "30px");
		w.getElement().getStyle().setFontSize(1, Unit.EM);
		btnUpload.setI18Constants((UploaderConstants) GWT.create(UploaderConstantsRU.class));
		btnUpload.addOnStartUploadHandler(new OnStartUploaderHandler() {
			@Override
			public void onStart(IUploader uploader) {
				tbTypeDrive.setValue(new Integer(getTypeDrive()).toString());
			}
		});
		btnUpload.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
			
			@Override
			public void onFinish(IUploader uploader) {
				if(uploader.getStatus()==Status.SUCCESS) {
					// проверяем на token
					JSONObject jsnobj = JSONParser.parseStrict(uploader.getServerInfo().message).isObject();
					SearchInfo result = SearhInfoParser.parse(jsnobj);
					if(result.error!=0) { 
						FileSearchBox.tokenNeeded(result.error, getTypeDrive(), btnUpload, new NotifyHandler<SingleUploader>(){
							@Override
							public void onNotify(SingleUploader upoader) {
								upoader.submit();
							}
						});
						return;
					}
					tbFileSearch.setFilelink((FileLink) result.data.toArray()[0]);
					rbFileExists.click();
				}
			}
		});
		
		hvFile.add(btnUpload);
		
		hvFile.setVisible(false);
		vp.add(hvFile);
		
		table.setWidget(row, 0, vp);
		table.getFlexCellFormatter().setColSpan(row, 0, 2);
		
		getButtonOk().setText("Прикрепить");

		this.setButtonOkClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				resetErrors();
				cancel();
				if(validate()) {
					click();
					hide();
				}
			}
		});
		
		init();
		
	}
	
	public FileLinksWrapper getFilelink() {
		return new FileLinksWrapper(tbFileSearch.getFilelink());
	}

	private void click() {
		if(okExternalHandler!=null) {
			class FormClickEvent extends ClickEvent {
				public FormClickEvent(Object source) {
					super();
					setSource(source);
				}
			};
			okExternalHandler.onClick(new FormClickEvent(FormFileLink.this));
		}
	}
	
	protected boolean validate() {
		if(tbFileSearch.getFilelink()==null) {
			rbFileExists.click();
			showError(vpExistFile, "Необходимо выбрать файл");
			return false;
		}	
		return true;
	}

	private void init() {
		btnGD.setDown(true);
		tbFileSearch.setTypeDrive(FileLinksWrapper.PROVIDER_GOOGLE_DRIVE);
	}

	private int getTypeDrive() {
		return btnGD.isDown()?FileLinksWrapper.PROVIDER_GOOGLE_DRIVE:FileLinksWrapper.PROVIDER_DROPBOX;
	}
	
	class UploadStatus extends BaseUploadStatus {
	
		public void setError(String msg) {
			setStatus(Status.ERROR);
			Ipplan.showError(msg.replaceAll("\\\\n", "\\n"));
		}
	}

}
