package cbit.vcell.client.desktop.biomodel;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.EditorScrollTable;

import cbit.gui.AutoCompleteSymbolFilter;
import cbit.vcell.model.Feature;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.Model;
import cbit.vcell.model.Structure;
import cbit.vcell.parser.SymbolTable;

@SuppressWarnings("serial")
public class BioModelEditorStructureTableModel extends BioModelEditorRightSideTableModel<Structure> {

	public final static int COLUMN_NAME = 0;
	public final static int COLUMN_TYPE = 1;
	public final static int COLUMN_INSIDE_COMPARTMENT = 2;
	public final static int COLUMN_OUTSIDE_COMPARTMENT = 3;	
	public final static int COLUMN_SIZE_NAME = 4;	
	public final static int COLUMN_OUTSIDE_VOLTAGE_NAME = 5;	
	private static String[] columnNames = new String[] {"Name", "Type", "Inside", "Outside Parent", "Size", "Voltage"};

	public BioModelEditorStructureTableModel(EditorScrollTable table) {
		super(table);
		setColumns(columnNames);
		addPropertyChangeListener(this);
	}
	
	public Class<?> getColumnClass(int column) {
		switch (column){		
			case COLUMN_NAME:{
				return String.class;
			}
			case COLUMN_TYPE:{
				return String.class;
			}
			case COLUMN_INSIDE_COMPARTMENT:{
				return Structure.class;
			}
			case COLUMN_OUTSIDE_COMPARTMENT:{
				return Structure.class;
			}
			case COLUMN_SIZE_NAME:{
				return String.class;
			}
			case COLUMN_OUTSIDE_VOLTAGE_NAME:{
				return String.class;
			}
		}
		return Object.class;
	}

	protected ArrayList<Structure> computeData() {
		ArrayList<Structure> structureList = new ArrayList<Structure>();
		if (getModel() != null){
			for (Structure s : getModel().getStructures()){
				if (searchText == null || searchText.length() == 0 || s.getName().startsWith(searchText)) {
					structureList.add(s);
				}
			}
		}
		return structureList;
	}

