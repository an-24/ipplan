package com.cantor.ipplan.shared;

import java.util.Date;

@SuppressWarnings("serial")
public class TaskWrapper  implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {
	public int taskId = 0;
	public StatusWrapper afterStatus;
	public TasktypeWrapper tasktype;
	public CalendarWrapper calendar;
	public String taskName;
	public Date taskDeadline;
	public Date taskStart;
	public Integer taskWarningDuration;
	public Integer taskWarningUnit;
	public String taskPlace;
	public int taskExecuted = 0;
	
	public TaskWrapper() {
		tasktype = new TasktypeWrapper();
		calendar = new CalendarWrapper();
	} 
	
	public TaskWrapper copy() {
		TaskWrapper wrap = new TaskWrapper();
		wrap.taskId = taskId;
		wrap.afterStatus = afterStatus;
		wrap.tasktype = tasktype;
		wrap.calendar = calendar;
		wrap.taskName = taskName;
		wrap.taskDeadline = taskDeadline;
		wrap.taskStart = taskStart;
		wrap.taskWarningDuration = taskWarningDuration;
		wrap.taskWarningUnit = taskWarningUnit;
		wrap.taskPlace = taskPlace;
		wrap.taskExecuted = taskExecuted;
		return wrap;
	}
}
