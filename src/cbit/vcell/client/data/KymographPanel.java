package cbit.vcell.client.data;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import cbit.image.DisplayAdapterService;
import cbit.vcell.client.PopupGenerator;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.client.task.ClientTaskDispatcher;
import cbit.vcell.simdata.DataIdentifier;
import cbit.vcell.simdata.VariableType;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

import org.vcell.util.BeanUtils;
import org.vcell.util.DataAccessException;
import org.vcell.util.Range;
import org.vcell.util.TSJobResultsNoStats;
import org.vcell.util.TimeSeriesJobResults;
import org.vcell.util.TimeSeriesJobSpec;
import org.vcell.util.document.User;
import org.vcell.util.document.VCDataJobID;

import java.awt.GridBagConstraints;
import java.util.Hashtable;
/**
 * Insert the type's description here.
 * Creation date: (12/14/2004 9:38:13 AM)
 * @author: Frank Morgan
 */
public class KymographPanel extends javax.swing.JPanel implements cbit.vcell.geometry.gui.DrawPaneModel {
	
	private PDEDataViewer.TimeSeriesJobStarter timeSeriesJobStarter;
	
	User userRequestingData;
	//
	private class MinMaxMeanHolder {
		private double min;
		private double max;
		private double mean;
		private Range range;;
		public MinMaxMeanHolder(double argMin,double argMax,double argMean){
			min = argMin;
			max = argMax;
			mean = argMean;
			range = new Range(min,max);
		};
		//public void update (MinMaxMeanHolder argMMMH){
			//if(argMMMH.min < min){
				//min = argMMMH.min;
			//}
			//if(argMMMH.max > max){
				//max = argMMMH.max;
			//}
			//mean = (min+max)/2;
		//};
		public Range getRange(){
			return range;
		}
	};
	//
	private MinMaxMeanHolder allDataMMMH;
	private MinMaxMeanHolder localDistDataMMMH;
	private MinMaxMeanHolder localTimeDataMMMH;
	private MinMaxMeanHolder userDefinedMMMH;
	//private double userDefinedMin,userDefinedMax;
//	private boolean bBlockInitialLoad = false;
	private static final int SCALE_IMAGE_ALL = 0;
	private static final int SCALE_IMAGE_LINESCAN = 1;
	private static final int SCALE_IMAGE_TIMESERIES = 2;
	private static final int SCALE_IMAGE_USERDEFINED = 3;
	private int scaleImageMode = SCALE_IMAGE_ALL;
	private boolean isInit = false;
	private cbit.plot.Plot2D currentLineScanPlot2D = null;
	private cbit.plot.Plot2D currentTimeSeriesPlot2D = null;
	private static final String NORMAL_MESSAGE = "Mouse Click, Arrow Keys Change Graph.  Mouse Menu for Options";
	private String NONE_MESSAGE = NORMAL_MESSAGE;
	private cbit.vcell.desktop.controls.DataManager dataManager = null;
	private int[] dataManagerIndices = null;
	private int[] crossingMembraneIndices = null;
	private double[] dataManagerAccumDistances = null;
	private boolean bLocalScaling = false;
	private cbit.vcell.parser.SymbolTable symbolTable;
	private cbit.vcell.parser.SymbolTableEntry currentSymbolTablEntry;
	//private double localDistDataMin = 0;
	//private double localDistDataMax = 0;
	//private double localDistDataMean = 0;
	//private double localTimeDataMin = 0;
	//private double localTimeDataMax = 0;
	//private double localTimeDataMean = 0;
	private String currentInfo = null;
	private double[] currentTimes = null;
	private double[] currentDistances = null;
	private double[] rawValues = null;
	//private double allDataMin,allDataMax,allDataMean;
	private int RESAMP_SIZE;
	private java.awt.Point currentSelectionImg = null;
	private java.awt.geom.Point2D currentSelectionUnit = null;
	private int resampleStepOrig = 0;
	private double resampleStartTimeOrig = 0;
	private double resampleEndTimeOrig = 0;
	private double initialLineScanTime = 0;
	private double[][] timeSeriesDataOrig = null;
	private double[] accumDistancesDataOrig = null;
	private cbit.image.DisplayAdapterService ivjDisplayAdapterService1 = null;  //  @jve:decl-index=0:
	private cbit.image.ImagePaneScroller ivjImagePaneScroller1 = null;
	private cbit.image.ImagePlaneManager ivjImagePlaneManager1 = null;
	private javax.swing.ImageIcon cmapImageIcon = null;  //  @jve:decl-index=0:
	private javax.swing.JLabel ivjColorMapJLabel = null;
	private javax.swing.JLabel ivjMaxJLabel = null;
	private javax.swing.JLabel ivjMinJLabel = null;
	private javax.swing.JLabel ivjDisplayJLabel = null;
	IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private boolean ivjConnPtoP1Aligning = false;
	private cbit.image.ImagePaneView ivjimagePaneView1 = null;
	private javax.swing.JMenuItem ivjCopyJMenuItem = null;
	private javax.swing.JPopupMenu ivjCopyJPopupMenu = null;
	private javax.swing.JButton ivjCopyJButton = null;
	private javax.swing.JPanel ivjJPanel1 = null;
	private javax.swing.JButton ivjZoomDownJButton = null;
	private javax.swing.JButton ivjZoomUpJButton = null;
	private javax.swing.JPanel ivjJPanel3 = null;
	private javax.swing.JLabel ivjJLabel1 = null;
	private javax.swing.JLabel ivjJLabel2 = null;
	private javax.swing.JPanel ivjGraphJPanel = null;
	private javax.swing.JPanel ivjButtonsJPanel = null;
	private javax.swing.JLabel ivjJLabel3 = null;
	private javax.swing.JComboBox ivjVarNamesJComboBox = null;
	private javax.swing.JCheckBox ivjJCheckBox1 = null;
	private javax.swing.JMenuItem ivjCopyLineScanJMenuItem = null;
	private javax.swing.JMenuItem ivjCopyTimeDataJMenuItem = null;
	private cbit.plot.PlotPane ivjPlotPaneLineScan = null;
	private cbit.plot.PlotPane ivjPlotPaneTimeSeries = null;
	private javax.swing.JMenuItem ivjScaleImageAllJMenuItem = null;
	private javax.swing.JMenuItem ivjScaleImageLSJMenuItem = null;
	private javax.swing.JMenuItem ivjScaleImageTSJMenuItem = null;
	private javax.swing.JLabel ivjScaleImageModeJLabel = null;

class IvjEventHandler implements java.awt.event.ActionListener, java.awt.event.KeyListener, java.awt.event.MouseListener, java.awt.event.MouseMotionListener, java.beans.PropertyChangeListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == KymographPanel.this.getCopyJMenuItem()) 
				connEtoC7(e);
			if (e.getSource() == KymographPanel.this.getCopyJButton()) 
				connEtoC8(e);
			if (e.getSource() == KymographPanel.this.getZoomUpJButton()) 
				connEtoC9(e);
			if (e.getSource() == KymographPanel.this.getZoomDownJButton()) 
				connEtoC10(e);
			if (e.getSource() == KymographPanel.this.getVarNamesJComboBox()) 
				connEtoC13(e);
			if (e.getSource() == KymographPanel.this.getJCheckBox1()) 
				connEtoC14(e);
			if (e.getSource() == KymographPanel.this.getCopyTimeDataJMenuItem()) 
				connEtoC15(e);
			if (e.getSource() == KymographPanel.this.getCopyLineScanJMenuItem()) 
				connEtoC16(e);
			if (e.getSource() == KymographPanel.this.getScaleImageAllJMenuItem()) 
				connEtoC18(e);
			if (e.getSource() == KymographPanel.this.getScaleImageLSJMenuItem()) 
				connEtoC19(e);
			if (e.getSource() == KymographPanel.this.getScaleImageTSJMenuItem()) 
				connEtoC20(e);
			if (e.getSource() == KymographPanel.this.getScaleImageUDJMenuItem()) 
				connEtoC21(e);
		};
		public void keyPressed(java.awt.event.KeyEvent e) {
			if (e.getSource() == KymographPanel.this.getimagePaneView1()) 
				connEtoC12(e);
		};
		public void keyReleased(java.awt.event.KeyEvent e) {};
		public void keyTyped(java.awt.event.KeyEvent e) {};
		public void mouseClicked(java.awt.event.MouseEvent e) {
			if (e.getSource() == KymographPanel.this.getimagePaneView1()) 
				connEtoC6(e);
		};
		public void mouseDragged(java.awt.event.MouseEvent e) {
			if (e.getSource() == KymographPanel.this.getimagePaneView1()) 
				connEtoC17(e);
		};
		public void mouseEntered(java.awt.event.MouseEvent e) {};
		public void mouseExited(java.awt.event.MouseEvent e) {
			if (e.getSource() == KymographPanel.this.getimagePaneView1()) 
				connEtoC3(e);
			if (e.getSource() == KymographPanel.this.getImagePaneScroller1()) 
				connEtoC11(e);
		};
		public void mouseMoved(java.awt.event.MouseEvent e) {
			if (e.getSource() == KymographPanel.this.getimagePaneView1()) 
				connEtoC2(e);
		};
		public void mousePressed(java.awt.event.MouseEvent e) {
			if (e.getSource() == KymographPanel.this.getimagePaneView1()) 
				connEtoC5(e);
		};
		public void mouseReleased(java.awt.event.MouseEvent e) {
			if (e.getSource() == KymographPanel.this.getimagePaneView1()) 
				connEtoC4(e);
		};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == KymographPanel.this.getImagePaneScroller1() && (evt.getPropertyName().equals("imagePaneView"))) 
				connPtoP1SetTarget();
		};
	};
	private javax.swing.JMenuItem ivjScaleImageUDJMenuItem = null;
	private JCheckBox jCheckBoxColor = null;

/**
 * Kymograph constructor comment.
 */
public KymographPanel() {
	super();
	initialize();

}

/**
 * Insert the method's description here.
 * Creation date: (5/24/2005 8:28:42 AM)
 * @param data double[]
 */
