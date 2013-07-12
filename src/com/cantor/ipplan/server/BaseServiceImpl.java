package com.cantor.ipplan.server;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import com.cantor.ipplan.shared.PUserWrapper;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;

@SuppressWarnings("serial")
public class BaseServiceImpl extends RemoteServiceServlet {

	//private static Logger log = Logger.getLogger(BaseServiceImpl.class.getName());
	// for tomcat
	private static Logger log = Logger.getLogger("JULI");
	
	
	private HttpSession session =  null;

	public BaseServiceImpl() {
		this(null);
	}

	public BaseServiceImpl(HttpSession session) {
		super();
		this.session = session; 
	}

	
	
	
	public String processCall(String payload) throws SerializationException {
	    // First, check for possible XSRF situation
	    checkPermutationStrongName();

	    try {
	      RPCRequest rpcRequest = RPC.decodeRequest(payload, this.getClass(), this);
	      onAfterRequestDeserialized(rpcRequest);
	      return invokeAndEncodeResponse(this, rpcRequest.getMethod(),
	          rpcRequest.getParameters(), rpcRequest.getSerializationPolicy(),
	          rpcRequest.getFlags());
	    } catch (IncompatibleRemoteServiceException ex) {
	    	log("An IncompatibleRemoteServiceException was thrown while processing this call.",ex);
	    	return RPC.encodeResponseForFailure(null, ex);
	    } catch (RpcTokenException tokenException) {
	    	log("An RpcTokenException was thrown while processing this call.",tokenException);
	    	return RPC.encodeResponseForFailure(null, tokenException);
	    }
}
	
	protected SessionFactory getSessionFactory() {
		return (SessionFactory) this.getSession().getAttribute("sessionFactory");
	}

	protected void createSessionFactory(String url) throws Exception {
		SessionFactory sessionFactory = getSessionFactory();
		if(sessionFactory==null) {
			// конфигурируем hibername
	    	Configuration cfg = new Configuration().configure();
	    	cfg.setProperty(Environment.CONNECTION_PROVIDER, "com.cantor.ipplan.server.UserDataPoolConnection");
	    	ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build(); 
	    	UserDataPoolConnection pool = (UserDataPoolConnection) serviceRegistry.getService(org.hibernate.engine.jdbc.connections.spi.ConnectionProvider.class);
	    	pool.setPool(url);
	    	sessionFactory = cfg.buildSessionFactory(serviceRegistry);
	    	// устанавливаем в сессии
	    	this.getSession().setAttribute("sessionFactory", sessionFactory);
		}
	}



	public HttpSession getSession() {
		return session==null?getThreadLocalRequest().getSession():session;
	}
	
	protected PUserWrapper getLoginUser() {
		HttpSession sess = this.getSession();
		return (PUserWrapper) sess.getAttribute("loginUser");
	}

	protected void setLoginUser(PUserWrapper u) {
		HttpSession sess = this.getSession();
		sess.setAttribute("loginUser",u);
	}
	
	
	private PUserWrapper isLogged() {
		SessionFactory sessionFactory = getSessionFactory();
		return sessionFactory!=null?getLoginUser():null;
	}

	public void checkAccess() throws Exception {
		if(isLogged()==null)
			throw new Exception("Доступ запрещен");
	}
	
