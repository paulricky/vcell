package cbit.vcell.client;

import cbit.vcell.xml.XmlHelper;
import cbit.xml.merge.*;
import cbit.image.*;
import cbit.vcell.export.server.*;
import cbit.vcell.xml.XMLInfo;
import cbit.vcell.geometry.gui.OverlayEditorPanelJAI;
import cbit.vcell.geometry.gui.ROISourceData;
import cbit.vcell.geometry.surface.VolumeGeometricRegion;
import cbit.vcell.geometry.surface.SurfaceGeometricRegion;
import cbit.vcell.geometry.surface.GeometricRegion;
import cbit.vcell.solver.ode.gui.*;
import cbit.vcell.solvers.CartesianMesh;
import cbit.vcell.solver.*;
import cbit.vcell.client.task.*;
import cbit.vcell.desktop.LoginDialog;
import cbit.vcell.desktop.controls.*;
import cbit.vcell.math.*;
import cbit.vcell.mapping.*;

import java.beans.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

import cbit.rmi.event.ExportEvent;
import java.awt.*;

import cbit.vcell.client.server.*;
import cbit.vcell.server.*;
import cbit.vcell.simdata.PDEDataContext;
import cbit.vcell.geometry.*;
import cbit.vcell.mathmodel.*;
import cbit.vcell.numericstest.TestCriteriaCrossRefOPResults;
import cbit.vcell.numericstest.TestSuiteInfoNew;
import cbit.vcell.VirtualMicroscopy.ImageDataset;
import cbit.vcell.VirtualMicroscopy.ROI;
import cbit.vcell.VirtualMicroscopy.UShortImage;
import cbit.vcell.biomodel.*;
import cbit.vcell.client.FieldDataWindowManager.SimInfoHolder;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.undo.UndoableEditSupport;

import org.jdom.Element;
import org.vcell.util.BeanUtils;
import org.vcell.util.CommentStringTokenizer;
import org.vcell.util.DataAccessException;
import org.vcell.util.UserCancelException;
import org.vcell.util.VCDataIdentifier;
import org.vcell.util.document.BioModelInfo;
import org.vcell.util.document.CurateSpec;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.MathModelInfo;
import org.vcell.util.document.VCDocument;
import org.vcell.util.document.VCDocumentInfo;
import org.vcell.util.document.Version;
import org.vcell.util.document.VersionableType;
import org.vcell.util.document.VersionableTypeVersion;
import org.vcell.util.gui.AsynchGuiUpdater;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.FileFilters;
import org.vcell.util.gui.VCFileChooser;
import org.vcell.util.gui.ZEnforcer;

import cbit.vcell.xml.XMLTags;
/**
 * Insert the type's description here.
 * Creation date: (5/21/2004 2:42:55 AM)
 * @author: Ion Moraru
 */
public class ClientRequestManager implements RequestManager, PropertyChangeListener, cbit.rmi.event.ExportListener, cbit.rmi.event.VCellMessageEventListener {
	private VCellClient vcellClient = null;
	private boolean bOpening = false;
	private boolean bExiting = false;

/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:22:13 AM)
 * @param vcellClient cbit.vcell.client.VCellClient
 */
public ClientRequestManager(VCellClient vcellClient) {
	setVcellClient(vcellClient);
	// listen to connectionStatus events
	getClientServerManager().addPropertyChangeListener(this);
	getClientServerManager().getAsynchMessageManager().addExportListener(this);
	getClientServerManager().getAsynchMessageManager().addVCellMessageEventListener(this);
	
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:20:47 AM)
 * @param windowManager cbit.vcell.client.desktop.DocumentWindowManager
 */
public void changeGeometry(DocumentWindowManager requester, SimulationContext simContext) {
	GeometryInfo geometryInfo = null;
	try {
		geometryInfo = (GeometryInfo)getMdiManager().getDatabaseWindowManager().selectDocument(VCDocument.GEOMETRY_DOC, requester);
	} catch (UserCancelException uexc) {
		System.out.println(uexc);
		return;
	} catch (Exception exc) {
		exc.printStackTrace(System.out);
		PopupGenerator.showErrorDialog(requester, "Failed to retrieve geometry\n"+exc);
		return;
	}
	changeGeometryAfterChecking(geometryInfo, requester, simContext);
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:20:47 AM)
 * @param windowManager cbit.vcell.client.desktop.DocumentWindowManager
 */
private void changeGeometryAfterChecking(final GeometryInfo geoInfo, final DocumentWindowManager requester, final SimulationContext simContext) {
	/* asynchronous and not blocking any window */
		
	AsynchClientTask task1 = new AsynchClientTask("Changing geometry", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			getMdiManager().blockWindow(requester.getManagerID());
		}		
	};
	
	AsynchClientTask task2 = new AsynchClientTask("Changing geometry", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			Geometry geometry = getDocumentManager().getGeometry(geoInfo);
			geometry.getGeometrySpec().getSampledImage();
			if (geometry.getDimension()>0 && geometry.getGeometrySurfaceDescription().getGeometricRegions()==null){
				geometry.getGeometrySurfaceDescription().updateAll();				
			}			
			hashTable.put("geometry", geometry);
		}		
	};
	
	AsynchClientTask task3 = new AsynchClientTask("Changing geometry", AsynchClientTask.TASKTYPE_SWING_BLOCKING, false, false) {
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			try {
				Geometry geometry = (Geometry)hashTable.get("geometry");
				if (geometry != null) {
					if (requester instanceof BioModelWindowManager) {
						simContext.setGeometry(geometry);
					} else if (requester instanceof MathModelWindowManager) {
						MathModel mathModel = (MathModel)((MathModelWindowManager)requester).getVCDocument();
						mathModel.getMathDescription().setGeometry(geometry);
					}
				}
			} finally {
				getMdiManager().unBlockWindow(requester.getManagerID());
			}
		}
	};
	
	ClientTaskDispatcher.dispatch(requester.getComponent(), new Hashtable<String, Object>(), new AsynchClientTask[] {task1, task2, task3}, false);

}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:20:47 AM)
 * @param windowManager cbit.vcell.client.desktop.DocumentWindowManager
 */
private boolean checkBeforeClosing(DocumentWindowManager windowManager) {
	getMdiManager().showWindow(windowManager.getManagerID());
	// we need to check for changes and get user confirmation...
	boolean isChanged = true;
	try {
		isChanged = getDocumentManager().isChanged(windowManager.getVCDocument());
	} catch (Exception exc) {
		String choice = PopupGenerator.showWarningDialog(windowManager, getUserPreferences(), UserMessage.warn_UnableToCheckForChanges, null);
		if (choice.equals(UserMessage.OPTION_CANCEL)){
			// user canceled
			return false;
		}
	}
	// warn if necessary
	if (isChanged) {
		String choice = PopupGenerator.showWarningDialog(windowManager, getUserPreferences(), UserMessage.warn_closeWithoutSave,null);
		if (choice.equals(UserMessage.OPTION_CANCEL)){
			// user canceled
			return false;
		}
	}
	// nothing changed, or user confirmed, close it
	return true;
}


/**
 * Insert the method's description here.
 * Creation date: (5/10/2004 3:48:16 PM)
 */
private boolean checkDifferentFromBlank(int documentType, VCDocument vcDocument) {
	// Handle Bio/Math models different from Geometry since createDefaultDoc for Geometry
	// will bring up the NewGeometryEditor which is unnecessary.
	// figure out if we come from a blank new document; if so, replace it inside same window

	if (documentType != vcDocument.getDocumentType()) {
		// If the docType we are trying to open is not the same as the requesting windowmanager's docType
		// we have to open the doc in a new window, hence return true;
		return true;
	}
	VCDocument blank = null;
	if (vcDocument.getDocumentType() != VCDocument.GEOMETRY_DOC) {
		// BioModel/MathModel
		blank = createDefaultDocument(vcDocument.getDocumentType());
		try {
			blank.setName(vcDocument.getName());
		} catch (PropertyVetoException e) {} // ignore. doesn't happen
		return !blank.compareEqual(vcDocument);
	} else {
		// Geometry
		blank = new Geometry(vcDocument.getName(), 1);
		if (blank.compareEqual(vcDocument)) {
			return false;
		}
		
		blank = new Geometry(vcDocument.getName(), 2);
		if (blank.compareEqual(vcDocument)) {
			return false;
		}
		
		blank = new Geometry(vcDocument.getName(), 3);
		if (blank.compareEqual(vcDocument)) {
			return false;
		}
		return true;
	} 
}

/**
 * Insert the method's description here.
 * Creation date: (6/16/2004 11:17:18 AM)
 */
private boolean closeAllWindows(boolean duringExit) {
	Enumeration<TopLevelWindowManager> en = getMdiManager().getWindowManagers();
	while (en.hasMoreElements()) {
		TopLevelWindowManager windowManager = (TopLevelWindowManager)en.nextElement();
		boolean closed = closeWindow(windowManager.getManagerID(), duringExit);
		if (!closed) {
			// user canceled, don't keep going...
			return false;
		}
	}
	return true;
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/2005 1:48:09 PM)
 * @param windowID java.lang.String
 */
public boolean closeWindow(java.lang.String windowID, boolean exitIfLast) {
	// called from DocumentWindow or from DatabaseWindow
	TopLevelWindowManager windowManager = getMdiManager().getWindowManager(windowID);
	if (windowManager instanceof DocumentWindowManager) {
		// we'll need to run some checks first
		getMdiManager().showWindow(windowID);
		getMdiManager().blockWindow(windowID);
		if (checkBeforeClosing((DocumentWindowManager)windowManager)) {
			int openWindows = getMdiManager().closeWindow(windowID);
			if (exitIfLast && (openWindows == 0)) {
				setBExiting(true);
				exitApplication();
			}
			return true;
		} else {
			// user canceled
			getMdiManager().unBlockWindow(windowID);
			return false;
		}
	} else if (windowManager instanceof DatabaseWindowManager) {
		// nothing to check here, just close it
		int openWindows = getMdiManager().closeWindow(windowID);
		if (exitIfLast && (openWindows == 0)) {
			setBExiting(true);
			exitApplication();
		}
		return true;
	} else if (windowManager instanceof TestingFrameworkWindowManager) {
		// nothing to check here, just close it
		int openWindows = getMdiManager().closeWindow(windowID);
		if (exitIfLast && (openWindows == 0)) {
			setBExiting(true);
			exitApplication();
		}
		return true;
	} else if (windowManager instanceof BNGWindowManager) {
		// nothing to check here, just close it
		int openWindows = getMdiManager().closeWindow(windowID);
		if (exitIfLast && (openWindows == 0)) {
			setBExiting(true);
			exitApplication();
		}
		return true;
	} else if (windowManager instanceof FieldDataWindowManager) {
		// nothing to check here, just close it
		int openWindows = getMdiManager().closeWindow(windowID);
		if (exitIfLast && (openWindows == 0)) {
			setBExiting(true);
			exitApplication();
		}
		return true;
	} else {
		return false;
	}
}