private MinMaxMeanHolder calcMMM(double[] data) {
	//
	//Calculate min,max,mean using only valid numeric values
	//
	double min = Double.POSITIVE_INFINITY;
	double max = Double.NEGATIVE_INFINITY;
	double mean = 0;
	long validCounter = 0;
	for(int j=0;j<data.length;j+= 1){
		//for(int k=0;k<data[j].length;k+= 1){
			double value = data[j];//[k];
			if(!Double.isNaN(value) && !Double.isInfinite(value)){
				if(value < min){
					min = value;
				}
				if(value > max){
					max = value;
				}
				mean+= value;
				validCounter+= 1;
			}
		//}
	}

	if(validCounter != 0){
		mean/= (double)validCounter;
	}else{
		return null;
	}
	
	return new MinMaxMeanHolder(min,max,mean);	
}


/**
 * Insert the method's description here.
 * Creation date: (12/28/2004 10:01:37 AM)
 * @param min double
 * @param max double
 */
private void configureMinMax() {
	
	getScaleImageModeJLabel().setText(
			(scaleImageMode == SCALE_IMAGE_ALL?"All":"")+
			(scaleImageMode == SCALE_IMAGE_LINESCAN?"LS":"")+
			(scaleImageMode == SCALE_IMAGE_TIMESERIES?"TS":"")+
			(scaleImageMode == SCALE_IMAGE_USERDEFINED?"User":"")
		);

	Range minmaxRange = null;
	switch(scaleImageMode){
		case SCALE_IMAGE_ALL:
			minmaxRange = (allDataMMMH != null?allDataMMMH.getRange():null);
		break;
		case SCALE_IMAGE_LINESCAN:
			minmaxRange = (localDistDataMMMH != null?localDistDataMMMH.getRange():null);
		break;
		case SCALE_IMAGE_TIMESERIES:
			minmaxRange = (localTimeDataMMMH != null?localTimeDataMMMH.getRange():null);
		break;
		case SCALE_IMAGE_USERDEFINED:
			minmaxRange = (userDefinedMMMH != null?userDefinedMMMH.getRange():null);
		break;
	}

	if(minmaxRange == null || minmaxRange.getMin() == minmaxRange.getMax()){
		getColorMapJLabel().setIcon(null);
		getColorMapJLabel().setText("No Range");
	}else{
		getColorMapJLabel().setIcon(cmapImageIcon);
		getColorMapJLabel().setText(null);
	}
	
	if(minmaxRange == null){
		getDisplayAdapterService1().setActiveScaleRange(null);
		getDisplayAdapterService1().setValueDomain(null);

		getMinJLabel().setText("?");
		getMinJLabel().setToolTipText("Unknown");
		getMaxJLabel().setText("?");
		getMaxJLabel().setToolTipText("Unknown");
		
		//updateColorMapDisplay();
		getimagePaneView1().getImagePaneModel().updateViewPortImage();
		
	}else if(getDisplayAdapterService1().getActiveScaleRange() == null || 
		getDisplayAdapterService1().getActiveScaleRange().getMin() != minmaxRange.getMin() ||
		getDisplayAdapterService1().getActiveScaleRange().getMax() != minmaxRange.getMax()){
			
		getDisplayAdapterService1().setValueDomain((allDataMMMH != null?allDataMMMH.getRange():null));
		getDisplayAdapterService1().setActiveScaleRange(minmaxRange);

		java.text.DecimalFormat nf = new java.text.DecimalFormat();
		nf.applyPattern("#.###E0");
		getMinJLabel().setText(((""+minmaxRange.getMin()).length() < 7?minmaxRange.getMin()+"":nf.format(minmaxRange.getMin())));
		getMinJLabel().setToolTipText(""+minmaxRange.getMin());
		getMaxJLabel().setText(((""+minmaxRange.getMax()).length() < 7?minmaxRange.getMax()+"":nf.format(minmaxRange.getMax())));
		getMaxJLabel().setToolTipText(""+minmaxRange.getMax());
		
		//updateColorMapDisplay();
		getimagePaneView1().getImagePaneModel().updateViewPortImage();
	}

}


/**
 * Insert the method's description here.
 * Creation date: (12/16/2004 10:46:05 AM)
 * @param imgX int
 * @param imgY int
 */
private void configurePlotData(int imgX,int imgY) {

	//
	//TimeScan Data
	//
	double[][] timeData = new double[2][currentTimes.length];
	timeData[0] = currentTimes;
	timeData[1] = new double[currentTimes.length];

	for(int i=0;i<currentTimes.length;i+= 1){
		//timeData[1][i] = timeSeriesDataOrig[1+imgX][i];
		timeData[1][i] = rawValues[imgX+(i*RESAMP_SIZE)];
	};
	localTimeDataMMMH = calcMMM(timeData[1]);
	
	
	final int MAX_TITLE_VAL_LENGTH = 9;
	java.text.DecimalFormat nf = new java.text.DecimalFormat();
	String valS = null;
	valS = currentDistances[imgX]+"";
	if(valS.length() > MAX_TITLE_VAL_LENGTH){
		nf.applyPattern("#.###E0");
		valS = nf.format(currentDistances[imgX]);
	}
	
	currentTimeSeriesPlot2D =
		new cbit.plot.SingleXPlot2D(new cbit.vcell.parser.SymbolTableEntry[] {currentSymbolTablEntry},"Time", new String[]{currentInfo}, timeData, new String[] {"Time Series (d="+valS+") Vert","Time"/*"Time (s)"*/,"Value"});
	getPlotPaneTimeSeries().setPlot2D(currentTimeSeriesPlot2D);

	
	//
	//LineScan Data
	//
	double[] lineData = new double[timeSeriesDataOrig.length-1];
	for(int i=1;i<timeSeriesDataOrig.length;i+= 1){
		lineData[i-1] = timeSeriesDataOrig[i][imgY];
	}
	double[] lineScanDistances = accumDistancesDataOrig;

	localDistDataMMMH = calcMMM(lineData);
	
	cbit.plot.PlotData plotData = new cbit.plot.PlotData(lineScanDistances,lineData);	
	valS = currentTimes[imgY]+"";
	if(valS.length() > MAX_TITLE_VAL_LENGTH){
		valS = nf.format(currentTimes[imgY]);
	}

	currentLineScanPlot2D =
		new cbit.plot.Plot2D(new cbit.vcell.parser.SymbolTableEntry[] {currentSymbolTablEntry},new String[] { currentInfo },new cbit.plot.PlotData[] { plotData }, new String[] {"Line Scan (t="+valS+") Horz","Distance"/*"Distance (\u00b5m)"*/, "Value"});
	getPlotPaneLineScan().setPlot2D(currentLineScanPlot2D);


	Range xRangeTime = new Range(currentTimes[0],currentTimes[currentTimes.length-1]);
	Range xRangeDist = new Range(lineScanDistances[0],lineScanDistances[lineScanDistances.length-1]);
	Range yRangeTime = (allDataMMMH != null?allDataMMMH.getRange():null);
	Range yRangeDist = yRangeTime;

	if(bLocalScaling){
		yRangeTime = (localTimeDataMMMH != null?localTimeDataMMMH.getRange():null);
		yRangeDist = (localDistDataMMMH != null?localDistDataMMMH.getRange():null);
	}

	getPlotPaneTimeSeries().forceXYRange(xRangeTime,yRangeTime);
	getPlotPaneLineScan().forceXYRange(xRangeDist,yRangeDist);

	configureMinMax();
	getimagePaneView1().repaint();
	
	
}


