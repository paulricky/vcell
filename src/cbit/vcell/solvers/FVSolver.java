/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.solvers;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Vector;

import org.vcell.util.BeanUtils;
import org.vcell.util.DataAccessException;
import org.vcell.util.ISize;
import org.vcell.util.NullSessionLog;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;

import cbit.vcell.field.FieldDataIdentifierSpec;
import cbit.vcell.field.FieldFunctionArguments;
import cbit.vcell.field.FieldUtilities;
import cbit.vcell.field.SimResampleInfoProvider;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.surface.GeometrySurfaceDescription;
import cbit.vcell.math.AnnotatedFunction;
import cbit.vcell.math.Variable;
import cbit.vcell.messaging.server.SimulationTask;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.simdata.DataSetControllerImpl;
import cbit.vcell.simdata.SimDataConstants;
import cbit.vcell.solver.SimulationMessage;
import cbit.vcell.solver.SolverException;
import cbit.vcell.solver.SolverStatus;
import cbit.vcell.solver.VCSimulationDataIdentifier;

/**
 * This interface was generated by a SmartGuide.
 * 
 */
public class FVSolver extends AbstractCompiledSolver {
	protected CppCoderVCell cppCoderVCell = null;
	private SimResampleInfoProvider simResampleInfoProvider;
	private Geometry resampledGeometry = null;
	
