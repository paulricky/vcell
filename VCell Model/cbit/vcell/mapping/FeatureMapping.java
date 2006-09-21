package cbit.vcell.mapping;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.vcell.math.BoundaryConditionType;
import cbit.vcell.model.*;
import cbit.vcell.geometry.*;
import cbit.vcell.math.VCML;
import cbit.vcell.math.Function;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.util.*;
import cbit.vcell.units.VCUnitDefinition;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class FeatureMapping extends StructureMapping {
	private cbit.vcell.math.BoundaryConditionType boundaryConditionTypes[] = new cbit.vcell.math.BoundaryConditionType[6];
	private boolean boundaryConditionValid[] = new boolean[6];
	private cbit.vcell.geometry.SubVolume fieldSubVolume = null;
	private boolean fieldResolved = false;

	public static boolean bTotalVolumeCorrectionBug = false;
	public static boolean bTotalVolumeCorrectionBugExercised = false;

/**
 * FeatureMapping constructor comment.
 * @param feature cbit.vcell.model.Feature
 * @param geoContext cbit.vcell.mapping.GeometryContext
 * @exception java.lang.Exception The exception description.
 */
public FeatureMapping(FeatureMapping featureMapping, SimulationContext argSimulationContext) {
	super(featureMapping, argSimulationContext);
	for (int i=0;i<6;i++){
		boundaryConditionTypes[i]=featureMapping.boundaryConditionTypes[i];
		boundaryConditionValid[i]=featureMapping.boundaryConditionValid[i];
	}
	fieldSubVolume = featureMapping.fieldSubVolume;
	fieldResolved = featureMapping.fieldResolved;
}


/**
 * FeatureMapping constructor comment.
 * @param feature cbit.vcell.model.Feature
 * @param geoContext cbit.vcell.mapping.GeometryContext
 * @exception java.lang.Exception The exception description.
 */
public FeatureMapping(cbit.vcell.model.Feature feature, SimulationContext argSimulationContext) {
	super(feature, argSimulationContext);
	try {
		setParameters(new StructureMappingParameter[] {						
			new StructureMappingParameter(DefaultNames[ROLE_Size], null, ROLE_Size, VCUnitDefinition.UNIT_um3)
		});
	}catch (java.beans.PropertyVetoException e){
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
	for (int i=0;i<6;i++){
		boundaryConditionTypes[i]=cbit.vcell.math.BoundaryConditionType.getNEUMANN();
		boundaryConditionValid[i]=false;
	}
}


/**
 * This method was created in VisualAge.
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean compareEqual(Matchable obj) {

	FeatureMapping fm = null;
	if (!(obj instanceof FeatureMapping)){
		return false;
	}
	fm = (FeatureMapping)obj;

	if (!compareEqual0(fm)){
		return false;
	}
	
	if (!Compare.isEqualOrNull(fieldSubVolume,fm.fieldSubVolume)){
		return false;
	}

	if (fieldResolved != fm.fieldResolved){
		return false;
	}

	for (int i=0;i<boundaryConditionTypes.length;i++){
		if (!boundaryConditionTypes[i].compareEqual(fm.boundaryConditionTypes[i])){
			return false;
		}
	}
	
	return true;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public cbit.vcell.math.BoundaryConditionType getBoundaryCondition(BoundaryLocation boundaryLocation) {
	return boundaryConditionTypes[boundaryLocation.getNum()];
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public cbit.vcell.math.BoundaryConditionType getBoundaryConditionTypeXm() {
	return getBoundaryCondition(BoundaryLocation.getXM());
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public cbit.vcell.math.BoundaryConditionType getBoundaryConditionTypeXp() {
	return getBoundaryCondition(BoundaryLocation.getXP());
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public cbit.vcell.math.BoundaryConditionType getBoundaryConditionTypeYm() {
	return getBoundaryCondition(BoundaryLocation.getYM());
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public cbit.vcell.math.BoundaryConditionType getBoundaryConditionTypeYp() {
	return getBoundaryCondition(BoundaryLocation.getYP());
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public cbit.vcell.math.BoundaryConditionType getBoundaryConditionTypeZm() {
	return getBoundaryCondition(BoundaryLocation.getZM());
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public cbit.vcell.math.BoundaryConditionType getBoundaryConditionTypeZp() {
	return getBoundaryCondition(BoundaryLocation.getZP());
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.model.Feature
 */
public Feature getFeature() {
	return (Feature)getStructure();
}


/**
 * This method was created in VisualAge.
 * @return Function
 */
public Expression getResidualVolumeFraction(SimulationContext simulationContext) throws ExpressionException {
	Expression exp = new Expression(1.0);
	if (simulationContext==null){
		throw new RuntimeException("FeatureMapping.getResidualVolumeFraction()");
	}
	Structure structures[] = simulationContext.getGeometryContext().getModel().getStructures();
	for (int i=0;i<structures.length;i++){
		//
		// for each membrane that is distributed within this feature, subtract that volume fraction
		// ????? beware, 1 - v1 - v2 ... can result in a negative number if we're not carefull.
		//
		Structure struct = structures[i];
		if (struct instanceof Membrane) {
			if (((Membrane) struct).getOutsideFeature() == getFeature()) {
				MembraneMapping mm = (MembraneMapping) simulationContext.getGeometryContext().getStructureMapping(struct);
				if (mm.getResolved(simulationContext)==false){
					exp = Expression.add(exp, Expression.negate(new Expression(simulationContext.getNameScope().getSymbolName(mm.getVolumeFractionParameter()))));
				}
			}
		}
	}
	return exp;
}


/**
 * Gets the resolved property (boolean) value.
 * @return The resolved property value.
 * @see #setResolved
 */
public boolean getResolved() {
	return fieldResolved;
}


/**
 * Gets the subVolume property (cbit.vcell.geometry.SubVolume) value.
 * @return The subVolume property value.
 * @see #setSubVolume
 */
public cbit.vcell.geometry.SubVolume getSubVolume() {
	return fieldSubVolume;
}


/**
 * TotalVolumeCorrection is the term that takes local units to volume normalized micro-Molar
 * @return cbit.vcell.parser.Expression
 */
Expression getTotalVolumeCorrection(SimulationContext simulationContext) throws ExpressionException {
	if (getResolved() || getFeature().getMembrane()==null) {
		return getResidualVolumeFraction(simulationContext);
	} else {
		//
		// for all distributed parent volumes (that have membranes), multiply each volume fraction
		//
		Membrane membrane = getFeature().getMembrane();
		Expression exp = getResidualVolumeFraction(simulationContext);
		//
		// support bug-compatability mode for testing old BioModel math generation
		//
		if (bTotalVolumeCorrectionBug && !exp.compareEqual(new Expression(1.0))) { 
			exp = new Expression(1.0);
			bTotalVolumeCorrectionBugExercised = true;
			System.out.println("FeatureMapping.getTotalVolumeCorrection() ... 'TotalVolumeCorrection' bug compatability mode");
		}
		while (membrane!=null){
			MembraneMapping memMapping = (MembraneMapping)simulationContext.getGeometryContext().getStructureMapping(membrane);
			if (memMapping.getResolved(simulationContext)==false){
				exp = Expression.mult(exp,new Expression(simulationContext.getNameScope().getSymbolName(memMapping.getVolumeFractionParameter())));
			}else{
				break;
			}
			membrane = membrane.getOutsideFeature().getMembrane();
		}
		return exp;
	}
}


/**
 * The hasListeners method was generated to support the vetoPropertyChange field.
 */
public synchronized boolean hasListeners(java.lang.String propertyName) {
	return getVetoPropertyChange().hasListeners(propertyName);
}


/**
 * This method was created by a SmartGuide.
 * @return boolean
 */
public boolean isBoundaryConditionValid(BoundaryLocation boundaryLocation) {
	return boundaryConditionValid[boundaryLocation.getNum()];
}


/**
 * Insert the method's description here.
 * Creation date: (2/19/2002 1:20:30 PM)
 */
public void refreshDependencies() {
	super.refreshDependencies();
}


/**
 * This method was created by a SmartGuide.
 * @param bct java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public void setBoundaryCondition(BoundaryLocation boundaryLocation, cbit.vcell.math.BoundaryConditionType bc) {
	boundaryConditionTypes[boundaryLocation.getNum()] = bc;
}


/**
 * This method was created by a SmartGuide.
 * @param bct java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public void setBoundaryConditionTypeXm(BoundaryConditionType bc) {
	BoundaryConditionType oldBCType = getBoundaryConditionTypeXm();
	setBoundaryCondition(BoundaryLocation.getXM(), bc);
	BoundaryConditionType newBCType = getBoundaryConditionTypeXm();
	firePropertyChange("boundaryConditionTypeXm",oldBCType,newBCType);
}


/**
 * This method was created by a SmartGuide.
 * @param bct java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public void setBoundaryConditionTypeXp(cbit.vcell.math.BoundaryConditionType bc) {
	BoundaryConditionType oldBCType = getBoundaryConditionTypeXp();
	setBoundaryCondition(BoundaryLocation.getXP(), bc);
	BoundaryConditionType newBCType = getBoundaryConditionTypeXp();
	firePropertyChange("boundaryConditionTypeXp",oldBCType,newBCType);
}


/**
 * This method was created by a SmartGuide.
 * @param bct java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public void setBoundaryConditionTypeYm(cbit.vcell.math.BoundaryConditionType bc) {
	BoundaryConditionType oldBCType = getBoundaryConditionTypeYm();
	setBoundaryCondition(BoundaryLocation.getYM(), bc);
	BoundaryConditionType newBCType = getBoundaryConditionTypeYm();
	firePropertyChange("boundaryConditionTypeYm",oldBCType,newBCType);
}


/**
 * This method was created by a SmartGuide.
 * @param bct java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public void setBoundaryConditionTypeYp(cbit.vcell.math.BoundaryConditionType bc) {
	BoundaryConditionType oldBCType = getBoundaryConditionTypeYp();
	setBoundaryCondition(BoundaryLocation.getYP(), bc);
	BoundaryConditionType newBCType = getBoundaryConditionTypeYp();
	firePropertyChange("boundaryConditionTypeYp",oldBCType,newBCType);
}


/**
 * This method was created by a SmartGuide.
 * @param bct java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public void setBoundaryConditionTypeZm(cbit.vcell.math.BoundaryConditionType bc) {
	BoundaryConditionType oldBCType = getBoundaryConditionTypeZm();
	setBoundaryCondition(BoundaryLocation.getZM(), bc);
	BoundaryConditionType newBCType = getBoundaryConditionTypeZm();
	firePropertyChange("boundaryConditionTypeZm",oldBCType,newBCType);
}


/**
 * This method was created by a SmartGuide.
 * @param bct java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public void setBoundaryConditionTypeZp(cbit.vcell.math.BoundaryConditionType bc) {
	BoundaryConditionType oldBCType = getBoundaryConditionTypeZp();
	setBoundaryCondition(BoundaryLocation.getZP(), bc);
	BoundaryConditionType newBCType = getBoundaryConditionTypeZp();
	firePropertyChange("boundaryConditionTypeZp",oldBCType,newBCType);

}


/**
 * Sets the resolved property (boolean) value.
 * @param resolved The new value for the property.
 * @exception java.beans.PropertyVetoException The exception description.
 * @see #getResolved
 */
public void setResolved(boolean resolved) throws java.beans.PropertyVetoException {
	boolean oldValue = fieldResolved;
	fireVetoableChange("resolved", new Boolean(oldValue), new Boolean(resolved));
	fieldResolved = resolved;
	firePropertyChange("resolved", new Boolean(oldValue), new Boolean(resolved));
}


/**
 * Sets the subVolume property (cbit.vcell.geometry.SubVolume) value.
 * @param subVolume The new value for the property.
 * @exception java.beans.PropertyVetoException The exception description.
 * @see #getSubVolume
 */
public void setSubVolume(cbit.vcell.geometry.SubVolume subVolume) throws java.beans.PropertyVetoException {
	SubVolume oldValue = fieldSubVolume;
	fireVetoableChange("subVolume", oldValue, subVolume);
	fieldSubVolume = subVolume;
	firePropertyChange("subVolume", oldValue, subVolume);
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String toString() {
	return getClass().getName()+"@"+Integer.toHexString(hashCode())+" "+getFeature().getName();
}
}