	protected int getUserId() {
		HttpSession sess = this.getSession();
		return (Integer) sess.getAttribute("userId");
	}
	
	
	public static String invokeAndEncodeResponse(Object target, Method serviceMethod, Object[] args,
	      SerializationPolicy serializationPolicy, int flags) throws SerializationException {
		if (serviceMethod == null) {
			throw new NullPointerException("serviceMethod");
		}

		if (serializationPolicy == null) {
			throw new NullPointerException("serializationPolicy");
		}
		try {
			Object result = serviceMethod.invoke(target, args);
			return RPC.encodeResponseForSuccess(serviceMethod, result, serializationPolicy, flags);
			
	    } catch (IllegalAccessException e) {
	    	SecurityException securityException =
	            new SecurityException(formatIllegalAccessErrorMessage(target, serviceMethod));
	        securityException.initCause(e);
	    	log.log(Level.SEVERE, "Exception: ", e);
	        throw securityException;
	    } catch (IllegalArgumentException e) {
	    	SecurityException securityException =
	            new SecurityException(formatIllegalArgumentErrorMessage(target, serviceMethod, args));
	        securityException.initCause(e);
	    	log.log(Level.SEVERE, "Exception: ", e);
	        throw securityException;
	    } catch (InvocationTargetException e) {
	    	Throwable cause = e.getCause();
	    	log.log(Level.SEVERE, "Exception: ", cause);
	    	return encodeResponse(cause.getClass(),new Exception(cause.getMessage()),true);
	    }
	}
	
	private static String encodeResponse(Class<?> responseClass, Object object, boolean wasThrown) throws SerializationException {

		ServerSerializationStreamWriter stream =
		        new ServerSerializationStreamWriter(new WithoutPolicy());
		//stream.setFlags(flags);

		stream.prepareToWrite();
		if (responseClass != void.class) {
		      stream.serializeValue(object, responseClass);
		}

		String bufferStr = (wasThrown ? "//EX" : "//OK") + stream.toString();
		return bufferStr;
	}
	
	
	private static String formatIllegalAccessErrorMessage(Object target, Method serviceMethod) {
	    StringBuffer sb = new StringBuffer();
	    sb.append("Blocked attempt to access inaccessible method '");
	    sb.append(getSourceRepresentation(serviceMethod));
	    sb.append("'");

	    if (target != null) {
	      sb.append(" on target '");
	      sb.append(printTypeName(target.getClass()));
	      sb.append("'");
	    }

	    sb.append("; this is either misconfiguration or a hack attempt");

	    return sb.toString();
	}

	private static String formatIllegalArgumentErrorMessage(Object target, Method serviceMethod,
		      Object[] args) {
	    StringBuffer sb = new StringBuffer();
	    sb.append("Blocked attempt to invoke method '");
	    sb.append(getSourceRepresentation(serviceMethod));
	    sb.append("'");
	
	    if (target != null) {
	      sb.append(" on target '");
	      sb.append(printTypeName(target.getClass()));
	      sb.append("'");
	    }
	
	    sb.append(" with invalid arguments");
	
	    if (args != null && args.length > 0) {
	      sb.append(Arrays.asList(args));
	    }
	
	    return sb.toString();
	}
	
	private static String getSourceRepresentation(Method method) {
		return method.toString().replace('$', '.');
	}
	
	private static String printTypeName(Class<?> type) {
	    // Primitives
	    //
	    if (type.equals(Integer.TYPE)) {
	      return "int";
	    } else if (type.equals(Long.TYPE)) {
	      return "long";
	    } else if (type.equals(Short.TYPE)) {
	      return "short";
	    } else if (type.equals(Byte.TYPE)) {
	      return "byte";
	    } else if (type.equals(Character.TYPE)) {
	      return "char";
	    } else if (type.equals(Boolean.TYPE)) {
	      return "boolean";
	    } else if (type.equals(Float.TYPE)) {
	      return "float";
	    } else if (type.equals(Double.TYPE)) {
	      return "double";
	    }

	    // Arrays
	    //
	    if (type.isArray()) {
	      Class<?> componentType = type.getComponentType();
	      return printTypeName(componentType) + "[]";
	    }

	    // Everything else
	    //
	    return type.getName().replace('$', '.');
	}
	
	static class WithoutPolicy extends  SerializationPolicy {

		@Override
		public boolean shouldDeserializeFields(Class<?> clazz) {
			return clazz!=null;
		}

		@Override
		public boolean shouldSerializeFields(Class<?> clazz) {
			return clazz!=null;
		}

		@Override
		public void validateDeserialize(Class<?> clazz)
				throws SerializationException {
		}

		@Override
		public void validateSerialize(Class<?> clazz)
				throws SerializationException {
		}
		
	}
	
}