	public static final int HESM_KEEP_AND_CONTINUE = 0;
	public static final int HESM_THROW_EXCEPTION = 1;
	public static final int HESM_OVERWRITE_AND_CONTINUE = 2;
	

/**
 * This method was created by a SmartGuide.
 * @param mathDesc cbit.vcell.math.MathDescription
 * @param platform cbit.vcell.solvers.Platform
 * @param directory java.lang.String
 * @param simID java.lang.String
 * @param clientProxy cbit.vcell.solvers.ClientProxy
 */
public FVSolver (SimulationTask simTask, File dir, SessionLog sessionLog, boolean bMsging) throws SolverException {
	super(simTask, dir, sessionLog, bMsging);
	if (! simTask.getSimulation().isSpatial()) {
		throw new SolverException("Cannot use FVSolver on non-spatial simulation");
	}
	this.simResampleInfoProvider = (VCSimulationDataIdentifier)simTask.getSimulationJob().getVCDataIdentifier();
	this.cppCoderVCell = new CppCoderVCell((new File(getBaseName())).getName(), getSaveDirectory(), simTask);
}


/**
 * This method was created by a SmartGuide.
 */
private void autoCode(boolean bNoCompile) throws SolverException {
	getSessionLog().print("LocalMathController.autoCode()");
	setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.MESSAGE_SOLVER_RUNNING_CODEGEN));
	fireSolverStarting(SimulationMessage.MESSAGE_SOLVEREVENT_STARTING_CODEGEN);
	
	String baseName = new File(getSaveDirectory(), cppCoderVCell.getBaseFilename()).getPath();

	String Compile = System.getProperty(PropertyLoader.compilerProperty);                 // "cl /c";
	String Link = System.getProperty(PropertyLoader.linkerProperty);                      // "cl";
	String exeOutputSpecifier = System.getProperty(PropertyLoader.exeOutputProperty);     // "/Fe";
	String objOutputSpecifier = System.getProperty(PropertyLoader.objOutputProperty);     // "/Fo";
	String compileFlags = System.getProperty(PropertyLoader.includeProperty)+" "+
							System.getProperty(PropertyLoader.definesProperty);           // "/I"+includeDir+" /DWIN32 /DDEBUG";
	String CodeFilename = baseName+System.getProperty(PropertyLoader.srcsuffixProperty);  // ".cpp";
	String libs = System.getProperty(PropertyLoader.libsProperty);                        // libraryDir+"VCLIB.lib";
	String exeSuffix = System.getProperty(PropertyLoader.exesuffixProperty);              // ".exe";
	String HeaderFilename = baseName+".h";
	String ExeFilename = baseName+exeSuffix;
	String ObjFilename = baseName+System.getProperty(PropertyLoader.objsuffixProperty);

	try {
		cppCoderVCell.initialize();
	}catch (Exception e){
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_ABORTED, SimulationMessage.solverAborted("autocode init exception: "+e.getMessage())));
		e.printStackTrace(System.out);
		throw new SolverException("autocode init exception: "+e.getMessage());
	}		
	setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.MESSAGE_SOLVER_RUNNING_CODEGEN));
	
	java.io.FileOutputStream osCode = null;
	java.io.FileOutputStream osHeader = null;
	try {
		osCode = new java.io.FileOutputStream(CodeFilename);
	}catch (java.io.IOException e){
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_ABORTED, SimulationMessage.solverAborted("error opening code file '"+CodeFilename+": "+e.getMessage())));
		e.printStackTrace(System.out);
		throw new SolverException("error opening code file '"+CodeFilename+": "+e.getMessage());
	}		
	
	try {
		osHeader = new java.io.FileOutputStream(HeaderFilename);
	}catch (java.io.IOException e){
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_ABORTED, SimulationMessage.solverAborted("error opening header file '"+HeaderFilename+": "+e.getMessage())));
		e.printStackTrace(System.out);
		throw new SolverException("error opening header file '"+HeaderFilename+": "+e.getMessage());
	}		
	
	try {
		cppCoderVCell.code(osHeader,osCode);
		osCode.close();
		osHeader.close();
	}catch (Exception e){
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_ABORTED, SimulationMessage.solverAborted(e.getMessage())));
		e.printStackTrace(System.out);
		throw new SolverException(e.getMessage());
	}	
	
	if (bNoCompile){
		return;
	}	
	
	setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.MESSAGE_SOLVER_RUNNING_COMPILING));
	fireSolverStarting(SimulationMessage.MESSAGE_SOLVEREVENT_STARTING_COMPILELINK);
	try {		
		String compileCommand = Compile+" "+CodeFilename+" "+compileFlags+" "+objOutputSpecifier+ObjFilename;
		System.out.println(compileCommand);
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.solverRunning_CompileCommand("% "+compileCommand)));
		
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(compileCommand);

		String stdoutString = "";
		String stderrString = "";

		InputStream inputStream = process.getInputStream();
		if (inputStream!=null){
			char charArray[] = new char[1000];
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			int numRead = inputStreamReader.read(charArray,0,charArray.length);
			if (numRead>0){
				stdoutString += new String(charArray,0,numRead);
				if (numRead == charArray.length){
					stdoutString += "\n(standard output truncated...)";
				}	
			}
			inputStreamReader.close();
		}	
		
		inputStream = process.getErrorStream();
		if (inputStream!=null){
			char charArray[] = new char[1000];
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			int numRead = inputStreamReader.read(charArray,0,charArray.length);
			if (numRead>0){
				stderrString += new String(charArray,0,numRead);
				if (numRead == charArray.length){
					stderrString += "\n(standard output truncated...)";
				}	
			}
			inputStreamReader.close();	
		}	
		
		try {
			process.waitFor();
//			throw new RemoteException("didn't wait for process");
		}catch (InterruptedException e){
		}	
		int retcode = 0;
		retcode = process.exitValue();
		if (retcode == 0){
			setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.MESSAGE_SOLVER_RUNNING_COMPILE_OK));	
		}else{
			getSessionLog().print("stderr:\n"+stderrString);
			getSessionLog().print("stdout:\n"+stdoutString);
			throw new SolverException("compilation failed, return code = "+retcode+"\n"+stderrString);
		}		
		process = null;
		
	}catch (Exception e){
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_ABORTED, SimulationMessage.solverAborted("error compiling: "+e.getMessage())));
		e.printStackTrace(System.out);
		throw new SolverException("Failed to compile your simulation, please contact the Virtual Cell for further assistance");		
	}

	
	setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.MESSAGE_SOLVER_RUNNING_LINKING));
	try {		
		String linkCommand = Link+" "+exeOutputSpecifier+ExeFilename+" "+ObjFilename+" "+libs;
		System.out.println(linkCommand);
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.solverRunning_LinkCommand("% "+linkCommand)));
		
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(linkCommand);

		String stdoutString = "";
		String stderrString = "";

		InputStream inputStream = process.getInputStream();
		if (inputStream!=null){
			char charArray[] = new char[1000];
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			int numRead = inputStreamReader.read(charArray,0,charArray.length);
			if (numRead>0){
				stdoutString += new String(charArray,0,numRead);
				if (numRead == charArray.length){
					stdoutString += "\n(standard output truncated...)";
				}	
			}
			inputStreamReader.close();
		}	
		
		inputStream = process.getErrorStream();
		if (inputStream!=null){
			char charArray[] = new char[1000];
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			int numRead = inputStreamReader.read(charArray,0,charArray.length);
			if (numRead>0){
				stderrString += new String(charArray,0,numRead);
				if (numRead == charArray.length){
					stderrString += "\n(standard output truncated...)";
				}	
			}
			inputStreamReader.close();	
		}	
		
		try {
			process.waitFor();
//			throw new RemoteException("didn't wait for process");
		}catch (InterruptedException e){
		}	
		int retcode = 0;
		retcode = process.exitValue();
		if (retcode == 0){
			setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.MESSAGE_SOLVER_RUNNING_LINK_OK));	
		}else{
			getSessionLog().print("stderr:\n"+stderrString);
			getSessionLog().print("stdout:\n"+stdoutString);
			throw new SolverException("link failed, return code = "+retcode);
		}		
		process = null;
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.MESSAGE_SOLVER_RUNNING_COMPILELINK_OK));
		
	}catch (Exception e){
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_ABORTED, SimulationMessage.solverAborted("error linking: "+e.getMessage())));
		e.printStackTrace(System.out);
		throw new SolverException("Failed to link your simulation, please contact the Virtual Cell for further assistance");	
	}	
}


