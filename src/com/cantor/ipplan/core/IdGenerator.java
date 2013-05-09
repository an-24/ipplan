package com.cantor.ipplan.core;

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class IdGenerator extends SequenceGenerator {
	
	@Override
	public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
		params.put( PersistentIdentifierGenerator.IDENTIFIER_NORMALIZER,new Configuration().createMappings().getObjectNameNormalizer());
		params.put("sequence","NEWRECORDID");
		super.configure(type, params, dialect);
	}

	@Override
	public Serializable generate(SessionImplementor session, Object obj) {
		int id = 0;
		if(obj instanceof IdGetter) 
			id = ((IdGetter) obj).getId();
		return id==0?super.generate(session, obj):id;
	}

	public static int generatorId(SessionFactory factory,Session session) {
		IdGenerator generator=new IdGenerator();
	    generator.configure(StandardBasicTypes.INTEGER, new Properties(), ((SessionFactoryImpl)factory).getDialect());
    	boolean openman = false;
    	if(session==null) {
    		session =	factory.openSession();
    		openman = true;
    	}
    	Number id = generator.generateHolder((SessionImplementor) session).makeValue();
    	if(openman)
    		session.close();
    	
	    return id.intValue();
	}
	
	public static int generatorId(SessionFactory factory) {
		return generatorId(factory,null);
	}
}
