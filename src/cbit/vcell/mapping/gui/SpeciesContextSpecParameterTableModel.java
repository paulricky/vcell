package cbit.vcell.mapping.gui;
import cbit.vcell.client.PopupGenerator;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.StructureMapping;
import cbit.vcell.model.*;
import java.util.*;

import org.vcell.util.gui.sorttable.ManageTableModel;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.ScopedExpression;
import cbit.vcell.parser.SymbolTableEntryFilter;
import cbit.vcell.mapping.SpeciesContextSpec;
/**
 * Insert the type's description here.
 * Creation date: (2/23/01 10:52:36 PM)
 * @author: 
 */
public class SpeciesContextSpecParameterTableModel extends ManageTableModel implements java.beans.PropertyChangeListener {

	static {
		System.out.println("SpeciesContextSpecParameterTableModel: artifically filtering out membrane diffusion parameters and diffusion/bc's that are not applicable");
	}

	
	private class ParameterColumnComparator implements Comparator<Object> {
		protected int index;
		protected boolean ascending;

		public ParameterColumnComparator(int index, boolean ascending){
			this.index = index;
			this.ascending = ascending;
		}
		
		/**
		 * Compares its two arguments for order.  Returns a negative integer,
		 * zero, or a positive integer as the first argument is less than, equal
		 * to, or greater than the second.<p>
		 */
		public int compare(Object o1, Object o2){
			
			Parameter parm1 = (Parameter)o1;
			Parameter parm2 = (Parameter)o2;
			
			switch (index){
				case COLUMN_NAME:{
					if (ascending){
						return parm1.getName().compareToIgnoreCase(parm2.getName());
					}else{
						return parm2.getName().compareToIgnoreCase(parm1.getName());
					}
					//break;
				}
				case COLUMN_VALUE:{
					String expression1 = parm1.getExpression() != null ? parm1.getExpression().infix() : "";
					String expression2 = parm2.getExpression() != null ? parm2.getExpression().infix() : "";
					if (ascending){
						return expression1.compareToIgnoreCase(expression2);
					}else{
						return expression2.compareToIgnoreCase(expression1);
					}
					//break;
				}
				case COLUMN_DESCRIPTION:{
					if (ascending){
						return parm1.getDescription().compareToIgnoreCase(parm2.getDescription());
					}else{
						return parm2.getDescription().compareToIgnoreCase(parm1.getDescription());
					}
					//break;
				}
				case COLUMN_UNIT:{
					String unit1 = (parm1.getUnitDefinition()!=null)?(parm1.getUnitDefinition().getSymbol()):"null";
					String unit2 = (parm2.getUnitDefinition()!=null)?(parm2.getUnitDefinition().getSymbol()):"null";
					if (ascending){
						return unit1.compareToIgnoreCase(unit2);
					}else{
						return unit2.compareToIgnoreCase(unit1);
					}
					//break;
				}
			}
			return 1;
		}
	}
	private final int NUM_COLUMNS = 4;
	private final int COLUMN_DESCRIPTION = 0;
	private final int COLUMN_NAME = 1;
	private final int COLUMN_VALUE = 2;
	private final int COLUMN_UNIT = 3;
	private String LABELS[] = { "Description", "Parameter", "Expression", "Units" };
	protected transient java.beans.PropertyChangeSupport propertyChange;
	private SpeciesContextSpec fieldSpeciesContextSpec = null;
	private SimulationContext fieldSimulationContext = null;
	
	private SymbolTableEntryFilter symbolTableEntryFilter = null;

/**
 * ReactionSpecsTableModel constructor comment.
 */
public SpeciesContextSpecParameterTableModel() {
	super();
	addPropertyChangeListener(this);
}

/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(listener);
}

/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(java.beans.PropertyChangeEvent evt) {
	getPropertyChange().firePropertyChange(evt);
}

/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}

/**
 * Insert the method's description here.
 * Creation date: (2/24/01 12:24:35 AM)
 * @return java.lang.Class
 * @param column int
 */
