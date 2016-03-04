/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.image.gui;
import java.awt.*;

import org.vcell.util.CoordinateIndex;
import org.vcell.util.Range;

import cbit.image.DisplayAdapter;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public abstract class ImageContainer extends java.util.Observable 
							implements java.io.Serializable, java.awt.image.ImageObserver {
								
	public static final String IMAGEPLANE_PROPERTY = "imagePlane";
	protected int[] displayPixels = null;
	private int displayWidth = 0;
	private int displayHeight = 0;
	private int sizeX = 1;
	private int sizeY = 1;
	private int sizeZ = 1;
	protected boolean bValid = false;
	protected int selectedSourceValue = -1;
	public final static int X_AXIS = 0;
	public final static int Y_AXIS = 1;
	public final static int Z_AXIS = 2;
	private int currNormalAxis = Z_AXIS;
	private int currSlice = 0;
	private int dimension = 0;
	protected transient java.beans.PropertyChangeSupport propertyChange;
	private boolean fieldAutoMode = true;
	private String fieldColorMode = new String();
	private Range fieldDataRange = new Range();
	private DisplayAdapter fieldDisplayAdapter = null;
	private Range fieldScaleRange = new Range();

/**
 * Geometry constructor comment.
 */
public ImageContainer() {
	super();
}


/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(listener);
}


/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(String property,java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(property,listener);
}


/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}


/**
 * Gets the autoMode property (boolean) value.
 * @return The autoMode property value.
 * @see #setAutoMode
 */
public final boolean getAutoMode() {
	return fieldAutoMode;
}


/**
 * Gets the colorMode property (java.lang.String) value.
 * @return The colorMode property value.
 * @see #setColorMode
 */
