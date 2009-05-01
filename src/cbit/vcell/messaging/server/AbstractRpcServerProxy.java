package cbit.vcell.messaging.server;
import cbit.vcell.messaging.JmsClientMessaging;
import javax.jms.JMSException;

import org.vcell.util.SessionLog;
import org.vcell.util.MessageConstants.ServiceType;
import org.vcell.util.document.User;

/**
 * Insert the type's description here.
 * Creation date: (5/13/2003 2:14:50 PM)
 * @author: Fei Gao
 */
public abstract class AbstractRpcServerProxy implements RpcServerProxy {
	private cbit.vcell.messaging.JmsClientMessaging clientMessaging = null;
	protected org.vcell.util.document.User user;
	private java.lang.String queueName = null;
	protected org.vcell.util.SessionLog log = null;

/**
 * RpcServerProxy constructor comment.
 */
protected AbstractRpcServerProxy(User argUser, JmsClientMessaging clientMessaging0, String queueName0, SessionLog argLog) throws JMSException {
	super();
	this.user = argUser;
	this.clientMessaging = clientMessaging0;
	this.log = argLog;
	queueName = queueName0;
}


/**
 * Insert the method's description here.
 * Creation date: (12/5/2001 9:39:03 PM)
 * @return java.lang.Object
 * @param methodName java.lang.String
 * @param args java.lang.Object[]
 * @exception java.lang.Exception The exception description.
 */
public Object rpc(ServiceType serviceType, String methodName, Object[] args, boolean returnRequired) throws Exception {
	return rpc(serviceType, methodName, args, returnRequired, null, null);
}


/**
 * Insert the method's description here.
 * Creation date: (12/5/2001 9:39:03 PM)
 * @return java.lang.Object
 * @param methodName java.lang.String
 * @param args java.lang.Object[]
 * @exception java.lang.Exception The exception description.
 */
public Object rpc(ServiceType serviceType, String methodName, Object[] args, boolean returnRequired, String[] specialProperties, Object[] specialValues) throws Exception {
	RpcRequest request = new RpcRequest(user, serviceType, methodName, args);
	return clientMessaging.rpc(request, queueName, returnRequired, specialProperties, specialValues);
}
}