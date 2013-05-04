package com.cantor.ipplan.core;

import java.io.Serializable;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.SequenceGenerator;

public class IdGenerator extends SequenceGenerator {

	//TODO отконфигурировать
	@Override
	public Serializable generate(SessionImplementor session, Object obj) {
		int id = 0;
		if(obj instanceof IdGetter) 
			id = ((IdGetter) obj).getId();
		return id==0?super.generate(session, obj):id;
	}
}