/**
 * Insert the method's description here.
 * Creation date: (12/9/2002 4:53:30 PM)
 */
public void cleanup() {
	// nothing special needed
}


/**
 * Insert the method's description here.
 * Creation date: (2/2/2004 5:31:41 PM)
 * @return cbit.vcell.simdata.AnnotatedFunction[]
 * @param simulation cbit.vcell.solver.Simulation
 */
@Override
public Vector<AnnotatedFunction> createFunctionList() {
	//Try to save existing user defined functions
	Vector<AnnotatedFunction> annotatedFunctionVector = new Vector<AnnotatedFunction>();
	try{
		annotatedFunctionVector = simTask.getSimulationJob().getSimulationSymbolTable().createAnnotatedFunctionsList(simTask.getSimulation().getMathDescription());
		String functionFileName = getBaseName() + FUNCTIONFILE_EXTENSION;
		File existingFunctionFile = new File(functionFileName);
		if(existingFunctionFile.exists()){
			Vector<AnnotatedFunction> oldUserDefinedFunctions =
				new Vector<AnnotatedFunction>();
			Vector<AnnotatedFunction> allOldFunctionV =
				FunctionFileGenerator.readFunctionsFile(existingFunctionFile, simTask.getSimulationJobID());
			for(int i = 0; i < allOldFunctionV.size(); i += 1){
				if(allOldFunctionV.elementAt(i).isOldUserDefined()){
					oldUserDefinedFunctions.add(allOldFunctionV.elementAt(i));
				}
			}
			
			annotatedFunctionVector.addAll(oldUserDefinedFunctions);
		}
	}catch(Exception e){
		e.printStackTrace();
		//ignore
	}
	return annotatedFunctionVector;
}
/**
 * Insert the method's description here.
 * Creation date: (6/27/01 3:25:11 PM)
 * @return cbit.vcell.solvers.ApplicationMessage
 * @param message java.lang.String
 */