private XmlTreeDiff compareDocuments(final VCDocument doc1, final VCDocument doc2, String comparisonSetting) throws Exception {

	VCellThreadChecker.checkCpuIntensiveInvocation();
	
	if (!TMLPanel.COMPARE_DOCS_SAVED.equals(comparisonSetting) && 
		!TMLPanel.COMPARE_DOCS_OTHER.equals(comparisonSetting)) {
		throw new RuntimeException("Unsupported comparison setting: " + comparisonSetting);
	} 
	if (doc1.getDocumentType() != doc2.getDocumentType()) { 
		throw new RuntimeException("Only comparison of documents of the same type is currently supported");
	}
	String doc1XML = null;
	String doc2XML = null;
	switch (doc1.getDocumentType()) {
		case VCDocument.BIOMODEL_DOC: {
			doc1XML = XmlHelper.bioModelToXML((BioModel)doc1);
			doc2XML = XmlHelper.bioModelToXML((BioModel)doc2);
			break;			 
		}
		case VCDocument.MATHMODEL_DOC: {
			doc1XML = XmlHelper.mathModelToXML((MathModel)doc1);
			doc2XML = XmlHelper.mathModelToXML((MathModel)doc2);
			break;			
		}
		case VCDocument.GEOMETRY_DOC: {
			doc1XML = XmlHelper.geometryToXML((Geometry)doc1);
			doc2XML = XmlHelper.geometryToXML((Geometry)doc2);
			break;			
		}
	}
	final XmlTreeDiff diffTree = XmlHelper.compareMerge(doc1XML, doc2XML, comparisonSetting, true);
	return diffTree;
}


/**
 Processes the model comparison request.
 * Creation date: (6/9/2004 1:07:09 PM)
 * @param vcDocument cbit.vcell.document.VCDocument
 */
public XmlTreeDiff compareWithOther(final VCDocumentInfo docInfo1, final VCDocumentInfo docInfo2) {

	VCDocument document1, document2;

	try {
		// get the VCDocument versions from documentManager
		if (docInfo1 instanceof BioModelInfo && docInfo2 instanceof BioModelInfo) {
			document1 = getDocumentManager().getBioModel((BioModelInfo)docInfo1);
			document2 = getDocumentManager().getBioModel((BioModelInfo)docInfo2);					
		} else if (docInfo1 instanceof MathModelInfo && docInfo2 instanceof MathModelInfo) {
			document1 = getDocumentManager().getMathModel((MathModelInfo)docInfo1);
			document2 = getDocumentManager().getMathModel((MathModelInfo)docInfo2);					
		} else if (docInfo1 instanceof GeometryInfo && docInfo2 instanceof GeometryInfo) {
			document1 = getDocumentManager().getGeometry((GeometryInfo)docInfo1);
			document2 = getDocumentManager().getGeometry((GeometryInfo)docInfo2);					
		} else {
			throw new IllegalArgumentException("The two documents are invalid or of different types.");
		}
		return compareDocuments(document1, document2, TMLPanel.COMPARE_DOCS_OTHER);
	} catch (Exception e) {
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	}
}


/**
   Processes the comparison (XML based) of the loaded model with its saved edition.
 * Creation date: (6/9/2004 1:07:09 PM)
 * @param vcDocument cbit.vcell.document.VCDocument
 */
public XmlTreeDiff compareWithSaved(VCDocument document) {
	
	VCDocument savedVersion = null;
	try {
		if (document == null) {
			throw new IllegalArgumentException("Invalid VC document: " + document);
		}
		// make the info and get saved version
		switch (document.getDocumentType()) {
			case VCDocument.BIOMODEL_DOC: {
				BioModel bioModel = (BioModel)document;
				savedVersion = getDocumentManager().getBioModel(bioModel.getVersion().getVersionKey());
				break;
			}
			case VCDocument.MATHMODEL_DOC: {
				MathModel mathModel = (MathModel)document;
				savedVersion = getDocumentManager().getMathModel(mathModel.getVersion().getVersionKey());
				break;
			}
			case VCDocument.GEOMETRY_DOC: {
				Geometry geometry = (Geometry)document;
				savedVersion = getDocumentManager().getGeometry(geometry.getKey());
				break;
			}
		}
		return compareDocuments(savedVersion, document, TMLPanel.COMPARE_DOCS_SAVED);
	} catch (Exception e) {
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	}				
}


/**
 * Insert the method's description here.
 * Creation date: (6/16/2004 11:07:33 AM)
 * @param clientServerInfo cbit.vcell.client.server.ClientServerInfo
 */
public void connectAs(final String user,  final String password, TopLevelWindowManager requester) {
	String confirm = PopupGenerator.showWarningDialog(requester, getUserPreferences(), UserMessage.warn_changeUser,null);
	if (confirm.equals(UserMessage.OPTION_CANCEL)){
		return;
	}
	else {
		boolean closedAllWindows = closeAllWindows(false);
		if (! closedAllWindows) {
			// user bailed out on closing some window
			return;
		} else {
			// ok, connect as a different user
			// asynch & nothing to do on Swing queue (updates handled by events)
			String taskName = "Connecting as " + user;
			AsynchClientTask[] newTasks = newDocument(new VCDocument.DocumentCreationInfo(VCDocument.BIOMODEL_DOC, 0));
			AsynchClientTask task1 = new AsynchClientTask(taskName, AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
				@Override
				public void run(Hashtable<String, Object> hashTable) throws Exception {
					getClientServerManager().connectAs(user, password);
				}
			};
			AsynchClientTask task2 = new AsynchClientTask(taskName, AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
				@Override
				public void run(Hashtable<String, Object> hashTable) throws Exception {
					getMdiManager().refreshRecyclableWindows();
				}
			};
			AsynchClientTask[] taskArray = new AsynchClientTask[newTasks.length + 2];
			System.arraycopy(newTasks, 0, taskArray, 0, newTasks.length);
			taskArray[newTasks.length] = task1;
			taskArray[newTasks.length + 1] = task2;
			
			Hashtable<String, Object> hash = new Hashtable<String, Object>();
			hash.put("requestManager", this);
			ClientTaskDispatcher.dispatch(requester.getComponent(), new Hashtable<String, Object>(), taskArray, false, false, null);
		}
	}
}


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:18:16 AM)
 * @param clientServerInfo cbit.vcell.client.server.ClientServerInfo
 */
public void connectToServer(final ClientServerInfo clientServerInfo) {
	getClientServerManager().connect(clientServerInfo);
}


/**
 * Insert the method's description here.
 * Creation date: (5/10/2004 3:48:16 PM)
 */
VCDocument createDefaultDocument(int docType) {
	VCDocument defaultDocument = null;
	try {
		switch (docType) {
			case VCDocument.BIOMODEL_DOC: {
				// blank			
				return createDefaultBioModelDocument();
			}
			case VCDocument.MATHMODEL_DOC: {			
				return createDefaultMathModelDocument();
			}
			default: {
				throw new RuntimeException("default document can only be BioModel or MathModel");
			}
		}
	} catch (Exception e) {
		// ignore, does not happen on defaults
	}
	return defaultDocument;
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/2004 12:22:11 PM)
 * @param windowID java.lang.String
 */
private MathModel createMathModel(String name, Geometry geometry) {
	MathModel mathModel = new MathModel(null);
	MathDescription mathDesc = mathModel.getMathDescription();
	try {
		mathDesc.setGeometry(geometry);
		if (geometry.getDimension()==0){
			mathDesc.addSubDomain(new CompartmentSubDomain("Compartment",CompartmentSubDomain.NON_SPATIAL_PRIORITY));
		}else{
			try {
				if (geometry.getDimension()>0 && geometry.getGeometrySurfaceDescription().getGeometricRegions() == null){
					geometry.getGeometrySurfaceDescription().updateAll();
				}
			}catch (ImageException e){
				e.printStackTrace(System.out);
				throw new RuntimeException("Geometric surface generation error: \n"+e.getMessage());
			}catch (GeometryException e){
				e.printStackTrace(System.out);
				throw new RuntimeException("Geometric surface generation error: \n"+e.getMessage());
			}

			SubVolume subVolumes[] = geometry.getGeometrySpec().getSubVolumes();
			for (int i=0;i<subVolumes.length;i++){
				mathDesc.addSubDomain(new CompartmentSubDomain(subVolumes[i].getName(),subVolumes[i].getHandle()));
			}
			//
			// add only those MembraneSubDomains corresponding to surfaces that acutally exist in geometry.
			//
			GeometricRegion regions[] = geometry.getGeometrySurfaceDescription().getGeometricRegions();
			for (int i = 0; i < regions.length; i++){
				if (regions[i] instanceof SurfaceGeometricRegion){
					SurfaceGeometricRegion surfaceRegion = (SurfaceGeometricRegion)regions[i];
					CompartmentSubDomain compartment1 = mathDesc.getCompartmentSubDomain(((VolumeGeometricRegion)surfaceRegion.getAdjacentGeometricRegions()[0]).getSubVolume().getName());
					CompartmentSubDomain compartment2 = mathDesc.getCompartmentSubDomain(((VolumeGeometricRegion)surfaceRegion.getAdjacentGeometricRegions()[1]).getSubVolume().getName());
					MembraneSubDomain membraneSubDomain = mathDesc.getMembraneSubDomain(compartment1,compartment2);
					if (membraneSubDomain==null){
						membraneSubDomain = new MembraneSubDomain(compartment1,compartment2);
						mathDesc.addSubDomain(membraneSubDomain);
					}
				}
			}
		}
		mathDesc.isValid();
		mathModel.setName(name);
	} catch (Exception e) {
		e.printStackTrace(System.out);
	}
	return mathModel;
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/2004 12:22:11 PM)
 * @param windowID java.lang.String
 */
public void createMathModelFromApplication(final String name, final SimulationContext simContext) {
	if (simContext == null) {
		PopupGenerator.showErrorDialog("Selected Application is null, cannot generate corresponding math model");
		return;
	}
	AsynchClientTask task1 = new AsynchClientTask("Creating MathModel from BioModel Applicaiton", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			MathModel newMathModel = new MathModel(null);
			//Get corresponding mathDesc to create new mathModel.
			MathDescription mathDesc = simContext.getMathDescription();
			MathDescription newMathDesc = null;
			newMathDesc = new MathDescription(name+"_"+(new java.util.Random()).nextInt());
			try {
				if (mathDesc.getGeometry().getDimension()>0 && mathDesc.getGeometry().getGeometrySurfaceDescription().getGeometricRegions() == null){
					mathDesc.getGeometry().getGeometrySurfaceDescription().updateAll();
				}

			}catch (cbit.image.ImageException e){
				e.printStackTrace(System.out);
				throw new RuntimeException("Geometric surface generation error:\n"+e.getMessage());
			}catch (cbit.vcell.geometry.GeometryException e){
				e.printStackTrace(System.out);
				throw new RuntimeException("Geometric surface generation error:\n"+e.getMessage());
			}
			newMathDesc.setGeometry(mathDesc.getGeometry());
			newMathDesc.read_database(new CommentStringTokenizer(mathDesc.getVCML_database()));
			newMathDesc.isValid();

			newMathModel.setName(name);
			newMathModel.setMathDescription(newMathDesc);
			hashTable.put("newMathModel", newMathModel);
		}		
	};
	
	AsynchClientTask task2 = new AsynchClientTask("Creating MathModel from BioModel Applicaiton", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			MathModel newMathModel = (MathModel)hashTable.get("newMathModel");
			DocumentWindowManager windowManager = createDocumentWindowManager(newMathModel);
			if(simContext.getBioModel().getVersion() != null){
				((MathModelWindowManager)windowManager). setCopyFromBioModelAppVersionableTypeVersion(
							new VersionableTypeVersion(VersionableType.BioModelMetaData, simContext.getBioModel().getVersion()));
			}
			getMdiManager().createNewDocumentWindow(windowManager);
		}
	};
	ClientTaskDispatcher.dispatch(null, new Hashtable<String, Object>(),  new AsynchClientTask[]{task1, task2}, false);
}

private BioModel createDefaultBioModelDocument() throws Exception {
	BioModel bioModel = new BioModel(null);
	bioModel.setName("BioModel" + (getMdiManager().getNewlyCreatedDesktops() + 1));
	bioModel.getModel().addFeature("Unnamed compartment", null, "Unnamed membrane");
	return bioModel;
}

