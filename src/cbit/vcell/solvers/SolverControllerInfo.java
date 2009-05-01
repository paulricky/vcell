package cbit.vcell.solvers;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.rmi.RemoteException;
import cbit.vcell.solver.SimulationInfo;
import java.io.*;
import cbit.vcell.math.*;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class SolverControllerInfo implements Serializable {
	public SimulationInfo simulationInfo;
	public cbit.vcell.solver.SolverStatus solverStatus;
	public java.util.Date startDate;
	public String taskType;
	public String host;
	public int jobIndex;

/**
 * This method was created in VisualAge.
 * @param mathController cbit.vcell.solvers.MathController
 */
public SolverControllerInfo(SolverProxy solverProxy) {
	try {
		this.simulationInfo = solverProxy.getSimulationJob().getWorkingSim().getSimulationInfo();
	}catch (RemoteException e){}
	try {
		this.solverStatus = solverProxy.getSolverStatus();
	}catch (org.vcell.util.DataAccessException e){
	}catch (RemoteException e){}
	try {
		this.host = solverProxy.getHost();
	}catch (org.vcell.util.DataAccessException e){
	}catch (RemoteException e){}
	this.startDate = solverProxy.getStartDate();
	try {
		this.taskType = solverProxy.getSimulationJob().getWorkingSim().getSolverTaskDescription().getVCML();
	}catch (RemoteException e){}
	try {
		this.jobIndex = solverProxy.getSimulationJob().getJobIndex();
	}catch (RemoteException e){}
}


/**
 * Insert the method's description here.
 * Creation date: (7/18/01 12:15:32 PM)
 * @return java.lang.String
 */
public String getHost() {
	return host;
}


/**
 * Insert the method's description here.
 * Creation date: (7/18/01 12:15:59 PM)
 * @return java.lang.String
 */
public SimulationInfo getSimulationInfo() {
	return simulationInfo;
}


/**
 * Insert the method's description here.
 * Creation date: (7/18/01 12:16:50 PM)
 * @return java.lang.String
 */
public cbit.vcell.solver.SolverStatus getSolverStatus() {
	return solverStatus;
}


/**
 * Insert the method's description here.
 * Creation date: (7/18/01 12:16:36 PM)
 * @return java.lang.String
 */
public java.util.Date getStartDate() {
	return startDate;
}


/**
 * This method was created in VisualAge.
 * @return boolean
 */
public boolean isRunning() {
	if (solverStatus==null){
		return false;
	}else{
		return solverStatus.isRunning();
	}
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String toString() {
	StringBuffer buffer = new StringBuffer();

	buffer.append("host="+host+"\n");
	buffer.append("simulationInfo="+simulationInfo+"\n");
	buffer.append("solverStatus="+solverStatus+"\n");
	buffer.append("startDate="+startDate+"\n");
	buffer.append("taskType="+taskType+"\n");
	buffer.append("jobIndex="+jobIndex+"\n");

	return buffer.toString();
}
}