protected ApplicationMessage getApplicationMessage(String message) {
	//
	// "data:iteration:time"  .... sent every time data written for FVSolver
	// "progress:xx.x%"        .... sent every 1% for FVSolver
	//
	//
	if (message.startsWith(DATA_PREFIX)){
		double timepoint = Double.parseDouble(message.substring(message.lastIndexOf(SEPARATOR)+1));
		setCurrentTime(timepoint);
		return new ApplicationMessage(ApplicationMessage.DATA_MESSAGE,getProgress(),timepoint,null,message);
	}else if (message.startsWith(PROGRESS_PREFIX)){
		String progressString = message.substring(message.lastIndexOf(SEPARATOR)+1,message.indexOf("%"));
		double progress = Double.parseDouble(progressString)/100.0;
		double startTime = simTask.getSimulation().getSolverTaskDescription().getTimeBounds().getStartingTime();
		double endTime = simTask.getSimulation().getSolverTaskDescription().getTimeBounds().getEndingTime();
		setCurrentTime(startTime + (endTime-startTime)*progress);
		return new ApplicationMessage(ApplicationMessage.PROGRESS_MESSAGE,progress,-1,null,message);
	}else{
		throw new RuntimeException("unrecognized message");
	}
}


/**
 * Insert the method's description here.
 * Creation date: (4/17/2001 8:47:08 AM)
 * @return java.lang.String
 */
public static String getDescription() {
	return "Finite Volume, Structured Grid";
}


/**
 * This method was created by a SmartGuide.
 */
protected void initialize() throws SolverException {
	writeFunctionsFile();
	writeVCGAndResampleFieldData();	

	setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.MESSAGE_SOLVER_RUNNING_INIT));
	fireSolverStarting(SimulationMessage.MESSAGE_SOLVEREVENT_STARTING_INIT);
	
	autoCode(false);
	
//	String baseName = cppCoderVCell.getBaseFilename();
//	String exeSuffix = System.getProperty(PropertyLoader.exesuffixProperty); // ".exe";
//	File exeFile = new File(getSaveDirectory(), baseName + exeSuffix);
	boolean bCORBA = false;

	setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING,SimulationMessage.MESSAGE_SOLVER_RUNNING_START));
	
	try{
		bCORBA = Boolean.getBoolean(PropertyLoader.corbaEnabled);
	}catch (Throwable t){}

	if (bCORBA) {
		throw new RuntimeException("MathExecutableCORBA not supported");
		//executable = new MathExecutableCORBA(exeFile,mathDesc.getSimulationID(),getSessionLog());
	}else{
		setMathExecutable(new MathExecutable(getMathExecutableCommand()));
	}

}

@Override
public String[] getMathExecutableCommand() {
	String exeSuffix = System.getProperty(PropertyLoader.exesuffixProperty); // ".exe";
	String baseName = cppCoderVCell.getBaseFilename();
	File exeFile = new File(getSaveDirectory(), baseName + exeSuffix);
	return new String[] { exeFile.getAbsolutePath() };
}



public Geometry getResampledGeometry() throws SolverException {
	if (resampledGeometry == null) {
		// clone and resample geometry
		try {
			resampledGeometry = (Geometry) BeanUtils.cloneSerializable(simTask.getSimulation().getMathDescription().getGeometry());
			GeometrySurfaceDescription geoSurfaceDesc = resampledGeometry.getGeometrySurfaceDescription();
			ISize newSize = simTask.getSimulation().getMeshSpecification().getSamplingSize();
			geoSurfaceDesc.setVolumeSampleSize(newSize);
			geoSurfaceDesc.updateAll();		
		} catch (Exception e) {
			e.printStackTrace();
			throw new SolverException(e.getMessage());
		}
	}
	return resampledGeometry;
}
		