public Class<?> getColumnClass(int column) {
	switch (column){
		case COLUMN_NAME:{
			return String.class;
		}
		case COLUMN_DESCRIPTION:{
			return String.class;
		}
		case COLUMN_UNIT:{
			return String.class;
		}
		case COLUMN_VALUE:{
			return ScopedExpression.class;
		}
		default:{
			return Object.class;
		}
	}
}


/**
 * getColumnCount method comment.
 */
public int getColumnCount() {
	return NUM_COLUMNS;
}


/**
 * Insert the method's description here.
 * Creation date: (2/24/01 12:24:35 AM)
 * @return java.lang.Class
 * @param column int
 */
public String getColumnName(int column) {
	if (column<0 || column>=NUM_COLUMNS){
		throw new RuntimeException("ParameterTableModel.getColumnName(), column = "+column+" out of range ["+0+","+(NUM_COLUMNS-1)+"]");
	}
	return LABELS[column];
}


/**
 * Insert the method's description here.
 * Creation date: (9/23/2003 1:24:52 PM)
 * @return cbit.vcell.model.Parameter
 * @param row int
 */
private Parameter getParameter(int row) {

	if (getSpeciesContextSpec()==null){
		return null;
	}
	int count = getSpeciesContextSpec().getParameters().length;
	if (row<0 || row>=count){
		throw new RuntimeException("SpeciesContextSpecParameterTableModel.getParameter("+row+") out of range ["+0+","+(count-1)+"]");
	}
	
	int geomDimension = fieldSimulationContext.getGeometry().getDimension();
	if (geomDimension == 0) {
		return getSpeciesContextSpec().getParameter(row);
	} else if (geomDimension == 1)  {
		if (row < 4) {
			return getSpeciesContextSpec().getParameter(row);	
		} else {
			// skip Yp, Ym, Zp, Zm, initCount in the SpeciesContextSpecParameter list
			return getSpeciesContextSpec().getParameter(row+5);
		}
	} else if (geomDimension == 2) {
		if (row < 6) {
			return getSpeciesContextSpec().getParameter(row);	
		} else {
			// skip Zp, Zm, initCount in the SpeciesContextSpecParameter list
			return getSpeciesContextSpec().getParameter(row+3);
		}
	} else if (geomDimension == 3) {
		if (row < 8) {
			return getSpeciesContextSpec().getParameter(row);	
		} else {
			// skip initCount in the SpeciesContextSpecParameter list
			return getSpeciesContextSpec().getParameter(row+1);
		}
	}
	return null;
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
 * getRowCount method comment.
 */
public int getRowCount() {
	if (getSpeciesContextSpec()==null){
		return 0;
	}else{
		return getSpeciesContextSpec().getNumDisplayableParameters();
	}
}


/**
 * Gets the simulationContext property (cbit.vcell.mapping.SimulationContext) value.
 * @return The simulationContext property value.
 * @see #setSimulationContext
 */
public SimulationContext getSimulationContext() {
	return fieldSimulationContext;
}


/**
 * Gets the speciesContextSpec property (cbit.vcell.mapping.SpeciesContextSpec) value.
 * @return The speciesContextSpec property value.
 * @see #setSpeciesContextSpec
 */
public SpeciesContextSpec getSpeciesContextSpec() {
	return fieldSpeciesContextSpec;
}


/**
 * Insert the method's description here.
 * Creation date: (9/23/2003 1:24:52 PM)
 * @return cbit.vcell.model.Parameter
 * @param row int
 */
private List<Parameter> getUnsortedParameters() {

	if (getSpeciesContextSpec()==null){
		return null;
	}
	int count = getRowCount();
	ArrayList<Parameter> list = new ArrayList<Parameter>();
	for (int i = 0; i < count; i++){
		list.add(getParameter(i));
	}
	return list;
}


/**
 * getValueAt method comment.
 */
public Object getValueAt(int row, int col) {
	if (col<0 || col>=NUM_COLUMNS){
		throw new RuntimeException("ParameterTableModel.getValueAt(), column = "+col+" out of range ["+0+","+(NUM_COLUMNS-1)+"]");
	}
	if (row<0 || row>=getRowCount()){
		throw new RuntimeException("ParameterTableModel.getValueAt(), row = "+row+" out of range ["+0+","+(getRowCount()-1)+"]");
	}
	if (getData().size() <= row){
		setData(getUnsortedParameters());
	}
	Parameter parameter = (Parameter)getData().get(row);
	switch (col){
		case COLUMN_NAME:{
			return parameter.getName();
		}
		case COLUMN_DESCRIPTION:{
			return parameter.getDescription();
		}
		case COLUMN_UNIT:{
			if (parameter.getUnitDefinition()!=null){
				return parameter.getUnitDefinition().getSymbol();
			}else{
				return "null";
			}
		}
		case COLUMN_VALUE:{
			if (parameter instanceof SpeciesContextSpec.SpeciesContextSpecParameter){
				if (parameter.getExpression()==null){
					return null;
				}else{
					return new ScopedExpression(parameter.getExpression(),parameter.getNameScope(),parameter.isExpressionEditable(), symbolTableEntryFilter);
				}
			}
		}
		default:{
			return null;
		}
	}
}


/**
 * The hasListeners method was generated to support the propertyChange field.
 */
public synchronized boolean hasListeners(java.lang.String propertyName) {
	return getPropertyChange().hasListeners(propertyName);
}


/**
 * Insert the method's description here.
 * Creation date: (2/24/01 12:27:46 AM)
 * @return boolean
 * @param rowIndex int
 * @param columnIndex int
 */
public boolean isCellEditable(int rowIndex, int columnIndex) {
	Parameter parameter = getParameter(rowIndex);
	if (columnIndex == COLUMN_NAME){
		return parameter.isNameEditable();
	}else if (columnIndex == COLUMN_DESCRIPTION){
		return false;
	}else if (columnIndex == COLUMN_UNIT){
		return false;
	}else if (columnIndex == COLUMN_VALUE){
		return parameter.isExpressionEditable();
	}else{
		return false;
	}
}


/**
 * isSortable method comment.
 */
public boolean isSortable(int col) {
	return true;
}


/**
	 * This method gets called when a bound property is changed.
	 * @param evt A PropertyChangeEvent object describing the event source 
	 *   and the property that has changed.
	 */
public void propertyChange(java.beans.PropertyChangeEvent evt) {
	//
	// must listen to the following events:
	//    this.simulationContext
	//		simulationContext.GeometryContext.geometry
	//      simulationContext.GeometryContext.structureMappings
	//        StructureMapping.resolved
	//
	//    this.speciesContextSpec
	//      SpeciesContextSpec.parameters
	//        Parameter.*
	//
	try {
		if (evt.getSource() == this && evt.getPropertyName().equals("simulationContext")){
			SimulationContext oldSimContext = (SimulationContext)evt.getOldValue();
			if (oldSimContext!=null){
				oldSimContext.getGeometryContext().removePropertyChangeListener(this);
				StructureMapping[] oldStructureMappings = oldSimContext.getGeometryContext().getStructureMappings();
				for (int i = 0; oldStructureMappings!=null && oldStructureMappings!=null && i < oldStructureMappings.length; i++){
					oldStructureMappings[i].removePropertyChangeListener(this);
				}
			}
			SimulationContext newSimContext = (SimulationContext)evt.getNewValue();
			if (newSimContext!=null){
				newSimContext.getGeometryContext().addPropertyChangeListener(this);
				StructureMapping[] newStructureMappings = newSimContext.getGeometryContext().getStructureMappings();
				for (int i = 0; newStructureMappings!=null && i < newStructureMappings.length; i++){
					newStructureMappings[i].addPropertyChangeListener(this);
				}
			}
			setData(getUnsortedParameters());
			fireTableDataChanged();
		}
		
		//
		// if geometry changes (could affect spatially resolved boundaries).
		//
		if (fieldSimulationContext!=null && evt.getSource() == fieldSimulationContext.getGeometryContext() && evt.getPropertyName().equals("geometry")){
			setData(getUnsortedParameters());
			fireTableDataChanged();
		}
		//
		// if structureMappings array changes (could affect spatially resolved boundaries).
		//
		if (fieldSimulationContext!=null && evt.getSource() == fieldSimulationContext.getGeometryContext() && evt.getPropertyName().equals("structureMappings")){
			StructureMapping[] oldStructureMappings = (StructureMapping[])evt.getOldValue();
			for (int i = 0; oldStructureMappings!=null && i < oldStructureMappings.length; i++){
				oldStructureMappings[i].removePropertyChangeListener(this);
			}
			StructureMapping[] newStructureMappings = (StructureMapping[])evt.getNewValue();
			for (int i = 0; newStructureMappings!=null && i < newStructureMappings.length; i++){
				newStructureMappings[i].addPropertyChangeListener(this);
			}
			setData(getUnsortedParameters());
			fireTableDataChanged();
		}
		//
		// if structureMapping changes (could affect spatially resolved boundaries).
		//
		if (evt.getSource() instanceof StructureMapping){
			setData(getUnsortedParameters());
			fireTableDataChanged();
		}
		
		if (evt.getSource() == this && evt.getPropertyName().equals("speciesContextSpec")) {
			SpeciesContextSpec oldValue = (SpeciesContextSpec)evt.getOldValue();
			if (oldValue!=null){
				oldValue.removePropertyChangeListener(this);
				Parameter oldParameters[] = oldValue.getParameters();
				for (int i = 0;oldParameters!=null && i<oldParameters.length; i++){
					oldParameters[i].removePropertyChangeListener(this);
				}
			}
			SpeciesContextSpec newValue = (SpeciesContextSpec)evt.getNewValue();
			if (newValue!=null){
				newValue.addPropertyChangeListener(this);
				Parameter newParameters[] = newValue.getParameters();
				for (int i = 0;newParameters!=null && i<newParameters.length; i++){
					newParameters[i].addPropertyChangeListener(this);
				}
			}
			setData(getUnsortedParameters());
			fireTableDataChanged();
		}
		if (evt.getSource() instanceof SpeciesContextSpec){
			// if parameters changed must update listeners
			if (evt.getPropertyName().equals("parameters")) {
				Parameter oldParameters[] = (Parameter[])evt.getOldValue();
				for (int i = 0;oldParameters!=null && i<oldParameters.length; i++){
					oldParameters[i].removePropertyChangeListener(this);
				}
				Parameter newParameters[] = (Parameter[])evt.getNewValue();
				for (int i = 0;newParameters!=null && i<newParameters.length; i++){
					newParameters[i].addPropertyChangeListener(this);
				}
			}
			// for any change to the SpeciesContextSpec, want to update all.
			setData(getUnsortedParameters());
			fireTableDataChanged();
		}
		if(evt.getSource() instanceof Parameter){
			setData(getUnsortedParameters());
			fireTableDataChanged();
		}
	}catch (Exception e){
		e.printStackTrace(System.out);
	}
}


/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(listener);
}


