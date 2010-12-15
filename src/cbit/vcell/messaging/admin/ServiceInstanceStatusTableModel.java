package cbit.vcell.messaging.admin;

import java.util.Comparator;
import java.util.Date;

import org.vcell.util.ComparableObject;
import org.vcell.util.MessageConstants.ServiceType;
import org.vcell.util.gui.sorttable.ColumnComparator;
import org.vcell.util.gui.sorttable.DefaultSortTableModel;


/**
 * Insert the type's description here.
 * Creation date: (8/19/2003 10:46:32 AM)
 * @author: Fei Gao
 */
@SuppressWarnings("serial")
public class ServiceInstanceStatusTableModel extends DefaultSortTableModel<ComparableObject> {

public ServiceInstanceStatusTableModel() {
	super(new String[]{"Site", "Type", "Ordinal", "Host", "Start Date", "Running"});
}

public Class<?> getColumnClass(int columnIndex) {
	if (columnIndex == 0 || columnIndex == 3) {
		return String.class;
	}		
	if (columnIndex == 1) {
		return ServiceType.class;
	}
	if (columnIndex == 5) {
		return Boolean.class;
	}
	if (columnIndex == 2) {
		return Number.class;
	}
	if (columnIndex == 4) {
		return Date.class;
	}
	return Object.class;
}

public Object getValueAt(int row, int col) {
	ComparableObject status = getValueAt(row);
	Object[] values = status.toObjects();
	return values[col];
}

public Comparator<ComparableObject> getComparator(int col, boolean ascending) {
	return new ColumnComparator(col, ascending);
}
}