protected void writeVCGAndResampleFieldData() throws SolverException {
	fireSolverStarting(SimulationMessage.MESSAGE_SOLVEREVENT_STARTING_PROC_GEOM);
	
	try {
		// write subdomains file
		SubdomainInfo.write(new File(getSaveDirectory(), cppCoderVCell.getBaseFilename() + SimDataConstants.SUBDOMAINS_FILE_SUFFIX), simTask.getSimulation().getMathDescription());
		
		PrintWriter pw = new PrintWriter(new FileWriter(new File(getSaveDirectory(), cppCoderVCell.getBaseFilename()+SimDataConstants.VCG_FILE_EXTENSION)));
		GeometryFileWriter.write(pw, getResampledGeometry());
		pw.close();
				
		FieldDataIdentifierSpec[] argFieldDataIDSpecs = simTask.getSimulationJob().getFieldDataIdentifierSpecs();
		if(argFieldDataIDSpecs != null && argFieldDataIDSpecs.length > 0){
			fireSolverStarting(SimulationMessage.MESSAGE_SOLVEREVENT_STARTING_RESAMPLE_FD);
			
			FieldFunctionArguments psfFieldFunc = null;
			Variable var = simTask.getSimulationJob().getSimulationSymbolTable().getVariable(SimDataConstants.PSF_FUNCTION_NAME);
			if (var != null) {
				FieldFunctionArguments[] ffas = FieldUtilities.getFieldFunctionArguments(var.getExpression());
				if (ffas == null || ffas.length == 0) {
					throw new DataAccessException("Point Spread Function " + SimDataConstants.PSF_FUNCTION_NAME + " can only be a single field function.");
				} else {				
					Expression newexp;
					try {
						newexp = new Expression(ffas[0].infix());
						if (!var.getExpression().compareEqual(newexp)) {
							throw new DataAccessException("Point Spread Function " + SimDataConstants.PSF_FUNCTION_NAME + " can only be a single field function.");
						}
						psfFieldFunc = ffas[0];
					} catch (ExpressionException e) {
						e.printStackTrace();
						throw new DataAccessException(e.getMessage());
					}
				}
			}			
			
			boolean bResample[] = new boolean[argFieldDataIDSpecs.length];
			Arrays.fill(bResample, true);
			for (int i = 0; i < argFieldDataIDSpecs.length; i++) {
				argFieldDataIDSpecs[i].getFieldFuncArgs().getTime().bindExpression(simTask.getSimulationJob().getSimulationSymbolTable());
				if (argFieldDataIDSpecs[i].getFieldFuncArgs().equals(psfFieldFunc)) {
					bResample[i] = false;
				}
			}
			
			int numMembraneElements = getResampledGeometry().getGeometrySurfaceDescription().getSurfaceCollection().getTotalPolygonCount();
			CartesianMesh simpleMesh = CartesianMesh.createSimpleCartesianMesh(getResampledGeometry().getOrigin(), 
					getResampledGeometry().getExtent(),
					simTask.getSimulation().getMeshSpecification().getSamplingSize(),
					getResampledGeometry().getGeometrySurfaceDescription().getRegionImage());
			String secondarySimDataDir = PropertyLoader.getProperty(PropertyLoader.secondarySimDataDirProperty, null);			
			DataSetControllerImpl dsci = new DataSetControllerImpl(new NullSessionLog(), null, getSaveDirectory().getParentFile(),
					secondarySimDataDir == null ? null : new File(secondarySimDataDir));
			dsci.writeFieldFunctionData(null,argFieldDataIDSpecs, bResample, simpleMesh, simResampleInfoProvider, 
					numMembraneElements, HESM_OVERWRITE_AND_CONTINUE);
		}
	} catch(Exception e){
		throw new SolverException(e.getMessage());
	}
}


/**
 * Insert the method's description here.
 * Creation date: (6/27/2001 2:33:03 PM)
 */
public void propertyChange(java.beans.PropertyChangeEvent event) {
	super.propertyChange(event);
	
	if (event.getSource() == getMathExecutable() && event.getPropertyName().equals("applicationMessage")) {
		String messageString = (String)event.getNewValue();
		if (messageString==null || messageString.length()==0){
			return;
		}
		ApplicationMessage appMessage = getApplicationMessage(messageString);
		if (appMessage!=null && appMessage.getMessageType() == ApplicationMessage.DATA_MESSAGE) {
			fireSolverPrinted(appMessage.getTimepoint());
		}
	}
}
}
