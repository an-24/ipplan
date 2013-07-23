package com.cantor.ipplan.client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.cantor.ipplan.client.widgets.CheckBox;
import com.cantor.ipplan.client.widgets.ComboBox;
import com.cantor.ipplan.client.widgets.DatePicker;
import com.cantor.ipplan.client.widgets.IntegerBox;
import com.cantor.ipplan.client.widgets.TextBox;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
import com.cantor.ipplan.shared.TaskWrapper;
import com.cantor.ipplan.shared.TasktypeWrapper;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.cantor.ipplan.client.widgets.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.datepicker.client.DateBox;

public class FormTask extends Dialog {
	private ClickHandler okExternalHandler;
	private TaskWrapper task;
	private TextBox tbName;
	private DateBox tbDeadline;
	private ComboBox cbDeadlineTime;
	private DateBox tbTaskStart;
	private IntegerBox tbDuration;
	private ComboBox cbDurationUnit;
	private TextBox tbPlace;
	private CheckBox cbExecuted;
	private ComboBox cbTasktype;
	private DatabaseServiceAsync dbservice;
	private NotifyHandler<TasktypeWrapper> setterTasktype = null;
	private ComboBox cbAfterStatus;
	private HorizontalPanel phDeadline;
	private static HashMap<Integer,TasktypeWrapper> types;

