/*
 * Copyright 2010 Manuel Carrasco Moñino. (manolo at apache/org) 
 * http://code.google.com/p/gwtupload
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gwtupload.client;

import static gwtupload.shared.UConsts.PARAM_BLOBKEY;
import static gwtupload.shared.UConsts.PARAM_BLOBSTORE;
import static gwtupload.shared.UConsts.PARAM_CANCEL;
import static gwtupload.shared.UConsts.PARAM_REMOVE;
import static gwtupload.shared.UConsts.PARAM_SESSION;
import static gwtupload.shared.UConsts.PARAM_SHOW;
import static gwtupload.shared.UConsts.TAG_BLOBSTORE;
import static gwtupload.shared.UConsts.TAG_BLOBSTORE_PATH;
import static gwtupload.shared.UConsts.TAG_CANCELED;
import static gwtupload.shared.UConsts.TAG_CTYPE;
import static gwtupload.shared.UConsts.TAG_CURRENT_BYTES;
import static gwtupload.shared.UConsts.TAG_FIELD;
import static gwtupload.shared.UConsts.TAG_FINISHED;
import static gwtupload.shared.UConsts.TAG_KEY;
import static gwtupload.shared.UConsts.TAG_MESSAGE;
import static gwtupload.shared.UConsts.TAG_MSG_END;
import static gwtupload.shared.UConsts.TAG_MSG_GT;
import static gwtupload.shared.UConsts.TAG_MSG_LT;
import static gwtupload.shared.UConsts.TAG_MSG_START;
import static gwtupload.shared.UConsts.TAG_NAME;
import static gwtupload.shared.UConsts.TAG_PERCENT;
import static gwtupload.shared.UConsts.TAG_SIZE;
import static gwtupload.shared.UConsts.TAG_TOTAL_BYTES;
import static gwtupload.shared.UConsts.TAG_WAIT;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;

/**
 * <p>
 * Uploader panel.
 * </p>
 *         
 * @author Manolo Carrasco Moñino
 * 
 *         <h3>Features</h3>
 *         <ul>
 *         <li>Renders a form with an input file for sending the file, and a hidden iframe where is received the server response</li>  
 *         <li>The user can add more elements to the form</li>
 *         <li>It asks the server for the upload progress continuously until the submit process has finished.</li>
 *         <li>It expects xml responses instead of gwt-rpc, so the server part can be implemented in any language</li>
 *         <li>It uses a progress interface so it is easy to use customized progress bars</li>
 *         <li>By default it renders a basic progress bar</li>
 *         <li>It can be configured to automatic submit after the user has selected the file</li>
 *         <li>It uses a queue that avoid submit more than a file at the same time</li>
 *         </ul>
 * 
 *         <h3>CSS Style Rules</h3>
 *         <ul>
 *         <li>.GWTUpld { Uploader container }</li>
 *         <li>.GWTUpld .upld-input { style for the FileInput element }</li>
 *         <li>.GWTUpld .upld-status { style for the IUploadStatus element }</li>
 *         <li>.GWTUpld .upld-button { style for submit button if present }</li>
 *         </ul>
 */
public class Uploader extends Composite implements IsUpdateable, IUploader, HasJsData {
  
  public Widget getWidget(){
    return this;
  }

  /**
   * FormPanel add method only can be called once.
   * This class override the add method to allow multiple additions
   * to a flowPanel.
   */
  public class FormFlowPanel extends FormPanel {
    FlowPanel formElements = new FlowPanel();
    public FormFlowPanel() {
      super.add(formElements);
      formElements.setStyleName("upld-form-elements");
    }
    public void add(Widget w) {
      if (w instanceof Hidden) {
        formElements.insert(w, 0);     
      } else {
        formElements.add(w);        
      }
    }
    public void add(Widget w, int index) {
      formElements.insert(w, Math.max(0, Math.min(index, formElements.getWidgetCount())));
    }
    public void clear() {
      formElements.clear();
      add(fileInput.getWidget());
    }
  }
  
  public static final int DEFAULT_FILEINPUT_SIZE = 40;
  
  public static final UploaderConstants I18N_CONSTANTS = GWT.create(UploaderConstants.class);
  
  protected static final String STYLE_BUTTON = "upld-button";
  
  protected static final String STYLE_INPUT = "upld-input";
  protected static final String STYLE_MAIN = "GWTUpld";
  protected static final String STYLE_STATUS = "upld-status";
  static HTML mlog;
  static Logger logger;
  private static final int DEFAULT_AJAX_TIMEOUT = 10000;
  private static final int DEFAULT_AUTOUPLOAD_DELAY = 600;
  
  private static final int DEFAULT_TIME_MAX_WITHOUT_RESPONSE = 60000;
  private static final int DEFAULT_UPDATE_INTERVAL = 500;
  