/**
 * connEtoC1:  (Kymograph.initialize() --> Kymograph.kymograph_Initialize()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1() {
	try {
		// user code begin {1}
		// user code end
		this.kymograph_Initialize();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC10:  (ZoomDownJButton.action.actionPerformed(java.awt.event.ActionEvent) --> Kymograph.zoomDownJButton_ActionPerformed(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC10(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.zoomDownJButton_ActionPerformed(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC11:  (ImagePaneScroller1.mouse.mouseExited(java.awt.event.MouseEvent) --> Kymograph.imagePaneScroller1_MouseExited(Ljava.awt.event.MouseEvent;)V)
 * @param arg1 java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC11(java.awt.event.MouseEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.imagePaneScroller1_MouseExited(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC12:  (imagePaneView1.key.keyPressed(java.awt.event.KeyEvent) --> Kymograph.imagePaneView1_KeyPressed(Ljava.awt.event.KeyEvent;)V)
 * @param arg1 java.awt.event.KeyEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC12(java.awt.event.KeyEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.imagePaneView1_KeyPressed(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC13:  (JComboBox1.action.actionPerformed(java.awt.event.ActionEvent) --> Kymograph.jComboBox1_ActionPerformed(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC13(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.jComboBox1_ActionPerformed(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC14:  (JCheckBox1.action.actionPerformed(java.awt.event.ActionEvent) --> Kymograph.jCheckBox1_ActionPerformed(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC14(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.jCheckBox1_ActionPerformed(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC15:  (CopyTimeDataJMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> Kymograph.copyTimeDataJMenuItem_ActionPerformed(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC15(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.copyTimeDataJMenuItem_ActionPerformed(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC16:  (CopyLineScanJMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> Kymograph.copyLineScanJMenuItem_ActionPerformed(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC16(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.copyLineScanJMenuItem_ActionPerformed(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC17:  (imagePaneView1.mouseMotion.mouseDragged(java.awt.event.MouseEvent) --> KymographPanel.imagePaneView1_Copy(Ljava.awt.event.MouseEvent;)V)
 * @param arg1 java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC17(java.awt.event.MouseEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.imagePaneView1_Copy(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC18:  (ScaleImageAllJMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> KymographPanel.scaleImageModeFromMenu(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC18(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.scaleImageModeFromMenu(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC19:  (ScaleImageLSJMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> KymographPanel.scaleImageModeFromMenu(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC19(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.scaleImageModeFromMenu(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC2:  (imagePaneView1.mouseMotion.mouseMoved(java.awt.event.MouseEvent) --> Kymograph.imagePaneScroller1_MouseMoved(Ljava.awt.event.MouseEvent;)V)
 * @param arg1 java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(java.awt.event.MouseEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.imagePaneScroller1_MouseMoved(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC20:  (ScaleImageTSJMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> KymographPanel.scaleImageModeFromMenu(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC20(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.scaleImageModeFromMenu(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC21:  (ScaleImageUDJMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> KymographPanel.scaleImageModeFromMenu(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC21(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.scaleImageModeFromMenu(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC3:  (imagePaneView1.mouse.mouseExited(java.awt.event.MouseEvent) --> Kymograph.imagePaneScroller1_MouseMoved(Ljava.awt.event.MouseEvent;)V)
 * @param arg1 java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC3(java.awt.event.MouseEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.imagePaneScroller1_MouseMoved(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC4:  (imagePaneView1.mouse.mouseReleased(java.awt.event.MouseEvent) --> Kymograph.imagePaneView1_Copy(Ljava.awt.event.MouseEvent;)V)
 * @param arg1 java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC4(java.awt.event.MouseEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.imagePaneView1_Copy(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC5:  (imagePaneView1.mouse.mousePressed(java.awt.event.MouseEvent) --> Kymograph.imagePaneView1_Copy(Ljava.awt.event.MouseEvent;)V)
 * @param arg1 java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC5(java.awt.event.MouseEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.imagePaneView1_Copy(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC6:  (imagePaneView1.mouse.mouseClicked(java.awt.event.MouseEvent) --> Kymograph.imagePaneView1_Copy(Ljava.awt.event.MouseEvent;)V)
 * @param arg1 java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC6(java.awt.event.MouseEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.imagePaneView1_Copy(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC7:  (CopyJMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> Kymograph.copyJMenuItem_ActionPerformed(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC7(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.copyJMenuItem_ActionPerformed(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC8:  (CopyJButton.action.actionPerformed(java.awt.event.ActionEvent) --> Kymograph.copyJMenuItem_ActionPerformed(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC8(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.copyJMenuItem_ActionPerformed(null);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC9:  (ZoomUpJButton.action.actionPerformed(java.awt.event.ActionEvent) --> Kymograph.zoomUpJButton_ActionPerformed(Ljava.awt.event.ActionEvent;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC9(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.zoomUpJButton_ActionPerformed(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connPtoP1SetTarget:  (ImagePaneScroller1.imagePaneView <--> imagePaneView1.this)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP1SetTarget() {
	/* Set the target from the source */
	try {
		if (ivjConnPtoP1Aligning == false) {
			// user code begin {1}
			// user code end
			ivjConnPtoP1Aligning = true;
			setimagePaneView1(getImagePaneScroller1().getImagePaneView());
			// user code begin {2}
			// user code end
			ivjConnPtoP1Aligning = false;
		}
	} catch (java.lang.Throwable ivjExc) {
		ivjConnPtoP1Aligning = false;
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * Comment
 */
private void copyJMenuItem_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	StringBuffer sb = new StringBuffer();
	sb.append("LineScan-Time Data ("+currentInfo+")"+
		" Distances(columns) from "+currentDistances[0]+" to "+currentDistances[currentDistances.length-1]+" along sample line "+
		" Times(rows) from "+currentTimes[0]+" to "+currentTimes[currentTimes.length-1]+"\n"+
		"\n");
	sb.append("Min(All)\tMax(All)\tMean(All)\n");
	sb.append((allDataMMMH != null?allDataMMMH.min+"\t"+allDataMMMH.max+"\t"+allDataMMMH.mean:"\"?\"\t\"?\"\t\"?\"")+"\n");
	sb.append("Distances");
	for(int i=0;i<currentDistances.length;i+= 1){
		sb.append("\t"+currentDistances[i]);
	}
	sb.append("\nTimes\n");
	int counter = 0;
	for(int i=0;i<currentTimes.length;i+= 1){
		sb.append(currentTimes[i]);
		for(int j=0;j<currentDistances.length;j+= 1){
			sb.append("\t"+rawValues[counter]);
			counter+= 1;
		}
		sb.append("\n");
	}
	cbit.vcell.desktop.VCellTransferable.sendToClipboard(sb.toString());
}


/**
 * Comment
 */
private void copyLineScanJMenuItem_ActionPerformed(java.awt.event.ActionEvent actionEvent) {

	StringBuffer sb = new StringBuffer();
	sb.append("LineScan Data "+" ("+currentInfo+")"+
		" Distances from "+currentDistances[0]+" to "+currentDistances[currentDistances.length-1]+" along sample line "+
		" at Time="+currentTimes[(int)currentSelectionImg.getY()]+"\n");
	sb.append("Min\tMax\tMean\n");
	//sb.append(localDistDataMin+"\t"+localDistDataMax+"\t"+localDistDataMean+"\n");
	sb.append((localDistDataMMMH != null?localDistDataMMMH.min+"\t"+localDistDataMMMH.max+"\t"+localDistDataMMMH.mean:"\"?\"\t\"?\"\t\"?\"")+"\n");
	sb.append("Dist\tValue\n");

	for(int i=0;i<currentLineScanPlot2D.getPlotDatas()[0].getIndependent().length;i+= 1){
		sb.append(currentLineScanPlot2D.getPlotDatas()[0].getIndependent()[i]+"\t");
		sb.append(currentLineScanPlot2D.getPlotDatas()[0].getDependent()[i]+"\n");
	}
	
	cbit.vcell.desktop.VCellTransferable.sendToClipboard(sb.toString());

}


/**
 * Comment
 */
private void copyTimeDataJMenuItem_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	StringBuffer sb = new StringBuffer();
	sb.append("TimeSeries Data "+" ("+currentInfo+")"+
		" Times from "+currentTimes[0]+" to "+currentTimes[currentTimes.length-1]+
		" at Distance="+ currentDistances[(int)currentSelectionImg.getX()]+" ("+currentDistances[currentDistances.length-1]+")\n");
	sb.append("Min\tMax\tMean\n");
	//sb.append(localTimeDataMin+"\t"+localTimeDataMax+"\t"+localTimeDataMean+"\n");
	sb.append((localTimeDataMMMH != null?localTimeDataMMMH.min+"\t"+localTimeDataMMMH.max+"\t"+localTimeDataMMMH.mean:"\"?\"\t\"?\"\t\"?\"")+"\n");
	sb.append("Time\tValue\n");

	for(int i=0;i<currentTimeSeriesPlot2D.getPlotDatas()[0].getIndependent().length;i+= 1){
		sb.append(currentTimeSeriesPlot2D.getPlotDatas()[0].getIndependent()[i]+"\t");
		sb.append(currentTimeSeriesPlot2D.getPlotDatas()[0].getDependent()[i]+"\n");
	}

	cbit.vcell.desktop.VCellTransferable.sendToClipboard(sb.toString());
}


/**
 * This is called when the canvas repaint.
 */
public void draw(java.awt.Graphics g) {

	g.setColor((!getJCheckBoxColor().isSelected()?java.awt.Color.white:java.awt.Color.red));
	if(currentSelectionUnit != null){
		int width = (int)getimagePaneView1().getImagePaneModel().getDimension().getWidth();
		int height = (int)getimagePaneView1().getImagePaneModel().getDimension().getHeight();
		int y = (int)((height-1) * currentSelectionUnit.getY());
		int x = (int)((width-1) * currentSelectionUnit.getX());
		g.drawLine(0,y-1,width-1,y-1);
		g.drawLine(0,y,width-1,y);
		g.drawLine(0,y+1,width-1,y+1);
		
		g.drawLine(x-1,0,x-1,height-1);
		g.drawLine(x,0,x,height-1);
		g.drawLine(x+1,0,x+1,height-1);
	}
}