	public Object getValueAt(int row, int column) {
		if (getModel() == null) {
			return null;
		}
		try{
			if (row >= 0 && row < getDataSize()) {
				Structure structure = getValueAt(row);
				switch (column) {
					case COLUMN_NAME: {
						return structure.getName();
					} 
					case COLUMN_TYPE: {
						return structure.getTypeName();
					} 
					case COLUMN_INSIDE_COMPARTMENT: {
						return structure instanceof Membrane ? ((Membrane)structure).getInsideFeature() : null;
					} 
					case COLUMN_OUTSIDE_COMPARTMENT: {
						return structure.getParentStructure();
					}
					case COLUMN_SIZE_NAME:
						return structure.getStructureSize().getName();
						
					case COLUMN_OUTSIDE_VOLTAGE_NAME:
						if (structure instanceof Feature) {
							return "n/a";
						}
						if (structure instanceof Membrane) {
							return  ((Membrane)structure).getMembraneVoltage().getName();
						}
				}
			} else {
				if (column == COLUMN_NAME) {
					return ADD_NEW_HERE_TEXT;
				} 
			}
		} catch(Exception e){
			e.printStackTrace(System.out);
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == COLUMN_NAME) {
			return true;
		}
		if (row < getDataSize()) {
			if (column == COLUMN_TYPE || column == COLUMN_SIZE_NAME || column == COLUMN_OUTSIDE_VOLTAGE_NAME) {
				return false;
			}
			if (bioModel != null && bioModel.getModel().getNumStructures() < 2) {
				return false;
			}
			if (column == COLUMN_INSIDE_COMPARTMENT) {	
				if (row >= 0 && row < getDataSize()) {
					Structure s = getValueAt(row);
					return s instanceof Membrane;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getModel() && evt.getPropertyName().equals(Model.PROPERTY_NAME_STRUCTURES)) {
			Structure[] oldValue = (Structure[]) evt.getOldValue();
			if (oldValue != null) {
				for (Structure s : oldValue) {
					s.removePropertyChangeListener(this);
				}
			}
			Structure[] newValue = (Structure[]) evt.getOldValue();
			if (newValue != null) {
				for (Structure s : newValue) {
					s.addPropertyChangeListener(this);
				}
			}
			refreshData();
			updateStructureComboBox();
		} else if (evt.getSource() instanceof Structure) {
			fireTableDataChanged();
		}
	}

	public void setValueAt(Object value, int row, int column) {
		if (getModel() == null || value == null) {
			return;
		}
		try{
			if (row >= 0 && row < getDataSize()) {
				Structure structure = getValueAt(row);
				switch (column) {
				case COLUMN_NAME: {
					String inputValue = (String)value;
					inputValue = inputValue.trim();
					if (!inputValue.equals(structure.getName())) {
						structure.setName(inputValue);
						structure.getStructureSize().setName(Structure.getDefaultStructureSizeName(inputValue));
						if (structure instanceof Membrane) {
							((Membrane)structure).getMembraneVoltage().setName(Membrane.getDefaultMembraneVoltageName(inputValue));
						}
					}
					break;
				} 
				case COLUMN_INSIDE_COMPARTMENT: {
					Structure insideFeature = (Structure)value;
					if (insideFeature instanceof Membrane) {
						DialogUtils.showErrorDialog(ownerTable, Structure.TYPE_NAME_FEATURE + " is expected!");
					} else {
						((Membrane)structure).setInsideFeature((Feature)insideFeature);
					}
					break;
				} 
				case COLUMN_OUTSIDE_COMPARTMENT: {
					Structure outsideFeature = (Structure)value;
					structure.setParentStructure(outsideFeature);
					break;
				} 
				}
			} else {
				switch (column) {
				case COLUMN_NAME: {
					String inputValue = ((String)value);
					if (inputValue.equals(ADD_NEW_HERE_TEXT)) {
						return;
					}
					inputValue = inputValue.trim();
					Feature parentFeature = null;
					for (int i = getModel().getNumStructures() - 1; i >= 0; i --) {
						if (getModel().getStructures()[i] instanceof Feature) {
							parentFeature = (Feature) getModel().getStructures()[i];
							break;
						}
					}
					Feature feature = getModel().createFeature(parentFeature);
					feature.setName(inputValue);
					break;
				} 
				}
			}
		} catch(Exception e){
			e.printStackTrace(System.out);
			DialogUtils.showErrorDialog(ownerTable, e.getMessage(), e);
		}
	}

	@Override
	public boolean isSortable(int col) {
		return false;
	}
	
	@Override
	public Comparator<Structure> getComparator(int col, boolean ascending) {
		// TODO Auto-generated method stub		
		return null;
	}

	public String checkInputValue(String inputValue, int row, int column) {
		Structure structure = null;
		if (row >= 0 && row < getDataSize()) {
			structure = getValueAt(row);
		}
		switch (column) {
		case COLUMN_NAME:
			if (structure == null || !structure.getName().equals(inputValue)) {
				if (getModel().getStructure(inputValue) != null) {
					return "Structure '" + inputValue + "' already exist!";
				}
			}
			break;
		case COLUMN_INSIDE_COMPARTMENT: {
			Structure s = getModel().getStructure(inputValue);
			if (s == null) {
				return "Compartment '" + inputValue + "' does not exist!";
			} 
			if (s instanceof Membrane) {
				return "Structure '" + inputValue + "' is not a compartment!";
			}
			break;
		}
		case COLUMN_OUTSIDE_COMPARTMENT: {
			Structure s = getModel().getStructure(inputValue);
			if (s == null) {
				return "Structure '" + inputValue + "' does not exist!";
			} 
			if (structure != null){
				return structure.checkNewParent(s);
			}
			break;
		}
		}
		return null;
	}
	
	public SymbolTable getSymbolTable(int row, int column) {
		return null;
	}
	
	public AutoCompleteSymbolFilter getAutoCompleteSymbolFilter(final int row, final int column) {
		return null;
	}

	public Set<String> getAutoCompletionWords(int row, int column) {
		return null;
	}

	@Override
	protected void bioModelChange(PropertyChangeEvent evt) {		
		super.bioModelChange(evt);
		ownerTable.getColumnModel().getColumn(COLUMN_INSIDE_COMPARTMENT).setCellEditor(getStructureComboBoxEditor());
		ownerTable.getColumnModel().getColumn(COLUMN_OUTSIDE_COMPARTMENT).setCellEditor(getStructureComboBoxEditor());
		updateStructureComboBox();
	}
}
