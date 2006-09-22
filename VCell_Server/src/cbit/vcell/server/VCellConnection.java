package cbit.vcell.server;

import java.rmi.RemoteException;

import cbit.util.DataAccessException;
import cbit.util.User;
/**
 * This interface was generated by a SmartGuide.
 * 
 */
public interface VCellConnection extends java.rmi.Remote {
/**
 * Insert the method's description here.
 * Creation date: (6/8/2001 1:17:05 AM)
 * @return cbit.vcell.server.DataSetController
 * @exception cbit.util.DataAccessException The exception description.
 * @exception java.rmi.RemoteException The exception description.
 */
cbit.vcell.server.bionetgen.BNGService getBNGService() throws java.rmi.RemoteException, DataAccessException;
/**
 * Insert the method's description here.
 * Creation date: (6/8/2001 1:17:05 AM)
 * @return cbit.vcell.server.DataSetController
 * @exception cbit.util.DataAccessException The exception description.
 * @exception java.rmi.RemoteException The exception description.
 */
DataSetController getDataSetController() throws DataAccessException, java.rmi.RemoteException;
/**
 * Insert the method's description here.
 * Creation date: (1/9/01 1:27:00 PM)
 * @return cbit.rmi.event.RemoteMessageHandler
 */
cbit.rmi.event.RemoteMessageHandler getRemoteMessageHandler() throws RemoteException;
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.solvers.MathController
 * @param simulationInfo cbit.vcell.solver.SimulationInfo
 * @param simulation cbit.vcell.solver.Simulation
 * @exception java.rmi.RemoteException The exception description.
 */
public SimulationController getSimulationController() throws RemoteException;
/**
 * Insert the method's description here.
 * Creation date: (3/2/01 11:15:25 PM)
 * @return cbit.vcell.server.URLFinder
 * @exception java.rmi.RemoteException The exception description.
 */
URLFinder getURLFinder() throws java.rmi.RemoteException;
/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public User getUser() throws RemoteException;
/**
 * This method was created by a SmartGuide.
 * @return DBManager
 * @param userid java.lang.String
 * @exception java.rmi.RemoteException The exception description.
 */
public UserMetaDbServer getUserMetaDbServer() throws RemoteException, DataAccessException;
}