/**
 * Return the JPanel4 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getButtonsJPanel() {
	if (ivjButtonsJPanel == null) {
		try {
			ivjButtonsJPanel = new javax.swing.JPanel();
			ivjButtonsJPanel.setName("ButtonsJPanel");
			ivjButtonsJPanel.setLayout(new java.awt.GridBagLayout());

			java.awt.GridBagConstraints constraintsZoomDownJButton = new java.awt.GridBagConstraints();
			constraintsZoomDownJButton.gridx = 2; constraintsZoomDownJButton.gridy = 0;
			constraintsZoomDownJButton.insets = new java.awt.Insets(4, 4, 4, 4);
			getButtonsJPanel().add(getZoomDownJButton(), constraintsZoomDownJButton);

			java.awt.GridBagConstraints constraintsZoomUpJButton = new java.awt.GridBagConstraints();
			constraintsZoomUpJButton.gridx = 3; constraintsZoomUpJButton.gridy = 0;
			constraintsZoomUpJButton.insets = new java.awt.Insets(4, 4, 4, 4);
			getButtonsJPanel().add(getZoomUpJButton(), constraintsZoomUpJButton);

			java.awt.GridBagConstraints constraintsCopyJButton = new java.awt.GridBagConstraints();
			constraintsCopyJButton.gridx = 4; constraintsCopyJButton.gridy = 0;
			constraintsCopyJButton.insets = new java.awt.Insets(4, 4, 4, 4);
			getButtonsJPanel().add(getCopyJButton(), constraintsCopyJButton);

			java.awt.GridBagConstraints constraintsVarNamesJComboBox = new java.awt.GridBagConstraints();
			constraintsVarNamesJComboBox.gridx = 1; constraintsVarNamesJComboBox.gridy = 0;
			constraintsVarNamesJComboBox.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsVarNamesJComboBox.weightx = 1.0;
			constraintsVarNamesJComboBox.insets = new java.awt.Insets(4, 4, 4, 4);
			getButtonsJPanel().add(getVarNamesJComboBox(), constraintsVarNamesJComboBox);

			java.awt.GridBagConstraints constraintsJLabel3 = new java.awt.GridBagConstraints();
			constraintsJLabel3.gridx = 0; constraintsJLabel3.gridy = 0;
			constraintsJLabel3.insets = new java.awt.Insets(4, 4, 4, 4);
			getButtonsJPanel().add(getJLabel3(), constraintsJLabel3);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButtonsJPanel;
}

/**
 * Return the ColorMapJlabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getColorMapJLabel() {
	if (ivjColorMapJLabel == null) {
		try {
			ivjColorMapJLabel = new javax.swing.JLabel();
			ivjColorMapJLabel.setName("ColorMapJLabel");
			ivjColorMapJLabel.setText("cm");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjColorMapJLabel;
}

/**
 * Return the CopyJButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getCopyJButton() {
	if (ivjCopyJButton == null) {
		try {
			ivjCopyJButton = new javax.swing.JButton();
			ivjCopyJButton.setName("CopyJButton");
			ivjCopyJButton.setText("Copy All");
			ivjCopyJButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjCopyJButton;
}

/**
 * Return the CopyJMenuItem property value.
 * @return javax.swing.JMenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JMenuItem getCopyJMenuItem() {
	if (ivjCopyJMenuItem == null) {
		try {
			ivjCopyJMenuItem = new javax.swing.JMenuItem();
			ivjCopyJMenuItem.setName("CopyJMenuItem");
			ivjCopyJMenuItem.setText("Copy All");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjCopyJMenuItem;
}

/**
 * Return the CopyJPopupMenu property value.
 * @return javax.swing.JPopupMenu
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPopupMenu getCopyJPopupMenu() {
	if (ivjCopyJPopupMenu == null) {
		try {
			ivjCopyJPopupMenu = new javax.swing.JPopupMenu();
			ivjCopyJPopupMenu.setName("CopyJPopupMenu");
			ivjCopyJPopupMenu.add(getCopyJMenuItem());
			ivjCopyJPopupMenu.add(getCopyTimeDataJMenuItem());
			ivjCopyJPopupMenu.add(getCopyLineScanJMenuItem());
			ivjCopyJPopupMenu.add(getScaleImageAllJMenuItem());
			ivjCopyJPopupMenu.add(getScaleImageLSJMenuItem());
			ivjCopyJPopupMenu.add(getScaleImageTSJMenuItem());
			ivjCopyJPopupMenu.add(getScaleImageUDJMenuItem());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjCopyJPopupMenu;
}

/**
 * Return the CopyLineScanJMenuItem property value.
 * @return javax.swing.JMenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JMenuItem getCopyLineScanJMenuItem() {
	if (ivjCopyLineScanJMenuItem == null) {
		try {
			ivjCopyLineScanJMenuItem = new javax.swing.JMenuItem();
			ivjCopyLineScanJMenuItem.setName("CopyLineScanJMenuItem");
			ivjCopyLineScanJMenuItem.setText("Copy LineScan");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjCopyLineScanJMenuItem;
}


/**
 * Return the CopyTimeDataJMenuItem property value.
 * @return javax.swing.JMenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JMenuItem getCopyTimeDataJMenuItem() {
	if (ivjCopyTimeDataJMenuItem == null) {
		try {
			ivjCopyTimeDataJMenuItem = new javax.swing.JMenuItem();
			ivjCopyTimeDataJMenuItem.setName("CopyTimeDataJMenuItem");
			ivjCopyTimeDataJMenuItem.setText("Copy TimeSeries");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjCopyTimeDataJMenuItem;
}


/**
 * Return the DisplayAdapterService1 property value.
 * @return cbit.image.DisplayAdapterService
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private cbit.image.DisplayAdapterService getDisplayAdapterService1() {
	if (ivjDisplayAdapterService1 == null) {
		try {
			ivjDisplayAdapterService1 = new cbit.image.DisplayAdapterService();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjDisplayAdapterService1;
}


/**
 * Return the JLabel1 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getDisplayJLabel() {
	if (ivjDisplayJLabel == null) {
		try {
			ivjDisplayJLabel = new javax.swing.JLabel();
			ivjDisplayJLabel.setName("DisplayJLabel");
			ivjDisplayJLabel.setText("DisplayJLabel");
			ivjDisplayJLabel.setMaximumSize(new java.awt.Dimension(80, 14));
			ivjDisplayJLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			ivjDisplayJLabel.setPreferredSize(new java.awt.Dimension(80, 14));
			ivjDisplayJLabel.setMinimumSize(new java.awt.Dimension(80, 14));
			ivjDisplayJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjDisplayJLabel;
}

/**
 * Return the JPanel2 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getGraphJPanel() {
	if (ivjGraphJPanel == null) {
		try {
			ivjGraphJPanel = new javax.swing.JPanel();
			ivjGraphJPanel.setName("GraphJPanel");
			ivjGraphJPanel.setLayout(new java.awt.GridBagLayout());
			ivjGraphJPanel.setPreferredSize(new java.awt.Dimension(300, 350));
			ivjGraphJPanel.setMinimumSize(new java.awt.Dimension(300, 350));

			java.awt.GridBagConstraints constraintsPlotPaneTimeSeries = new java.awt.GridBagConstraints();
			constraintsPlotPaneTimeSeries.gridx = 0; constraintsPlotPaneTimeSeries.gridy = 1;
			constraintsPlotPaneTimeSeries.fill = java.awt.GridBagConstraints.BOTH;
			constraintsPlotPaneTimeSeries.weightx = 1.0;
			constraintsPlotPaneTimeSeries.weighty = 1.0;
			constraintsPlotPaneTimeSeries.insets = new java.awt.Insets(4, 0, 2, 4);
			getGraphJPanel().add(getPlotPaneTimeSeries(), constraintsPlotPaneTimeSeries);

			java.awt.GridBagConstraints constraintsPlotPaneLineScan = new java.awt.GridBagConstraints();
			constraintsPlotPaneLineScan.gridx = 0; constraintsPlotPaneLineScan.gridy = 0;
			constraintsPlotPaneLineScan.fill = java.awt.GridBagConstraints.BOTH;
			constraintsPlotPaneLineScan.weightx = 1.0;
			constraintsPlotPaneLineScan.weighty = 1.0;
			constraintsPlotPaneLineScan.insets = new java.awt.Insets(2, 0, 4, 4);
			getGraphJPanel().add(getPlotPaneLineScan(), constraintsPlotPaneLineScan);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjGraphJPanel;
}

/**
 * Return the ImagePaneScroller1 property value.
 * @return cbit.image.ImagePaneScroller
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private cbit.image.ImagePaneScroller getImagePaneScroller1() {
	if (ivjImagePaneScroller1 == null) {
		try {
			ivjImagePaneScroller1 = new cbit.image.ImagePaneScroller();
			ivjImagePaneScroller1.setName("ImagePaneScroller1");
			ivjImagePaneScroller1.setPreferredSize(new java.awt.Dimension(400, 350));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjImagePaneScroller1;
}

/**
 * Return the imagePaneView1 property value.
 * @return cbit.image.ImagePaneView
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private cbit.image.ImagePaneView getimagePaneView1() {
	// user code begin {1}
	// user code end
	return ivjimagePaneView1;
}


/**
 * Return the ImagePlaneManager1 property value.
 * @return cbit.image.ImagePlaneManager
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private cbit.image.ImagePlaneManager getImagePlaneManager1() {
	if (ivjImagePlaneManager1 == null) {
		try {
			ivjImagePlaneManager1 = new cbit.image.ImagePlaneManager();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjImagePlaneManager1;
}


/**
 * Return the JCheckBox1 property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getJCheckBox1() {
	if (ivjJCheckBox1 == null) {
		try {
			ivjJCheckBox1 = new javax.swing.JCheckBox();
			ivjJCheckBox1.setName("JCheckBox1");
			ivjJCheckBox1.setText("Local Scaling");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJCheckBox1;
}


/**
 * Return the JLabel1 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1() {
	if (ivjJLabel1 == null) {
		try {
			ivjJLabel1 = new javax.swing.JLabel();
			ivjJLabel1.setName("JLabel1");
			ivjJLabel1.setText("Max");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1;
}


/**
 * Return the JLabel2 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel2() {
	if (ivjJLabel2 == null) {
		try {
			ivjJLabel2 = new javax.swing.JLabel();
			ivjJLabel2.setName("JLabel2");
			ivjJLabel2.setText("Min");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel2;
}


/**
 * Return the JLabel3 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel3() {
	if (ivjJLabel3 == null) {
		try {
			ivjJLabel3 = new javax.swing.JLabel();
			ivjJLabel3.setName("JLabel3");
			ivjJLabel3.setText("Variables");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel3;
}


/**
 * Return the JPanel1 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel1() {
	if (ivjJPanel1 == null) {
		try {
			ivjJPanel1 = new javax.swing.JPanel();
			ivjJPanel1.setName("JPanel1");
			ivjJPanel1.setLayout(new java.awt.GridBagLayout());

			java.awt.GridBagConstraints constraintsDisplayJLabel = new java.awt.GridBagConstraints();
			constraintsDisplayJLabel.gridx = 0; constraintsDisplayJLabel.gridy = 0;
			constraintsDisplayJLabel.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsDisplayJLabel.weightx = 1.0;
			constraintsDisplayJLabel.insets = new java.awt.Insets(4, 0, 0, 4);
			getJPanel1().add(getDisplayJLabel(), constraintsDisplayJLabel);

			java.awt.GridBagConstraints constraintsJCheckBox1 = new java.awt.GridBagConstraints();
			constraintsJCheckBox1.gridx = 1; constraintsJCheckBox1.gridy = 0;
			constraintsJCheckBox1.insets = new java.awt.Insets(4, 0, 0, 4);
			getJPanel1().add(getJCheckBox1(), constraintsJCheckBox1);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel1;
}

/**
 * Return the JPanel3 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel3() {
	if (ivjJPanel3 == null) {
		try {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			ivjJPanel3 = new javax.swing.JPanel();
			ivjJPanel3.setName("JPanel3");
			ivjJPanel3.setLayout(new java.awt.GridBagLayout());

			java.awt.GridBagConstraints constraintsMaxJLabel = new java.awt.GridBagConstraints();
			constraintsMaxJLabel.gridx = 0;
 	constraintsMaxJLabel.gridy = 3;
			java.awt.GridBagConstraints constraintsColorMapJLabel = new java.awt.GridBagConstraints();
			constraintsColorMapJLabel.gridx = 0;
 	constraintsColorMapJLabel.gridy = 4;
			constraintsColorMapJLabel.anchor = java.awt.GridBagConstraints.EAST;
			constraintsColorMapJLabel.insets = new java.awt.Insets(4, 4, 4, 4);
			ivjJPanel3.add(getMaxJLabel(), constraintsMaxJLabel);
			java.awt.GridBagConstraints constraintsMinJLabel = new java.awt.GridBagConstraints();
			constraintsMinJLabel.gridx = 0;
 	constraintsMinJLabel.gridy = 5;
			ivjJPanel3.add(getColorMapJLabel(), constraintsColorMapJLabel);
			java.awt.GridBagConstraints constraintsJLabel1 = new java.awt.GridBagConstraints();
			constraintsJLabel1.gridx = 0;
 	constraintsJLabel1.gridy = 2;
			java.awt.GridBagConstraints constraintsJLabel2 = new java.awt.GridBagConstraints();
			constraintsJLabel2.gridx = 0;
 	constraintsJLabel2.gridy = 6;
	ivjJPanel3.add(getMinJLabel(), constraintsMinJLabel);
			ivjJPanel3.add(getJLabel1(), constraintsJLabel1);
			java.awt.GridBagConstraints constraintsScaleImageModeJLabel = new java.awt.GridBagConstraints();
			constraintsScaleImageModeJLabel.gridx = 0;
 	constraintsScaleImageModeJLabel.gridy = 1;
	ivjJPanel3.add(getJLabel2(), constraintsJLabel2);
	ivjJPanel3.add(getScaleImageModeJLabel(), constraintsScaleImageModeJLabel);
			ivjJPanel3.add(getJCheckBoxColor(), gridBagConstraints);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}	}
	return ivjJPanel3;
}

/**
 * Return the JLabel1 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getMaxJLabel() {
	if (ivjMaxJLabel == null) {
		try {
			ivjMaxJLabel = new javax.swing.JLabel();
			ivjMaxJLabel.setName("MaxJLabel");
			ivjMaxJLabel.setText("255");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjMaxJLabel;
}

/**
 * Return the JLabel2 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getMinJLabel() {
	if (ivjMinJLabel == null) {
		try {
			ivjMinJLabel = new javax.swing.JLabel();
			ivjMinJLabel.setName("MinJLabel");
			ivjMinJLabel.setText("0");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjMinJLabel;
}

/**
 * Return the PlotPane2 property value.
 * @return cbit.plot.PlotPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private cbit.plot.PlotPane getPlotPaneLineScan() {
	if (ivjPlotPaneLineScan == null) {
		try {
			ivjPlotPaneLineScan = new cbit.plot.PlotPane();
			ivjPlotPaneLineScan.setName("PlotPaneLineScan");
			ivjPlotPaneLineScan.setBorder(new org.vcell.util.gui.LineBorderBean());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjPlotPaneLineScan;
}

/**
 * Return the PlotPane1 property value.
 * @return cbit.plot.PlotPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private cbit.plot.PlotPane getPlotPaneTimeSeries() {
	if (ivjPlotPaneTimeSeries == null) {
		try {
			ivjPlotPaneTimeSeries = new cbit.plot.PlotPane();
			ivjPlotPaneTimeSeries.setName("PlotPaneTimeSeries");
			ivjPlotPaneTimeSeries.setBorder(new org.vcell.util.gui.LineBorderBean());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjPlotPaneTimeSeries;
}

/**
 * Return the ScaleImageAllJMenuItem property value.
 * @return javax.swing.JMenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JMenuItem getScaleImageAllJMenuItem() {
	if (ivjScaleImageAllJMenuItem == null) {
		try {
			ivjScaleImageAllJMenuItem = new javax.swing.JMenuItem();
			ivjScaleImageAllJMenuItem.setName("ScaleImageAllJMenuItem");
			ivjScaleImageAllJMenuItem.setText("Scale Image to All Data");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjScaleImageAllJMenuItem;
}


/**
 * Return the ScaleImageLSJMenuItem property value.
 * @return javax.swing.JMenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JMenuItem getScaleImageLSJMenuItem() {
	if (ivjScaleImageLSJMenuItem == null) {
		try {
			ivjScaleImageLSJMenuItem = new javax.swing.JMenuItem();
			ivjScaleImageLSJMenuItem.setName("ScaleImageLSJMenuItem");
			ivjScaleImageLSJMenuItem.setText("Scale Image to LineScan");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjScaleImageLSJMenuItem;
}


/**
 * Return the SclaeImageModeJLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getScaleImageModeJLabel() {
	if (ivjScaleImageModeJLabel == null) {
		try {
			ivjScaleImageModeJLabel = new javax.swing.JLabel();
			ivjScaleImageModeJLabel.setName("ScaleImageModeJLabel");
			ivjScaleImageModeJLabel.setText("All");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjScaleImageModeJLabel;
}

/**
 * Return the ScaleImageTSJMenuItem property value.
 * @return javax.swing.JMenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JMenuItem getScaleImageTSJMenuItem() {
	if (ivjScaleImageTSJMenuItem == null) {
		try {
			ivjScaleImageTSJMenuItem = new javax.swing.JMenuItem();
			ivjScaleImageTSJMenuItem.setName("ScaleImageTSJMenuItem");
			ivjScaleImageTSJMenuItem.setText("Scale Image to TimeSeries");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjScaleImageTSJMenuItem;
}


/**
 * Return the ScaleImageUDJMenuItem property value.
 * @return javax.swing.JMenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JMenuItem getScaleImageUDJMenuItem() {
	if (ivjScaleImageUDJMenuItem == null) {
		try {
			ivjScaleImageUDJMenuItem = new javax.swing.JMenuItem();
			ivjScaleImageUDJMenuItem.setName("ScaleImageUDJMenuItem");
			ivjScaleImageUDJMenuItem.setText("Scale Image User Defined");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjScaleImageUDJMenuItem;
}


/**
 * Return the JComboBox1 property value.
 * @return javax.swing.JComboBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JComboBox getVarNamesJComboBox() {
	if (ivjVarNamesJComboBox == null) {
		try {
			ivjVarNamesJComboBox = new javax.swing.JComboBox();
			ivjVarNamesJComboBox.setName("VarNamesJComboBox");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjVarNamesJComboBox;
}

/**
 * Return the ZoomDownJButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getZoomDownJButton() {
	if (ivjZoomDownJButton == null) {
		try {
			ivjZoomDownJButton = new javax.swing.JButton();
			ivjZoomDownJButton.setName("ZoomDownJButton");
			ivjZoomDownJButton.setText("ZoomOut");
			ivjZoomDownJButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjZoomDownJButton;
}

/**
 * Return the ZoomUpJButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getZoomUpJButton() {
	if (ivjZoomUpJButton == null) {
		try {
			ivjZoomUpJButton = new javax.swing.JButton();
			ivjZoomUpJButton.setName("ZoomUpJButton");
			ivjZoomUpJButton.setText("ZoomIn");
			ivjZoomUpJButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjZoomUpJButton;
}

/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	// System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	// exception.printStackTrace(System.out);
}


/**
 * Comment
 */