private MathModel createDefaultMathModelDocument() throws Exception {
	Geometry geometry = new Geometry("Untitled", 0);
	MathModel mathModel = createMathModel("Untitled", geometry);
	mathModel.setName("MathModel" + (getMdiManager().getNewlyCreatedDesktops() + 1));
	return mathModel;
}

/**
 * Insert the method's description here.
 * Creation date: (5/10/2004 3:48:16 PM)
 */
private AsynchClientTask[] createNewDocument(final VCDocument.DocumentCreationInfo documentCreationInfo) {//throws UserCancelException, Exception {
	/* asynchronous and not blocking any window */
	AsynchClientTask[] taskArray =  null;

	final int createOption = documentCreationInfo.getOption();
	switch (documentCreationInfo.getDocumentType()) {
		case VCDocument.BIOMODEL_DOC: {		
			AsynchClientTask task1 = new AsynchClientTask("creating biomodel", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
				@Override
				public void run(Hashtable<String, Object> hashTable) throws Exception {
					BioModel bioModel = new BioModel(null);
					bioModel.setName("BioModel" + (getMdiManager().getNewlyCreatedDesktops() + 1));
					bioModel.getModel().addFeature("Unnamed compartment", null, "Unnamed membrane");
					hashTable.put("doc", bioModel);
				}			
			};
			taskArray = new AsynchClientTask[] {task1};
			break;
		}
		case VCDocument.MATHMODEL_DOC: {
			if ((createOption == VCDocument.MATH_OPTION_NONSPATIAL) || (createOption == VCDocument.MATH_OPTION_SPATIAL)) {
				AsynchClientTask task1 = new AsynchClientTask("asking for geometry", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {		
						// spatial or non-spatial
						if (createOption == VCDocument.MATH_OPTION_SPATIAL) {
							GeometryInfo geometryInfo = (GeometryInfo)getMdiManager().getDatabaseWindowManager().selectDocument(VCDocument.GEOMETRY_DOC, getMdiManager().getDatabaseWindowManager());
							hashTable.put("geometryInfo", geometryInfo);
						}
					}
				};
				AsynchClientTask task2 = new AsynchClientTask("creating mathmodel", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {
						Geometry geometry = null;
						if (createOption == VCDocument.MATH_OPTION_NONSPATIAL) {
							geometry = new Geometry("Untitled", 0);
						} else {
							GeometryInfo geometryInfo = (GeometryInfo)hashTable.get("geometryInfo");
							geometry = (Geometry)getDocumentManager().getGeometry(geometryInfo);
						}
						MathModel mathModel = createMathModel("Untitled", geometry);
						mathModel.setName("MathModel" + (getMdiManager().getNewlyCreatedDesktops() + 1));
						hashTable.put("doc", mathModel);
					}
				};
				taskArray = new AsynchClientTask[] {task1, task2};
				break;
			} else if (createOption == VCDocument.MATH_OPTION_FROMBIOMODELAPP){
				AsynchClientTask task1 = new AsynchClientTask("select biomodel application", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {		
						// spatial or non-spatial
						BioModelInfo bioModelInfo = getMdiManager().getDatabaseWindowManager().selectBioModelInfo();
						if (bioModelInfo != null) { // may throw UserCancelException
							hashTable.put("bioModelInfo", bioModelInfo);
						}
					}
				};
				AsynchClientTask task2 = new AsynchClientTask("create math model from biomodel application", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {		
						// spatial or non-spatial
						// Get the simContexts in the corresponding BioModel 
						BioModelInfo bioModelInfo = (BioModelInfo)hashTable.get("bioModelInfo");						
						SimulationContext[] simContexts = getDocumentManager().getBioModel(bioModelInfo).getSimulationContexts();
						if (simContexts != null) { // may throw UserCancelException
							hashTable.put("simContexts", simContexts);
						}
					}
				};
				AsynchClientTask task3 = new AsynchClientTask("create math model from biomodel application", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {								
						SimulationContext[] simContexts = (SimulationContext[])hashTable.get("simContexts");
						String[] simContextNames = new String[simContexts.length];
						
						if (simContextNames.length == 0) {
							throw new RuntimeException("no application is available");
						} else {
							for (int i = 0; i < simContexts.length; i++){
								simContextNames[i] = simContexts[i].getName();
							}
							// Get the simContext names, so that user can choose which simContext math to import
							String simContextChoice = (String)PopupGenerator.showListDialog(getMdiManager().getDatabaseWindowManager().getComponent(), simContextNames, "Please select Application");
							if (simContextChoice == null) {
								throw UserCancelException.CANCEL_DB_SELECTION;
							}
							SimulationContext chosenSimContext = null;
							for (int i = 0; i < simContexts.length; i++){
								if (simContexts[i].getName().equals(simContextChoice)) {
									chosenSimContext = simContexts[i];
									break;
								}
							}
							BioModelInfo bioModelInfo = (BioModelInfo)hashTable.get("bioModelInfo");
							//Get corresponding mathDesc to create new mathModel and return.
							String newName = bioModelInfo.getVersion().getName()+"_"+chosenSimContext.getName();
							MathDescription bioMathDesc = chosenSimContext.getMathDescription();
							MathDescription newMathDesc = null;
							newMathDesc = new MathDescription(newName+"_"+(new Random()).nextInt());

							newMathDesc.setGeometry(bioMathDesc.getGeometry());
							newMathDesc.read_database(new CommentStringTokenizer(bioMathDesc.getVCML_database()));
							newMathDesc.isValid();							
							
							MathModel newMathModel = new MathModel(null);
							newMathModel.setName(newName);
							newMathModel.setMathDescription(newMathDesc);
							hashTable.put("doc", newMathModel);
						}						
					}
				};
				taskArray = new AsynchClientTask[] {task1, task2, task3};
				break;
			} else {
				throw new RuntimeException("Unknown MathModel Document creation option value="+documentCreationInfo.getOption());
			}			
		}
		case VCDocument.GEOMETRY_DOC: {
			if (createOption == VCDocument.GEOM_OPTION_1D ||
					createOption == VCDocument.GEOM_OPTION_2D ||
					createOption == VCDocument.GEOM_OPTION_3D) {
				// analytic
				AsynchClientTask task1 = new AsynchClientTask("creating analytic geometry", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {
						Geometry geometry = new Geometry("Geometry" + (getMdiManager().getNewlyCreatedDesktops() + 1), documentCreationInfo.getOption());
						geometry.getGeometrySpec().addSubVolume(new AnalyticSubVolume("subdomain0",new cbit.vcell.parser.Expression(1.0)));					
						hashTable.put("doc", geometry);
					}
				};
				taskArray = new AsynchClientTask[] {task1};
				break;
			} else  {
				// image-based
				AsynchClientTask saveImage = new AsynchClientTask("saving image", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {
						RequestManager theRequestManager = (RequestManager)hashTable.get("requestManager");
						VCImage image = (VCImage)hashTable.get("vcImage");
						String newName = (String)hashTable.get("newName");
						
						VCImage editedImage = theRequestManager.getDocumentManager().saveAsNew(image,newName);					
				
						getClientTaskStatusSupport().setMessage("finished saving " + newName);
						//
						// check that save actually occured (it should have since an insert should be new)
						//
						Version newVersion = editedImage.getVersion();
						Version oldVersion = image.getVersion();
						if ((oldVersion != null) && newVersion.getVersionKey().compareEqual(oldVersion.getVersionKey())){
							throw new DataAccessException("Save New Image failed, Old version has same id as New");
						}
						hashTable.put("image", editedImage);
					}
				};	
				
				AsynchClientTask createGeometry =  new AsynchClientTask("creating geometry", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {
						VCImage image = (VCImage)hashTable.get("image");
						Geometry newGeom = new Geometry("Untitled", image);
						newGeom.setDescription(image.getDescription());
						hashTable.put("doc", newGeom);
					}
				};
				if (documentCreationInfo.getOption() == VCDocument.GEOM_OPTION_DBIMAGE) {
					// Get image from database
					AsynchClientTask task1 = new AsynchClientTask("select from database", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
						@Override
						public void run(Hashtable<String, Object> hashTable) throws Exception {
							Object option = getMdiManager().getDatabaseWindowManager().showImageSelectorDialog();
							hashTable.put("selectOption", option);
						}
					};
					AsynchClientTask task2 = new AsynchClientTask("creating geometry from database image", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
						@Override
						public void run(Hashtable<String, Object> hashTable) throws Exception {
							Object choice = hashTable.get("selectOption");
							if (choice != null && choice.equals("OK")) {
								VCImage image = getMdiManager().getDatabaseWindowManager().selectImageFromDatabase();
								if (image == null){
									throw new RuntimeException("failed to create new Geometry, no image");
								}
								hashTable.put("image", image);
							} else {
								throw UserCancelException.CANCEL_DB_SELECTION;
							}
						}
					};
					taskArray = new AsynchClientTask[] {task1, task2, createGeometry};
					break;
				} else if(documentCreationInfo.getOption() == VCDocument.GEOM_OPTION_FILE) {
					// Get image from file --- INCOMPLETE
					AsynchClientTask task1 = new AsynchClientTask("creating geometry from file", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
						@Override
						public void run(Hashtable<String, Object> hashTable) throws Exception {
							File imageFile = DatabaseWindowManager.showFileChooserDialog(FileFilters.FILE_FILTER_FIELDIMAGES, getUserPreferences());
							hashTable.put("imageFile", imageFile);
						}
					};
					taskArray = new AsynchClientTask[] {task1, new SelectImageFromFile(), new EditImageAttributes(), saveImage, createGeometry};
					break;
				} else if (documentCreationInfo.getOption() == VCDocument.GEOM_OPTION_FIELDDATA){
					AsynchClientTask task1 = new AsynchClientTask("retrieving data", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

						@Override
						public void run(Hashtable<String, Object> hashTable) throws Exception {
							VCDocument.GeomFromFieldDataCreationInfo docInfo = (VCDocument.GeomFromFieldDataCreationInfo)documentCreationInfo;
							PDEDataContext pdeDataContext =	getMdiManager().getFieldDataWindowManager().getPDEDataContext(docInfo.getExternalDataID());
							pdeDataContext.setVariableAndTime(docInfo.getVarName(), pdeDataContext.getTimePoints()[docInfo.getTimeIndex()]);
							double[] data = pdeDataContext.getDataValues();
							CartesianMesh mesh = pdeDataContext.getCartesianMesh();
							byte[] segmentedData = new byte[data.length];
							Vector<Double> distinctValues = new Vector<Double>();
							int index = -1;
							int MAX_NUMBER_OF_COLORS = 256;
							boolean bTooManyColors = false;
							for (int i = 0; i < data.length; i++) {
								if((index = distinctValues.indexOf(data[i])) == -1){
									index = distinctValues.size();
									distinctValues.add(data[i]);
									if(distinctValues.size() > MAX_NUMBER_OF_COLORS){
										bTooManyColors = true;
										break;
									}
								}
								segmentedData[i] = (byte)index;
							}
							
							double minValue = Double.POSITIVE_INFINITY;
							double maxValue = Double.NEGATIVE_INFINITY;
							for (int i = 0; i < data.length; i++) {
								minValue = Math.min(minValue,data[i]);
								maxValue = Math.max(maxValue,data[i]);
							}
							short[] dataToSegment = new short[data.length];
							for (int i = 0; i < data.length; i++) {
								dataToSegment[i] = (short)((data[i]-minValue)/(maxValue-minValue)*30000);
							}
							hashTable.put("dataToSegment", dataToSegment);
							hashTable.put("mesh", mesh);
							if (!bTooManyColors) {
								VCImage initImage = new VCImageUncompressed(null,segmentedData, mesh.getExtent(), mesh.getSizeX(), mesh.getSizeY(), mesh.getSizeZ());
								hashTable.put("vcImage", initImage);
							}
						}
					};
					
					AsynchClientTask segmentTask = new AsynchClientTask("segmenting data", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {

						@Override
						public void run(Hashtable<String, Object> hashTable) throws Exception {
							short[] dataToSegment = (short[])hashTable.get("dataToSegment");
							CartesianMesh mesh = (CartesianMesh)hashTable.get("mesh");
							if (dataToSegment==null){
								return;
							}
							if (mesh==null){
								throw new Exception("mesh not found");
							}

							Component parent = (Component)hashTable.get("parent");
							if (hashTable.get("vcImage") != null) {	
								String segment = "Proceed with Segmentation";
								String nosegment = "Proceed without Segmentation";
								String cancel = "Cancel";
								String choice = PopupGenerator.showWarningDialog(parent, "The image contains multiple colors. Segmentation is recommended.", 
										new String[] {segment, nosegment, cancel}, segment);							
								if (choice.equals(cancel)) {
									throw UserCancelException.CANCEL_GENERIC;
								} else if (choice.equals(nosegment)) {
									return;
								}
							} else {
								PopupGenerator.showInfoDialog("The image contains multiple colors. Segmentation is required.");
							}
							OverlayEditorPanelJAI overlayEditorPanelJAI = new OverlayEditorPanelJAI();
							overlayEditorPanelJAI.setUndoableEditSupport(new UndoableEditSupport());
							overlayEditorPanelJAI.setROITimePlotVisible(false);
							overlayEditorPanelJAI.setAllowAddROI(false);
							UShortImage usImage = new UShortImage(dataToSegment,mesh.getOrigin(),mesh.getExtent(),mesh.getSizeX(),mesh.getSizeY(),mesh.getSizeZ());
							ImageDataset imageDataset = new ImageDataset(new UShortImage[] { usImage }, new double[] { 0.0 }, mesh.getSizeZ());
							overlayEditorPanelJAI.setImages(imageDataset, true, OverlayEditorPanelJAI.DEFAULT_SCALE_FACTOR, OverlayEditorPanelJAI.DEFAULT_OFFSET_FACTOR);
							overlayEditorPanelJAI.setROITimePlotVisible(false);
							UShortImage roiImage = new UShortImage(new short[dataToSegment.length],mesh.getOrigin(),mesh.getExtent(),mesh.getSizeX(),mesh.getSizeY(),mesh.getSizeZ());
							ROI roi = new ROI(roiImage,ROISourceData.VFRAP_ROI_ENUM.ROI_CELL.name());
							overlayEditorPanelJAI.addROIName(ROISourceData.VFRAP_ROI_ENUM.ROI_CELL.name(),true,ROISourceData.VFRAP_ROI_ENUM.ROI_CELL.name());
							overlayEditorPanelJAI.setROI(roi);
							
							int retCode = DialogUtils.showComponentOKCancelDialog(parent, overlayEditorPanelJAI, "segment cell image for geometry");
							if (retCode == JOptionPane.OK_OPTION){
								overlayEditorPanelJAI.saveUserChangesToROI();
								roi = overlayEditorPanelJAI.getROI();
								short[] roiData = roi.getPixelsXYZ();
								byte[] segmentedData = new byte[roiData.length];
								Vector<Short> distinctValues = new Vector<Short>();
								for (int i = 0; i < roiData.length; i++) {
									int index = -1;
									if((index = distinctValues.indexOf(roiData[i])) == -1){
										index = distinctValues.size();
										distinctValues.add(roiData[i]);
										if(distinctValues.size() > 2){
											throw new Exception("expecting 2 colors ... background and ROI");
										}
									}
									segmentedData[i] = (byte)index;
								}
								VCImage initImage = new VCImageUncompressed(null,segmentedData, mesh.getExtent(), mesh.getSizeX(), mesh.getSizeY(), mesh.getSizeZ());
								hashTable.put("vcImage", initImage);
							} else{
								throw UserCancelException.CANCEL_GENERIC;
							}
						}
					};
					
					taskArray = new AsynchClientTask[] {task1, segmentTask, new EditImageAttributes(), saveImage, createGeometry};
					break;
				} else{
					throw new RuntimeException("Unknown Geometry Document creation option value="+documentCreationInfo.getOption());
				}
			}
		}
		default: {
			throw new RuntimeException("Unknown default document type: " + documentCreationInfo.getDocumentType());
		}
	}
	return taskArray;
}


/**
 * Insert the method's description here.
 * Creation date: (6/22/2004 10:50:34 PM)
 * @param documentInfo cbit.vcell.document.VCDocumentInfo
 */
public void curateDocument(final VCDocumentInfo documentInfo, final int curateType, final TopLevelWindowManager requester) {

	if (documentInfo != null) {
		// see if we have this open
		String documentID = documentInfo.getVersion().getVersionKey().toString();
		if (getMdiManager().haveWindow(documentID)) {
			// already open, refuse
			PopupGenerator.showErrorDialog(requester, "Selected edition is open, cannot "+CurateSpec.CURATE_TYPE_NAMES[curateType]);
			return;
		} else {
			// don't have it open, try to CURATE it
			int confirm = PopupGenerator.showComponentOKCancelDialog(
				requester.getComponent(),
				new JTextArea(CurateSpec.CURATE_TYPE_ACTIONS[curateType]+" cannot be undone without VCELL administrative assistance.\n"+
					CurateSpec.CURATE_TYPE_STATES[curateType]+" versions of documents cannot be deleted without VCELL administrative assistance.\n"+
					(curateType == CurateSpec.PUBLISH?CurateSpec.CURATE_TYPE_STATES[curateType]+" versions of documents MUST remain publically accessible to other VCELL users.\n":"")+
					"Do you want to "+CurateSpec.CURATE_TYPE_NAMES[curateType]+" document '"+documentInfo.getVersion().getName()+"'"+
					"\nwith version date '"+documentInfo.getVersion().getDate().toString()+"'?")
				,"WARNING -- "+CurateSpec.CURATE_TYPE_ACTIONS[curateType]+" operation cannot be undone");
			if (confirm == JOptionPane.OK_OPTION){
				AsynchClientTask task1 = new AsynchClientTask(CurateSpec.CURATE_TYPE_ACTIONS[curateType]+" document...", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {
						if (documentInfo instanceof BioModelInfo) {
							getDocumentManager().curate(new CurateSpec((BioModelInfo)documentInfo,curateType));
						} else if (documentInfo instanceof MathModelInfo) {
							getDocumentManager().curate(new CurateSpec((MathModelInfo)documentInfo,curateType));
						} else {
							throw new RuntimeException(CurateSpec.CURATE_TYPE_ACTIONS[curateType]+" not supported for VCDocumentInfo type "+documentInfo.getClass().getName());
						}
					}
				};
				ClientTaskDispatcher.dispatch(requester.getComponent(), new Hashtable<String, Object>(), new AsynchClientTask[] {task1}, false);
			} else {
				// user canceled
				return;
			}
		}
	} else {
		// nothing selected
		return;
	}
}


public void updateUserRegistration(final boolean bNewUser){
	try {
		UserRegistrationOP.registrationOperationGUI(this, 
				getClientServerManager().getClientServerInfo(),
				(bNewUser?LoginDialog.USERACTION_REGISTER:LoginDialog.USERACTION_EDITINFO),
				(bNewUser?null:getClientServerManager()));
	} catch (UserCancelException e) {
		return;
	} catch (Exception e) {
		e.printStackTrace();
		PopupGenerator.showErrorDialog((bNewUser?"Create new":"Update")+" user Registration error:\n"+e.getMessage());
		return;
	}
}

public void sendLostPassword(final String userid){
	try {
		UserRegistrationOP.registrationOperationGUI(this, 
				VCellClient.createClientServerInfo(
					getClientServerManager().getClientServerInfo(), userid, null),
				LoginDialog.USERACTION_LOSTPASSWORD,
				null);
	} catch (Exception e) {
		e.printStackTrace();
		PopupGenerator.showErrorDialog("Update user Registration error:\n"+e.getMessage());
	}
}
/**
 * Insert the method's description here.
 * Creation date: (6/22/2004 10:50:34 PM)
 * @param documentInfo cbit.vcell.document.VCDocumentInfo
 */
public void deleteDocument(final VCDocumentInfo documentInfo, final TopLevelWindowManager requester) {
	if (documentInfo != null) {
		// see if we have this open
		String documentID = documentInfo.getVersion().getVersionKey().toString();
		if (getMdiManager().haveWindow(documentID)) {
			// already open, refuse
			PopupGenerator.showErrorDialog(requester, "Selected edition is open, cannot delete");
			return;
		} else {
			// don't have it open, try to delete it
			String confirm = PopupGenerator.showWarningDialog(requester, getUserPreferences(), UserMessage.warn_deleteDocument,documentInfo.getVersion().getName());
			if (confirm.equals(UserMessage.OPTION_DELETE)){
				AsynchClientTask task1 = new AsynchClientTask("Deleting document...", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

					@Override
					public void run(Hashtable<String, Object> hashTable) throws Exception {
						if (documentInfo instanceof BioModelInfo) {
							getDocumentManager().delete((BioModelInfo)documentInfo);
						} else if (documentInfo instanceof MathModelInfo) {
							getDocumentManager().delete((MathModelInfo)documentInfo);
						} else if (documentInfo instanceof GeometryInfo) {
							getDocumentManager().delete((GeometryInfo)documentInfo);
						} else {
							throw new RuntimeException("delete not supported for VCDocumentInfo type "+documentInfo.getClass().getName());
						}
					}
				};
				ClientTaskDispatcher.dispatch(requester.getComponent(), new Hashtable<String, Object>(), new AsynchClientTask[] {task1}, false);
			} else {
				// user canceled
				return;
			}
		}
	} else {
		// nothing selected
		return;
	}
}


/**
 * Comment
 */
protected void downloadExportedData(final ExportEvent evt) {
	URL location = null;
	try {
		location = new URL(evt.getLocation());
	} catch (java.net.MalformedURLException exc) {
		exc.printStackTrace(System.out);
		PopupGenerator.showErrorDialog((Component)null, "Reported file location does not seem to be a valid URL\n"+exc.getMessage());
		return;
	}
	final URL url = location;
	AsynchClientTask task1 = new AsynchClientTask("Retrieving data from "+url, AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			// get it
		    URLConnection connection = url.openConnection();
		    byte[] bytes = new byte[connection.getContentLength()];
		    java.io.InputStream is = connection.getInputStream();
		    int bytesRead = 0;
		    int offset = 0;
		    while (bytesRead >= 0 && offset<(bytes.length-1)) {
		        //System.out.println("offset: " + offset + ", bytesRead: " + bytesRead);
		        bytesRead = is.read(bytes, offset, bytes.length - offset);
		        offset += bytesRead;
		    }
		    is.close();
		    hashTable.put("bytes", bytes);
		}		
	};
	AsynchClientTask task2 = new AsynchClientTask("selecting file to save", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {

		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			String defaultPath = getUserPreferences().getGenPref(UserPreferences.GENERAL_LAST_PATH_USED);
			final VCFileChooser fileChooser = new VCFileChooser(defaultPath);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.addChoosableFileFilter(FileFilters.FILE_FILTER_ZIP);
			fileChooser.setFileFilter(FileFilters.FILE_FILTER_ZIP);
		    String name = evt.getVCDataIdentifier().getID();
		    String suffix = "_exported.zip";
		    File file = new File(name + suffix);
		    if (file.exists()) {
			    int count = 0;
			    do {
			    	file = new File(name + "_" + count + suffix);
			    } while (file.exists());
		    }

		    fileChooser.setSelectedFile(file);
			fileChooser.setDialogTitle("Save exported dataset...");
			int approve = fileChooser.showSaveDialog((Component)null);
			if (approve == JFileChooser.APPROVE_OPTION) {
				hashTable.put("selectedFile", fileChooser.getSelectedFile());
			} else {
				fileChooser.setSelectedFile(null);
			}
		}
	};
	AsynchClientTask task3 = new AsynchClientTask("saving to file", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {			
			File selectedFile = (File)hashTable.get("selectedFile");
			if (selectedFile == null) {
				return;
			}
			String defaultPath = getUserPreferences().getGenPref(UserPreferences.GENERAL_LAST_PATH_USED);
			String newPath = selectedFile.getParent();
	        if (!newPath.equals(defaultPath)) {
				getUserPreferences().setGenPref(UserPreferences.GENERAL_LAST_PATH_USED, newPath);
	        }
//	        System.out.println("New preferred file path: " + newPath + ", Old preferred file path: " + defaultPath);
			//
			if (selectedFile.exists()) {
				String question = PopupGenerator.showWarningDialog((Component)null, getUserPreferences(), UserMessage.warn_OverwriteFile,selectedFile.getAbsolutePath());
				if (question.equals(UserMessage.OPTION_CANCEL)){
					return;
				}
			}
			byte[] bytes = (byte[])hashTable.get("bytes");
            FileOutputStream fo = new FileOutputStream(selectedFile);
            fo.write(bytes);
            fo.close();
		}
	};
	ClientTaskDispatcher.dispatch(null, new Hashtable<String, Object>(), new AsynchClientTask[] {task1, task2, task3}, false);
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:20:47 AM)
 */
