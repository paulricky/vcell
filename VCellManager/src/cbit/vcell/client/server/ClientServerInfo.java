package cbit.vcell.client.server;
/**
 * Insert the type's description here.
 * Creation date: (5/12/2004 4:07:23 PM)
 * @author: Ion Moraru
 */
public class ClientServerInfo {
	public final static int SERVER_REMOTE = 0;
	public final static int SERVER_LOCAL = 1;
	public final static int SERVER_FILE = 2;
	public final static String LOCAL_SERVER = "LOCAL";

	private int serverType = -1;
	private String host = null;
	private String username = null;
	private String password = null;

/**
 * Insert the method's description here.
 * Creation date: (5/12/2004 4:10:03 PM)
 * @param host java.lang.String
 * @param username java.lang.String
 * @param password java.lang.String
 */
private ClientServerInfo(String host, String username, String password) {
	this.host = host;
	this.username = username;
	this.password = password;
}


/**
 * Insert the method's description here.
 * Creation date: (6/1/2004 11:18:42 PM)
 * @return cbit.vcell.client.server.ClientServerInfo
 */
public static ClientServerInfo createFileBasedServerInfo() {
	ClientServerInfo csi = new ClientServerInfo(null, null, null);
	csi.setServerType(SERVER_FILE);
	return csi;
}


/**
 * Insert the method's description here.
 * Creation date: (6/1/2004 11:18:42 PM)
 * @return cbit.vcell.client.server.ClientServerInfo
 * @param host java.lang.String
 * @param username java.lang.String
 * @param password java.lang.String
 */
public static ClientServerInfo createLocalServerInfo(String username, String password) {
	ClientServerInfo csi = new ClientServerInfo(LOCAL_SERVER, username, password);
	csi.setServerType(SERVER_LOCAL);
	return csi;
}


/**
 * Insert the method's description here.
 * Creation date: (6/1/2004 11:18:42 PM)
 * @return cbit.vcell.client.server.ClientServerInfo
 * @param host java.lang.String
 * @param username java.lang.String
 * @param password java.lang.String
 */
public static ClientServerInfo createRemoteServerInfo(String host, String username, String password) {
	ClientServerInfo csi = new ClientServerInfo(host, username, password);
	csi.setServerType(SERVER_REMOTE);
	return csi;
}


/**
 * Insert the method's description here.
 * Creation date: (6/28/2004 12:35:46 AM)
 * @return boolean
 * @param o java.lang.Object
 */
public boolean equals(Object o) {
	return (
		o != null &&
		o instanceof ClientServerInfo &&
		o.toString().equals(toString())
	);
}


/**
 * Insert the method's description here.
 * Creation date: (5/12/2004 4:09:30 PM)
 * @return java.lang.String
 */
public java.lang.String getHost() {
	return host;
}


/**
 * Insert the method's description here.
 * Creation date: (5/12/2004 4:09:30 PM)
 * @return java.lang.String
 */
public java.lang.String getPassword() {
	return password;
}


/**
 * Insert the method's description here.
 * Creation date: (6/1/2004 11:20:12 PM)
 * @return int
 */
public int getServerType() {
	return serverType;
}


/**
 * Insert the method's description here.
 * Creation date: (5/12/2004 4:09:30 PM)
 * @return java.lang.String
 */
public java.lang.String getUsername() {
	return username;
}


/**
 * Insert the method's description here.
 * Creation date: (6/28/2004 12:48:39 AM)
 * @return int
 */
public int hashCode() {
	return toString().hashCode();
}


/**
 * Insert the method's description here.
 * Creation date: (6/1/2004 11:20:12 PM)
 * @param newServerType int
 */
private void setServerType(int newServerType) {
	serverType = newServerType;
}


/**
 * Insert the method's description here.
 * Creation date: (6/28/2004 12:40:07 AM)
 * @return java.lang.String
 */
public String toString() {
	String details = null;
	switch (getServerType()) {
		case SERVER_LOCAL: {
			details = "SERVER_LOCAL, user:" + getUsername() + ", password:" + getPassword();
			break;
		}
		case SERVER_REMOTE: {
			details = "SERVER_REMOTE, host:" + getHost() + ", user:" + getUsername() + ", password:" + getPassword();
			break;
		}
		case SERVER_FILE: {
			details = "SERVER_FILE";
			break;
		}
	}
	return "ClientServerInfo: [" + details + "]";
}
}