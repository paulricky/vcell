package org.vcell.imagej.plugin;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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
  	static JFrame e = new JFrame();
  	static JEditorPane pane = new JEditorPane();
  	String helpText = ("example text");
  	JLabel a = new JLabel(helpText);
	public static void main(String[] args) {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
        pane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        pane.setText("<a href=\"https://vcell.org/\">Click for help</a>");
	    pane.setEditable(false);
	    pane.setVisible(true);
	    pane.addHyperlinkListener(new HyperlinkListener() {
	    	public void hyperlinkUpdate(HyperlinkEvent e) {
	    		 if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
	    			 try {
	    				 URI url = null;
	    					try {
	    						url = new URI("https://vcell.org");
	    					} catch (URISyntaxException e1) {
	    						// TODO Auto-generated catch block
	    						e1.printStackTrace();
	    					}
						Desktop.getDesktop().browse(url);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		         }
	    	}
	    });
	}

	@Override
	public void run() {
		e.setVisible(true);
		e.setSize(new Dimension(100,100));
		e.add(pane);
		
	}
}