public void exitApplication() {
	if (!bExiting) {
		// close all windows - this will run checks
		boolean closedAllWindows = closeAllWindows(true);
		if (! closedAllWindows) {
			// user bailed out at some point, we're not gonna exit
			return;
		}
	}
	// ready to exit
	if (getVcellClient().isApplet()) {
		// can't just exit, since it will take down other applets and possibly the browser, try cleanup
		// if all end up being closed, we should take care of threads and object references so that the application exits
		
		/* more work needed here... */
		
		((ClientMDIManager)getMdiManager()).cleanup();
		getClientServerManager().cleanup();
		getVcellClient().getStatusUpdater().stop();
		System.gc();
	} else {
		// simply exit in this case
		System.exit(0);
	}
}


/**
 * Comment
 */
public void exportDocument(TopLevelWindowManager manager) {
	/* block window */
	JFrame currentWindow = getMdiManager().blockWindow(manager.getManagerID());
	/* prepare hashtable for tasks */
	Hashtable<String,Object> hash = new Hashtable<String,Object>();
	hash.put("mdiManager", getMdiManager());
	hash.put("documentManager", getDocumentManager());
	hash.put("topLevelWindowManager", manager);
	hash.put("currentWindow", currentWindow);
	hash.put("userPreferences", getUserPreferences());
	/* create tasks */
	// get document to be exported
	AsynchClientTask documentToExport = new DocumentToExport();
	// get file
	AsynchClientTask chooseFile = new ChooseFile();
	// export it
	AsynchClientTask exportDocument = new ExportToXML();
	// clean-up
	AsynchClientTask finishExport = new FinishExport();
	// assemble array
	AsynchClientTask[] tasks = null;
	tasks = new AsynchClientTask[] {
		documentToExport,
		chooseFile,
		exportDocument,
		finishExport
	};
	/* run tasks */
	ClientTaskDispatcher.dispatch(currentWindow, hash, tasks, false);
}


