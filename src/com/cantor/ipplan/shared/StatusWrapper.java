package com.cantor.ipplan.shared;

import java.util.HashMap;
import java.util.List;

import com.cantor.ipplan.client.DatabaseServiceAsync;
import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings("serial")
public class StatusWrapper implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable, Cloneable  {

	public static final int PRIMARY_CONTACT = 1;
	public static final int TALK = 10;
	public static final int DECISION_MAKING = 20;
	public static final int RECONCILIATION_AGREEMENT = 30;
	public static final int EXECUTION = 40;
	public static final int SUSPENDED = 50;
	public static final int COMPLETION = 60;
	public static final int CLOSE_OK = 100;
	public static final int CLOSE_FAULT = 99;
	
	public int statusId;
	public int puser_owner_id;
	public String statusName;
	public int statusDayLimit;
	

	private static HashMap<Integer, StatusWrapper> allStatuses = null;
	
	static public int[] getNextState(int state) {
		return getNextState(state,false);
	}

	static public int[] getNextState(int state, boolean skipSuspended) {
		switch (state) {
			case PRIMARY_CONTACT: return new int[]{TALK,CLOSE_FAULT}; 
			case TALK: return new int[]{DECISION_MAKING,CLOSE_FAULT}; 
			case DECISION_MAKING: return new int[]{RECONCILIATION_AGREEMENT,CLOSE_FAULT};
			case RECONCILIATION_AGREEMENT: return new int[]{EXECUTION};
			case EXECUTION: if(skipSuspended) return new int[]{COMPLETION,CLOSE_FAULT}; 
										else  return new int[]{SUSPENDED,COMPLETION,CLOSE_FAULT};  
			case SUSPENDED: return new int[]{EXECUTION,COMPLETION,CLOSE_FAULT};
			case COMPLETION: return new int[]{CLOSE_OK,CLOSE_FAULT};
			case CLOSE_OK: 
			case CLOSE_FAULT: return new int[0];
		}
		return null;
	}

	public static StatusWrapper getPauseStatus() {
		return getAllStatuses().get(SUSPENDED);
	}

	public static StatusWrapper getStatus(int id) {
		return getAllStatuses().get(id);
	}
	
	public static void requestStatusesOnServer(DatabaseServiceAsync database) {
		if(allStatuses==null) {
			database.getAllStatuses(new AsyncCallback<List<StatusWrapper>>() {
				
				@Override
				public void onSuccess(List<StatusWrapper> list) {
					allStatuses = new HashMap<Integer, StatusWrapper>();
					for (StatusWrapper st : list) 
						allStatuses.put(st.statusId, st);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					// since
				}
			});
		}
	}

	public static HashMap<Integer, StatusWrapper> getAllStatuses() {
		return allStatuses;
	}

    public static String getBackgroundColor(int state) {
		switch (state) {
			case PRIMARY_CONTACT: return "#F8F8FF"; 
			case TALK: return "#FFFACD"; 
			case DECISION_MAKING: return "#F0FFF0";
			case RECONCILIATION_AGREEMENT: return "#ADD8E6";
			case CLOSE_OK: return "#7CFC00";
			case COMPLETION:return "#3CB371";
			case EXECUTION: return "#004276";
			case SUSPENDED: return "#FF9900";
			case CLOSE_FAULT: return "#AA0000";
			
		}
		return null;
    }

    public static String getTextColor(int state) {
		switch (state) {
			case PRIMARY_CONTACT: 
			case TALK: 
			case DECISION_MAKING:
			case SUSPENDED:
			case CLOSE_OK:
			case RECONCILIATION_AGREEMENT: 
				return "black";
			
			case COMPLETION:
			case CLOSE_FAULT:
			case EXECUTION:
					return "white";
			
		}
		return null;
    }

	public StatusWrapper copy() {
		StatusWrapper wrap = new StatusWrapper();
		
		wrap.statusId = statusId;
		wrap.puser_owner_id = puser_owner_id;
		wrap.statusName = statusName;
		wrap.statusDayLimit = statusDayLimit;
		
		return wrap;
	}
	
}
