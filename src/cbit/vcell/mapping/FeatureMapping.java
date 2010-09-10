package cbit.vcell.mapping;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import org.vcell.util.Matchable;

import cbit.vcell.geometry.CompartmentSubVolume;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.SubVolume;
import cbit.vcell.geometry.SurfaceClass;
import cbit.vcell.model.Feature;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.ReservedSymbol;
import cbit.vcell.model.Structure;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.units.VCUnitDefinition;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class FeatureMapping extends StructureMapping {

//	public static boolean bTotalVolumeCorrectionBug = false;
//	public static boolean bTotalVolumeCorrectionBugExercised = false;

/**
 * FeatureMapping constructor comment.
 * @param feature cbit.vcell.model.Feature
 * @param geoContext cbit.vcell.mapping.GeometryContext
 * @exception java.lang.Exception The exception description.
 */
public FeatureMapping(FeatureMapping featureMapping, SimulationContext argSimulationContext,Geometry newGeometry) {
	super(featureMapping, argSimulationContext,newGeometry);
}


/**
 * FeatureMapping constructor comment.
 * @param feature cbit.vcell.model.Feature
 * @param geoContext cbit.vcell.mapping.GeometryContext
 * @exception java.lang.Exception The exception description.
 */
public FeatureMapping(Feature feature, SimulationContext argSimulationContext) {
	super(feature, argSimulationContext);
	try {
		setParameters(new StructureMappingParameter[] {						
				new StructureMappingParameter(DefaultNames[ROLE_Size], null, ROLE_Size, VCUnitDefinition.UNIT_um3),
				new StructureMappingParameter(DefaultNames[ROLE_VolumePerUnitArea], null, ROLE_VolumePerUnitArea, VCUnitDefinition.UNIT_um),
				new StructureMappingParameter(DefaultNames[ROLE_VolumePerUnitVolume], null, ROLE_VolumePerUnitVolume, VCUnitDefinition.UNIT_DIMENSIONLESS),			
		});
	}catch (java.beans.PropertyVetoException e){
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
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
	
	return true;
}

/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.model.Feature
 */
public Feature getFeature() {
	return (Feature)getStructure();
}

/**
 * This method was created by a SmartGuide.
 * @return double
 */
public StructureMappingParameter getVolumePerUnitVolumeParameter() {
	return getParameterFromRole(ROLE_VolumePerUnitVolume);
}

/**
 * This method was created by a SmartGuide.
 * @return double
 */
public StructureMappingParameter getVolumePerUnitAreaParameter() {
	return getParameterFromRole(ROLE_VolumePerUnitArea);
}

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
				StructureMapping parentStructureMapping = simulationContext.getGeometryContext().getStructureMapping(mm.getStructure().getParentStructure());
				boolean bResolved = parentStructureMapping.getGeometryClass() != getGeometryClass();
				if (!bResolved) {
					exp = Expression.add(exp, Expression.negate(new Expression(mm.getVolumeFractionParameter(), simulationContext.getNameScope())));
				}
			}
		}
	}
	return exp;
}

/**
 * TotalConservationCorrection is the term that takes local units (micro-molar) to either volume normalized micro-molar or area normalized molecules/sq-um
 * @return cbit.vcell.parser.Expression
 */
Expression getNormalizedConcentrationCorrection(SimulationContext simulationContext) throws ExpressionException {
	if (getGeometryClass() instanceof CompartmentSubVolume){
		if (simulationContext.getGeometryContext().isAllSizeSpecifiedPositive()) {
			//
			// everything mapped to micro-molar : just need size
			//
			Expression exp = new Expression(getSizeParameter(),simulationContext.getNameScope());
			return exp;
		} else {
			if (getFeature().getMembrane()==null) {
				return getResidualVolumeFraction(simulationContext);
			} else {
				//
				// for all distributed parent volumes (that have membranes), multiply each volume fraction
				//
				Membrane membrane = getFeature().getMembrane();
				Expression exp = getResidualVolumeFraction(simulationContext);
				while (membrane!=null){
					MembraneMapping memMapping = (MembraneMapping)simulationContext.getGeometryContext().getStructureMapping(membrane);
					StructureMapping parentStructureMapping = simulationContext.getGeometryContext().getStructureMapping(memMapping.getStructure().getParentStructure());
					boolean bResolved = parentStructureMapping.getGeometryClass() != getGeometryClass();
					if (!bResolved) {
						exp = Expression.mult(exp,new Expression(memMapping.getVolumeFractionParameter(), simulationContext.getNameScope()));
					}else{
						break;
					}
					membrane = membrane.getOutsideFeature().getMembrane();
				}
				return exp;
			}	
		}
	}else if (getGeometryClass() instanceof SubVolume){
		//
		// everything mapped to micro-molar : just need volume fraction
		//
		Expression exp = new Expression(getVolumePerUnitVolumeParameter(),simulationContext.getNameScope());
		return exp;
	}else if (getGeometryClass() instanceof SurfaceClass){
		//
		// everything mapped to molecules/sq-um : need volume/area fraction and KMOLE
		//
		Expression exp = Expression.div(new Expression(getVolumePerUnitAreaParameter(),simulationContext.getNameScope()),
				new Expression(ReservedSymbol.KMOLE,simulationContext.getNameScope()));
		return exp;
	}else{
		throw new RuntimeException("structure "+getStructure().getName()+" not mapped");
	}
}

/**
 * TotalVolumeCorrection is the term that takes local units to volume normalized micro-Molar
 * @return cbit.vcell.parser.Expression
 */
@Override
public Expression getStructureSizeCorrection(SimulationContext simulationContext) throws ExpressionException {
	return getNormalizedConcentrationCorrection(simulationContext);
}

/**
 * The hasListeners method was generated to support the vetoPropertyChange field.
 */
public synchronized boolean hasListeners(java.lang.String propertyName) {
	return getVetoPropertyChange().hasListeners(propertyName);
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
 * @return java.lang.String
 */
public String toString() {
	return getClass().getName()+"@"+Integer.toHexString(hashCode())+" "+getFeature().getName();
}


@Override
public StructureMappingParameter getUnitSizeParameter() {
	if (getGeometryClass() instanceof SubVolume){
		return getVolumePerUnitVolumeParameter();
	}else if (getGeometryClass() instanceof SurfaceClass){
		return getVolumePerUnitAreaParameter();
	}
	return null;
}

}