private void imagePaneScroller1_MouseExited(java.awt.event.MouseEvent mouseEvent) {
	getDisplayJLabel().setText(NONE_MESSAGE);
}

private boolean isErrorMode(){
	if(NONE_MESSAGE.startsWith("ERROR")){
		return true;
	}
	return false;
}
/**
 * Comment
 */
private void imagePaneScroller1_MouseMoved(java.awt.event.MouseEvent mouseEvent) {
	if(isErrorMode()){return;}
	java.awt.Point point = getimagePaneView1().getImagePoint(mouseEvent.getPoint());
	if(point != null){
		int imgX = point.x;
		int imgY = point.y;
		getDisplayJLabel().setText(
			"Time = "+currentTimes[imgY]+
			"  Dist = "+currentDistances[imgX]+
			"  Value = "+rawValues[(imgY*RESAMP_SIZE) + imgX]);
		return;
	}
	
	getDisplayJLabel().setText(NONE_MESSAGE);
}


/**
 * Comment
 */
private void imagePaneView1_Copy(java.awt.event.MouseEvent mouseEvent) {
	
	if(mouseEvent.isPopupTrigger()){
		getCopyJPopupMenu().show(getImagePaneScroller1().getImagePaneView(),mouseEvent.getX(),mouseEvent.getY());
	}else if(mouseEvent.getID() == java.awt.event.MouseEvent.MOUSE_RELEASED
		//mouseEvent.getID() == java.awt.event.MouseEvent.MOUSE_CLICKED
		/*|| mouseEvent.getID() == java.awt.event.MouseEvent.MOUSE_DRAGGED*/){
		if(isErrorMode()){return;}
		java.awt.Point point = getimagePaneView1().getImagePoint(mouseEvent.getPoint());
		if(point != null){
			currentSelectionImg = point;
			currentSelectionUnit = getimagePaneView1().getImagePointUnitized(mouseEvent.getPoint());
			configurePlotData(currentSelectionImg.x,currentSelectionImg.y);
		}
	}
}


/**
 * Comment
 */
