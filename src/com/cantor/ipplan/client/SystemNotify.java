package com.cantor.ipplan.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class SystemNotify {
	
	private Map<Class,List<NotifyHandler>> handlers = new HashMap<Class,List<NotifyHandler>>();

	private static final SystemNotify updateNotify = new SystemNotify();
	private static final SystemNotify insertNotify = new SystemNotify();
	private static final SystemNotify deleteNotify = new SystemNotify();
	
	public <E> void registerNotify(Class<E> cls, NotifyHandler<E> handler) {
		List<NotifyHandler> list = handlers.get(cls);
		if(list==null) {
			list = new ArrayList<NotifyHandler>();
			handlers.put(cls, list);
		};	
		list.add(handler);
	};
	
	public <E> void notify(E object) {
		List<NotifyHandler> list = handlers.get(object.getClass());
		if(list!=null) {
			for (NotifyHandler<E> h : list) {
				h.onNotify(object);
			}
		};
	}

	public static SystemNotify getUpdateNotify() {
		return updateNotify;
	}

	public static SystemNotify getInsertNotify() {
		return insertNotify;
	}

	public static SystemNotify getDeleteNotify() {
		return deleteNotify;
	};
	

};