/**
 * Insert the method's description here.
 * Creation date: (1/18/2005 3:14:12 PM)
 * @param event cbit.rmi.event.ExportEvent
 */
public void exportMessage(cbit.rmi.event.ExportEvent event) {
	if (event.getEventTypeID() == cbit.rmi.event.ExportEvent.EXPORT_COMPLETE) {
		// update document manager
		//try {
			//((ClientDocumentManager)getRequestManager().getDocumentManager()).reloadExportLog(exportEvent.getVCDataIdentifier());
		//}catch (Throwable e){
			//e.printStackTrace(System.out);
		//}
		// try to download the thing
		downloadExportedData(event);
	}	
}


/**
 * Insert the method's description here.
 * Creation date: (6/9/2004 4:45:51 PM)
 * @return cbit.vcell.client.AsynchMessageManager
 */
public cbit.vcell.client.server.AsynchMessageManager getAsynchMessageManager() {
	return getClientServerManager().getAsynchMessageManager();
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:21:50 AM)
 */
private ClientServerManager getClientServerManager() {
	// shorthand
	return getVcellClient().getClientServerManager();
}

public ConnectionStatus getConnectionStatus(){
	return getClientServerManager().getConnectionStatus();
}
/**
 * Insert the method's description here.
 * Creation date: (6/11/2004 10:53:47 AM)
 * @return cbit.vcell.desktop.controls.DataManager
 * @param vcDataIdentifier cbit.vcell.server.VCDataIdentifier
 */
public DataManager getDataManager(VCDataIdentifier vcDataId, boolean isSpatial) throws DataAccessException {
	//
	// Create ODE or PDE or Merged Datamanager depending on ODE or PDE or Merged data.
	//
	DataManager dataManager = null;
	if (vcDataId instanceof cbit.vcell.simdata.MergedDataInfo) {
		dataManager = new MergedDataManager(getClientServerManager().getVCDataManager(), vcDataId);
	} else if (!isSpatial) {
		dataManager = new ODEDataManager(getClientServerManager().getVCDataManager(), vcDataId);
	} else {
		dataManager = new PDEDataManager(getClientServerManager().getVCDataManager(), vcDataId);
	}
	dataManager.connect();
	return dataManager;
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 9:57:39 AM)
 * @return cbit.vcell.clientdb.DocumentManager
 */
public cbit.vcell.clientdb.DocumentManager getDocumentManager() {
	// this should not be exposed here, but needs many changes outside project in order to live without it...
	// will eliminate when finishing up new client
	return getVcellClient().getClientServerManager().getDocumentManager();
}


/**
 * Insert the method's description here.
 * Creation date: (6/11/2004 10:53:47 AM)
 * @return cbit.vcell.desktop.controls.DataManager
 * @param vcDataIdentifier cbit.vcell.server.VCDataIdentifier
 */
public DynamicDataManager getDynamicDataManager(VCDataIdentifier vcdId) throws DataAccessException {
	if (vcdId instanceof cbit.vcell.simdata.MergedDataInfo) {
		return new MergedDynamicDataManager(getClientServerManager().getVCDataManager(), vcdId);
	} else {
		return null;
	}
}


/**
 * Insert the method's description here.
 * Creation date: (6/11/2004 10:53:47 AM)
 * @return cbit.vcell.desktop.controls.DataManager
 * @param vcDataIdentifier cbit.vcell.server.VCDataIdentifier
 */
public DynamicDataManager getDynamicDataManager(Simulation simulation) throws DataAccessException {
	return new SimulationDataManager(getClientServerManager().getVCDataManager(), simulation);
}


	//utility method
	private VCDocumentInfo getMatchingDocumentInfo(VCDocument vcDoc) throws DataAccessException {

		VCDocumentInfo vcDocInfo = null;
		
		switch (vcDoc.getDocumentType()) {
			case VCDocument.BIOMODEL_DOC: {
				BioModel bm = ((BioModel)vcDoc);
				vcDocInfo = getDocumentManager().getBioModelInfo(bm.getVersion().getVersionKey());
				break;			 
			}
			case VCDocument.MATHMODEL_DOC: {
				MathModel mm = ((MathModel)vcDoc);
				vcDocInfo = getDocumentManager().getMathModelInfo(mm.getVersion().getVersionKey());
				break;			
			}
			case VCDocument.GEOMETRY_DOC: {
				Geometry geom = ((Geometry)vcDoc);
				vcDocInfo = getDocumentManager().getGeometryInfo(geom.getKey());
				break;			
			}
			default: {
				throw new IllegalArgumentException("Invalid VC document: " + vcDoc.getDocumentType());
			}
		}

		return vcDocInfo;
	}

/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:21:50 AM)
 */
private MDIManager getMdiManager() {
	// shorthand
	return getVcellClient().getMdiManager();
}


/**
 * Insert the method's description here.
 * Creation date: (6/7/2004 1:27:25 PM)
 * @return cbit.vcell.solver.ode.gui.SimulationStatus
 * @param simulation cbit.vcell.solver.Simulation
 */
public SimulationStatus getServerSimulationStatus(SimulationInfo simInfo) {
	
	SimulationStatus simStatus = null;
	try {
		VCSimulationIdentifier vcSimulationIdentifier = simInfo.getAuthoritativeVCSimulationIdentifier();
		simStatus = getClientServerManager().getJobManager().getServerSimulationStatus(vcSimulationIdentifier);
	} catch (Throwable exc) {
		exc.printStackTrace(System.out);
	}
	return simStatus;
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:21:50 AM)
 */
public UserPreferences getUserPreferences() {
	return getVcellClient().getClientServerManager().getUserPreferences();
}

/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:21:50 AM)
 * @return cbit.vcell.client.VCellClient
 */
private VCellClient getVcellClient() {
	return vcellClient;
}


/**
 * Insert the method's description here.
 * Creation date: (8/26/2005 3:14:24 PM)
 * @return boolean
 */
public boolean isApplet() {
	return getVcellClient().isApplet();
}

/**
 * Insert the method's description here.
 * Creation date: (6/30/2004 12:30:51 PM)
 * @param documentInfo cbit.vcell.document.VCDocumentInfo
 */
public void managerIDchanged(java.lang.String oldID, java.lang.String newID) {
	if (oldID != null) {
		getMdiManager().updateDocumentID(oldID, newID);
	}
}

/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:20:47 AM)
 * @param documentType int
 */
public AsynchClientTask[] newDocument(final VCDocument.DocumentCreationInfo documentCreationInfo) {
	/* asynchronous and not blocking any window */
	AsynchClientTask[] taskArray1 =  createNewDocument(documentCreationInfo);
	AsynchClientTask[] taskArray = new AsynchClientTask[taskArray1.length + 2];
	System.arraycopy(taskArray1, 0, taskArray, 0, taskArray1.length);
	
	AsynchClientTask taskNew1 = new AsynchClientTask("Creating New Document", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {		
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			VCDocument doc = (VCDocument)hashTable.get("doc");
			prepareDocumentToLoad(doc);
		}
	};
	AsynchClientTask taskNew2 = new AsynchClientTask("Creating New Document", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {		
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			VCDocument doc = (VCDocument)hashTable.get("doc");			
			DocumentWindowManager windowManager = createDocumentWindowManager(doc);
			getMdiManager().createNewDocumentWindow(windowManager);
		}
	};
	taskArray[taskArray1.length] = taskNew1;
	taskArray[taskArray1.length + 1] = taskNew2;
	return taskArray;
}


/**
 * onVCellMessageEvent method comment.
 */
public void onVCellMessageEvent(final cbit.rmi.event.VCellMessageEvent event) {
	if (event.getEventTypeID() == cbit.rmi.event.VCellMessageEvent.VCELL_MESSAGEEVENT_TYPE_BROADCAST) {
	    PopupGenerator.showErrorDialog(event.getMessageData().getData().toString());
	}
}

/**
 * Insert the method's description here.
 * Creation date: (5/24/2004 9:37:46 PM)
 */