private void imagePaneView1_KeyPressed(java.awt.event.KeyEvent keyEvent) {
	if(isErrorMode()){return;}
	int dx =
		(keyEvent.getKeyCode() == KeyEvent.VK_LEFT?-1:0) + 
		(keyEvent.getKeyCode() == KeyEvent.VK_RIGHT?1:0);
	int dy =
		(keyEvent.getKeyCode() == KeyEvent.VK_UP?-1:0) + 
		(keyEvent.getKeyCode() == KeyEvent.VK_DOWN?1:0);
	if(dx != 0 ||  dy != 0){
		int imgWidth = getimagePaneView1().getImagePaneModel().getSourceData().getXSize();
		int imgHeight = getimagePaneView1().getImagePaneModel().getSourceData().getYSize();
		int newImgX = currentSelectionImg.x+dx;
		int newImgY = currentSelectionImg.y+dy;
		double newImgXUnit = (double)(newImgX)/(double)(imgWidth-1);
		double newImgYUnit = (double)(newImgY)/(double)(imgHeight-1);
		if(newImgXUnit >= 0 && newImgXUnit <= 1 && newImgYUnit >= 0 && newImgYUnit <= 1){
			currentSelectionUnit = new java.awt.geom.Point2D.Double(newImgXUnit,newImgYUnit);
			currentSelectionImg = new java.awt.Point(newImgX,newImgY);
			configurePlotData(newImgX,newImgY);
		}
	}
	//if(keyEvent.getKeyCode() == keyEvent.VK_UP || 
		//keyEvent.getKeyCode() == keyEvent.VK_DOWN || 
		//keyEvent.getKeyCode() == keyEvent.VK_LEFT || 
		//keyEvent.getKeyCode() == keyEvent.VK_RIGHT){
			
	//}
}


/**
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() throws java.lang.Exception {
	// user code begin {1}
	// user code end
	getImagePaneScroller1().addPropertyChangeListener(ivjEventHandler);
	getCopyJMenuItem().addActionListener(ivjEventHandler);
	getCopyJButton().addActionListener(ivjEventHandler);
	getZoomUpJButton().addActionListener(ivjEventHandler);
	getZoomDownJButton().addActionListener(ivjEventHandler);
	getImagePaneScroller1().addMouseListener(ivjEventHandler);
	getVarNamesJComboBox().addActionListener(ivjEventHandler);
	getJCheckBox1().addActionListener(ivjEventHandler);
	getCopyTimeDataJMenuItem().addActionListener(ivjEventHandler);
	getCopyLineScanJMenuItem().addActionListener(ivjEventHandler);
	getScaleImageAllJMenuItem().addActionListener(ivjEventHandler);
	getScaleImageLSJMenuItem().addActionListener(ivjEventHandler);
	getScaleImageTSJMenuItem().addActionListener(ivjEventHandler);
	getScaleImageUDJMenuItem().addActionListener(ivjEventHandler);
	connPtoP1SetTarget();
}

/**
 * Insert the method's description here.
 * Creation date: (12/14/2004 9:47:38 AM)
 * @param timeSeries double[][]
 * @param distances double[]
 */
public void initDataManager(
	User argUserRequestingData,
	cbit.vcell.desktop.controls.DataManager argDataManager,
	final String variableName,double initTime,int step,double endTime,
	int[] indices,int[] argCrossingMembraneIndices,
	double[] accumDistances,
	boolean waitOnInitialLoad,
	double argInitialLineScanTime,
	cbit.vcell.parser.SymbolTable argSymbolTable,
	PDEDataViewer.TimeSeriesJobStarter argTimeSeriesJobStarter)
				throws org.vcell.util.DataAccessException{

	timeSeriesJobStarter = argTimeSeriesJobStarter;
	userRequestingData = argUserRequestingData;
	symbolTable = argSymbolTable;
	currentSymbolTablEntry = null;
//	bBlockInitialLoad = waitOnInitialLoad;
	resampleStepOrig = step;
	resampleStartTimeOrig = initTime;
	resampleEndTimeOrig = endTime;
	isInit = true;
	initialLineScanTime = argInitialLineScanTime;
		
	dataManager = argDataManager;
	dataManagerAccumDistances = accumDistances;
	dataManagerIndices = indices;
	crossingMembraneIndices = argCrossingMembraneIndices;
	currentSelectionImg = new java.awt.Point(0,0);
	currentSelectionUnit = new java.awt.geom.Point2D.Double(0,0);;

	Hashtable<String, Object> hash = new Hashtable<String, Object>();
	
	AsynchClientTask task1  = new AsynchClientTask("Retrieving time points", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			DataIdentifier[] sortedDataIDs = VariableType.collectSimilarDataTypes(variableName,dataManager.getDataIdentifiers());
			hashTable.put("sortedDataIDs", sortedDataIDs);
		}
	};
	AsynchClientTask task2 = new AsynchClientTask("Setting time points", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
		public void run(Hashtable<String, Object> hashTable) throws Exception {
			DataIdentifier[] sortedDataIDs = (DataIdentifier[])hashTable.get("sortedDataIDs");
	
			//Add to combobox
			getVarNamesJComboBox().setEnabled(true);
			getVarNamesJComboBox().removeAllItems();
			getVarNamesJComboBox().removeActionListener(ivjEventHandler);	
			for(int i=0;i<sortedDataIDs.length;i+= 1){
				getVarNamesJComboBox().addItem(sortedDataIDs[i].getName());
			}
			getVarNamesJComboBox().addActionListener(ivjEventHandler);
		
			getVarNamesJComboBox().setSelectedItem(variableName);
		}
	};
	AsynchClientTask[] taskArray = new AsynchClientTask[]{task1, task2};
	ClientTaskDispatcher.dispatch(this, hash, taskArray, false, true, null);
}


private boolean failMethod(final Throwable timeSeriesJobFailed,final String varName){
	try {
		if(timeSeriesJobFailed != null){
			javax.swing.SwingUtilities.invokeAndWait(
					new Runnable(){
						public void run(){
							NONE_MESSAGE = "ERROR ("+varName+") -- "+timeSeriesJobFailed.getMessage();
							getDisplayJLabel().setText(NONE_MESSAGE);
							currentSelectionImg.x = 0;
							currentSelectionImg.y = 0;
							currentSelectionUnit = new Point2D.Double(0,0);
							try {
								initStandAloneTimeSeries_private(new double[][] {{0,1},{0,0},{0,0}}, new double[] {0,1});
							} catch (DataAccessException e) {
								failMethod(e, varName);
								e.printStackTrace();
							}
//						getImagePaneScroller1().setVisible(false);
							getPlotPaneLineScan().setPlot2D(null);
							getPlotPaneTimeSeries().setPlot2D(null);
							getZoomUpJButton().setEnabled(false);
							getZoomDownJButton().setEnabled(false);
							getJCheckBox1().setEnabled(false);
							getJCheckBoxColor().setEnabled(false);
							getCopyJButton().setEnabled(false);
//						getPlotPaneLineScan().setEnabled(false);
//						getPlotPaneTimeSeries().setEnabled(false);
							BeanUtils.enableComponents(getCopyJPopupMenu(), false);
						}
					}
				);
			return true;
		}else{
			javax.swing.SwingUtilities.invokeAndWait(
					new Runnable(){
						public void run(){
							NONE_MESSAGE = NORMAL_MESSAGE;
//						getImagePaneScroller1().setVisible(true);
							getZoomUpJButton().setEnabled(true);
							getZoomDownJButton().setEnabled(true);
							getJCheckBox1().setEnabled(true);
							getJCheckBoxColor().setEnabled(true);
							getCopyJButton().setEnabled(true);
							getCopyJPopupMenu().setEnabled(true);
//						getPlotPaneLineScan().setEnabled(true);
//						getPlotPaneTimeSeries().setEnabled(true);
							BeanUtils.enableComponents(getCopyJPopupMenu(), true);
						}
					}
				);
			return false;
		}
	} catch (Exception e) {
		PopupGenerator.showErrorDialog(this.getClass().getName()+".failMethod: "+e.getMessage());
		e.printStackTrace();
		return true;
	}

}
/**
 * Insert the method's description here.
 * Creation date: (12/14/2004 9:47:38 AM)
 * @param timeSeries double[][]
 * @param distances double[]
 */
private void initDataManagerVariable(final String finalVarName) {

	//Create SymbolTableEntry for Copy/Paste functionality
	try{
		currentSymbolTablEntry = (symbolTable != null?symbolTable.getEntry(finalVarName):null);
	}catch(cbit.vcell.parser.ExpressionBindingException e){
		e.printStackTrace();
	}
	
	try{
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		
		AsynchClientTask task1  = new AsynchClientTask("Retrieving time points", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {	
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				double[] timeValues = dataManager.getDataSetTimes();
				hashTable.put("times", timeValues);
			}
		};
		AsynchClientTask task2  = new AsynchClientTask("Retrieving time series", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				double[] timeValues = (double[])hashTable.get("times");
				TimeSeriesJobSpec timeSeriesJobSpec =
					new TimeSeriesJobSpec(
							new String[] {finalVarName},
							new int[][] {dataManagerIndices},
							(crossingMembraneIndices != null?new int[][] {crossingMembraneIndices}:null),
							resampleStartTimeOrig,resampleStepOrig,timeValues[timeValues.length-1]/*resampleEndTimeOrig*/,
							VCDataJobID.createVCDataJobID(userRequestingData, true));
				
				PDEDataViewer.TimeSeriesJobResultsAction action = new PDEDataViewer.TimeSeriesJobResultsAction(){
					private TimeSeriesJobResults timeSeriesJobResults;
					private Throwable timeSeriesJobFailed;
					
					public void setTimeSeriesJobResults(TimeSeriesJobResults timeSeriesJobResults) {
						this.timeSeriesJobResults = timeSeriesJobResults;
					}
					public void setTimeSeriesJobFailed(Throwable e) {
						timeSeriesJobFailed = e;
					}
		
					public void run() {
						try {
							currentInfo = finalVarName;
							if(failMethod(timeSeriesJobFailed,finalVarName)){return;}
							final double[][] timeSeries = ((TSJobResultsNoStats)timeSeriesJobResults).getTimesAndValuesForVariable(finalVarName);
							javax.swing.SwingUtilities.invokeAndWait(
								new Runnable(){
									public void run(){
										try {
											initStandAloneTimeSeries_private(timeSeries,dataManagerAccumDistances);
										} catch (Exception e) {
											failMethod(e,finalVarName);
											e.printStackTrace();
										}
									}
								}
							);
							if(isInit){// set crosshair to init time
								double initTime = initialLineScanTime;//resampleStartTimeOrig;
								isInit = false;
								int closestTimeIndex = 0;
								double closestDiff = Double.MAX_VALUE;
								for(int i=0;i<currentTimes.length;i+= 1){
									double diff = Math.abs(initTime-currentTimes[i]);
									if( diff < closestDiff){
										closestTimeIndex = i;
										closestDiff = diff;
									}
								}
								currentSelectionImg = new java.awt.Point(0,closestTimeIndex);
								currentSelectionUnit = new java.awt.geom.Point2D.Double(0,(double)closestTimeIndex/(double)(currentTimes.length-1));
								javax.swing.SwingUtilities.invokeAndWait(
									new Runnable(){
										public void run(){
											configurePlotData((int)currentSelectionImg.getX(),(int)currentSelectionImg.getY());
										}
									}
								);
								javax.swing.SwingUtilities.invokeAndWait(
										new Runnable(){
											public void run(){
												zoomToFill();
											}
										}
									);
								
							}else{
								getImagePaneScroller1().zooming(new cbit.image.ZoomEvent(getimagePaneView1(),0,0));
							}
						} catch (Exception e) {
							PopupGenerator.showErrorDialog("Kymograph: "+e.getMessage());
							e.printStackTrace();
						}
					}
				};
				timeSeriesJobStarter.startTimeSeriesJob(timeSeriesJobSpec, action, false);
			}
		};
		AsynchClientTask[] taskArray = new AsynchClientTask[]{task1, task2};
		ClientTaskDispatcher.dispatch(this, hash, taskArray, false, true, null);
	}catch(Throwable e){
		final Throwable finale = e;
					cbit.vcell.client.PopupGenerator.showErrorDialog(
						"Error init variable "+finalVarName+" "+finale.getClass().getName()+" "+finale.getMessage());
	}	
}