/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.lang.String propertyName, java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(propertyName, listener);
}


/**
 * Sets the simulationContext property (cbit.vcell.mapping.SimulationContext) value.
 * @param simulationContext The new value for the property.
 * @see #getSimulationContext
 */
public void setSimulationContext(SimulationContext simulationContext) {
	SimulationContext oldValue = fieldSimulationContext;
	fieldSimulationContext = simulationContext;
	if (simulationContext != null) {
		symbolTableEntryFilter = simulationContext.getSymbolTableEntryFilter();
	}
	firePropertyChange("simulationContext", oldValue, simulationContext);
}


/**
 * Sets the speciesContextSpec property (cbit.vcell.mapping.SpeciesContextSpec) value.
 * @param speciesContextSpec The new value for the property.
 * @see #getSpeciesContextSpec
 */
public void setSpeciesContextSpec(SpeciesContextSpec speciesContextSpec) {
	SpeciesContextSpec oldValue = fieldSpeciesContextSpec;
	fieldSpeciesContextSpec = speciesContextSpec;
	firePropertyChange("speciesContextSpec", oldValue, speciesContextSpec);
}


public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	if (columnIndex<0 || columnIndex>=NUM_COLUMNS){
		throw new RuntimeException("ParameterTableModel.setValueAt(), column = "+columnIndex+" out of range ["+0+","+(NUM_COLUMNS-1)+"]");
	}
	Parameter parameter = getParameter(rowIndex);