  private static HashSet<String> fileDone = new HashSet<String>();
  private static HashSet<String> fileUploading = new HashSet<String>();
  private static List<String> fileQueue = new ArrayList<String>();

  private static int statusInterval = DEFAULT_UPDATE_INTERVAL;
  
  private static int uploadTimeout = DEFAULT_TIME_MAX_WITHOUT_RESPONSE;
  public static void log(String msg, Throwable e) {
    if (mlog == null) {
      if (Window.Location.getParameter("log") != null) {
        mlog = new HTML();
        RootPanel.get().add(mlog);
        log(msg, e);
      } else {
        if (logger == null) {
          logger = Logger.getLogger("Gwt client Uploader");
        }
        logger.info(msg);
        GWT.log(msg, e);
      }
    } else {
      String html = (msg + "\n" + (e != null ? e.getMessage() :"")).replaceAll("\n", "<br/>");
      mlog.setHTML(mlog.getHTML() + html);
    }
  }
  
  /**
   * Configure the frequency to send status requests to the server.
   */
  public static void setStatusInterval(int statusInterval) {
    Uploader.statusInterval = statusInterval;
  }
  
  /**
   * Configure the maximal time without a valid response from the server.
   * When this period is reached, the upload process is canceled.
   */
  public static void setUploadTimeout(int uploadTimeout) {
    Uploader.uploadTimeout = uploadTimeout;
  }
  
  private static long now() {
    return (new Date()).getTime();
  }
  protected Panel uploaderPanel;
  private final Timer automaticUploadTimer = new Timer() {
    private boolean firstTime = true;
    public void run() {
      if (autoSubmit && isTheFirstInQueue()) {
        this.cancel();
        firstTime = true;
        statusWidget.setStatus(IUploadStatus.Status.SUBMITING);
        statusWidget.setVisible(true);
        // See issue #134
        try {
          uploadForm.submit();
        } catch (Exception e) {
          cancel();
          cancelUpload(i18nStrs.uploaderInvalidPathError());
        }        
      } else if (firstTime) {
        addToQueue();
        firstTime = false;
      }
    }
  };
  