/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("Kymograph");
		setLayout(new java.awt.GridBagLayout());
		setSize(736, 448);

		java.awt.GridBagConstraints constraintsImagePaneScroller1 = new java.awt.GridBagConstraints();
		constraintsImagePaneScroller1.gridx = 1; constraintsImagePaneScroller1.gridy = 0;
		constraintsImagePaneScroller1.fill = java.awt.GridBagConstraints.BOTH;
		constraintsImagePaneScroller1.weightx = 1.0;
		constraintsImagePaneScroller1.weighty = 1.0;
		add(getImagePaneScroller1(), constraintsImagePaneScroller1);

		java.awt.GridBagConstraints constraintsJPanel1 = new java.awt.GridBagConstraints();
		constraintsJPanel1.gridx = 0; constraintsJPanel1.gridy = 1;
		constraintsJPanel1.gridwidth = 3;
		constraintsJPanel1.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsJPanel1.weightx = 1.0;
		add(getJPanel1(), constraintsJPanel1);

		java.awt.GridBagConstraints constraintsGraphJPanel = new java.awt.GridBagConstraints();
		constraintsGraphJPanel.gridx = 2; constraintsGraphJPanel.gridy = 0;
		constraintsGraphJPanel.fill = java.awt.GridBagConstraints.BOTH;
		add(getGraphJPanel(), constraintsGraphJPanel);

		java.awt.GridBagConstraints constraintsJPanel3 = new java.awt.GridBagConstraints();
		constraintsJPanel3.gridx = 0; constraintsJPanel3.gridy = 0;
		constraintsJPanel3.fill = java.awt.GridBagConstraints.BOTH;
		constraintsJPanel3.insets = new java.awt.Insets(4, 4, 4, 0);
		add(getJPanel3(), constraintsJPanel3);

		java.awt.GridBagConstraints constraintsButtonsJPanel = new java.awt.GridBagConstraints();
		constraintsButtonsJPanel.gridx = 0; constraintsButtonsJPanel.gridy = 2;
		constraintsButtonsJPanel.gridwidth = 3;
		constraintsButtonsJPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsButtonsJPanel.weightx = 1.0;
		constraintsButtonsJPanel.insets = new java.awt.Insets(0, 4, 0, 4);
		add(getButtonsJPanel(), constraintsButtonsJPanel);
		initConnections();
		connEtoC1();
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	// user code end
}

/**
 * Insert the method's description here.
 * Creation date: (12/14/2004 9:47:38 AM)
 * @param timeSeries double[][]
 * @param distances double[]
 */
public void initStandAloneTimeSeries(double[][] timeSeries,double[] accumDistances,String info /*,double initTime,double endTime*/) throws DataAccessException{

	//timeseries is in the format returned by pdeDatacontext.getTimeSeries
	//timeSeries[0][0...numTimePoints-1] = timePointArray
	//timeSeries[1...numSamplePoints][0...numTimePoints-1] = dataValueArrays

	isInit = true;
	resampleStartTimeOrig = timeSeries[0][0];
	resampleStepOrig = 1;
	resampleEndTimeOrig = timeSeries[0][timeSeries[0].length-1];
	currentInfo = info;
	currentSelectionImg = new java.awt.Point(0,0);
	currentSelectionUnit = new java.awt.geom.Point2D.Double(0,0);;
	getVarNamesJComboBox().removeAllItems();
	getVarNamesJComboBox().removeActionListener(ivjEventHandler);	
	getVarNamesJComboBox().addItem(info);
	getVarNamesJComboBox().setEnabled(false);
	getVarNamesJComboBox().addActionListener(ivjEventHandler);
	initStandAloneTimeSeries_private(timeSeries,accumDistances);
}


/**
 * Insert the method's description here.
 * Creation date: (12/14/2004 9:47:38 AM)
 * @param timeSeries double[][]
 * @param distances double[]
 */
private void initStandAloneTimeSeries_private(double[][] timeSeriesOrig,double[] accumDistancesOrig) throws DataAccessException {

	//timesereis is in the format returned by pdeDatacontext.getTimeSeries
	//timeSeries[0][0...numTimePoints-1] = timePointArray
	//timeSeries[1...numSamplePoints][0...numTimePoints-1] = dataValueArrays

	if(accumDistancesOrig.length != (timeSeriesOrig.length-1)){
		throw new IllegalArgumentException(this.getClass().getName()+" accumDistances.length != numSamplePoints");
	}


	timeSeriesDataOrig = timeSeriesOrig;
	accumDistancesDataOrig = accumDistancesOrig;
	
	currentTimes = timeSeriesDataOrig[0];
	//Resample for even distances
	RESAMP_SIZE = timeSeriesDataOrig.length-1;
	int rvSize = (currentTimes.length)*RESAMP_SIZE;
	rawValues = new double[rvSize];
	double incr = accumDistancesDataOrig[accumDistancesDataOrig.length-1]/(double)(RESAMP_SIZE-1);
	currentDistances = new double[RESAMP_SIZE];
	for(int j=0;j<currentTimes.length;j+= 1){
		int sourceIndex = 0;
		double currentDistance = 0;
		for(int k=0;k<RESAMP_SIZE;k+= 1){
			while(currentDistance > accumDistancesDataOrig[sourceIndex+1]){
				sourceIndex+= 1;
			}
			double subShort = currentDistance-accumDistancesDataOrig[sourceIndex];
			double subLong  = accumDistancesDataOrig[sourceIndex+1]-accumDistancesDataOrig[sourceIndex];
			double proportion = subShort/subLong;
//System.out.println("prop="+proportion+" j="+j+" k="+k+" sourceIndex="+sourceIndex+"sourcedist="+accumDistancesDataOrig[sourceIndex]+" currentDistance="+currentDistance);
			double value = timeSeriesDataOrig[1+sourceIndex+(proportion > .5?1:0)][j];
			//double value = timeSeriesDataOrig[1+sourceIndex][j] + ((timeSeriesDataOrig[1+sourceIndex+1][j]-timeSeriesDataOrig[1+sourceIndex][j])*proportion);
			//if(k == (RESAMP_SIZE-1)){
				//value = timeSeriesDataOrig[timeSeriesDataOrig.length-1][j];
			//}
			rawValues[(j*RESAMP_SIZE)+(k)] = value;
			currentDistances[k] = currentDistance;
			currentDistance+= incr;
			if(currentDistance > accumDistancesDataOrig[accumDistancesDataOrig.length-1]){
				currentDistance = accumDistancesDataOrig[accumDistancesDataOrig.length-1];
			}
		}
	}

	Range minmaxRange = null;
	allDataMMMH = calcMMM(rawValues);
	if(allDataMMMH != null){
		minmaxRange = allDataMMMH.getRange();
	}
	
	cbit.image.SourceDataInfo sdi =
		new cbit.image.SourceDataInfo(
			cbit.image.SourceDataInfo.RAW_VALUE_TYPE,
			rawValues,
			minmaxRange,
			0,
			RESAMP_SIZE,1,
			0,accumDistancesDataOrig[accumDistancesDataOrig.length-1],
			currentTimes.length,RESAMP_SIZE,
			currentTimes[0],currentTimes[currentTimes.length-1]-currentTimes[0]
		);

	getImagePaneScroller1().getImagePaneModel().setSourceData(sdi);
	getImagePlaneManager1().setSourceDataInfo(sdi);
	if(isInit){
		zoomUpJButton_ActionPerformed(null);
		zoomDownJButton_ActionPerformed(null);
		//getImagePaneScroller1().getImagePaneModel().changeZoomToFillViewport();
	}


	getDisplayJLabel().setText(NONE_MESSAGE);
	configurePlotData(currentSelectionImg.x,currentSelectionImg.y);

}


/**
 * Comment
 */
private void jCheckBox1_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	
	bLocalScaling = getJCheckBox1().isSelected();
	configurePlotData((int)currentSelectionImg.getX(),(int)currentSelectionImg.getY());
}


/**
 * Comment
 */
private void jComboBox1_ActionPerformed(java.awt.event.ActionEvent actionEvent) {

	initDataManagerVariable((String)getVarNamesJComboBox().getSelectedItem());
	getimagePaneView1().requestFocusInWindow();
}


/**
 * Comment
 */