	public FormTask(TaskWrapper task, DatabaseServiceAsync dbservice) {
		super(task==null?"Новая задача":"Изменение задачи \""+task.taskName+"\"");
		this.dbservice = dbservice;
		this.getButtonOk().setText("Сохранить");
		setWidth("544px");
		FlexTable table = getContent();
		int col = 0, row = 0;
		HorizontalPanel ph;
		
		tbName = new TextBox();
		tbName.setWidth("500px");
		tbName.getElement().setAttribute("placeholder", "Что нужно сделать");
		table.setWidget(row, col,  tbName);
		table.getFlexCellFormatter().setColSpan(row, col, 2);
		
		row++;
		
		table.setWidget(row,col, new Label("Тип задачи"));
		cbTasktype = new ComboBox();
		makeTypes();
		table.setWidget(row, col+1,  cbTasktype);
		
		row++;
		
		phDeadline = new HorizontalPanel();
		phDeadline.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
		
		table.setWidget(row,col, new Label("До какого времени"));
		tbDeadline = new DateBox(new DatePicker(),null,Ipplan.DEFAULT_DATE_FORMAT);
		
		cbDeadlineTime = new ComboBox();
		makeTimes(cbDeadlineTime);
		cbDeadlineTime.setSelectedIndex(24); //12:00
		phDeadline.add(tbDeadline);
		phDeadline.add(cbDeadlineTime);
		table.setWidget(row, col+1,  phDeadline);
		cbDeadlineTime.setWidth("60px");
		
		row++;
		
		table.setWidget(row,col, new Label("Когда начинать"));
		tbTaskStart = new DateBox(new DatePicker(),null,Ipplan.DEFAULT_DATE_FORMAT);
		table.setWidget(row, col+1,  tbTaskStart);
		
		row++;
		
		table.setWidget(row,col, new Label("Напомнить за"));
		ph = new HorizontalPanel();
		ph.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
		tbDuration = new IntegerBox();
		ph.add(tbDuration);
		cbDurationUnit = new ComboBox();
		cbDurationUnit.addItem("минут");
		cbDurationUnit.addItem("часов");
		cbDurationUnit.addItem("дней");
		cbDurationUnit.addItem("недель");
		ph.add(cbDurationUnit);
		
		table.setWidget(row, col+1,  ph);
		cbDurationUnit.setWidth("60px");
		
		row++;
		
		table.setWidget(row,col, new Label("Место"));
		tbPlace = new TextBox();
		tbPlace.setWidth("310px");
		table.setWidget(row, col+1,  tbPlace);

		row++;
		cbExecuted = new CheckBox("Выполнено");
		table.setWidget(row, col,  cbExecuted);
		
		ph = new HorizontalPanel();
		ph.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
		ph.add(new Label("После выполнения статус может изменится на"));
		cbAfterStatus = new ComboBox();
		makeStatuses();
		ph.add(cbAfterStatus);
		
		table.setWidget(row, col+1,  ph);
		cbAfterStatus.setWidth("90px");
		
		if(task!=null) {
			this.task = task.copy();
			toEditFields();
		} else {
			this.task = new TaskWrapper();
		}

		this.setButtonOkClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				resetErrors();
				cancel();
				if(validate()) {
					fromEditFields();
					if(okExternalHandler!=null) {

						class FormClickEvent extends ClickEvent {
							public FormClickEvent(Object source) {
								super();
								setSource(source);
							}
						};
						okExternalHandler.onClick(new FormClickEvent(FormTask.this));
					}
					hide();
				}
			}
		});
		
		setFirstFocusedWidget(tbName);
	}

	private void makeStatuses() {
		HashMap<Integer, StatusWrapper> stats = StatusWrapper.getAllStatuses();
		cbAfterStatus.addItem("-","0");
		for (StatusWrapper s : stats.values()) {
			cbAfterStatus.addItem(s.statusName,((Integer)s.statusId).toString());
		}
	}

	private void makeTypes() {
		// кэшируемый запрос
		if(types==null)
		dbservice.getTasktypes(new AsyncCallback<List<TasktypeWrapper>>() {
			
			@Override
			public void onSuccess(List<TasktypeWrapper> result) {
				types = new HashMap<Integer,TasktypeWrapper>();
				for (TasktypeWrapper tt : result) {
					types.put(tt.tasktypeId, tt);
					cbTasktype.addItem(tt.tasktypeName);
				}	
				// отложенная установка значения
				if(setterTasktype!=null)
					setterTasktype.onNotify(task.tasktype);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Ipplan.showError(caught);
			}
		}); else
		for (TasktypeWrapper tt : types.values()) {
			cbTasktype.addItem(tt.tasktypeName);
		}
	}

	private void makeTimes(ComboBox cb) {
		for (int i = 0; i < 23; i++) {
			String h = i<10?"0"+i:""+i;
			cb.addItem(h+":00");
			cb.addItem(h+":30");
		}
	}

	protected void fromEditFields() {
		task.taskName = tbName.getText();
		task.tasktype = types.get(cbTasktype.getSelectedIndex()+1);
		Date dt = tbDeadline.getValue();
		int timeidx = cbDeadlineTime.getSelectedIndex();
		task.taskDeadline = new Date(dt.getYear(),dt.getMonth(),dt.getDate(),
									 timeidx/2,timeidx%2*30);
		task.taskStart = tbTaskStart.getValue();
		task.taskPlace = tbPlace.getText();
		task.taskWarningDuration = tbDuration.getValue();
		if(task.taskWarningDuration!=null)
			task.taskWarningUnit = cbDurationUnit.getSelectedIndex();
		task.taskExecuted = cbExecuted.getValue()?1:0;
		int idx = cbAfterStatus.getSelectedIndex();
		task.afterStatus = idx==0?null:StatusWrapper.getStatus(new Integer(cbAfterStatus.getValue(idx)));
	}

	protected boolean validate() {
		if(tbName.getText().isEmpty()) {
			showError(tbName, "Задача не может быть пустой");
			return false;
		}
		if(tbDeadline.getValue()==null) {
			showError(phDeadline, "Предельная дата не может быть пустой");
			return false;
		}
		
		return true;
	}

	@SuppressWarnings("deprecation")
	private void toEditFields() {
		tbName.setText(task.taskName);
		if(types!=null) cbTasktype.setSelectedIndex(task.tasktype.tasktypeId-1);else
			setterTasktype = new NotifyHandler<TasktypeWrapper>() {
				@Override
				public void onNotify(TasktypeWrapper tt) {
					cbTasktype.setSelectedIndex(tt.tasktypeId-1);
				}
			
			};
		
		tbDeadline.setValue(task.taskDeadline);
		cbDeadlineTime.setSelectedIndex(task.taskDeadline.getHours()*2+
				((task.taskDeadline.getMinutes()>30)?1:0));
		if(task.taskStart!=null) {
			tbTaskStart.setValue(task.taskStart);
		}
		if(task.taskWarningDuration!=null) {
			tbDuration.setValue(task.taskWarningDuration);
			cbDurationUnit.setSelectedIndex(task.taskWarningUnit);
		}
		if(task.taskPlace !=null) {
			tbPlace.setText(task.taskPlace);
		}
		cbExecuted.setValue(task.taskExecuted!=0);

		if(task.afterStatus!=null)
			cbAfterStatus.setSelectedIndex(cbAfterStatus.valueIndexOf(new Integer(task.afterStatus.statusId).toString()));
	}

	public TaskWrapper getTask() {
		return task;
	}

	public void setExternalHandler(ClickHandler clickHandler) {
		okExternalHandler = clickHandler;
	}

	public static void addTask(DatabaseServiceAsync dbservice,BargainWrapper bargain, ClickHandler clickHandler) {
		final FormTask formTask = new FormTask(null, dbservice);
		formTask.getTask().calendar.bargainId = bargain.bargainId;
		formTask.setExternalHandler(clickHandler);
		formTask.center();
	}

	public static void editTask(DatabaseServiceAsync dbservice,BargainWrapper bargain, TaskWrapper task, ClickHandler clickHandler) {
		final FormTask formTask = new FormTask(task, dbservice);
		formTask.setExternalHandler(clickHandler);
		formTask.center();
	}
}
