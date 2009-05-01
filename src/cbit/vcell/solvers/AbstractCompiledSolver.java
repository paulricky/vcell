package cbit.vcell.solvers;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.io.*;
import java.util.Vector;

import org.vcell.util.SessionLog;

import cbit.vcell.math.AnnotatedFunction;
import cbit.vcell.server.*;
import cbit.vcell.solver.*;
/**
 * Insert the type's description here.
 * Creation date: (6/26/2001 3:18:18 PM)
 * @author: Ion Moraru
 */
public abstract class AbstractCompiledSolver extends AbstractSolver implements java.beans.PropertyChangeListener {
	private transient boolean fieldRunning = false;
	private transient Thread fieldThread = null;
	private cbit.vcell.solvers.MathExecutable mathExecutable = null;
	private double currentTime = -1;
	protected final static String DATA_PREFIX = "data:";
	protected final static String PROGRESS_PREFIX = "progress:";
	protected final static String SEPARATOR = ":";
/**
 * AbstractPDESolver constructor comment.
 */
public AbstractCompiledSolver(SimulationJob simulationJob, File directory, SessionLog sessionLog) throws SolverException {
	super(simulationJob, directory, sessionLog);
	setCurrentTime(getSimulation().getSolverTaskDescription().getTimeBounds().getStartingTime());
}
/**
 * Insert the method's description here.
 * Creation date: (12/9/2002 4:50:53 PM)
 */
public abstract void cleanup();
/**
 * Insert the method's description here.
 * Creation date: (6/27/2001 2:48:22 PM)
 * @return cbit.vcell.solvers.ApplicationMessage
 * @param message java.lang.String
 */
protected abstract ApplicationMessage getApplicationMessage(String message);
/**
 * Insert the method's description here.
 * Creation date: (6/28/01 2:44:43 PM)
 * @return double
 */
public double getCurrentTime() {
	return currentTime;
}
/**
 * Insert the method's description here.
 * Creation date: (6/26/2001 5:03:04 PM)
 * @return cbit.vcell.solvers.MathExecutable
 */
protected MathExecutable getMathExecutable() {
	return mathExecutable;
}
/**
 * Insert the method's description here.
 * Creation date: (6/28/01 2:48:52 PM)
 * @return double
 */
public double getProgress() {
	double startTime = getSimulation().getSolverTaskDescription().getTimeBounds().getStartingTime();
	double endTime = getSimulation().getSolverTaskDescription().getTimeBounds().getEndingTime();
	return (currentTime-startTime)/(endTime-startTime);
}

/**
 * Insert the method's description here.
 * Creation date: (6/27/2001 2:33:03 PM)
 */
public void propertyChange(java.beans.PropertyChangeEvent event) {
	if (event.getSource() == getMathExecutable() && event.getPropertyName().equals("applicationMessage")) {
		String messageString = (String)event.getNewValue();
		if (messageString==null || messageString.length()==0){
			return;
		}
		ApplicationMessage appMessage = getApplicationMessage(messageString);
		if (appMessage==null){
			getSessionLog().alert("AbstractCompiledSolver: Unexpected Message '"+messageString+"'");
			return;
		}else{
			switch (appMessage.getMessageType()) {
				case ApplicationMessage.PROGRESS_MESSAGE: {
					fireSolverProgress(appMessage.getProgress());
					break;	
				}
				case ApplicationMessage.DATA_MESSAGE: {
					fireSolverPrinted(appMessage.getTimepoint());
					break;
				}
				case ApplicationMessage.ERROR_MESSAGE: {
					getSessionLog().alert(appMessage.getError());
					break;
				}
				// ignore unknown message types
			}
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (6/26/2001 3:08:31 PM)
 */
private void runSolver() {
	try {
		fieldRunning = true;
		setCurrentTime(getSimulation().getSolverTaskDescription().getTimeBounds().getStartingTime());
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_STARTING, "initializing"));
		// fireSolverStarting("initializing");
		// depends on solver; the initialize() method in actual solver will fire detailed messages
		initialize();
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, "starting"));
		fireSolverStarting("Solver starting...");
		getMathExecutable().start();
		cleanup();
		//  getMathExecutable().start() may end prematurely (error or user stop), so check status before firing...
		if (getMathExecutable().getStatus().equals(org.vcell.util.ExecutableStatus.COMPLETE)) {
			setSolverStatus(new SolverStatus(SolverStatus.SOLVER_FINISHED, "finished"));
			fireSolverFinished();
		}
	} catch (SolverException integratorException) {
		getSessionLog().exception(integratorException);
		cleanup();
		setSolverStatus(new SolverStatus (SolverStatus.SOLVER_ABORTED, integratorException.getMessage()));
		fireSolverAborted(integratorException.getMessage());
	} catch (org.vcell.util.ExecutableException executableException) {
		getSessionLog().exception(executableException);
		cleanup();
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_ABORTED, "Could not execute code: " + executableException.getMessage()));
		fireSolverAborted(executableException.getMessage());
	} catch (Throwable throwable) {
		getSessionLog().alert("AbstractODESolver.start() : Caught Throwable instead of SolverException -- THIS EXCEPTION SHOULD NOT HAPPEN!");
		getSessionLog().exception(throwable);
		cleanup();
		setSolverStatus(new SolverStatus (SolverStatus.SOLVER_ABORTED, throwable.getMessage()));
		fireSolverAborted(throwable.getMessage());
	} finally {
		fieldRunning = false;
	}
}
/**
 * Insert the method's description here.
 * Creation date: (6/28/01 2:44:43 PM)
 * @param newCurrentTime double
 */
protected void setCurrentTime(double newCurrentTime) {
	currentTime = newCurrentTime;
}
/**
 * Insert the method's description here.
 * Creation date: (6/26/2001 5:03:04 PM)
 * @param newMathExecutable cbit.vcell.solvers.MathExecutable
 */
protected void setMathExecutable(MathExecutable newMathExecutable) {
	if (mathExecutable != null) {
		mathExecutable.removePropertyChangeListener(this);
	}
	if (newMathExecutable != null) {
		newMathExecutable.addPropertyChangeListener(this);
	}
	mathExecutable = newMathExecutable;
}
public final void startSolver() {
	if (!fieldRunning) {
		setMathExecutable(null);
		fieldThread = new Thread() {
			public void run() {
				runSolver();
			}
		};
		fieldThread.setName("Compiled Solver (" + getClass().getName() + ")");
		fieldThread.start();
	}
}
/**
 */
public final void stopSolver() {
	if (getMathExecutable()!=null){
		getMathExecutable().stop();
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_STOPPED,"User aborted simulation"));
		fireSolverStopped();
	}
}

//Added Nov 2008.
public abstract Vector<AnnotatedFunction> createFunctionList();


//Added Nov 2008. For new mechanism of simulation data retrive. (No binary file is written for variables and functions from now on)
public void writeFunctionsFile() {
	// ** Dumping the functions of a simulation into a '.functions' file.
	String functionFileName = getBaseName() + FUNCTIONFILE_EXTENSION;
	Vector<AnnotatedFunction> funcList = createFunctionList();
	
	//Try to save existing user defined functions
	FunctionFileGenerator functionFileGenerator = new FunctionFileGenerator(functionFileName, funcList);

	try {
		functionFileGenerator.generateFunctionFile();		
	}catch (Exception e){
		e.printStackTrace(System.out);
		throw new RuntimeException("Error creating .function file for "+functionFileGenerator.getBasefileName()+e.getMessage());
	}		
}
}
