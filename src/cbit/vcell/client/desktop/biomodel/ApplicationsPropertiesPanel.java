package cbit.vcell.client.desktop.biomodel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vcell.util.document.BioModelChildSummary;
import org.vcell.util.document.Version;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.mapping.SimulationContext;
/**
 * Insert the type's description here.
 * Creation date: (2/3/2003 2:07:01 PM)
 * @author: Frank Morgan
 */
@SuppressWarnings("serial")
public class ApplicationsPropertiesPanel extends JPanel {
	
	private BioModel bioModel = null;
	private EventHandler eventHandler = new EventHandler();
	private JPanel applicationsPanel = null;
	private Icon geometryIcon = new ImageIcon(getClass().getResource("/images/geometry2_16x16.gif"));
	private Icon appTypeIcon = new ImageIcon(getClass().getResource("/images/type.gif"));
	private Icon simulationContextIcon = new ImageIcon(getClass().getResource("/images/application3_16x16.gif"));
	
	private class EventHandler implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			// TODO Auto-generated method stub
			if (evt.getSource() == bioModel && evt.getPropertyName().equals(BioModel.PROPERTY_NAME_SIMULATION_CONTEXTS)) {
				updateInterface();
			}
		}
	}

/**
 * EditSpeciesDialog constructor comment.
 */
public ApplicationsPropertiesPanel() {
	super();
	initialize();
}

/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	exception.printStackTrace(System.out);
}

/**
 * Initialize the class.
 */
private void initialize() {
	try {		
		applicationsPanel = new JPanel(new GridBagLayout());
		applicationsPanel.setBackground(Color.white);
		
		setLayout(new BorderLayout());
		add(new JScrollPane(applicationsPanel), BorderLayout.CENTER);
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
}

/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		javax.swing.JFrame frame = new javax.swing.JFrame();
		ApplicationsPropertiesPanel aEditSpeciesPanel = new ApplicationsPropertiesPanel();
		frame.add(aEditSpeciesPanel);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		frame.pack();
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of cbit.gui.JInternalFrameEnhanced");
		exception.printStackTrace(System.out);
	}
}

/**
 * Sets the speciesContext property (cbit.vcell.model.SpeciesContext) value.
 * @param speciesContext The new value for the property.
 * @see #getSpeciesContext
 */
public void setBioModel(BioModel newValue) {
	if (newValue == bioModel) {
		return;
	}
	BioModel oldValue = bioModel;
	if (oldValue != null) {
		oldValue.removePropertyChangeListener(eventHandler);
		for (SimulationContext simContext : oldValue.getSimulationContexts()) {
			simContext.removePropertyChangeListener(eventHandler);
		}
	}
	bioModel = newValue;
	if (newValue != null) {
		newValue.addPropertyChangeListener(eventHandler);
		for (SimulationContext simContext : newValue.getSimulationContexts()) {
			simContext.addPropertyChangeListener(eventHandler);
		}
	}
	updateInterface();
}

/**
 * Comment
 */
private void updateInterface() {
	if (bioModel == null) {
		return;
	}
	applicationsPanel.removeAll();
	int gridy = 0;
	SimulationContext[] simulationContexts = bioModel.getSimulationContexts();
	if (simulationContexts != null) {
		for (int i = 0; i < simulationContexts.length; i ++) {
			SimulationContext simContext = simulationContexts[i];
			JLabel label = new JLabel(simContext.getName());
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			label.setIcon(simulationContextIcon);
			
			GridBagConstraints gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 0; 
			gbc.gridy = gridy ++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			if (i > 0) {
				gbc.insets = new Insets(4, 0, 0, 0);
			}
			applicationsPanel.add(label, gbc);

			Geometry geometry = simContext.getGeometry();
			String geometryText = "Compartmental geometry";
			if (geometry != null) {
				Version geometryVersion = geometry.getVersion();
				int dimension = geometry.getDimension();
				if (dimension > 0){
					String description = geometry.getDimension() + "D " + (geometry.getGeometrySpec().hasImage() ? "image" : "analytic") + " geometry";
					geometryText = description;
					if (geometryVersion != null) {
						geometryText += " - " + geometryVersion.getName()/* + " ("+geometryVersion.getDate() + ")"*/;
					}
				}
			}
			JLabel geometryLabel = new JLabel(geometryText);
			geometryLabel.setIcon(geometryIcon);
			JLabel detStochLabel = new JLabel((simContext.isStoch() ? BioModelChildSummary.TYPE_STOCH_STR : BioModelChildSummary.TYPE_DETER_STR));
			detStochLabel.setIcon(appTypeIcon);
			
			gbc.insets = new Insets(2, 20, 2, 2);
			gbc.gridy = gridy ++;
			applicationsPanel.add(detStochLabel, gbc);
			gbc.gridy = gridy ++;
			if (i == simulationContexts.length - 1) {
				gbc.weighty = 1.0;
			}
			applicationsPanel.add(geometryLabel, gbc);
		}
	}
}

}