private void openAfterChecking(final VCDocumentInfo documentInfo, final TopLevelWindowManager requester, final boolean inNewWindow) {

	/* asynchronous and not blocking any window */
	bOpening = true;
	
	// start a thread that gets it and updates the GUI by creating a new document desktop
	String taskName = "Loading Document: '" + documentInfo.getVersion().getName() + "' from " + (documentInfo instanceof XMLInfo?"XML":"database");
	
	AsynchClientTask task0 = new AsynchClientTask(taskName, AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			if (! inNewWindow) {
				// request was to replace the document in an existing window
				getMdiManager().blockWindow(requester.getManagerID());
			}
		}
	};
	AsynchClientTask task1 = new AsynchClientTask(taskName, AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			VCDocument doc = null;
			if (documentInfo instanceof BioModelInfo) {
				BioModelInfo bmi = (BioModelInfo)documentInfo;
				doc = getDocumentManager().getBioModel(bmi);
			} else if (documentInfo instanceof MathModelInfo) {
				MathModelInfo mmi = (MathModelInfo)documentInfo;
				doc = getDocumentManager().getMathModel(mmi);
			} else if (documentInfo instanceof GeometryInfo) {
				GeometryInfo gmi = (GeometryInfo)documentInfo;
				doc = getDocumentManager().getGeometry(gmi);
			} else if (documentInfo instanceof XMLInfo) {
				String xmlStr = ((XMLInfo)documentInfo).getXmlString();
				org.jdom.Element rootElement = cbit.util.xml.XmlUtil.stringToXML(xmlStr, null);         //some overhead.
				String xmlType = rootElement.getName();
				String modelXmlType = null;
				if (xmlType.equals(XMLTags.VcmlRootNodeTag)) {
					// For now, assuming that <vcml> element has only one child (biomodel, mathmodel or geometry). 
					// Will deal with multiple children of <vcml> Element when we get to model composition.
					List<Element> childElementList = rootElement.getChildren();
					Element modelElement = childElementList.get(0);	// assuming first child is the biomodel, mathmodel or geometry.
					modelXmlType = modelElement.getName();
				}
				if (xmlType.equals(XMLTags.BioModelTag) || (xmlType.equals(XMLTags.VcmlRootNodeTag) && modelXmlType.equals(XMLTags.BioModelTag))) {
					doc = XmlHelper.XMLToBioModel(xmlStr);
				} else if (xmlType.equals(XMLTags.MathModelTag) || (xmlType.equals(XMLTags.VcmlRootNodeTag) && modelXmlType.equals(XMLTags.MathModelTag))) {
					doc = XmlHelper.XMLToMathModel(xmlStr);					
				} else if (xmlType.equals(XMLTags.GeometryTag) || (xmlType.equals(XMLTags.VcmlRootNodeTag) && modelXmlType.equals(XMLTags.GeometryTag))) {
					doc = XmlHelper.XMLToGeometry(xmlStr);
				} else if (xmlType.equals(XMLTags.SbmlRootNodeTag)) {
					TranslationLogger transLogger = new TranslationLogger(requester);
					doc = XmlHelper.importSBML(transLogger, xmlStr);
				} else if (xmlType.equals(XMLTags.CellmlRootNodeTag)) {
					if (requester instanceof BioModelWindowManager){
						TranslationLogger transLogger = new TranslationLogger(requester);
						doc = XmlHelper.importBioCellML(transLogger, xmlStr);
					}else{
						TranslationLogger transLogger = new TranslationLogger(requester);
						doc = XmlHelper.importMathCellML(transLogger, xmlStr);
					}
				} else { // unknown XML format
					throw new RuntimeException("unsupported XML format, first element tag is <"+rootElement.getName()+">");
				}
			}
			prepareDocumentToLoad(doc);
			hashTable.put("doc", doc);
		}
	};
		
	AsynchClientTask task2 = new AsynchClientTask("Showing document", AsynchClientTask.TASKTYPE_SWING_BLOCKING, false, false) {
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			try {
				Throwable exc = (Throwable)hashTable.get(ClientTaskDispatcher.TASK_ABORTED_BY_ERROR);
				if (exc == null) {
					VCDocument doc = (VCDocument)hashTable.get("doc");
					DocumentWindowManager windowManager = null;
					if (inNewWindow) {
						windowManager = createDocumentWindowManager(doc);
						// request was to create a new top-level window with this doc
						getMdiManager().createNewDocumentWindow(windowManager);
						if (windowManager instanceof BioModelWindowManager) {
							((BioModelWindowManager)windowManager).preloadApps();
						}
					} else {
						// request was to replace the document in an existing window
						windowManager = (DocumentWindowManager)requester;
						windowManager.resetDocument(doc);
						getMdiManager().setCanonicalTitle(requester.getManagerID());
					}
				}
			} finally {
				if (!inNewWindow) {
					getMdiManager().unBlockWindow(requester.getManagerID());
				}
				bOpening = false;
			}
		}		
	};
	ClientTaskDispatcher.dispatch(requester.getComponent(), new Hashtable<String, Object>(), new AsynchClientTask[]{task0, task1, task2}, false);
}

private DocumentWindowManager createDocumentWindowManager(final VCDocument doc){
	JPanel newJPanel = new JPanel();
	if(doc instanceof BioModel){
		return new BioModelWindowManager(newJPanel, ClientRequestManager.this, (BioModel)doc, getMdiManager().getNewlyCreatedDesktops());
	}else if(doc instanceof MathModel){
		return new MathModelWindowManager(newJPanel, ClientRequestManager.this, (MathModel)doc, getMdiManager().getNewlyCreatedDesktops());
	}else if(doc instanceof Geometry){
		return new GeometryWindowManager(newJPanel, ClientRequestManager.this, (Geometry)doc, getMdiManager().getNewlyCreatedDesktops());
	}
	throw new RuntimeException("Unknown VCDocument type "+doc);
}
/**
 * Insert the method's description here.
 * Creation date: (5/24/2004 8:53:05 PM)
 * @param documentInfo cbit.vcell.document.VCDocumentInfo
 */
public void openDocument(int documentType, DocumentWindowManager requester) {
	/* trying to open from database; called by DocumentWindow */
	// get an info first
	VCDocumentInfo documentInfo = null;
	try {
		documentInfo = getMdiManager().getDatabaseWindowManager().selectDocument(documentType, requester);
		// check whether request comes from a blank, unchanged document window; if so, open in same window, otherwise in a new window
		boolean inNewWindow = checkDifferentFromBlank(documentType, requester.getVCDocument());
		openDocument(documentInfo, requester, inNewWindow);
	} catch (UserCancelException uexc) {
		System.out.println(uexc);
		return;
	} catch (Exception exc) {
		exc.printStackTrace(System.out);
		PopupGenerator.showErrorDialog(requester, "Open document failed\n"+exc);
	}
}

	
	/**
 * Insert the method's description here.
 * Creation date: (5/24/2004 8:53:05 PM)
 * @param documentInfo cbit.vcell.document.VCDocumentInfo
 */
