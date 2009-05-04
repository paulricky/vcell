package cbit.gui.graph;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import javax.swing.*;

import org.vcell.util.gui.ButtonGroupCivilized;

import java.util.*;
import cbit.gui.*;
import java.awt.event.*;
import java.awt.*;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public abstract class CartoonTool implements MouseListener, MouseMotionListener, ActionListener, KeyListener {
	//
	private GraphPane graphPane = null;
	private ButtonGroupCivilized buttonGroup = null;
	//
	public static final String ANNEALER = "Annealer";
	public static final String CIRCULARIZER = "Circularizer";
	public static final String CYCLEIZER = "Cycleizer";
	public static final String FORCEDIRECT = "ForceDirect";
	public static final String LEVELLER = "Leveller";
	public static final String RANDOMIZER = "Randomizer";
	public static final String RELAXER = "Relaxer";
	public static final String STABILIZER = "Stabilizer";

	//
	public static final String DELETE_MENU_ACTION = "Delete";
	public static final String PROPERTIES_MENU_ACTION = "Properties";
	public static final String REACTIONS_MENU_ACTION = "Reactions...";
	public static final String ADD_ENZYME_REACTION_MENU_ACTION = "Add Searchable Reaction...";
	public static final String ADD_FEATURE_MENU_ACTION = "Add Feature...";
	public static final String ADD_SPECIES_MENU_ACTION = "Add Species...";
	public static final String ADD_GLOBAL_PARAM_MENU_ACTION = "Add Global Parameter...";
	public static final String ADD_BINDINGSITE_MENU_ACTION = "Add Binding Site";
	public static final String ADD_COMPLEX_MENU_ACTION = "Add Complex";
	public static final String COPY_MENU_ACTION = "Copy";
	public static final String CUT_MENU_ACTION = "Cut";
	public static final String PASTE_MENU_ACTION = "Paste";
	public static final String PASTE_NEW_MENU_ACTION = "Paste New";
	public static final String MOVE_MENU_ACTION = "Move...";
	public static final String ENABLE_MENU_ACTION = "Enable";
	public static final String DISABLE_MENU_ACTION = "Disable";
	public static final String SOLVE_MENU_ACTION = "Solve";
	public static final String RESET_MENU_ACTION = "Reset";
	public static final String SHOW_PARAMETERS_MENU_ACTION = "Parameters and Rate Expressions";
	//reaction and structure cartoon image actions
	public static final String HIGH_RES_MENU_ACTION = "High Res (x3.0)";
	public static final String MED_RES_MENU_ACTION = "Medium Res (x2.0)";
	public static final String LOW_RES_MENU_ACTION = "Low Res (x1.0)";
	//MIRIAM
	public static final String ANNOTATE_MENU_ACTION = "Annotate...";	
	//
	public static final String DELETE_MENU_TEXT = DELETE_MENU_ACTION;
	public static final String PROPERTIES_MENU_TEXT = PROPERTIES_MENU_ACTION;
	public static final String REACTIONS_MENU_TEXT = REACTIONS_MENU_ACTION;
	public static final String ADD_ENZYME_REACTION_MENU_TEXT = ADD_ENZYME_REACTION_MENU_ACTION;
	public static final String ADD_FEATURE_MENU_TEXT = ADD_FEATURE_MENU_ACTION;
	public static final String ADD_SPECIES_MENU_TEXT = ADD_SPECIES_MENU_ACTION;
	public static final String ADD_GLOBAL_PARAM_MENU_TEXT = ADD_GLOBAL_PARAM_MENU_ACTION;
	public static final String ADD_BINDINGSITE_MENU_TEXT = ADD_BINDINGSITE_MENU_ACTION;
	public static final String ADD_COMPLEX_MENU_TEXT = ADD_COMPLEX_MENU_ACTION;
	public static final String COPY_MENU_TEXT = COPY_MENU_ACTION;
	public static final String CUT_MENU_TEXT = CUT_MENU_ACTION;
	public static final String PASTE_MENU_TEXT = PASTE_MENU_ACTION;
	public static final String PASTE_NEW_MENU_TEXT = PASTE_NEW_MENU_ACTION;
	public static final String MOVE_MENU_TEXT = MOVE_MENU_ACTION;
	public static final String ENABLE_MENU_TEXT = ENABLE_MENU_ACTION;
	public static final String DISABLE_MENU_TEXT = DISABLE_MENU_ACTION;
	public static final String SOLVE_MENU_TEXT = SOLVE_MENU_ACTION;
	public static final String RESET_MENU_TEXT = RESET_MENU_ACTION;
	public static final String SHOW_PARAMETERS_MENU_TEXT = SHOW_PARAMETERS_MENU_ACTION;
	//reaction and structure cartoon image menu texts
	public static final String SAVE_AS_IMAGE_MENU_TEXT = "Save As Image";
	public static final String HIGH_RES_MENU_TEXT = HIGH_RES_MENU_ACTION;
	public static final String MED_RES_MENU_TEXT = MED_RES_MENU_ACTION;
	public static final String LOW_RES_MENU_TEXT = LOW_RES_MENU_ACTION;
	//
	//MIRIAM
	public static final String ANNOTATE_MENU_TEXT = ANNOTATE_MENU_ACTION;	

	public static final String[] ACTION_COMMANDS = {"select", "feature", "species", "line", "step", "flux", "spline","addCP","complex","bindingSite","interaction"};
	
	public static final int SELECT_MODE = 0;
	public static final int FEATURE_MODE = 1;
	public static final int SPECIES_MODE = 2;
	public static final int LINE_MODE = 3;
	protected static final int STEP_MODE = 4;
	protected static final int FLUX_MODE = 5;
	public static final int SPLINE_MODE = 6;
	public static final int ADDCP_MODE = 7;
	public static final int COMPLEX_MODE = 8;
	public static final int BINDINGSITE_MODE = 9;
	public static final int INTERACTION_MODE = 10;
	//
	//Menu Stuff
	//
		private JPopupMenu lastJPopupMenu = null;
		private Vector lastMenuList = null;
		//
		JMenuItem addFeatureJMenuItem = new javax.swing.JMenuItem();
		JMenuItem addSpeciesJMenuItem = new javax.swing.JMenuItem();
		JMenuItem addGlobalParamJMenuItem = new javax.swing.JMenuItem();
		JMenuItem addEnzymeReactionJMenuItem = new javax.swing.JMenuItem();
		JMenuItem reactionsJMenuItem = new javax.swing.JMenuItem();
		JMenuItem propertiesJMenuItem = new javax.swing.JMenuItem();
		JMenuItem enableJMenuItem = new javax.swing.JMenuItem();
		JMenuItem disableJMenuItem = new javax.swing.JMenuItem();
		JMenuItem solveJMenuItem = new javax.swing.JMenuItem();
		JMenuItem resetJMenuItem = new javax.swing.JMenuItem();
		JMenuItem showParametersJMenuItem = new javax.swing.JMenuItem();
		JMenuItem addBindingSiteJMenuItem = new javax.swing.JMenuItem();
		JMenuItem addComplexJMenuItem = new javax.swing.JMenuItem();
		//reaction and structure cartoon image menus
		JMenuItem saveAsImageJMenu = new javax.swing.JMenu();                  
		JMenuItem highResJMenuItem = new javax.swing.JMenuItem();
		JMenuItem medResJMenuItem = new javax.swing.JMenuItem();
		JMenuItem lowResJMenuItem = new javax.swing.JMenuItem();
		//Things for Edit menu
		JMenuItem copyJMenuItem = new javax.swing.JMenuItem();
		JMenuItem deleteJMenuItem = new javax.swing.JMenuItem();
		JMenuItem cutJMenuItem = new javax.swing.JMenuItem();
		JMenuItem pasteJMenuItem = new javax.swing.JMenuItem();
		JMenuItem pastenewJMenuItem = new javax.swing.JMenuItem();
		JMenuItem moveJMenuItem = new javax.swing.JMenuItem();
		//MIRIAM
		JMenuItem annotateJMenuItem = new JMenuItem();
		//
		//Add new JMenuItems here too.  Used for convenience to add and remove actionlisteners
		private JMenuItem[] jmenuItemArr =
			{	addFeatureJMenuItem,addSpeciesJMenuItem,addGlobalParamJMenuItem,addEnzymeReactionJMenuItem,reactionsJMenuItem,propertiesJMenuItem,
				enableJMenuItem,disableJMenuItem,solveJMenuItem,resetJMenuItem,showParametersJMenuItem,
				copyJMenuItem,deleteJMenuItem,cutJMenuItem,pasteJMenuItem,pastenewJMenuItem,moveJMenuItem,addBindingSiteJMenuItem,
				addComplexJMenuItem,saveAsImageJMenu,annotateJMenuItem
			};

/**
 * This method was created by a SmartGuide.
 * @param canvas cbit.vcell.graph.CartoonCanvas
 */
public CartoonTool () {
	//MIRIAM
	annotateJMenuItem.setName("JMenuItemAnnotate");
	annotateJMenuItem.setActionCommand(ANNOTATE_MENU_ACTION);
//	annotateJMenuItem.setMnemonic('m');
	annotateJMenuItem.setText(ANNOTATE_MENU_TEXT);
	//
	addFeatureJMenuItem.setName("JMenuItemAddFeature");
	addFeatureJMenuItem.setActionCommand(ADD_FEATURE_MENU_ACTION);
	addFeatureJMenuItem.setMnemonic('f');
	addFeatureJMenuItem.setText(ADD_FEATURE_MENU_TEXT);
	
	addSpeciesJMenuItem.setName("JMenuItemAddSpecies");
	addSpeciesJMenuItem.setActionCommand(ADD_SPECIES_MENU_ACTION);
	addSpeciesJMenuItem.setMnemonic('s');
	addSpeciesJMenuItem.setText(ADD_SPECIES_MENU_TEXT);

	addGlobalParamJMenuItem.setName("JMenuItemAddGlobalParam");
	addGlobalParamJMenuItem.setActionCommand(ADD_GLOBAL_PARAM_MENU_ACTION);
	addGlobalParamJMenuItem.setMnemonic('g');
	addGlobalParamJMenuItem.setText(ADD_GLOBAL_PARAM_MENU_TEXT);

	addComplexJMenuItem.setName("JMenuItemAddComplex");
	addComplexJMenuItem.setActionCommand(ADD_COMPLEX_MENU_ACTION);
	//addComplexJMenuItem.setMnemonic('c');
	addComplexJMenuItem.setText(ADD_COMPLEX_MENU_TEXT);

	addBindingSiteJMenuItem.setName("JMenuItemAddBindingSite");
	addBindingSiteJMenuItem.setActionCommand(ADD_BINDINGSITE_MENU_ACTION);
	//addBindingSiteJMenuItem.setMnemonic('b');
	addBindingSiteJMenuItem.setText(ADD_BINDINGSITE_MENU_TEXT);

	addEnzymeReactionJMenuItem.setName("JMenuItemAddEnzymeReaction");
	addEnzymeReactionJMenuItem.setActionCommand(ADD_ENZYME_REACTION_MENU_ACTION);
	addEnzymeReactionJMenuItem.setMnemonic('r');
	addEnzymeReactionJMenuItem.setText(ADD_ENZYME_REACTION_MENU_TEXT);

	reactionsJMenuItem.setName("JMenuItemReactions");
	reactionsJMenuItem.setActionCommand(REACTIONS_MENU_ACTION);
	reactionsJMenuItem.setMnemonic('r');
	reactionsJMenuItem.setText(REACTIONS_MENU_TEXT);

	propertiesJMenuItem.setName("JMenuItemProperties");
	propertiesJMenuItem.setActionCommand(PROPERTIES_MENU_ACTION);
	propertiesJMenuItem.setMnemonic('p');
	propertiesJMenuItem.setText(PROPERTIES_MENU_TEXT);
	
	enableJMenuItem.setName("JMenuItemEnable");
	enableJMenuItem.setActionCommand(ENABLE_MENU_ACTION);
	//enableJMenuItem.setMnemonic('e');
	enableJMenuItem.setText(ENABLE_MENU_TEXT);
	
	disableJMenuItem.setName("JMenuItemDisable");
	disableJMenuItem.setActionCommand(DISABLE_MENU_ACTION);
	//disableJMenuItem.setMnemonic('d');
	disableJMenuItem.setText(DISABLE_MENU_TEXT);
	
	solveJMenuItem.setName("JMenuItemSolve");
	solveJMenuItem.setActionCommand(SOLVE_MENU_ACTION);
	//solveJMenuItem.setMnemonic('s');
	solveJMenuItem.setText(SOLVE_MENU_TEXT);
	
	resetJMenuItem.setName("JMenuItemReset");
	resetJMenuItem.setActionCommand(RESET_MENU_ACTION);
	//resetJMenuItem.setMnemonic('r');
	resetJMenuItem.setText(RESET_MENU_TEXT);

	showParametersJMenuItem.setName("JMenuItemShowParameters");
	showParametersJMenuItem.setActionCommand(SHOW_PARAMETERS_MENU_ACTION);
	//showParametersJMenuItem.setMnemonic('p');
	showParametersJMenuItem.setText(SHOW_PARAMETERS_MENU_TEXT);

	//image menu items
	highResJMenuItem.setName("JMenuItemHigRes");
	highResJMenuItem.setActionCommand(HIGH_RES_MENU_ACTION);
	highResJMenuItem.setMnemonic('h');
	highResJMenuItem.setText(HIGH_RES_MENU_TEXT);
	medResJMenuItem.setName("JMenuItemMedRes");
	medResJMenuItem.setActionCommand(MED_RES_MENU_ACTION);
	medResJMenuItem.setMnemonic('m');
	medResJMenuItem.setText(MED_RES_MENU_TEXT);
	lowResJMenuItem.setName("JMenuItemLowRes");
	lowResJMenuItem.setActionCommand(LOW_RES_MENU_ACTION);
	lowResJMenuItem.setMnemonic('l');
	lowResJMenuItem.setText(LOW_RES_MENU_TEXT);
	saveAsImageJMenu.setName("JMenuSaveAsImage");
	saveAsImageJMenu.setMnemonic('i');
	saveAsImageJMenu.setText("Save As Image");
	saveAsImageJMenu.add(highResJMenuItem);
	saveAsImageJMenu.add(medResJMenuItem);
	saveAsImageJMenu.add(lowResJMenuItem);
	//
	//Things for Edit menu
	//
	copyJMenuItem.setName("JMenuItemCopy");
	copyJMenuItem.setActionCommand(COPY_MENU_ACTION);
	copyJMenuItem.setMnemonic('c');
	copyJMenuItem.setText(COPY_MENU_TEXT);
	copyJMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

	deleteJMenuItem.setName("JMenuItemDelete");
	deleteJMenuItem.setActionCommand(DELETE_MENU_ACTION);
	deleteJMenuItem.setMnemonic('d');
	deleteJMenuItem.setText(DELETE_MENU_TEXT);
	//deleteJMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
	
	cutJMenuItem.setName("JMenuItemCut");
	cutJMenuItem.setActionCommand(CUT_MENU_ACTION);
	cutJMenuItem.setMnemonic('x');
	cutJMenuItem.setText(CUT_MENU_TEXT);
	cutJMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	
	pasteJMenuItem.setName("JMenuItemPaste");
	pasteJMenuItem.setActionCommand(PASTE_MENU_ACTION);
	pasteJMenuItem.setMnemonic('v');
	pasteJMenuItem.setText(PASTE_MENU_TEXT);
	pasteJMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	
	pastenewJMenuItem.setName("JMenuItemPasteNew");
	pastenewJMenuItem.setActionCommand(PASTE_NEW_MENU_ACTION);
	pastenewJMenuItem.setMnemonic('w');
	pastenewJMenuItem.setText(PASTE_NEW_MENU_TEXT);

	moveJMenuItem.setName("JMenuItemMove");
	moveJMenuItem.setActionCommand(MOVE_MENU_ACTION);
	moveJMenuItem.setMnemonic('m');
	moveJMenuItem.setText(MOVE_MENU_TEXT);
	
	//JPopupMenu items for cartoon tools
	//
	for(int i = 0 ; i < jmenuItemArr.length;i+= 1){
		if (jmenuItemArr[i] instanceof JMenu) {           //special handling for the image-submenu
			for (int k = 0; k < ((JMenu)jmenuItemArr[i]).getItemCount(); k++) {
				JMenuItem menuItem = ((JMenu)jmenuItemArr[i]).getItem(k);
				menuItem.addActionListener(this);
			}
		} else {
			jmenuItemArr[i].addActionListener(this);
		}
	}
}


/**
 * This method was created by a SmartGuide.
 * @param event java.awt.event.ItemEvent
 */
public final void actionPerformed(ActionEvent event) {
	if(getGraphModel() == null){
		return;
	}
//	try {
		Shape popupMenuShape = getGraphModel().getSelectedShape();
		if ((event.getSource() instanceof JMenuItem)){
			//if (popupMenuShape!=null){
				menuAction(popupMenuShape,event.getActionCommand());		
			//}
		}
	//}catch (Exception e){
		//e.printStackTrace(System.out);
	//}		
}


/**
 * Insert the method's description here.
 * Creation date: (7/14/00 10:31:36 AM)
 * @return java.lang.String
 * @param mode int
 */
public final static String getActionCommand(int mode) {
	try {
		return ACTION_COMMANDS[mode];
	} catch (ArrayIndexOutOfBoundsException exc) {
		System.out.println("ERROR: mode " + mode + " not defined");
		return null;
	}
}


/**
 * Insert the method's description here.
 * Creation date: (7/14/00 11:40:05 AM)
 * @return cbit.gui.ButtonGroupCivilized
 */
public org.vcell.util.gui.ButtonGroupCivilized getButtonGroup() {
	return buttonGroup;
}


/**
 * Insert the method's description here.
 * Creation date: (5/13/2003 7:23:09 PM)
 * @return java.awt.Container
 */
protected static final java.awt.Container getDialogOwner(GraphPane graphPaneSeekingOwner) {
	if(graphPaneSeekingOwner == null){
		return null;
	}
	
	Container dialogOwner = (JDesktopPane)org.vcell.util.BeanUtils.findTypeParentOfComponent(graphPaneSeekingOwner,javax.swing.JDesktopPane.class);
	if (dialogOwner!=null){
		return dialogOwner;
	}
	
	dialogOwner = (JFrame)org.vcell.util.BeanUtils.findTypeParentOfComponent(graphPaneSeekingOwner,javax.swing.JFrame.class);
	if (dialogOwner!=null){
		return ((JFrame)dialogOwner).getContentPane();
	}
	
	return dialogOwner;
}


/**
 * Insert the method's description here.
 * Creation date: (9/9/2002 10:25:13 AM)
 * @return cbit.vcell.graph.GraphModel
 */
public abstract GraphModel getGraphModel();


/**
 * This method was created in VisualAge.
 * @param graphPane cbit.vcell.graph.GraphPane
 */
public GraphPane getGraphPane() {
	return this.graphPane;
}


/**
 * Insert the method's description here.
 * Creation date: (5/13/2003 7:23:09 PM)
 * @return java.awt.Container
 */
protected JDesktopPane getJDesktopPane() {
	if(getGraphPane() == null){
		return null;
	}
	return (JDesktopPane)org.vcell.util.BeanUtils.findTypeParentOfComponent(getGraphPane(),javax.swing.JDesktopPane.class);
}


/**
 * Insert the method's description here.
 * Creation date: (7/14/00 10:28:20 AM)
 * @return int
 * @param actionCommand java.lang.String
 */
public final static int getMode(String actionCommand) {
	for (int i=0;i<ACTION_COMMANDS.length;i++) {
		if (ACTION_COMMANDS[i].equals(actionCommand)) {
			return i;
		}
	}
	System.out.println("ERROR: action command " + actionCommand + " not defined");
	return -1;
}


/**
 * Insert the method's description here.
 * Creation date: (5/10/2003 10:10:14 PM)
 * @param e java.awt.event.KeyEvent
 */
public void keyPressed(KeyEvent e) {
	
	if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
		if(lastJPopupMenu != null && lastJPopupMenu.isVisible()){
			MenuSelectionManager.defaultManager().clearSelectedPath();
		}
		else{
			getGraphModel().clearSelection();
		}	
	}
	if((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)){
		menuAction(getGraphModel().getSelectedShape(),COPY_MENU_ACTION);
	}
	if((e.getKeyCode() == KeyEvent.VK_X) && ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)){
		menuAction(getGraphModel().getSelectedShape(),CUT_MENU_ACTION);
	}
	if((e.getKeyCode() == KeyEvent.VK_V) && ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)){
		menuAction(getGraphModel().getSelectedShape(),PASTE_MENU_ACTION);
	}
	if((e.getKeyCode() == KeyEvent.VK_DELETE)){
		menuAction(getGraphModel().getSelectedShape(),DELETE_MENU_ACTION);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (5/10/2003 10:10:32 PM)
 * @param e java.awt.event.KeyEvent
 */
public void keyReleased(KeyEvent e) {
}


/**
 * Insert the method's description here.
 * Creation date: (5/10/2003 10:10:56 PM)
 * @param e java.awt.event.KeyEvent
 */
public void keyTyped(KeyEvent e) {
}


/**
 * Insert the method's description here.
 * Creation date: (9/17/2002 3:56:54 PM)
 * @param shape cbit.vcell.graph.Shape
 * @param menuAction java.lang.String
 */
protected abstract void menuAction(Shape shape, String menuAction);


/**
 * This method was created by a SmartGuide.
 * @param event java.awt.event.MouseEvent
 */
public void mouseClicked(java.awt.event.MouseEvent event) {
}


/**
 * This method was created by a SmartGuide.
 * @param event java.awt.event.MouseEvent
 */
public void mouseDragged(java.awt.event.MouseEvent event) {
}


/**
 * This method was created by a SmartGuide.
 * @param event java.awt.event.MouseEvent
 */
public void mouseEntered(java.awt.event.MouseEvent event) {
}


/**
 * This method was created by a SmartGuide.
 * @param event java.awt.event.MouseEvent
 */
public void mouseExited(java.awt.event.MouseEvent event) {
}


/**
 * This method was created by a SmartGuide.
 * @param event java.awt.event.MouseEvent
 */
public void mouseMoved(java.awt.event.MouseEvent event) {
}


/**
 * This method was created by a SmartGuide.
 * @param event java.awt.event.MouseEvent
 */
public void mousePressed(java.awt.event.MouseEvent event) {
}


/**
 * This method was created by a SmartGuide.
 * @param event java.awt.event.MouseEvent
 */
public void mouseReleased(java.awt.event.MouseEvent event) {
}


/**
 * This method was created by a SmartGuide.
 */
protected final void popupMenu(Shape shape,int x, int y) throws Exception {

	if(getGraphPane() == null){
		return;
	}
	//
	//For efficiency, this will reuse last JPopupMenu if possible.
	//Could be made smarter and keep list of more than 1 previously
	//  constructed JPopupMenus and reuse
	//
	if(shape != null){

		Vector currentMenuList = new Vector();
		//
		//Add stuff to menu based on state of cartoons
		//
		for(int i = 0 ; i < jmenuItemArr.length;i+= 1){
			if (jmenuItemArr[i] instanceof JMenu) {           //accomodate the image-submenu
				boolean menuNeeded = false;
				for (int k = 0; k < ((JMenu)jmenuItemArr[i]).getItemCount(); k++) {
					JMenuItem menuItem = ((JMenu)jmenuItemArr[i]).getItem(k);
					if (shapeHasMenuAction(shape,menuItem.getActionCommand())){
						boolean actionEnabled = shapeHasMenuActionEnabled(shape, menuItem.getActionCommand());
						menuItem.setEnabled(actionEnabled);
						if (!menuNeeded) {
							menuNeeded = true;
						}
					}
				}
				if (menuNeeded) {
					jmenuItemArr[i].setEnabled(true);
					currentMenuList.add(jmenuItemArr[i]);
				}
				continue;
			}
			if (shapeHasMenuAction(shape,jmenuItemArr[i].getActionCommand())){
				currentMenuList.add(jmenuItemArr[i]);
				jmenuItemArr[i].setEnabled(shapeHasMenuActionEnabled(shape,jmenuItemArr[i].getActionCommand()));
			}
		}
		//
		//See if anything changed
		//
		JPopupMenu popupMenu = null;
		//
		boolean bSame = true;
		if(lastMenuList == null || !lastMenuList.equals(currentMenuList)){bSame = false;}
		if(bSame){
			for(int i = 0; i < currentMenuList.size();i+= 1){
				if(((JMenuItem)lastMenuList.get(i)).isEnabled() != ((JMenuItem)currentMenuList.get(i)).isEnabled()){
					bSame = false;
					break;
				}
			}
		}
		if(!bSame){//Make new JPopupMenu if its different than the last
			Vector editV = new Vector();
			popupMenu = new javax.swing.JPopupMenu();
			popupMenu.setName("CartoonToolJPopupMenu");
			//
			for(int i = 0; i < currentMenuList.size();i+= 1){
				JMenuItem addableJMenuItem = (JMenuItem)currentMenuList.get(i);
				if(
					addableJMenuItem == annotateJMenuItem ||
					addableJMenuItem == addFeatureJMenuItem ||
					addableJMenuItem == addSpeciesJMenuItem ||
					addableJMenuItem == addGlobalParamJMenuItem ||
					addableJMenuItem == addEnzymeReactionJMenuItem ||
					addableJMenuItem == addFeatureJMenuItem ||
					addableJMenuItem == reactionsJMenuItem ||
					addableJMenuItem == propertiesJMenuItem ||
					addableJMenuItem == enableJMenuItem ||
					addableJMenuItem == disableJMenuItem ||
					addableJMenuItem == solveJMenuItem ||
					addableJMenuItem == resetJMenuItem ||
					addableJMenuItem == showParametersJMenuItem || 
					addableJMenuItem == addComplexJMenuItem ||
					addableJMenuItem == addBindingSiteJMenuItem ||
					addableJMenuItem == saveAsImageJMenu
				){ 
					popupMenu.add(addableJMenuItem);
				}else if(
					addableJMenuItem == copyJMenuItem ||
					addableJMenuItem == deleteJMenuItem ||
					addableJMenuItem == cutJMenuItem ||
					addableJMenuItem == pasteJMenuItem ||
					addableJMenuItem == pastenewJMenuItem ||
					addableJMenuItem == moveJMenuItem
				){
					editV.add(addableJMenuItem);
				}else{
					throw new RuntimeException("Unknown JMenuItem="+lastMenuList.get(i).toString());
				}
			}
			//
			if(editV.size() > 0){
				for(int i=0;i < editV.size();i+= 1){
					popupMenu.add((JMenuItem)editV.get(i),i);
				}
				popupMenu.add(new JSeparator(),editV.size());
			}
			//
			lastMenuList = currentMenuList;
			lastJPopupMenu = popupMenu;
		}
		//
		//Show menu if anything is in it
		//
		if(lastJPopupMenu != null && (lastJPopupMenu.getComponentCount() > 0)){
			//graphPane.add(popupMenu);//JPopupMenus don't need to add themselves to Components
			lastJPopupMenu.show(getGraphPane(),x,y);
		}
	}
}


/**
 * Insert the method's description here.
 * Creation date: (9/9/2002 9:46:30 AM)
 * @return java.awt.Point
 * @param worldPoint java.awt.Point
 */
protected Point screenToWorld(int x, int y) {
	if(getGraphModel() == null){
		return null;
	}
	double zoomUnscaling = 100.0/getGraphModel().getZoomPercent();
	return new Point((int)(x*zoomUnscaling),(int)(y*zoomUnscaling));
}


/**
 * Insert the method's description here.
 * Creation date: (9/9/2002 9:46:30 AM)
 * @return java.awt.Point
 * @param worldPoint java.awt.Point
 */
protected Point screenToWorld(Point screenPoint) {
	if(getGraphModel() == null){
		return null;
	}
	double zoomUnscaling = 100.0/getGraphModel().getZoomPercent();
	return new Point((int)(screenPoint.x*zoomUnscaling),(int)(screenPoint.y*zoomUnscaling));
}


/**
 * Insert the method's description here.
 * Creation date: (7/14/00 11:40:05 AM)
 * @param newButtonGroup cbit.gui.ButtonGroupCivilized
 */
public void setButtonGroup(org.vcell.util.gui.ButtonGroupCivilized newButtonGroup) {
	buttonGroup = newButtonGroup;
	setMode(SELECT_MODE);
}


/**
 * This method was created in VisualAge.
 * @param graphPane cbit.vcell.graph.GraphPane
 */
public void setGraphPane(GraphPane pane) {
	
	if (this.graphPane != null){
		this.graphPane.removeMouseListener(this);
		this.graphPane.removeMouseMotionListener(this);
		this.graphPane.removeKeyListener(this);
	}
	
	this.graphPane = pane;
	
	if(this.graphPane != null){
		this.graphPane.addMouseListener(this);
		this.graphPane.addMouseMotionListener(this);
		this.graphPane.addKeyListener(this);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (7/14/00 11:17:33 AM)
 * @param newMode int
 */
public final void setMode(int newMode) {
	String newActionCommand = getActionCommand(newMode);
	updateButtonGroup(getButtonGroup(), newActionCommand);
	updateMode(newMode);
}


/**
 * Insert the method's description here.
 * Creation date: (7/14/00 11:27:22 AM)
 * @param actionCommand java.lang.String
 */
public final void setModeString(String newActionCommand) {
	int newMode = getMode(newActionCommand);
	updateButtonGroup(getButtonGroup(), newActionCommand);
	updateMode(newMode);
}


/**
 * Insert the method's description here.
 * Creation date: (9/17/2002 3:47:00 PM)
 * @return boolean
 * @param actionString java.lang.String
 */
protected abstract boolean shapeHasMenuAction(Shape shape, String menuAction);


/**
 * Insert the method's description here.
 * Creation date: (9/17/2002 3:47:00 PM)
 * @return boolean
 * @param actionString java.lang.String
 */
protected abstract boolean shapeHasMenuActionEnabled(Shape shape, String menuAction);


/**
 * This method was created in VisualAge.
 * @param newMode int
 */
public final void updateButtonGroup(ButtonGroupCivilized buttonGroup, String actionCommand) {
	if (buttonGroup == null){
		return;
	}
	//
	// if selected button does not have this action command, select the first button we find with appropriate action command
	//
	String currSelectedString = (buttonGroup.getSelection() != null?buttonGroup.getSelection().getActionCommand():null);
	if (currSelectedString != null){
		if (currSelectedString.equals(actionCommand)){
			return;
		}
	}
	Enumeration buttons = buttonGroup.getElements();
	while (buttons.hasMoreElements()) {
		ButtonModel button = ((JToggleButton)buttons.nextElement()).getModel();
		if (button.getActionCommand().equals(actionCommand)) {
			buttonGroup.setSelection(button);
			return;
		}
	}
	//
	// if we got this far...
	//
	System.out.println("ERROR: button with actionCommand " + actionCommand + " not found");
	return;
}


/**
 * Insert the method's description here.
 * Creation date: (7/14/00 11:32:51 AM)
 * @param newMode int
 */
public abstract void updateMode(int newMode);
}