package cbit.vcell.anonymizer;
import cbit.sql.UserInfo;
import cbit.vcell.server.WatchdogMonitor;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.vcell.server.VCellBootstrap;
import cbit.vcell.server.VCellConnection;
import cbit.vcell.server.AuthenticationException;
import cbit.vcell.server.VCellServer;
import cbit.vcell.server.AdminDatabaseServer;
import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.rmi.activation.*;

import org.vcell.util.DataAccessException;
import org.vcell.util.PermissionException;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.StdoutSessionLog;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.User;

/**
 * This class was generated by a SmartGuide.
 * 
 */
public class AnonymizerBootstrap extends Activatable implements VCellBootstrap {
	private SessionLog sessionLog = new StdoutSessionLog("AnonymizerBootstrap");
	private String localHost = null;
	private int localPort = 0;
	private String remoteHost = null;
	private int remotePort = 0;

	private AnonymizerSetup anonymizerSetup = null;

	private String softwareVersion = null;

/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
private AnonymizerBootstrap(ActivationID id, MarshalledObject data) throws RemoteException {
	super(id, 0);

	loadProperties();
	
	checkUpdate();
}


/**
 * Insert the method's description here.
 * Creation date: (6/8/2006 2:37:35 PM)
 */
private void checkUpdate() {
	Thread checkUpdateThread = new Thread() {
		public void run() {
			while (true) {
				try {
					Thread.sleep(3600 * 1000);
					if (softwareVersion == null) {
						softwareVersion = getRemoteVCellSoftwareVersion();						
					} 
					
					String remoteSoftwareVersion = getRemoteVCellSoftwareVersion();
					sessionLog.print("current softwareVersion is " + softwareVersion + ", remote software version is " + remoteSoftwareVersion);
					if (softwareVersion == null || !softwareVersion.equals(remoteSoftwareVersion)) { 
						sessionLog.print("----------------------------------------------------------");
						sessionLog.print("Can't synchronzie with the virtual cell server, exiting...");
						sessionLog.print("----------------------------------------------------------");
						System.exit(0);
					}					
				} catch (InterruptedException ex) {
					sessionLog.exception(ex);
				}
			}
		}
	};

	checkUpdateThread.start();
}


/**
 * Insert the method's description here.
 * Creation date: (6/8/2006 2:51:06 PM)
 * @return java.lang.String
 */
private java.lang.String getRemoteVCellSoftwareVersion() {
	return cbit.vcell.server.RMIVCellConnectionFactory.getVCellSoftwareVersion(remoteHost+":"+remotePort);
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.DataSetController
 * @exception java.lang.Exception The exception description.
 */
public VCellConnection getVCellConnection(String userid, String password) throws DataAccessException, AuthenticationException, RemoteException {
	try {
		cbit.vcell.server.RMIVCellConnectionFactory rmiVCellConnectionFactory = new cbit.vcell.server.RMIVCellConnectionFactory(remoteHost+":"+remotePort,userid,password);
		AnonymizerVCellConnection vcConn = new AnonymizerVCellConnection(rmiVCellConnectionFactory, new StdoutSessionLog(userid));
		return vcConn;
	}catch (cbit.vcell.server.ConnectionException e){
		e.printStackTrace(System.out);
		throw new RemoteException(e.getMessage(),e);
	}
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.DataSetController
 * @exception java.lang.Exception The exception description.
 */
public VCellServer getVCellServer(User user, String password) throws DataAccessException, AuthenticationException, PermissionException {
	throw new AuthenticationException("Anonymous connection not allowed for administrative access");
}


/**
 * Insert the method's description here.
 * Creation date: (6/8/2006 2:51:06 PM)
 * @return java.lang.String
 */
public java.lang.String getVCellSoftwareVersion() throws RemoteException {
	return softwareVersion;
}


/**
 * Insert the method's description here.
 * Creation date: (6/8/2006 12:29:59 PM)
 */
private void loadProperties() {
	try {
		PropertyLoader.loadAnonymizerProperties();
		
		String logfilename = PropertyLoader.getProperty(PropertyLoader.vcellAnonymizerBootstrapLogfile, null);		
		if (logfilename != null) {
			sessionLog.print("trying to log to file [" + logfilename + "]");	
			File logFile = new File(logfilename);
			if (logFile.getParentFile() != null && !logFile.getParentFile().exists()) {
				logFile.getParentFile().mkdirs();				
			}
			sessionLog.print("log is redirected to file [" + logfilename + "]");
			System.setOut(new PrintStream(new FileOutputStream(logFile)));
		}


		localHost = PropertyLoader.getRequiredProperty(org.vcell.util.PropertyLoader.vcellAnonymizerBootstrapLocalHost);
		localPort = Integer.parseInt(PropertyLoader.getRequiredProperty(org.vcell.util.PropertyLoader.vcellAnonymizerBootstrapLocalPort));
		remoteHost = PropertyLoader.getRequiredProperty(org.vcell.util.PropertyLoader.vcellAnonymizerBootstrapRemoteHost);
		remotePort = Integer.parseInt(PropertyLoader.getRequiredProperty(org.vcell.util.PropertyLoader.vcellAnonymizerBootstrapRemotePort));

		sessionLog.print("local server : " + localHost + ":" + localPort);
		sessionLog.print("remote server : " + remoteHost + ":" + remotePort);		
	} catch (Exception ex) {
		ex.printStackTrace();
	}	
}


public UserInfo insertUserInfo(UserInfo newUserInfo) throws RemoteException,DataAccessException {
	throw new RuntimeException("Not Yet Implemented");
}
public UserInfo updateUserInfo(UserInfo newUserInfo) throws RemoteException,DataAccessException {
	throw new RuntimeException("Not Yet Implemented");
}
public UserInfo getUserInfo(KeyValue userKey) throws RemoteException,DataAccessException {
	throw new RuntimeException("Not Yet Implemented");
}
public void sendLostPassword(String userid) throws RemoteException,DataAccessException {
	throw new RuntimeException("Not Yet Implemented");
}

}