  protected boolean autoSubmit = false;  
  private boolean avoidRepeatedFiles = false;
  private boolean avoidEmptyFile = true;
  private String basename = "";
  private boolean blobstore = false;
  private IUploadStatus.UploadCancelHandler cancelHandler = new IUploadStatus.UploadCancelHandler() {
    public void onCancel() {
      cancel();
    }
  };
  private boolean canceled = false;
  private boolean enabled = true;
  private IFileInput fileInput;
  protected String fileInputPrefix = "GWTU";
  private FileInputType fileInputType;
  private boolean finished = false;
  private boolean hasSession = false;
  private long lastData = now();
  private final RequestCallback onBlobstoreReceivedCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      String message = removeHtmlTags(exception.getMessage());
      cancelUpload(i18nStrs.uploaderServerUnavailable() + " (1) " + getServletPath() + "\n\n" + message);
    }
    public void onResponseReceived(Request request, Response response) {
      String text = response.getText();
      String url = null;
      try {
        url = Utils.getXmlNodeValue(XMLParser.parse(text), TAG_BLOBSTORE_PATH);
      } catch (DOMParseException e) {
        String bpath = "<" + TAG_BLOBSTORE_PATH + ">";
        String sbpath = "</" + TAG_BLOBSTORE_PATH + ">";
        if (text.contains(bpath)) {
          url = text.replaceAll("[\r\n]+","").replaceAll("^.*" + bpath + "\\s*", "").replaceAll("\\s*" + sbpath + ".*$", "");
        }
      } catch (Exception e) {
        cancelUpload(i18nStrs.uploaderBlobstoreError() + "\n>>>\n" + e.getMessage() + "\n>>>>\n" + e);
        return;
      }
      if (url != null && url.length() > 0 && !"null".equalsIgnoreCase(url)) {
        uploadForm.setAction(url);
      }
      receivedBlobPath = true;
      uploadForm.submit();
    }
  };
  
  private final RequestCallback onCancelReceivedCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      log("onCancelReceivedCallback onError: " , exception);
      statusWidget.setStatus(IUploadStatus.Status.CANCELED);
    }
    public void onResponseReceived(Request request, Response response) {
      if (getStatus() == Status.CANCELING) {
        updateStatusTimer.scheduleRepeating(3000);
      }
    }
  };

  private List<IUploader.OnChangeUploaderHandler> onChangeHandlers = new ArrayList<IUploader.OnChangeUploaderHandler>();
  private final RequestCallback onDeleteFileCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      statusWidget.setStatus(Status.DELETED);
      log("onCancelReceivedCallback onError: ", exception);
    }

    public void onResponseReceived(Request request, Response response) {
      statusWidget.setStatus(Status.DELETED);
      fileDone.remove(getFileName());
    }
  };
  private final ChangeHandler onFileInputChanged = new ChangeHandler() {
    public void onChange(ChangeEvent event) {
      basename = Utils.basename(getFileName());
      statusWidget.setFileName(basename);

      if (avoidRepeatedFiles && (fileDone.contains(getFileName()) || fileUploading.contains(getFileName()))) {
        statusWidget.setStatus(Status.REPEATED);
        return;
      }
      if (autoSubmit && !validateExtension(basename)) {
        return;
      }
      if (autoSubmit && basename.length() > 0) {
        automaticUploadTimer.scheduleRepeating(DEFAULT_AUTOUPLOAD_DELAY);
      }
      onChangeInput();
    }
  };
  
  private List<IUploader.OnFinishUploaderHandler> onFinishHandlers = new ArrayList<IUploader.OnFinishUploaderHandler>();
  
  private final RequestCallback onSessionReceivedCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      String message = removeHtmlTags(exception.getMessage());
      cancelUpload(i18nStrs.uploaderServerUnavailable() + " (2) " + getServletPath() + "\n\n" + message);
    }
    public void onResponseReceived(Request request, Response response) {
      hasSession = true;
      try {
        String s = Utils.getXmlNodeValue(XMLParser.parse(response.getText()), TAG_BLOBSTORE);
        blobstore = "true".equalsIgnoreCase(s);
        // with blobstore status does not make sense
        if (blobstore) {
          updateStatusTimer.setInterval(5000);
        }
        uploadForm.submit();
      } catch (Exception e) {
        String message = e.getMessage().contains("error:") 
            ? i18nStrs.uploaderServerUnavailable() + " (3) " + getServletPath() + "\n\n" + i18nStrs.uploaderServerError() + "\nAction: " + getServletPath() + "\nException: " + e.getMessage() + response.getText()
            : i18nStrs.submitError();
        cancelUpload( message);
      }
    }
  };
  
  private List<IUploader.OnStartUploaderHandler> onStartHandlers = new ArrayList<IUploader.OnStartUploaderHandler>();
  
  private List<IUploader.OnStatusChangedHandler> onStatusChangeHandlers = new ArrayList<IUploader.OnStatusChangedHandler>();

  /**
   * Handler called when the status request response comes back.
   * 
   * In case of success it parses the xml document received and updates the progress widget
   * In case of a non timeout error, it stops the status repeater and notifies the user with the exception.
   */
  private final RequestCallback onStatusReceivedCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      waitingForResponse = false;
      if (exception instanceof RequestTimeoutException) {
        log("GWTUpload: onStatusReceivedCallback timeout error, asking the server again.", null);
      } else {
        log("GWTUpload: onStatusReceivedCallback error: " + exception.getMessage(), exception);
        updateStatusTimer.cancel();
        String message = removeHtmlTags(exception.getMessage());
        message += "\n" + exception.getClass().getName();
        message += "\n" + exception.toString();
        statusWidget.setError(i18nStrs.uploaderServerUnavailable() + " (4) " + getServletPath() + "\n\n" + message);
      }
    }

    public void onResponseReceived(Request request, Response response) {
      waitingForResponse = false;
      if (finished == true && !uploading) {
        updateStatusTimer.cancel();
        return;
      }
      parseAjaxResponse(response.getText());
    }

  };

  private boolean onSubmitComplete;

  private SubmitCompleteHandler onSubmitCompleteHandler = new SubmitCompleteHandler() {
    public void onSubmitComplete(SubmitCompleteEvent event) {
      updateStatusTimer.cancel();
      onSubmitComplete = true;
      serverResponse = event.getResults();
      if (serverResponse != null) {
        serverResponse = serverResponse.replaceFirst(".*" + TAG_MSG_START + "([\\s\\S]*?)" + TAG_MSG_END + ".*", "$1");
        serverResponse = serverResponse.replace(TAG_MSG_LT, "<").replace(TAG_MSG_GT, ">").replace("&lt;", "<").replaceAll("&gt;", ">");
        // fix kav
        serverResponse = serverResponse.replaceAll("&nbsp;", " ");
      }
      log("onSubmitComplete: " + serverResponse, null);
      try {
        // Parse the xml and extract serverInfo
        Document doc = XMLParser.parse(serverResponse);
        serverInfo.name = Utils.getXmlNodeValue(doc, TAG_NAME);
        serverInfo.ctype = Utils.getXmlNodeValue(doc, TAG_CTYPE);
        String size = Utils.getXmlNodeValue(doc, TAG_SIZE);
        if (size != null) {
          serverInfo.size = Integer.parseInt(size);
        }
        serverInfo.field = Utils.getXmlNodeValue(doc, TAG_FIELD);
        serverInfo.message = Utils.getXmlNodeValue(doc, TAG_MESSAGE);
        serverInfo.key = Utils.getXmlNodeValue(doc, TAG_KEY);
        
        // If the server response is a valid xml
        parseAjaxResponse(serverResponse);
      } catch (Exception e) {
        log("onSubmitComplete exception parsing response: ", e);
        // Otherwise force an ajax request so as we have not to wait to the timer schedule
        updateStatusTimer.run();
      }
    }
  };
  
  /**
   *  Handler called when the file form is submitted
   *  
   *  If any validation fails, the upload process is canceled.
   *  
   *  If the client hasn't got the session, it asks for a new one 
   *  and the submit process is delayed until the client has got it
   */
  private SubmitHandler onSubmitFormHandler = new SubmitHandler() {
    public void onSubmit(SubmitEvent event) {
      if (!finished && uploading) {
        uploading = false;
        statusWidget.setStatus(IUploadStatus.Status.CANCELED);
        return;
      }

      if (!autoSubmit && fileQueue.size() > 0) {
        statusWidget.setError(i18nStrs.uploaderActiveUpload());
        event.cancel();
        return;
      }

      if (avoidEmptyFile && fileDone.contains(getFileName())) {
        statusWidget.setStatus(IUploadStatus.Status.REPEATED);
        successful = true;
        event.cancel();
        uploadFinished();
        return;
      }
      
      if (!validateExtension(basename)) {
        event.cancel();
        return;
      }

      if (!hasSession) {
        event.cancel();
        try {
          sendAjaxRequestToValidateSession();
        } catch (Exception e) {
          log("Exception in validateSession", null);
        }
        return;
      }
      
      if (blobstore && !receivedBlobPath) {
        event.cancel();
        try {
          sendAjaxRequestToGetBlobstorePath();
        } catch (Exception e) {
          log("Exception in getblobstorePath", null);
        }
        return;
      }
      receivedBlobPath = false;

      addToQueue();
      uploading = true;
      finished = false;
      serverResponse = null;
      serverInfo = new UploadedInfo();

      statusWidget.setVisible(true);
      updateStatusTimer.squeduleStart();
      statusWidget.setStatus(IUploadStatus.Status.INPROGRESS);
      lastData = now();
    }
  };
  
  private boolean receivedBlobPath = false;

  private int requestsCounter = 0;

  private String serverResponse = null;
  private UploadedInfo serverInfo = new UploadedInfo();
  
  private String servletPath = "servlet.gupld";

  private IUploadStatus.UploadStatusChangedHandler statusChangedHandler = new IUploadStatus.UploadStatusChangedHandler() {
    public void onStatusChanged(IUploadStatus statusWiget) {
      for (IUploader.OnStatusChangedHandler handler : onStatusChangeHandlers) {
        handler.onStatusChanged(thisInstance);
      }
    }
  };
  
  private IUploadStatus statusWidget = new BaseUploadStatus();
  
  protected UploaderConstants i18nStrs = I18N_CONSTANTS;

  private boolean successful = false;

  private Uploader thisInstance;
  
  private final UpdateTimer updateStatusTimer = new UpdateTimer(this, statusInterval);

  private FormPanel uploadForm;

  private boolean uploading = false;
  
  private String[] validExtensions = null;
  
  private String validExtensionsMsg = "";

  private boolean waitingForResponse = false;

  /**
   * Default constructor.
   * Initialize widget components and layout elements using the 
   * standard file input. 
   */
  public Uploader() {
    this(FileInputType.BROWSER_INPUT);
  }

  /**
   * Initialize widget components and layout elements using the 
   * standard file input. 
   * 
   * @param automaticUpload 
   *    when true the upload starts as soon as the user selects a file 
   */
  public Uploader(boolean automaticUpload) {
    this(FileInputType.BROWSER_INPUT, automaticUpload);
  }
  
  /**
   * Initialize widget components and layout elements. 
   * 
   * @param type
   *   file input to use
   */
  public Uploader(FileInputType type) {
    this(type, null);
  }
  
  /**
   * Initialize widget components and layout elements. 
   * 
   * @param type
   *   file input to use
   * @param automaticUpload
   *   when true the upload starts as soon as the user selects a file 
   */
  public Uploader(FileInputType type, boolean automaticUpload) {
    this(type);
    setAutoSubmit(automaticUpload);
  }
  
  /**
   * Initialize widget components and layout elements. 
   * 
   * @param type
   *   file input to use
   * @param form
   *   An existing form panel to use
   */
  public Uploader(FileInputType type, FormPanel form) {
    thisInstance = this;
    this.fileInputType = type;
    
    if (form == null) {
      form = new FormFlowPanel();
    }
    uploadForm = form;
    uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
    uploadForm.setMethod(FormPanel.METHOD_POST);
    uploadForm.setAction(servletPath);
    uploadForm.addSubmitHandler(onSubmitFormHandler);
    uploadForm.addSubmitCompleteHandler(onSubmitCompleteHandler);

    uploaderPanel = getUploaderPanel();
    uploaderPanel.add(uploadForm);
    uploaderPanel.setStyleName(STYLE_MAIN);

    setFileInput(fileInputType.getInstance());
    
    setStatusWidget(statusWidget);

    super.initWidget(uploaderPanel);
  }
  
  protected Panel getUploaderPanel() {
    return new HorizontalPanel();
  }
  
  /**
   * Adds a widget to formPanel.
   */
  public void add(Widget w) {
    uploadForm.add(w);
  }

  /**
   * Adds a widget to formPanel in a specified position.
   */
  public void add(Widget w, int index) {
    if (uploadForm instanceof FormFlowPanel) {
      ((FormFlowPanel)uploadForm).add(w, index);
    } else {
      add(w);
    }
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnCancelUploadHandler(gwtupload.client.IUploader.OnCancelUploaderHandler)
   */
  public HandlerRegistration addOnCancelUploadHandler(final OnCancelUploaderHandler handler) {
    assert handler != null;
    return statusWidget.addCancelHandler(new IUploadStatus.UploadCancelHandler() {
      public void onCancel() {
        handler.onCancel(thisInstance);
      }
    });
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnChangeUploadHandler(gwtupload.client.IUploader.OnChangeUploaderHandler)
   */
  public HandlerRegistration addOnChangeUploadHandler(final IUploader.OnChangeUploaderHandler handler) {
    assert handler != null;
    onChangeHandlers.add(handler);
    return new HandlerRegistration() {
      public void removeHandler() {
        onChangeHandlers.remove(handler);
      }
    };
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnFinishUploadHandler(gwtupload.client.IUploader.OnFinishUploaderHandler)
   */
  public HandlerRegistration addOnFinishUploadHandler(final IUploader.OnFinishUploaderHandler handler) {
    assert handler != null;
    onFinishHandlers.add(handler);
    return new HandlerRegistration() {
      public void removeHandler() {
        onFinishHandlers.remove(handler);
      }
    };
  }
  
  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnStartUploadHandler(gwtupload.client.IUploader.OnStartUploaderHandler)
   */
  public HandlerRegistration addOnStartUploadHandler(final IUploader.OnStartUploaderHandler handler) {
    assert handler != null;
    onStartHandlers.add(handler);
    return new HandlerRegistration() {
      public void removeHandler() {
        onStartHandlers.remove(handler);
      }
    };
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnStatusChangedHandler(gwtupload.client.IUploader.OnStatusChangedHandler)
   */
  public HandlerRegistration addOnStatusChangedHandler(final OnStatusChangedHandler handler) {
    assert handler != null;
    onStatusChangeHandlers.add(handler);
    return new HandlerRegistration() {
      public void removeHandler() {
        onStatusChangeHandlers.remove(handler);
      }
    };
  }

  /**
   * Don't send files that have already been uploaded.
   */
  public void avoidRepeatFiles(boolean avoidRepeat) {
    this.avoidRepeatedFiles = avoidRepeat;
  }

  /**
   * Don't submit the form if the user has not selected any file.
   * 
   * It is useful in forms where the developer whats the user to submit
   * information but the attachment is optional.
   * 
   * By default avoidEmptyFile is true.
   */
  public void avoidEmptyFiles(boolean b) {
    this.avoidEmptyFile = b;
  }
  
  /**
   * Cancel the current upload process.
   */
  public void cancel() {
    if (getStatus() == Status.UNINITIALIZED) {
      return;
    }
    
    if (finished && !uploading) {
      if (successful) {
        try {
          sendAjaxRequestToDeleteUploadedFile();
        } catch (Exception e) {
        }
      } else {
        statusWidget.setStatus(Status.DELETED);
      }
      return;
    }
      
    if (canceled || getStatus() == Status.CANCELING) {
      return;
    }
    
    canceled = true;
    automaticUploadTimer.cancel();
    log("cancelling " +  uploading, null);
    if (uploading) {
      updateStatusTimer.cancel();
      try {
        sendAjaxRequestToCancelCurrentUpload();
      } catch (Exception e) {
        log("Exception cancelling request " + e.getMessage(), e);
      }
      statusWidget.setStatus(IUploadStatus.Status.CANCELING);
    } else {
      uploadFinished();
    }
  }

  /**
   * Remove all widget from the form.
   */
  public void clear() {
    uploadForm.clear();
  }

  /**
   * Returns the link for getting the uploaded file from the server
   * It's useful to display uploaded images or generate links to uploaded files.
   */
  public String fileUrl() {
    String ret =  composeURL(PARAM_SHOW + "=" + getInputName());
    if (serverInfo.key != null) {
      ret += "&" + PARAM_BLOBKEY + "=" + serverInfo.key;
    }
    return ret;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getBasename()
   */
  public String getBasename() {
    return Utils.basename(getFileName());
  }

  /**
   * Returns a JavaScriptObject properties with the url of the uploaded file.
   * It's useful in the exported version of the library. 
   * Because native javascript needs it
   */
  public JavaScriptObject getData() {
    return getDataImpl(fileUrl(), getInputName(), getFileName(), getBasename(), getServerResponse(), getServerInfo().message, getStatus().toString(), getServerInfo().size);
  }

  public IFileInput getFileInput() {
    return fileInput;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getFileName()
   */
  public String getFileName() {
    return fileInput.getFilename();
  }
  
  public FormPanel getForm() {
    return uploadForm;
  }
  
  public UploaderConstants getI18NConstants(){
    return i18nStrs;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getInputName()
   */
  public String getInputName() {
    return fileInput.getName();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getServerResponse()
   */
  public String getServerResponse() {
    return serverResponse;
  }
  
  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getServerInfo()
   */
  public UploadedInfo getServerInfo() {
    return serverInfo;
  }

  /**
   * return the configured server service in the form-panel.
   */
  public String getServletPath() {
    return servletPath;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getStatus()
   */
  public Status getStatus() {
    return statusWidget.getStatus();
  }

  /**
   * Get the status progress used.
   */
  public IUploadStatus getStatusWidget() {
    return statusWidget;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }
  
  public boolean isFinished() {
    return finished;
  }

  /**
   * Returns a iterator of the widgets contained in the form panel.
   */
  public Iterator<Widget> iterator() {
    return uploadForm.iterator();
  }

  /**
   * remove a widget from the form panel.
   */
  public boolean remove(Widget w) {
    return uploadForm.remove(w);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#reset()
   */
  public void reset() {
    reuse();
    fileDone = new HashSet<String>();
    fileUploading = new HashSet<String>();
  }
  
  /**
   * Prepare the uploader for a new upload.
   */
  public void reuse() {
    this.uploadForm.reset();
    updateStatusTimer.cancel();
    onSubmitComplete = uploading = canceled = finished = successful = false;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setAutoSubmit(boolean)
   */
  public void setAutoSubmit(boolean b) {
    autoSubmit = b;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setEnabled(boolean)
   */
  public void setEnabled(boolean b) {
    enabled = b;
    if (fileInput != null) {
      fileInput.setEnabled(b);
    }
  }

  public void setFileInput(IFileInput input) {
    if (fileInput != null) {
      fileInput.getWidget().removeFromParent();
    }
    fileInput = input;
    fileInput.addChangeHandler(onFileInputChanged);
    fileInput.setText(i18nStrs.uploaderBrowse());
    fileInput.setEnabled(enabled);
    setFileInputSize(DEFAULT_FILEINPUT_SIZE);
    assignNewNameToFileInput();
    uploadForm.add(fileInput.getWidget());
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setFileInputPrefix(java.lang.String)
   */
  public void setFileInputPrefix(String prefix) {
    fileInputPrefix = prefix;
    assignNewNameToFileInput();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setFileInputSize(int)
   */
  public void setFileInputSize(int length) {
    fileInput.setLength(length);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setI18Constants(gwtupload.client.I18nUploadConstants)
   */
  public void setI18Constants(UploaderConstants strs) {
    this.i18nStrs = strs;
    fileInput.setText(strs.uploaderBrowse());
    statusWidget.setI18Constants(strs);
  }

  /**
   * set the url of the server service that receives the files and informs 
   * about the progress.  
   */
  public void setServletPath(String path) {
    if (path != null) {
      servletPath = path;
      uploadForm.setAction(path);
    }
  }

  /**
   * set the status widget used to display the upload progress.
   */
  public void setStatusWidget(IUploadStatus stat) {
    if (stat == null) {
      return;
    }
    uploaderPanel.remove(statusWidget.getWidget());
    statusWidget = stat;
    if (!stat.getWidget().isAttached()) {
      uploaderPanel.add(statusWidget.getWidget());
    }
    statusWidget.getWidget().addStyleName(STYLE_STATUS);
    statusWidget.setVisible(false);
    statusWidget.addCancelHandler(cancelHandler);
    statusWidget.setStatusChangedHandler(statusChangedHandler);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setValidExtensions(java.lang.String[])
   */
  public void setValidExtensions(String... validExtensions) {
    if (validExtensions == null) {
      this.validExtensions = new String[0];
      return;
    }
    this.validExtensions = new String[validExtensions.length];
    validExtensionsMsg = "";
    for (int i = 0, j = 0; i < validExtensions.length; i++) {
      String ext = validExtensions[i];
      if (ext == null) {
        continue;
      }
      if (ext.charAt(0) != '.') {
        ext = "." + ext;
      }
      if (i > 0) {
        validExtensionsMsg += ", ";
      }
      validExtensionsMsg += ext;

      ext = ext.replaceAll("\\.", "\\\\.");
      ext = ".+" + ext;
      this.validExtensions[j++] = ext.toLowerCase();
    }
  }
  
  public void setValidExtensions(String ext) {
    setValidExtensions(ext.split("[, ]+"));
  }
  
  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#submit()
   */
  public void submit() {
    this.uploadForm.submit();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setUploaded()
   */
  public void setUploaded(UploadedInfo info) {
    serverInfo = info;
    successful = true;
    statusWidget.setFileName(info.name);
    fileInput.setName(info.field);
    uploadFinished();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUpdateable#update()
   */
  public void update() {
    try {
      if (waitingForResponse) {
        return;
      }
      waitingForResponse = true;
      // Using a reusable builder makes IE fail because it caches the response
      // So it's better to change the request path sending an additional random parameter
      RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, composeURL("filename=" + fileInput.getName() , "c=" + requestsCounter++));
      reqBuilder.setTimeoutMillis(DEFAULT_AJAX_TIMEOUT);
      reqBuilder.sendRequest("get_status", onStatusReceivedCallback);
    } catch (RequestException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Method called when the file input has changed. This happens when the 
   * user selects a file.
   * 
   * Override this method if you want to add a customized behavior,
   * but remember to call this in your function
   */
  protected void onChangeInput() {
    statusWidget.setStatus(Status.CHANGED);
    for (IUploader.OnChangeUploaderHandler handler : onChangeHandlers) {
      handler.onChange(this);
    }
  }
  
  /**
   * Method called when the file upload process has finished,  
   * or the file has been canceled or removed from the queue.
   * Override this method if you want to add a customized behavior,
   * but remember to call this in your function.
   */
  protected void onFinishUpload() {
    for (IUploader.OnFinishUploaderHandler handler : onFinishHandlers) {
      handler.onFinish(this);
    }
  }
  
  /**
   * Method called when the file is added to the upload queue.
   * Override this if you want to add a customized behavior,
   * but remember to call this from your method.
   */
  protected void onStartUpload() {
    for (IUploader.OnStartUploaderHandler handler : onStartHandlers) {
      handler.onStart(this);
    }
  }

  /**
   * Adds a file to the upload queue.
   */
  private void addToQueue() {
    statusWidget.setStatus(IUploadStatus.Status.QUEUED);
    statusWidget.setProgress(0, 0);
    if (!fileQueue.contains(getInputName())) {
      onStartUpload();
      fileQueue.add(getInputName());
      if (avoidRepeatedFiles) {
        fileUploading.add(getFileName());
      }
    }
  }
  
  /**
   * Change the fileInput name, because the server uses it as an uniq identifier.
   */
  protected void assignNewNameToFileInput() {
    String fileInputName = (fileInputPrefix + "-" + Math.random()).replaceAll("\\.", "");
    fileInput.setName(fileInputName);
  }

  /**
   * Cancel upload process and show an error message to the user.
   */
  private void cancelUpload(String msg) {
    successful = false;
    uploadFinished();
    statusWidget.setStatus(IUploadStatus.Status.ERROR);
    statusWidget.setError(msg);
  }

  private String composeURL(String... params) {
    String ret = getServletPath();
    ret = ret.replaceAll("[\\?&]+$", "");
    String sep = ret.contains("?") ? "&" : "?";
    for (String par : params) { 
      ret += sep + par;
      sep = "&";
    }
    for (Entry<String, List<String>> e : Window.Location.getParameterMap().entrySet()) {
      ret += sep + e.getKey() + "=" + e.getValue().get(0);
    }
    ret += sep + "random=" + Math.random();
    return ret;
  }

  private native JavaScriptObject getDataImpl(String url, String inputName, String fileName, String baseName, String serverResponse, String serverMessage, String status, int size) /*-{
    return {
       url: url,
       name: inputName,
       filename: fileName,
       basename: baseName,
       response: serverResponse,
       message: serverMessage,
       status:  status,
       size: size
    };
  }-*/;

  private boolean isTheFirstInQueue() {
    return fileQueue.size() > 0 && fileQueue.get(0).equals(getInputName());
  }

  private void parseAjaxResponse(String responseTxt) {
    if (responseTxt == null) {
      return;
    }
    
    String error = null;
    Document doc = null;
    try {
      doc = XMLParser.parse(responseTxt);
      error = Utils.getXmlNodeValue(doc, "error");
    } catch (Exception e) {
      if (responseTxt.toLowerCase().matches("error")) {
        error = i18nStrs.uploaderServerError() + "\nAction: " + getServletPath() + "\nException: " + e.getMessage() + responseTxt;
      }
    }
    
    if (error != null) {
      successful = false;
      cancelUpload(error);
      return;
    } else if (Utils.getXmlNodeValue(doc, TAG_WAIT) != null) {
      if (serverResponse != null) {
        log("server response received, cancelling the upload " + getFileName() + " " + serverResponse, null);
        successful = true;
        uploadFinished();
      }
    } else if (Utils.getXmlNodeValue(doc, TAG_CANCELED) != null) {
      log("server response is: canceled " + getFileName(), null);
      successful = false;
      canceled = true;
      uploadFinished();
      return;
    } else if (Utils.getXmlNodeValue(doc, TAG_FINISHED) != null) {
      log("server response is: finished " + getFileName(), null);
      successful = true;
      if (onSubmitComplete) {
        log("POST response from server has been received", null);
        uploadFinished();
      }
      return;
    } else if (Utils.getXmlNodeValue(doc, TAG_PERCENT) != null) {
      lastData = now();
      long transferredKB = Long.valueOf(Utils.getXmlNodeValue(doc, TAG_CURRENT_BYTES)) / 1024;
      long totalKB = Long.valueOf(Utils.getXmlNodeValue(doc, TAG_TOTAL_BYTES)) / 1024;
      statusWidget.setProgress(transferredKB, totalKB);
      log("server response transferred  " + transferredKB + "/" + totalKB + " " + getFileName(), null);
      if (onSubmitComplete) {
        successful = false;
        String msg = i18nStrs.uploaderBadServerResponse() + "\n" + serverResponse;
        if (blobstore) {
          msg += "\n" + i18nStrs.uploaderBlobstoreBilling();
        }
        log(msg, null);
        statusWidget.setError(msg);
        uploadFinished();
      }
      return;
    } else {
      log("incorrect response: " + getFileName() + " " + responseTxt, null);
    }
    
    if (uploadTimeout > 0 && now() - lastData >  uploadTimeout) {
      successful = false;
      cancelUpload(i18nStrs.uploaderTimeout());
      try {
        sendAjaxRequestToCancelCurrentUpload();
      } catch (Exception e) {
      }
    }
  }

  /**
   * remove a file from the upload queue.
   */
  private void removeFromQueue() {
    fileQueue.remove(getInputName());
    fileUploading.remove(getFileName());
  }

  private String removeHtmlTags(String message) {
    return message.replaceAll("<[^>]+>", "");
  }

  private void sendAjaxRequestToCancelCurrentUpload() throws RequestException {
    RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, composeURL(PARAM_CANCEL + "=true"));
    reqBuilder.sendRequest("cancel_upload", onCancelReceivedCallback);
  }

  private void sendAjaxRequestToDeleteUploadedFile() throws RequestException {
    RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, composeURL(PARAM_REMOVE + "=" + getInputName()));
    reqBuilder.sendRequest("remove_file", onDeleteFileCallback);
  }
  
  /**
   * Sends a request to the server in order to get the blobstore path.
   * When the response with the session comes, it submits the form.
   */
  private void sendAjaxRequestToGetBlobstorePath() throws RequestException {
    RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, composeURL(PARAM_BLOBSTORE + "=true"));
    reqBuilder.setTimeoutMillis(DEFAULT_AJAX_TIMEOUT);
    reqBuilder.sendRequest("blobstore", onBlobstoreReceivedCallback);
  }

  /**
   * Sends a request to the server in order to get the session cookie,
   * when the response with the session comes, it submits the form.
   * 
   * This is needed because this client application usually is part of 
   * static files, and the server doesn't set the session until dynamic pages
   * are requested.
   * 
   * If we submit the form without a session, the server creates a new
   * one and send a cookie in the response, but the response with the
   * cookie comes to the client at the end of the request, and in the
   * meanwhile the client needs to know the session in order to ask
   * the server for the upload status.
   */
  private void sendAjaxRequestToValidateSession() throws RequestException {
    // Using a reusable builder makes IE fail
    RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, composeURL(PARAM_SESSION + "=true"));
    reqBuilder.setTimeoutMillis(DEFAULT_AJAX_TIMEOUT);
    reqBuilder.sendRequest("create_session", onSessionReceivedCallback);
  }

  /**
   * Called when the uploader detects that the upload process has finished:
   * - in the case of submit complete.
   * - in the case of error talking with the server.
   */
  private void uploadFinished() {
    removeFromQueue();
    finished = true;
    uploading = false;
    updateStatusTimer.cancel();
    statusWidget.setVisible(false);
    
    if (successful) {
      if (avoidRepeatedFiles) {
        fileDone.add(getFileName());
        statusWidget.setStatus(IUploadStatus.Status.SUCCESS);
      } else {
        statusWidget.setStatus(IUploadStatus.Status.SUCCESS);
      }
    } else if (canceled) {
      statusWidget.setStatus(IUploadStatus.Status.CANCELED);
    } else {
      statusWidget.setStatus(IUploadStatus.Status.ERROR);
    }
    
    onFinishUpload();
  }

  private boolean validateExtension(String filename) {
    if (filename == null || filename.length() == 0) {
      return !avoidEmptyFile;
    }
    boolean valid = Utils.validateExtension(validExtensions, filename);
    if (!valid) {
      finished = true;
      statusWidget.setStatus(Status.INVALID);
      statusWidget.setError(i18nStrs.uploaderInvalidExtension() + validExtensionsMsg);
    }
    return valid;
  }

}