private void kymograph_Initialize() {
	
	getDisplayAdapterService1().addColorModelForValues(DisplayAdapterService.createGrayColorModel(), DisplayAdapterService.createGraySpecialColors(), "Gray");
	getDisplayAdapterService1().addColorModelForValues(DisplayAdapterService.createBlueRedColorModel(), DisplayAdapterService.createBlueRedSpecialColors(), "BlueRed");
	getDisplayAdapterService1().setActiveColorModelID("BlueRed");

	//int[] sc = getDisplayAdapterService1().fetchSpecialColors("BlueRed");
	//sc[DisplayAdapterService.BELOW_MIN_COLOR_OFFSET] = java.awt.Color.darkGray.getRGB();
	//sc[DisplayAdapterService.ABOVE_MAX_COLOR_OFFSET] = java.awt.Color.lightGray.getRGB();
	//getDisplayAdapterService1().updateSpecialColors("BlueRed",sc);
	updateColorMapDisplay();
	
	getImagePaneScroller1().getImagePaneModel().setMode(cbit.image.ImagePaneModel.MESH_MODE);
	getImagePaneScroller1().initRowColumnDescriptions("simulation Time","Distance Along Sample Line");
	getimagePaneView1().setDrawPaneModel(this);
	getDisplayJLabel().setText(NONE_MESSAGE);
	getPlotPaneTimeSeries().setBCompact(true);
	getPlotPaneLineScan().setBCompact(true);
	getImagePaneScroller1().getImagePaneModel().setDisplayAdapterService(getDisplayAdapterService1());
	getImagePaneScroller1().getImagePaneModel().setBackgroundColor(new java.awt.Color(32,32,32));
	getImagePaneScroller1().setImagePlaneManager(getImagePlaneManager1());

}


/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		final int numTP = 27;
		final int numDP = 100;
		double[][] timeSeries = new double[numDP+1][numTP];
		double[] accumArr = new double[numDP];
		timeSeries[0] = new double[numTP];
		for(int i=0;i<numTP;i+= 1){
			timeSeries[0][i] = i+10;
		}
		java.util.Random rand = new java.util.Random();
		for(int i=0;i<numDP;i+= 1){
			accumArr[i] = (i==0?0:accumArr[i-1]+rand.nextDouble()*10);
			for(int j=0;j<numTP;j+= 1){
				//timeSeries[i+1][j] = rand.nextDouble()*10000;
				timeSeries[i+1][j] = j*numTP + i;
			}
			if(args.length > 0 && args[0].equals("allNAN")){
				java.util.Arrays.fill(timeSeries[i+1],Double.NaN);
			}
		}
		timeSeries[1][0] = Double.NaN;
		timeSeries[1][1] = Double.NaN;
		timeSeries[2][0] = Double.NaN;
		timeSeries[2][1] = Double.NaN;
			
		javax.swing.JFrame frame = new javax.swing.JFrame();
		KymographPanel aKymograph;
		aKymograph = new KymographPanel();
		frame.setContentPane(aKymograph);
		frame.setSize(aKymograph.getSize());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		frame.setVisible(true);
		java.awt.Insets insets = frame.getInsets();
		frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
		frame.setVisible(true);
		aKymograph.initStandAloneTimeSeries(timeSeries,accumArr,"Test Data"/*,timeSeries[0][0],timeSeries[0][timeSeries[0].length-1]*/);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JPanel");
		exception.printStackTrace(System.out);
	}
}


/**
 * Comment
 */
private void scaleImageModeFromMenu(java.awt.event.ActionEvent actionEvent) {
	
	if(actionEvent.getSource() == getScaleImageAllJMenuItem()){
		scaleImageMode = SCALE_IMAGE_ALL;
	}else if(actionEvent.getSource() == getScaleImageLSJMenuItem()){
		scaleImageMode = SCALE_IMAGE_LINESCAN;
	}else if(actionEvent.getSource() == getScaleImageTSJMenuItem()){
		scaleImageMode = SCALE_IMAGE_TIMESERIES;
	}else if(actionEvent.getSource() == getScaleImageUDJMenuItem()){
		try{
			String userMinMaxS =
				cbit.vcell.client.PopupGenerator.showInputDialog(this,
				"Enter min and max scaling value separated by comma (e.g. 0,100)",
				(allDataMMMH != null?allDataMMMH.getRange().getMin()+","+allDataMMMH.getRange().getMax():"?,?"));
			int commaIndex = userMinMaxS.indexOf(",");
			if(commaIndex == -1){
				cbit.vcell.client.PopupGenerator.showErrorDialog("Min and Max values must be separted by a comma");
				return;
			}
			try{
				double min = Double.parseDouble(userMinMaxS.substring(0,commaIndex));
				double max = Double.parseDouble(userMinMaxS.substring(commaIndex+1,userMinMaxS.length()));
				userDefinedMMMH = new MinMaxMeanHolder(min,max,(min+max)/2);
			}catch(NumberFormatException e){
				cbit.vcell.client.PopupGenerator.showErrorDialog("Min or Max value cannot be parsed to a number");
				return;
			}
			scaleImageMode = SCALE_IMAGE_USERDEFINED;
		}catch(org.vcell.util.UserCancelException e){
			//getimagePaneView1().getImagePaneModel().updateViewPortImage();
			getimagePaneView1().repaint();
			return;
		}
	}

	configureMinMax();
}


/**
 * Set the imagePaneView1 to a new value.
 * @param newValue cbit.image.ImagePaneView
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setimagePaneView1(cbit.image.ImagePaneView newValue) {
	if (ivjimagePaneView1 != newValue) {
		try {
			/* Stop listening for events from the current object */
			if (ivjimagePaneView1 != null) {
				ivjimagePaneView1.removeMouseMotionListener(ivjEventHandler);
				ivjimagePaneView1.removeMouseListener(ivjEventHandler);
				ivjimagePaneView1.removeKeyListener(ivjEventHandler);
			}
			ivjimagePaneView1 = newValue;

			/* Listen for events from the new object */
			if (ivjimagePaneView1 != null) {
				ivjimagePaneView1.addMouseMotionListener(ivjEventHandler);
				ivjimagePaneView1.addMouseListener(ivjEventHandler);
				ivjimagePaneView1.addKeyListener(ivjEventHandler);
			}
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	// user code begin {3}
	// user code end
}

/**
 * Comment
 */
private void updateColorMapDisplay() {

	if(cmapImageIcon == null && getDisplayAdapterService1().getActiveColorModel() != null){
		int[] cmap = getDisplayAdapterService1().getActiveColorModel();
		int cmapLength = cmap.length - getDisplayAdapterService1().getSpecialColors().length;
		int cmapWidth = 12;
		java.awt.image.BufferedImage cmapImage = new java.awt.image.BufferedImage(cmapWidth,cmapLength,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		int[] row = new int[cmapWidth];
		for(int i=0;i<cmapLength;i+=1){
			java.util.Arrays.fill(row,cmap[i]);
			cmapImage.setRGB(0,cmapLength-1-i,cmapWidth,1,row,0,cmapWidth);
		}
		cmapImageIcon = new ImageIcon(cmapImage);
		getColorMapJLabel().setIcon(cmapImageIcon);
		getColorMapJLabel().setText(null);
		getColorMapJLabel().repaint();
	}
	////Set ColorMap
	//int[] row = new int[cmapWidth];
	//java.awt.image.BufferedImage bufferedCmap = (java.awt.image.BufferedImage )cmapImageIcon.getImage();
	//if(getDisplayAdapterService1() != null &&
		//getDisplayAdapterService1().getActiveScaleRange() != null){
		//double value = getDisplayAdapterService1().getActiveScaleRange().getMin();
		//double inc = (getDisplayAdapterService1().getActiveScaleRange().getMax() - getDisplayAdapterService1().getActiveScaleRange().getMin())/(double)(cmapLength-1);
		//for(int i=0;i<cmapLength;i+=1){
			//int colorFromValue = getDisplayAdapterService1().getColorFromValue(value);
			//if(i == (cmapLength-1)){
				//colorFromValue = getDisplayAdapterService1().getColorFromValue(getDisplayAdapterService1().getActiveScaleRange().getMax());
			//}
////System.out.println("i="+i+" colorValue="+cbit.util.Hex.toString(colorFromValue));
			//java.util.Arrays.fill(row,colorFromValue);
			//bufferedCmap.setRGB(0,cmapLength-1-i,cmapWidth,1,row,0,cmapWidth);
			//value+= inc;
		//}
	//}else{
		//java.util.Arrays.fill(row,java.awt.Color.white.getRGB());
		//for(int i=0;i<cmapLength;i+=1){
			//bufferedCmap.setRGB(0,cmapLength-1-i,cmapWidth,1,row,0,cmapWidth);
		//}
	//}
	//getColorMapJLabel().repaint();
}


/**
 * Comment
 */
private void zoomDownJButton_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	getImagePaneScroller1().zooming(new cbit.image.ZoomEvent(getimagePaneView1(),0,-1));
}


/**
 * Insert the method's description here.
 * Creation date: (12/29/2004 12:57:18 PM)
 */
private void zoomToFill() {

	getimagePaneView1().getImagePaneModel().changeZoomToFillViewport();
	getImagePaneScroller1().zooming(new cbit.image.ZoomEvent(getimagePaneView1(),0,0));
}


/**
 * Comment
 */
private void zoomUpJButton_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	getImagePaneScroller1().zooming(new cbit.image.ZoomEvent(getimagePaneView1(),0,1));
}

/**
 * This method initializes jCheckBoxColor	
 * 	
 * @return javax.swing.JCheckBox	
 */
private JCheckBox getJCheckBoxColor() {
	if (jCheckBoxColor == null) {
		jCheckBoxColor = new JCheckBox();
		jCheckBoxColor.setText("B/W");
		jCheckBoxColor.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JCheckBox jCheckBox = (JCheckBox)e.getSource();
				if(jCheckBox.isSelected()){
					getDisplayAdapterService1().setActiveColorModelID("Gray");
				}else{
					getDisplayAdapterService1().setActiveColorModelID("BlueRed");
				}
				cmapImageIcon = null;
				updateColorMapDisplay();
				getimagePaneView1().getImagePaneModel().updateViewPortImage();
			}
		});
	}
	return jCheckBoxColor;
}
}