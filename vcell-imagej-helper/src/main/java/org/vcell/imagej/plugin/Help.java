package org.vcell.imagej.plugin;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
  	static  JOptionPane e = new JOptionPane();

  	JFrame a = new JFrame();
	public static void main(String[] args) {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
     }
  
        

	@Override
	public void run() {
		a.setSize(new Dimension(1500,1000));
		a.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		String string1 =("The Start ImageJ VCell Service allows the image analysis program Fiji to communicate with VCell. Fiji can then query and transfer simulation data from VCell to Fiji. In VCell select Start Fiji (ImageJ) Service from the Tools menu");
	  	String string2 = ("One of two dialog boxes will appear.");
	  	String string3 = ("If a VCell plugin is not currently installed in Fiji choose install new plugin, then navigate to the FIJI installation folder and select the 'plugins' folder; the VCell plugin for FIJI will be automatically installed in the selected plugins folder.\n"
	  			+ "If a plugin was previously installed choose either Continue to use the current plugin -or- Update plugin or Change path to reinstall/update the plugin.\n"
	  			+ "If no error messages are dislayed the VCell Tools menu should now have the item 'Stop Fiji (ImageJ) Service xxxx' where xxxx is the port number VCell client is listening for commands from the plugin you have installed. You can choose Tools->'Stop Fiji (ImageJ) Service xxxx' to stop the VCell Fiji service. ");
	  	JLabel label1 = new JLabel();
	  	label1.setText("<html>"+ string1 +"</html>");
	  	c.gridx = 0;
	  	c.gridy = 0;
	  	a.add(label1,c);
		c.gridx = 0;
	  	c.gridy = 1;
	  	Image imageRow;
		try {

		    URL url = new URL("https://i.imgur.com/QEUyTH3.png");
		    imageRow = ImageIO.read(url);
		    JLabel picLabel = new JLabel(new ImageIcon(imageRow));
		    a.add(picLabel,c);
		} catch (Exception exp) {
		    exp.printStackTrace();
		} 
		c.gridx = 0;
	  	c.gridy = 2;
	  	JLabel label2 = new JLabel();
	  	label2.setText("<html>"+ string2 +"</html>");
	  	a.add(label2,c);
		c.gridx = 0;
	  	c.gridy = 3;
	  	c.fill = GridBagConstraints.NONE;
	  	Image image2;
		try {

		    URL url = new URL("https://i.imgur.com/G5bC542.png");
		    image2 = ImageIO.read(url);
		    JLabel picLabel = new JLabel(new ImageIcon(image2));
		    picLabel.setMaximumSize(new Dimension(100,100));
		    a.add(picLabel,c);
		} catch (Exception exp) {
		    exp.printStackTrace();
		} 
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
	  	c.gridy = 4;
		JLabel label3 = new JLabel();
	  	label3.setText("<html>"+ string3 +"</html>");
		a.add(label3,c);
		//a.setPreferredSize(new Dimension(500,500));
	  	a.setVisible(true);
		/*e.add(a);
		a.setSize(new Dimension(500,500));
		e.setVisible(true); */
		
		
	}
}
