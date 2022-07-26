package org.vcell.imagej.plugin;

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.UIService;
import org.vcell.imagej.helper.VCellHelper;
import org.vcell.imagej.helper.VCellHelper.IJDataList;
import org.vcell.imagej.helper.VCellHelper.IJTimeSeriesJobResults;
import org.vcell.imagej.helper.VCellHelper.IJVarInfos;
import org.vcell.imagej.helper.VCellHelper.VARTYPE_POSTPROC;
import org.vcell.imagej.helper.VCellHelper.VCellModelSearchResults;

import net.imagej.ImageJ;

@Plugin(type = ContextCommand.class, menuPath = "Plugins>VCell>Help")
public class Help extends ContextCommand{

	@Parameter
	private UIService uiService;

  	@Parameter
	private VCellHelper vcellHelper;
  	
  	@Parameter
  	private String helpText;

	public static void main(String[] args) {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
	}

	@Override
	public void run() {
		 JOptionPane e = new JOptionPane(helpText);
   		 Image image;
		try {

		    URL url = new URL("https://i.imgur.com/mhxoPJD.png");
		    image = ImageIO.read(url);
		    JLabel picLabel = new JLabel(new ImageIcon(image));
		    e.add(picLabel,0,0);
		} catch (Exception exp) {
		    exp.printStackTrace();
		} 
		JDialog dialog = e.createDialog(e, helpText + " Help");
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		//e.add(editor);
 		e.setSize(500,200);            
	    e.setVisible(true);
		 
	}
}