public final String getColorMode() {
	return fieldColorMode;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 * @param x int
 * @param y int
 */
public CoordinateIndex getCoordinateIndexFromDisplay(int x, int y) throws Exception {
	if (!bValid) throw new Exception("image not valid, display data not availlable");
	int i,j,k;
	switch (getNormalAxis()){
		case X_AXIS:{
			i=getSlice();
			j=Math.max(0,Math.min(x,getSizeY()));
			k=Math.max(0,Math.min(y,getSizeZ()));
			break;
		}
		case Y_AXIS:{
			i=Math.max(0,Math.min(y,getSizeX()));
			j=getSlice();
			k=Math.max(0,Math.min(x,getSizeZ()));
			break;
		}
		case Z_AXIS:{
			i=Math.max(0,Math.min(x,getSizeX()));
			j=Math.max(0,Math.min(y,getSizeY()));
			k=getSlice();
			break;
		}
		default:{
			throw new Exception("bad axis");
		}
	}	
	CoordinateIndex ci = new CoordinateIndex();
	ci.x = i;
	ci.y = j;
	ci.z = k;
	return ci;				
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 * @param x int
 * @param y int
 * @param z int
 */
public String getCoordinateString(int x, int y, int z) {
	return "("+x+","+y+","+z+")";
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String getDataID() {
	return "unknown";
}


/**
 * Gets the dataRange property (cbit.image.Range) value.
 * @return The dataRange property value.
 * @see #setDataRange
 */
public final Range getDataRange() {
	return fieldDataRange;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 */
public int getDimension() {
	return dimension;
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String getDisplacementLabel() {
	return "elements";
}


/**
 * Gets the displayAdapter property (cbit.image.DisplayAdapter) value.
 * @return The displayAdapter property value.
 * @see #setDisplayAdapter
 */
public DisplayAdapter getDisplayAdapter() {
	return fieldDisplayAdapter;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 */
public int getDisplayHeight() throws Exception {
	if (!bValid) throw new Exception("image not valid, height not availlable");
	return displayHeight;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 */
public int getDisplayWidth() throws Exception {
	if (!bValid) throw new Exception("image not valid, width not availlable");
	return displayWidth;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 * @param x int
 * @param y int
 */
public int getIndexFromDisplay(int x, int y) throws Exception {
	if (!bValid) throw new Exception("image not valid, display data not availlable");
	switch (getNormalAxis()){
		case X_AXIS:{
			int i=getSlice();
			int j=x;
			int k=y;
			return i+getSizeX()*(j+getSizeY()*k);
		}
		case Y_AXIS:{
			int i=y;
			int j=getSlice();
			int k=x;
			return i+getSizeX()*(j+getSizeY()*k);
		}
		case Z_AXIS:{
			int i=x;
			int j=y;
			int k=getSlice();
			return i+getSizeX()*(j+getSizeY()*k);
		}
		default:{
			throw new Exception("bad axis");
		}
	}					
}


/**
 * This method was created in VisualAge.
 * @return cbit.plot.PlotData
 * @param displayBeginPoint java.awt.Point
 * @param displayEndPoint java.awt.Point
 */
public cbit.plot.PlotData getLineScan(Point displayBeginPoint, Point displayEndPoint) throws Exception {
	//
	// go from screen space to image space
	//
	CoordinateIndex begin = getCoordinateIndexFromDisplay(displayBeginPoint.x,displayBeginPoint.y);
	CoordinateIndex end = getCoordinateIndexFromDisplay(displayEndPoint.x,displayEndPoint.y);

	//
	// get length of span
	//
	double lengthScan = Math.sqrt((begin.x-end.x)*(begin.x-end.x) +
								(begin.y-end.y)*(begin.y-end.y) +
								 (begin.z-end.z)*(begin.z-end.z));
													 
	if (lengthScan <= 0){
		return null;
	}	
			
	int sizeScan = Math.min(200,4*((int)lengthScan));
	double lineScan[] = new double[sizeScan];
	double line[] = new double[sizeScan];
	for (int i=0;i<sizeScan;i++){
		// operate on normalized length (parametric line)
		line[i] = ((double)i)/(sizeScan-1);
		double coordX = begin.x + line[i]*(end.x-begin.x);
		double coordY = begin.y + line[i]*(end.y-begin.y);
		double coordZ = begin.z + line[i]*(end.z-begin.z);
		int pointX = (int)(coordX + 0.5);
		int pointY = (int)(coordY + 0.5);
		int pointZ = (int)(coordZ + 0.5);
		lineScan[i] = getValue(pointX,pointY,pointZ);
		// restore scale to
		line[i] *= lengthScan;
	}	
	try {
		return new cbit.plot.PlotData(line,lineScan);
	}catch (Exception e){
		return null;
	}		
}


/**
 * This method was created by a SmartGuide.
 * @return int
 */
public int getNormalAxis() {
	return currNormalAxis;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 * @param x int
 * @param y int
 */
public int getPixel(int x, int y) throws Exception {
	if (!bValid) throw new Exception("image not valid, pixel data not availlable");
	return displayPixels[x+y*getDisplayWidth()];
}


/**
 * This method was created by a SmartGuide.
 * @return int[]
 */
public int[] getPixels() throws Exception {
	if (!bValid) throw new Exception("image not valid, pixels not availlable");
	return displayPixels;
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public final String getPointString(int x, int y, int z) {
	try {
		String statusText = "@"+getCoordinateString(x,y,z);
		while(statusText.length()<35) {
			statusText = " "+statusText;
		}	
		statusText += "  "+getValueString(x,y,z);
		return statusText;
	}catch (Exception e){
		return getCoordinateString(x,y,z)+" error: "+e.getMessage();
	}
}


/**
 * Accessor for the propertyChange field.
 */
protected java.beans.PropertyChangeSupport getPropertyChange() {
	if (propertyChange == null) {
		propertyChange = new java.beans.PropertyChangeSupport(this);
	};
	return propertyChange;
}


/**
 * Gets the scaleRange property (cbit.image.Range) value.
 * @return The scaleRange property value.
 * @see #setScaleRange
 */
public final Range getScaleRange() {
	return fieldScaleRange;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 */
public final int getSizeX() {
	return sizeX;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 */
public final int getSizeY() {
	return sizeY;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 */
public final int getSizeZ() {
	return sizeZ;
}


/**
 * This method was created by a SmartGuide.
 * @return int
 */
public int getSlice() {
	return currSlice;
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String getUnits() {
	return "";
}


/**
 * This method was created by a SmartGuide.
 * @return double
 * @param x int
 * @param y int
 */
public double getValue(int x, int y) throws Exception {
	CoordinateIndex ci = getCoordinateIndexFromDisplay(x,y);
	return getValue(ci.x,ci.y,ci.z);
}


/**
 * This method was created in VisualAge.
 * @return double
 * @param x int
 * @param y int
 * @param z int
 */
public abstract double getValue(int x, int y, int z) throws Exception;


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public abstract String getValueLabel();


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
protected String getValueString(int x, int y, int z) {
	try {
		return getValueLabel() + " = " + Double.toString(getValue(x,y,z));
	}catch (Exception e){
		return "error: "+e.getMessage();
	}
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 * @param image java.awt.Image
 * @param infoflags int
 * @param x int
 * @param y int
 * @param width int
 * @param height int
 */
public final boolean imageUpdate(java.awt.Image image, int infoflags, int x, int y, int width, int height) {
	return false;
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isValid() {
	return bValid;
}


/**
 * This method was created by a SmartGuide.
 */
public abstract void refreshDisplayPixels();


/**
 * This method was created by a SmartGuide.
 */
public void refreshDisplayScale() {
	if (getDisplayAdapter()!=null){
		getDisplayAdapter().setAutoMode(getAutoMode());
		getDisplayAdapter().setColorMode(getColorMode());
		getDisplayAdapter().setScaleRange(getScaleRange());
	}
}


/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(listener);
}


/**
 * This method was created by a SmartGuide.
 * @param value int
 */
public void selectSourceValue(int value) {
	selectedSourceValue = value;
	refreshDisplayPixels();
}


/**
 * Sets the autoMode property (boolean) value.
 * @param autoMode The new value for the property.
 * @see #getAutoMode
 */
public final void setAutoMode(boolean autoMode) {
	getDisplayAdapter().setAutoMode(autoMode);
	boolean oldValue = fieldAutoMode;
	fieldAutoMode = autoMode;
	firePropertyChange("autoMode", new Boolean(oldValue), new Boolean(autoMode));
}


/**
 * Sets the colorMode property (java.lang.String) value.
 * @param colorMode The new value for the property.
 * @see #getColorMode
 */
public final void setColorMode(String colorMode) {
	getDisplayAdapter().setColorMode(colorMode);
	String oldValue = fieldColorMode;
	fieldColorMode = colorMode;
	firePropertyChange("colorMode", oldValue, colorMode);
}


/**
 * Sets the dataRange property (cbit.image.Range) value.
 * @param dataRange The new value for the property.
 * @see #getDataRange
 */
public final void setDataRange(Range dataRange) {
	getDisplayAdapter().setDataRange(dataRange);
	Range oldValue = fieldDataRange;
	fieldDataRange = dataRange;
	firePropertyChange("dataRange", oldValue, dataRange);
}


/**
 * Sets the displayAdapter property (cbit.image.DisplayAdapter) value.
 * @param displayAdapter The new value for the property.
 * @see #getDisplayAdapter
 */
public void setDisplayAdapter(DisplayAdapter displayAdapter) {
	DisplayAdapter oldValue = fieldDisplayAdapter;
	fieldDisplayAdapter = displayAdapter;
	firePropertyChange("displayAdapter", oldValue, displayAdapter);
}


/**
 * This method was created by a SmartGuide.
 * @param normalAxis int
 * @param slice int
 */
public final void setImagePlane(int normalAxis, int slice) throws Exception {
	switch (normalAxis){
		case X_AXIS:{
			slice = Math.min(Math.max(0,slice),sizeX-1);
//			if (slice >= sizeX){
//				throw new Exception("slice ("+slice+") along X axis greater than X size ("+sizeX+")");
//			}
			displayWidth = sizeY;
			displayHeight = sizeZ;
			break;	
		}
		case Y_AXIS:{
			slice = Math.min(Math.max(0,slice),sizeY-1);
//			if (slice >= sizeY){
//				throw new Exception("slice ("+slice+") along Y axis greater than Y size ("+sizeY+")");
//			}
			displayWidth = sizeZ;
			displayHeight = sizeX;
			break;	
		}
		case Z_AXIS:{
			slice = Math.min(Math.max(0,slice),sizeZ-1);
//			if (slice >= sizeZ){
//				throw new Exception("slice ("+slice+") along Z axis greater than Z size ("+sizeZ+")");
//			}
			displayWidth = sizeX;
			displayHeight = sizeY;
			break;	
		}
		default:{
			throw new Exception("normalAxis not X, Y or Z");
		}	
	}		
	currNormalAxis = normalAxis;
	currSlice = slice;
	if (displayPixels == null){
		displayPixels = new int[displayWidth*displayHeight];
	}
	if (displayPixels.length != displayWidth*displayHeight){
		displayPixels = new int[displayWidth*displayHeight];
	}		
	if (bValid){
		refreshDisplayPixels();
	}
	
	firePropertyChange(IMAGEPLANE_PROPERTY, null, null);
}


/**
 * Sets the scaleRange property (cbit.image.Range) value.
 * @param scaleRange The new value for the property.
 * @see #getScaleRange
 */
public final void setScaleRange(Range scaleRange) {
	if (getDisplayAdapter()==null){
		return;
	}
	getDisplayAdapter().setScaleRange(scaleRange);
	Range oldValue = fieldScaleRange;
	fieldScaleRange = scaleRange;
	firePropertyChange("scaleRange", oldValue, scaleRange);
}


/**
 * This method was created by a SmartGuide.
 * @param sizeX int
 * @param sizeY int
 * @param sizeZ int
 * @exception java.lang.Exception The exception description.
 */
protected void setSize(int sizeX, int sizeY, int sizeZ) throws Exception {
	if (sizeX==0 && sizeY==0 && sizeZ==0){
		dimension = 0;
		return;
	}	
	if (sizeX<=0 || sizeY<=0 || sizeZ<=0){
		throw new Exception("size includes non-positive values ("+sizeX+","+sizeY+","+sizeZ+")");
	}	
	if (sizeX==1 && sizeY==1 && sizeZ==1){
		dimension = 0;
	}else if (sizeY==1 && sizeZ==1){
		dimension = 1;
	}else if (sizeZ==1){
		dimension = 2;
	}else{
		dimension = 3;
	}	
	this.sizeX = sizeX;
	this.sizeY = sizeY;
	this.sizeZ = sizeZ;
	setImagePlane(currNormalAxis,currSlice);
}
}