public void openDocument(VCDocumentInfo documentInfo, TopLevelWindowManager requester, boolean inNewWindow) {
	// called directly from DatabaseWindow or after invoking an open dialog (see openDocument(int, Component))
	// need to check whether we opened this before and we still have it open
	if (documentInfo == null) {
		return;
	}
	String documentID = null;
	if (documentInfo.getVersion()!=null && documentInfo.getVersion().getVersionKey()!=null){ // CLEAN UP BOGUS VERSION in XmlInfo !!!
		documentID = documentInfo.getVersion().getVersionKey().toString();
	}

	// see if we have this open
	if (documentID != null && getMdiManager().haveWindow(documentID)) {
		// already open, block it
		getMdiManager().blockWindow(documentID);
		// check for changes
		VCDocument openedDoc = ((DocumentWindowManager)getMdiManager().getWindowManager(documentID)).getVCDocument();
		boolean isChanged = true;
		try {
			isChanged = getDocumentManager().isChanged(openedDoc);
		} catch (DataAccessException exc) {
			// *maybe* something wrong trying to go to database, may not be able to load in the end, but warn anyway and try
			String choice = PopupGenerator.showWarningDialog(requester, getUserPreferences(), UserMessage.warn_UnableToCheckForChanges,null);
			if (choice.equals(UserMessage.OPTION_CANCEL)){
				// user canceled, just show existing document
				getMdiManager().unBlockWindow(documentID);
				getMdiManager().showWindow(documentID);
				return;
			}
		}
		// we managed to check
		if (isChanged) {
			// it changed, warn the user
			String choice = PopupGenerator.showWarningDialog(requester, getUserPreferences(), UserMessage.choice_AlreadyOpened,null);
			if (choice.equals(UserMessage.OPTION_CANCEL)){
				// user canceled, just show existing document
				getMdiManager().unBlockWindow(documentID);
				getMdiManager().showWindow(documentID);
				return;
			} else {
				// user confirmed, close existing window first
				getMdiManager().closeWindow(documentID);
				// we are ready to try to get the new document
			}
		} else {
			// nothing changed, just show that window
			getMdiManager().unBlockWindow(documentID);
			getMdiManager().showWindow(documentID);
			return;
		}
	}
	openAfterChecking(documentInfo, requester, inNewWindow);
}


	public void processComparisonResult(TMLPanel comparePanel, TopLevelWindowManager requester) {
		// this is called from the EventDispatchQueue, so take care with threading...

		if (comparePanel == null || requester == null) {
			throw new IllegalArgumentException("Invalid params: " + comparePanel + " " + requester);
		}
		try {
			final VCDocument vcDoc = comparePanel.processComparisonResult();
			if (requester instanceof DatabaseWindowManager) {
				final DatabaseWindowManager dataWinManager = (DatabaseWindowManager)requester;
				final VCDocumentInfo vcDocInfo = getMatchingDocumentInfo(vcDoc);
				this.openDocument(vcDocInfo, dataWinManager, true);
				Thread waiter = new Thread() {
					public void run() {
						try {
							BeanUtils.setCursorThroughout((Container)dataWinManager.getComponent(), Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							while (bOpening){
								try { 
									Thread.sleep(100);
								} catch (InterruptedException e) {}	
							}
							String ID = vcDocInfo.getVersion().getVersionKey().toString();
							DocumentWindowManager dwm = (DocumentWindowManager)ClientRequestManager.this.getMdiManager().getWindowManager(ID);
							dwm.resetDocument(vcDoc);
						}finally{
							BeanUtils.setCursorThroughout((Container)dataWinManager.getComponent(), Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}
				};
				waiter.start();
			} else if (requester instanceof DocumentWindowManager) {
				DocumentWindowManager docWinManager = (DocumentWindowManager)requester;
				docWinManager.resetDocument(vcDoc);
			} else {
				throw new IllegalArgumentException("Invalid TopLevelWindowManager instance: " + requester.getClass().getName());
			}
			System.out.println("Processing new model ..." + vcDoc.getVersion().getName());
		} catch (Exception e){
			e.printStackTrace(System.out);
			throw new RuntimeException(e.getMessage());
		}
	}


	/**
	 * This method gets called when a bound property is changed.
	 * @param evt A PropertyChangeEvent object describing the event source 
	 *   	and the property that has changed.
	 */
public void propertyChange(final PropertyChangeEvent evt) {
	if (evt.getSource() == getVcellClient().getClientServerManager() && evt.getPropertyName().equals("connectionStatus")) {
		// update status display
		updateStatusNow(); // this is already thread-safe
		// other updates
		AsynchGuiUpdater updater = new AsynchGuiUpdater() {
			public void guiToDo() {}
			public void guiToDo(Object params) {
				// so far just update the DatabaseWindow
				// need logic to deal with disconnection, different credentials etc.

				// only when NOT initializing
				if (((ConnectionStatus)evt.getNewValue()).getStatus() != ConnectionStatus.INITIALIZING) {
					getMdiManager().getDatabaseWindowManager().initializeAll();
					if (getMdiManager().getTestingFrameworkWindowManager() != null) {
						getMdiManager().getTestingFrameworkWindowManager().initializeAllPanels();
					}
				}
			}
		};
		updater.updateNow("some arg needed...");
	}
}	


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:18:16 AM)
 */
public void reconnect() {
	// asynch & nothing to do on Swing queue (updates handled by events)
	AsynchClientTask task1 = new AsynchClientTask("reconnect", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				getClientServerManager().reconnect();
				
			}
	};
	ClientTaskDispatcher.dispatch(null, new Hashtable<String, Object>(), new AsynchClientTask[] { task1 });
}


/**
 * Insert the method's description here.
 * Creation date: (6/9/2004 1:07:09 PM)
 * @param vcDocument cbit.vcell.document.VCDocument
 */
public void revertToSaved(DocumentWindowManager documentWindowManager) {
	// make the info
	VCDocument document = documentWindowManager.getVCDocument();
	VCDocumentInfo info = null;
	try {
		KeyValue versionKey = document.getVersion().getVersionKey();
		switch (document.getDocumentType()) {
			case VCDocument.BIOMODEL_DOC: {
				info = getDocumentManager().getBioModelInfo(versionKey);
				break;
			}
			case VCDocument.MATHMODEL_DOC: {
				info = getDocumentManager().getMathModelInfo(versionKey);
				break;
			}
			case VCDocument.GEOMETRY_DOC: {
				info = getDocumentManager().getGeometryInfo(versionKey);
				break;
			}
		}
	}catch (DataAccessException e){
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
	// reload and reset into same window
	openAfterChecking(info, documentWindowManager, false);
}


/**
 * Insert the method's description here.
 * Creation date: (6/2/2004 2:29:35 AM)
 * @param documentWindowManager cbit.vcell.client.DocumentWindowManager
 * @param simulations cbit.vcell.solver.Simulation[]
 */
public void runSimulation(final SimulationInfo simInfo) throws DataAccessException{

	getClientServerManager().
		getJobManager().
			startSimulation(simInfo.getAuthoritativeVCSimulationIdentifier());
}		


/**
 * Insert the method's description here.
 * Creation date: (6/2/2004 2:29:35 AM)
 * @param documentWindowManager cbit.vcell.client.DocumentWindowManager
 * @param simulations cbit.vcell.solver.Simulation[]
 */
public void runSimulations(final ClientSimManager clientSimManager, final Simulation[] simulations) {
	DocumentWindowManager documentWindowManager = clientSimManager.getDocumentWindowManager();
	/*	run some quick checks to see if we need to do a SaveAs */
	boolean needSaveAs = false;
	if (documentWindowManager.getVCDocument().getVersion() == null) {
		// never saved
		needSaveAs = true;
	} else if (!documentWindowManager.getVCDocument().getVersion().getOwner().compareEqual(getDocumentManager().getUser())) {
		// not the owner
		// keep the user informed this time
		String choice = PopupGenerator.showWarningDialog(documentWindowManager, getUserPreferences(), UserMessage.warn_SaveNotOwner,null);
		if (choice.equals(UserMessage.OPTION_SAVE_AS_NEW)){
			needSaveAs = true;
		} else {
			// user canceled, just show existing document
			getMdiManager().showWindow(documentWindowManager.getManagerID());
			return;
		}
	}

	// Before running the simulation, check if all the sizes of structures are set
	if(simulations != null && simulations.length > 0)
	{
		VCDocument vcd = documentWindowManager.getVCDocument();
		if(vcd instanceof BioModel)
		{
			//we want to check when there is stochastic application if the rate laws set in model can be automatically transformed.
			String stochChkMsg =((BioModel)vcd).isValidForStochApp();
			for(int i=0; i<simulations.length; i++)
			{
				if(simulations[i].getMathDescription().isStoch())
				{
					if(!(stochChkMsg.equals("")))
					{
						DialogUtils.showErrorDialog("Problem in simulation: "+simulations[i].getName()+".\n"+stochChkMsg);
						throw new RuntimeException("Problem in simulation: "+simulations[i].getName()+"\n"+stochChkMsg);
					}
				}
			}
		}
	}
	//
	// when we run simulations, we want to force these exact editions to be run (not their older "equivalent" simulations).
	//
	// rather than trying to update immutable objects (e.g. directly clear parent references), we can do two things:
	//
	// 1) if simulation already points back to a previous "equivalent" edition, 
	//       clear the version to force a clean save with no equivalency relationship
	// 2) and, to prevent a simulation save from creating a new equivalency relationship, 
	//       send the list of simulations to be run to force independence (see SaveDocument).
	//
	for (int i = 0;simulations!=null && i < simulations.length; i++){
		if (simulations[i].getSimulationVersion() != null && simulations[i].getSimulationVersion().getParentSimulationReference()!=null){
			simulations[i].clearVersion();
		}
	}
	/* now start the dirty work */
	
	/* block document window */
	JFrame currentDocumentWindow = getMdiManager().blockWindow(documentWindowManager.getManagerID());
	/* prepare hashtable for tasks */
	Hashtable<String, Object> hash = new Hashtable<String, Object>();
	hash.put("mdiManager", getMdiManager());
	hash.put("documentManager", getDocumentManager());
	hash.put("documentWindowManager", documentWindowManager);
	hash.put("currentDocumentWindow", currentDocumentWindow);
	hash.put("clientSimManager", clientSimManager);
	hash.put("simulations", simulations);
	hash.put("jobManager", getClientServerManager().getJobManager());
	hash.put("requestManager", this);
	
	/* create tasks */
	AsynchClientTask[] tasks = null;
	if (needSaveAs) {
		// check document consistency first
		AsynchClientTask documentValid = new DocumentValid();
		AsynchClientTask setMathDescription = new SetMathDescription();
		// get a new name
		AsynchClientTask newName = new NewName();
		// save it
		AsynchClientTask saveDocument = new SaveDocument();
		// clean up
		AsynchClientTask finishSave = new FinishSave();
		// run the simulations
		AsynchClientTask runSims = new RunSims();
		// assemble array
		tasks = new AsynchClientTask[] {
			documentValid,
			setMathDescription,
			newName,
			saveDocument,
			finishSave,
			runSims
			};
	} else {
		// check document consistency first
		AsynchClientTask documentValid = new DocumentValid();
		AsynchClientTask setMathDescription = new SetMathDescription();
		// check if unchanged document
		AsynchClientTask checkUnchanged = new CheckUnchanged(true);
		// save it
		AsynchClientTask saveDocument = new SaveDocument();
		// check for lost results
		AsynchClientTask checkBeforeDelete = new CheckBeforeDelete();
		// delete old document
		AsynchClientTask deleteOldDocument = new DeleteOldDocument();
		// clean up
		AsynchClientTask finishSave = new FinishSave();
		// run the simulations
		AsynchClientTask runSims = new RunSims();
		// assemble array
		tasks = new AsynchClientTask[] {
			documentValid,
			setMathDescription,
			checkUnchanged,
			saveDocument,
			checkBeforeDelete,
			deleteOldDocument,
			finishSave,
			runSims
			};
	}
	/* run the tasks */
	ClientTaskDispatcher.dispatch(currentDocumentWindow, hash, tasks, true);
}


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 3:09:25 PM)
 * @param vcDocument cbit.vcell.document.VCDocument
 * @param replace boolean
 */
public void saveDocument(DocumentWindowManager documentWindowManager, boolean replace) {
	
	/*	run some quick checks first to validate request to save or save edition */
	if (documentWindowManager.getVCDocument().getVersion() == null) {
		// never saved - can't see this happening, but check anyway and default to save as
		// (save/save edition buttons should have not been enabled upon document window creation)
		System.out.println("\nIGNORED ERROR: should not have been able to use save/save edition on doc with no version key\n");
		saveDocumentAsNew(documentWindowManager);
	}
	if(!documentWindowManager.getVCDocument().getVersion().getOwner().compareEqual(getDocumentManager().getUser())) {
		// not the owner - this should also not happen, but check anyway...
		// keep the user informed this time
		System.out.println("\nIGNORED ERROR: should not have been able to use save/save edition on doc with different owner\n");
		String choice = PopupGenerator.showWarningDialog(documentWindowManager, getUserPreferences(), UserMessage.warn_SaveNotOwner,null);
		if (choice.equals(UserMessage.OPTION_SAVE_AS_NEW)){
			// user chose to Save As
			saveDocumentAsNew(documentWindowManager);
			return;
		} else {
			// user canceled, just show existing document
			getMdiManager().showWindow(documentWindowManager.getManagerID());
			return;
		}
	}
		
	/* request is valid, go ahead with save */
	
	/* block document window */
	JFrame currentDocumentWindow = getMdiManager().blockWindow(documentWindowManager.getManagerID());
	/* prepare hashtable for tasks */
	Hashtable<String, Object> hash = new Hashtable<String, Object>();
	hash.put("mdiManager", getMdiManager());
	hash.put("documentManager", getDocumentManager());
	hash.put("documentWindowManager", documentWindowManager);
	hash.put("currentDocumentWindow", currentDocumentWindow);
	hash.put("requestManager", this);
	
	/* create tasks */
	// check document consistency first
	AsynchClientTask documentValid = new DocumentValid();
	AsynchClientTask setMathDescription = new SetMathDescription();
	// check if unchanged document
	AsynchClientTask checkUnchanged = new CheckUnchanged(false);
	// save it
	AsynchClientTask saveDocument = new SaveDocument();
	// check for lost results
	AsynchClientTask checkBeforeDelete = new CheckBeforeDelete();
	// delete old document
	AsynchClientTask deleteOldDocument = new DeleteOldDocument();
	// clean up
	AsynchClientTask finishSave = new FinishSave();
	// assemble array
	AsynchClientTask[] tasks = null;
	if (replace) {
		tasks = new AsynchClientTask[] {
			documentValid,
			setMathDescription,
			checkUnchanged,
			saveDocument,
			checkBeforeDelete,
			deleteOldDocument,
			finishSave
			};
	} else {
		tasks = new AsynchClientTask[] {
			documentValid,
			setMathDescription,
			checkUnchanged,
			saveDocument,
			finishSave
			};
	}
	/* run tasks */
	ClientTaskDispatcher.dispatch(currentDocumentWindow, hash, tasks, false);
}


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 3:09:25 PM)
 * @param vcDocument cbit.vcell.document.VCDocument
 */
public void saveDocumentAsNew(DocumentWindowManager documentWindowManager) {
	
	/* block document window */
	JFrame currentDocumentWindow = getMdiManager().blockWindow(documentWindowManager.getManagerID());
	/* prepare hashtable for tasks */
	Hashtable<String, Object> hash = new Hashtable<String, Object>();
	hash.put("mdiManager", getMdiManager());
	hash.put("documentManager", getDocumentManager());
	hash.put("documentWindowManager", documentWindowManager);
	hash.put("currentDocumentWindow", currentDocumentWindow);
	hash.put("requestManager", this);
	
	/* create tasks */
	// check document consistency first
	AsynchClientTask documentValid = new DocumentValid();
	AsynchClientTask setMathDescription = new SetMathDescription();
	// get a new name
	AsynchClientTask newName = new NewName();
	// save it
	AsynchClientTask saveDocument = new SaveDocument();
	// clean up
	AsynchClientTask finishSave = new FinishSave();
	// assemble array
	AsynchClientTask[] tasks = new AsynchClientTask[] {
		documentValid,
		setMathDescription,
		newName,
		saveDocument,
		finishSave
		};
	/* run tasks */
	ClientTaskDispatcher.dispatch(currentDocumentWindow, hash, tasks, false);
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:20:47 AM)
 * @param windowManager cbit.vcell.client.desktop.DocumentWindowManager
 */