//	try {
		switch (columnIndex){
			case COLUMN_NAME:{
				try {
					if (aValue instanceof String){
						String newName = (String)aValue;
						if (!parameter.getName().equals(newName)){
							if (parameter instanceof SpeciesContextSpec.SpeciesContextSpecParameter){
								SpeciesContextSpec.SpeciesContextSpecParameter scsParm = (SpeciesContextSpec.SpeciesContextSpecParameter)parameter;
								scsParm.setName(newName);
								//fireTableRowsUpdated(rowIndex,rowIndex);
							}
						}
					}
				}catch (java.beans.PropertyVetoException e){
					e.printStackTrace(System.out);
					PopupGenerator.showErrorDialog("error changing parameter name\n"+e.getMessage());
				}
				break;
			}
			case COLUMN_VALUE:{
				try {
					if (aValue instanceof ScopedExpression){
						Expression exp = ((ScopedExpression)aValue).getExpression();
						if (parameter instanceof SpeciesContextSpec.SpeciesContextSpecParameter){
							SpeciesContextSpec.SpeciesContextSpecParameter scsParm = (SpeciesContextSpec.SpeciesContextSpecParameter)parameter;
							if (!(scsParm.getRole() == SpeciesContextSpec.ROLE_VelocityX || scsParm.getRole() == SpeciesContextSpec.ROLE_VelocityY || scsParm.getRole() == SpeciesContextSpec.ROLE_VelocityZ )) {
								scsParm.setExpression(exp);
							} else {
								// scsParam is a velocity parameter
								if (!exp.compareEqual(new Expression(0.0))) {
									scsParm.setExpression(exp);
								} else {
									scsParm.setExpression(null);
								}
							}
							//fireTableRowsUpdated(rowIndex,rowIndex);
						}
					}else if (aValue instanceof String) {
						String newExpressionString = (String)aValue;
						if (parameter instanceof SpeciesContextSpec.SpeciesContextSpecParameter){
							SpeciesContextSpec.SpeciesContextSpecParameter scsParm = (SpeciesContextSpec.SpeciesContextSpecParameter)parameter;
							if (!(scsParm.getRole() == SpeciesContextSpec.ROLE_VelocityX || scsParm.getRole() == SpeciesContextSpec.ROLE_VelocityY || scsParm.getRole() == SpeciesContextSpec.ROLE_VelocityZ )) {
								scsParm.setExpression(new Expression(newExpressionString));
							} else {
								// scsParam is a velocity parameter
								if (!newExpressionString.equals("0.0")) {
									scsParm.setExpression(new Expression(newExpressionString));
								} else {
									scsParm.setExpression(null);
								}
							}
							//fireTableRowsUpdated(rowIndex,rowIndex);
						}
					}
				}catch (java.beans.PropertyVetoException e){
					e.printStackTrace(System.out);
					PopupGenerator.showErrorDialog(e.getMessage());
				}catch (ExpressionException e){
					e.printStackTrace(System.out);
					PopupGenerator.showErrorDialog("expression error\n"+e.getMessage());
				}
				break;
			}
		}
//	}catch (java.beans.PropertyVetoException e){
//		e.printStackTrace(System.out);
//	}
}


  public void sortColumn(int col, boolean ascending)
  {
    Collections.sort(rows, new ParameterColumnComparator(col, ascending));
  }
}