public BioModelInfo selectBioModelInfo(TopLevelWindowManager requester) {
	VCDocumentInfo documentInfo = null;
	try {
		documentInfo = getMdiManager().getDatabaseWindowManager().selectDocument(VCDocument.BIOMODEL_DOC, requester);
	} catch (UserCancelException uexc) {
		System.out.println(uexc);
		return null;
	} catch (Exception exc) {
		exc.printStackTrace(System.out);
		PopupGenerator.showErrorDialog(requester, "Selection of BioModel failed\n"+exc);
	}
	return (BioModelInfo)documentInfo;
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:20:47 AM)
 * @param windowManager cbit.vcell.client.desktop.DocumentWindowManager
 */
public MathModelInfo selectMathModelInfo(TopLevelWindowManager requester) {
	VCDocumentInfo documentInfo = null;
	try {
		documentInfo = getMdiManager().getDatabaseWindowManager().selectDocument(VCDocument.MATHMODEL_DOC, requester);
	} catch (UserCancelException uexc) {
		System.out.println(uexc);
		return null;
	} catch (Exception exc) {
		exc.printStackTrace(System.out);
		PopupGenerator.showErrorDialog(requester, "Selection of MathModel failed\n"+exc);
	}
	return (MathModelInfo)documentInfo;
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/2005 2:02:23 PM)
 * @param newBExiting boolean
 */
private void setBExiting(boolean newBExiting) {
	bExiting = newBExiting;
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:21:50 AM)
 * @param newVcellClient cbit.vcell.client.VCellClient
 */
private void setVcellClient(VCellClient newVcellClient) {
	vcellClient = newVcellClient;
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:20:48 AM)
 */
public void showBNGWindow() {
	getMdiManager().showWindow(ClientMDIManager.BIONETGEN_WINDOW_ID);
}

public void showFieldDataWindow() {
	getMdiManager().showWindow(ClientMDIManager.FIELDDATA_WINDOW_ID);
}

/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:20:48 AM)
 */
public void showDatabaseWindow() {
	getMdiManager().showWindow(ClientMDIManager.DATABASE_WINDOW_ID);
}


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:20:48 AM)
 */
public void showTestingFrameworkWindow() {
	getMdiManager().showWindow(ClientMDIManager.TESTING_FRAMEWORK_WINDOW_ID);
}


/**
 * Insert the method's description here.
 * Creation date: (6/15/2004 2:37:01 AM)
 */
public void startExport(final TopLevelWindowManager windowManager, final ExportSpecs exportSpecs) {
	// start a thread to get it; not blocking any window/frame
	AsynchClientTask task1 = new AsynchClientTask("starting exporting", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			getClientServerManager().getJobManager().startExport(exportSpecs);
		}
	};
	ClientTaskDispatcher.dispatch(windowManager.getComponent(), new Hashtable<String, Object>(), new AsynchClientTask[] { task1 });
}


/**
 * Insert the method's description here.
 * Creation date: (6/2/2004 2:29:35 AM)
 * @param documentWindowManager cbit.vcell.client.DocumentWindowManager
 * @param simulations cbit.vcell.solver.Simulation[]
 */
public void stopSimulations(final ClientSimManager clientSimManager, final Simulation[] simulations) {
	// stop is single step operation, don't bother with tasks, thread inline
	AsynchClientTask task1 = new AsynchClientTask("stopping simulations", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {

		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			Hashtable<Simulation, Throwable> failures = new Hashtable<Simulation, Throwable>();
			if (simulations != null && simulations.length > 0) {
				for (int i = 0; i < simulations.length; i++){
					try {
						SimulationInfo simInfo = simulations[i].getSimulationInfo();
						if (simInfo != null) {
							// check for running once more... directly from job status
							SimulationStatus serverSimulationStatus = getServerSimulationStatus(simInfo);
							if (serverSimulationStatus != null && serverSimulationStatus.numberOfJobsDone() < simulations[i].getScanCount()) {
								getClientServerManager().getJobManager().stopSimulation(simInfo.getAuthoritativeVCSimulationIdentifier());
								// updateStatus
								clientSimManager.updateStatusFromStopRequest(simulations[i]);
							}
						} else {
							// this should really not happen...
							throw new RuntimeException(">>>>>>>>>> trying to stop an unsaved simulation...");
						}	
					} catch (Throwable exc) {
						exc.printStackTrace(System.out);
						failures.put(simulations[i], exc);
					}
				}
				hashTable.put("failures", failures);
			}
		}
	};
	
	AsynchClientTask task2 = new AsynchClientTask("stopping simulations", AsynchClientTask.TASKTYPE_SWING_BLOCKING, false, false) {

		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			Hashtable<Simulation,Throwable> failures = (Hashtable<Simulation, Throwable>)hashTable.get("failures");
			if (failures != null && ! failures.isEmpty()) {
				Enumeration<Simulation> en = failures.keys();
				while (en.hasMoreElements()) {
					Simulation sim = (Simulation)en.nextElement();
					Throwable exc = (Throwable)failures.get(sim);
					// notify user
					PopupGenerator.showErrorDialog(clientSimManager.getDocumentWindowManager(), "Failed to dispatch stop request for simulation'"+sim.getName()+"'\n"+exc.getMessage());
				}
			}
		}
	};
	ClientTaskDispatcher.dispatch(clientSimManager.getDocumentWindowManager().getComponent(), new Hashtable<String, Object>(), new AsynchClientTask[] { task1, task2 });	
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/2004 12:10:07 PM)
 */
public void updateStatusNow() {
	// thread safe update of gui
	AsynchClientTask task1 = new AsynchClientTask("updateStatusNow", AsynchClientTask.TASKTYPE_SWING_NONBLOCKING) {
		@Override
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			getVcellClient().getStatusUpdater().updateNow(getVcellClient().getClientServerManager().getConnectionStatus());
			
		}
	};
	ClientTaskDispatcher.dispatch(null, new Hashtable<String, Object>(), new AsynchClientTask[] {task1});
}

public SimInfoHolder[] getOpenDesktopDocumentInfos() throws DataAccessException{
	Vector<SimInfoHolder> simInfoHolderV = new Vector<SimInfoHolder>();
	Enumeration<TopLevelWindowManager> dwmEnum = getMdiManager().getWindowManagers();
	while(dwmEnum.hasMoreElements()){
		TopLevelWindowManager tlwm = dwmEnum.nextElement();
		if(tlwm instanceof DocumentWindowManager){
			DocumentWindowManager dwm = (DocumentWindowManager)tlwm;
			VCDocument vcDoc = dwm.getVCDocument();
			if(vcDoc.getVersion() != null){
				if(vcDoc.getDocumentType() == VCDocument.BIOMODEL_DOC){
					BioModel bioModel =
						getDocumentManager().getBioModel(vcDoc.getVersion().getVersionKey());
					SimulationContext[] simContexts = bioModel.getSimulationContexts();
					for(int i=0;i<simContexts.length;i+= 1){
						if(simContexts[i].getGeometry() == null){
							throw new DataAccessException("Error gathering document info (isCompartmental check failed):\nOpen BioModel document "+bioModel.getName()+" has no Geometry");
						}
						Simulation[] sims = simContexts[i].getSimulations();
						for(int j=0;j<sims.length;j+= 1){
							for(int k=0;k<sims[j].getScanCount();k+= 1){
								FieldDataWindowManager.SimInfoHolder simInfoHolder =
									new FieldDataWindowManager.FDSimBioModelInfo(
											bioModel.getVersion().getVersionKey(),
											simContexts[i].getName(),sims[j].getSimulationInfo(),
											k,
											//!sims[j].getSolverTaskDescription().getSolverDescription().hasVariableTimestep(),
											simContexts[i].getGeometry().getDimension() == 0
									);
								simInfoHolderV.add(simInfoHolder);
							}
						}
					}
				}else if(vcDoc.getDocumentType() == VCDocument.MATHMODEL_DOC) {
					MathModel mathModel =
						getDocumentManager().getMathModel(vcDoc.getVersion().getVersionKey());
					if(mathModel.getMathDescription() == null || mathModel.getMathDescription().getGeometry() == null){
						throw new DataAccessException("Error gathering document info (isCompartmental check failed):\nOpen MathModel document "+mathModel.getName()+" has either no MathDescription or no Geometry");
					}
					Simulation[] sims = mathModel.getSimulations();
					for(int i=0;i<sims.length;i+= 1){
						for(int k=0;k<sims[i].getScanCount();k+= 1){
							FieldDataWindowManager.SimInfoHolder simInfoHolder =
								new FieldDataWindowManager.FDSimMathModelInfo(
										mathModel.getVersion().getVersionKey(),
										sims[i].getSimulationInfo(),
										k,
										//!sims[i].getSolverTaskDescription().getSolverDescription().hasVariableTimestep(),
										mathModel.getMathDescription().getGeometry().getDimension() == 0
								);
							simInfoHolderV.add(simInfoHolder);
						}
					}
				}
			}
		}
		
	}
	SimInfoHolder[] simInfoHolderArr = new SimInfoHolder[simInfoHolderV.size()];
	simInfoHolderV.copyInto(simInfoHolderArr);
	return simInfoHolderArr;
}


public void checkClientServerSoftwareVersion() {
	getClientServerManager().checkClientServerSoftwareVersion();	
}

public void showComparisonResults(TopLevelWindowManager requester, XmlTreeDiff diffTree, String baselineDesc, String modifiedDesc) {
	TMLPanel comparePanel = new TMLPanel();
	comparePanel.setXmlTreeDiff(diffTree);
	comparePanel.setBaselineVersionDescription(baselineDesc);
	comparePanel.setModifiedVersionDescription(modifiedDesc);
	
	JOptionPane comparePane = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE, 0, null, new Object[] {"Apply Changes", "Close"});
	comparePane.setMessage(comparePanel);
	JDialog compareDialog = comparePane.createDialog(requester.getComponent(), "Compare Models");
	compareDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	ZEnforcer.showModalDialogOnTop(compareDialog,requester.getComponent());
	if ("Apply Changes".equals(comparePane.getValue())) {
		if (!comparePanel.tagsResolved()) {
			JOptionPane messagePane = new JOptionPane("Please resolve all tagged elements/attributes before proceeding.");
			JDialog messageDialog = messagePane.createDialog(comparePanel, "Error");
			messageDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			ZEnforcer.showModalDialogOnTop(messageDialog,comparePanel);
		} else {
			BeanUtils.setCursorThroughout((Container)requester.getComponent(), Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				processComparisonResult(comparePanel, requester);
			} catch (RuntimeException e) {
				throw e;
			} finally {
				BeanUtils.setCursorThroughout((Container)requester.getComponent(), Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		} 
	}
}

public void prepareDocumentToLoad(VCDocument doc) throws Exception {
	Simulation[] simulations = null;
	if (doc instanceof MathModel) {
		Geometry geometry = ((MathModel)doc).getMathDescription().getGeometry();
		geometry.precomputeAll();
		simulations = ((MathModel)doc).getSimulations();		
	} else if (doc instanceof Geometry) {
		((Geometry)doc).precomputeAll();		
	} else if (doc instanceof BioModel) {
		BioModel bioModel = (BioModel)doc;
		SimulationContext[] simContexts = bioModel.getSimulationContexts();
		for (SimulationContext simContext : simContexts) {
			simContext.getGeometry().precomputeAll();
		}
		simulations = ((BioModel)doc).getSimulations();		
	}
	if (simulations != null) {	
		// preload simulation status
		VCSimulationIdentifier simIDs[] = new VCSimulationIdentifier[simulations.length];
		for (int i = 0; i < simulations.length; i++){
			simIDs[i] = simulations[i].getSimulationInfo().getAuthoritativeVCSimulationIdentifier();
		}
		getDocumentManager().preloadSimulationStatus(simIDs);
	}
}
}