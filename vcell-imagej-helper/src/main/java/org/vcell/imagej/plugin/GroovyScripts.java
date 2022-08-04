package org.vcell.imagej.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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

@Plugin(type = ContextCommand.class, menuPath = "Plugins>VCell>Groovy Scripts")
public class GroovyScripts extends ContextCommand{

	@Parameter
	private UIService uiService;

  	@Parameter
	private VCellHelper vcellHelper;
  	static JFrame frame = new JFrame();
  
	String chartText = "#@VCellHelper vh\n"
			+ "//(See https://github.com/virtualcell/vcell/tree/master/vcell-imagej-helper/src/main/java/org/vcell/imagej/helper/VCellHelper.java)\n"
			+ "\n"
			+ "import org.jfree.chart.ChartFactory\n"
			+ "import org.jfree.chart.JFreeChart\n"
			+ "import org.jfree.chart.ChartPanel\n"
			+ "import org.jfree.data.category.CategoryDataset\n"
			+ "import org.jfree.data.xy.XYDataset\n"
			+ "import org.jfree.data.xy.DefaultXYDataset\n"
			+ "import javax.swing.JFrame\n"
			+ "import org.vcell.imagej.helper.VCellHelper.IJTimeSeriesJobResults\n"
			+ "import org.vcell.imagej.helper.VCellHelper.VCellModelSearch\n"
			+ "import org.vcell.imagej.helper.VCellHelper.ModelType\n"
			+ "import org.vcell.imagej.helper.VCellHelper.VCellModelSearchResults\n"
			+ "\n"
			+ "\n"
			+ "//Demo chart in ImageJ of line-scan values from VCell simulation results\n"
			+ "\n"
			+ "// BioModel \"Tutorial_FRAPbinding\" owned by user \"tutorial\" in application \"Spatial\" for simulation \"FRAP binding\"\n"
			+ "// values for variable=\"r\" at timePoint=22.0\n"
			+ "// line-scan along x-axis at y=0.0\n"
			+ "// start and end indexes found by moving mouse over x,y data points (-9.24,0.0) and (9.24,0,0) and\n"
			+ "// see index in \"info' box at the bottom of the sim results viewer in VCell\n"
			+ "\n"
			+ "//Create list of datapoint indexes to extract from data\n"
			+ "startIndex = 1279\n"
			+ "endIndex = 1321\n"
			+ "int[] dataIndexes = new int[endIndex-startIndex]\n"
			+ "for(int i=0;i<dataIndexes.length;i++){\n"
			+ "	dataIndexes[i] = startIndex+i\n"
			+ "}\n"
			+ "//Define list of variables to extract (see variable list in VCell simulation results viewer)\n"
			+ "String[] vars = [\"r\"]\n"
			+ "\n"
			+ "//Define search parameters to find simulation results dataset in VCell:\n"
			+ "// enum ModelType {bm,mm,quick}; defined in @VCellHelper, bm(saved BioModels), mm(saved MathModels), quick(currently displayed quickrun sim results  in open model in VCellClient)\n"
			+ "// VCellModelSearch(ModelType modelType, String vcellUserID, String modelName(can be null),\n"
			+ "//   String applicationName(can be null),String simulationName(can be null, always null for mathmodel), Integer geometryDimension(can be null), String mathType(can be null))\n"
			+ "// null values tells search to ignore that search parameter\n"
			+ "vcms = new VCellModelSearch(ModelType.bm,\n"
			+ "		\"tutorial\"/*only models owned by this user, if null search all models the VCell logged in user has access to*/,\n"
			+ "		\"Tutorial_FRAPbinding\"/*model names matching*/,\n"
			+ "		\"Spatial\"/*BioModel application names matching, for MathModels this is null*/,\n"
			+ "		\"FRAP binding\"/*simulation names matching*/,\n"
			+ "		null/*GeomDim 0,1,2,3*/,null/*String mathType \"Deterministic\" or \"Stochastic\"*/)\n"
			+ "\n"
			+ "//Do a search: (for this demo must have already opened public BioModel \"Tutorial_FRAPbinding\" listed under \"Tutorials within VCell client\"\n"
			+ "// vh.getSearchedModelSimCacheKey(boolean openOnly,VCellModelSearch vcms,VCellModelVersionTimeRange vcmvtr)\n"
			+ "/*ArrayList<VCellModelSearchResults>*/vcmsrArrayList = vh.getSearchedModelSimCacheKey(false/*only open models*/,vcms,null/*only saved models withing this date range*/)\n"
			+ "\n"
			+ "//For this search there should only be 1 result, get the search cache-key from the returned list, used for subsequent data operations\n"
			+ "/*VCellModelSearchResults extends VCellModelSearch*/vcmsr = vcmsrArrayList.get(0)\n"
			+ "/*String*/theCacheKey = vcmsr.getCacheKey();\n"
			+ "println(theCacheKey)\n"
			+ "\n"
			+ "//Get the data values using the cache-key\n"
			+ "//IJTimeSeriesJobResults getTimeSeries(String[] variableNames, int[] indices, double startTime, int step, double endTime,boolean calcSpaceStats, boolean calcTimeStats, int jobid, int cachekey) throws Exception{\n"
			+ "ijTimeSeriesJobResults = vh.getTimeSeries(vars, dataIndexes, 22 as double, 1, 22 as double, false, false, 0/*jobid, if parameter scan can be non-zero*/, theCacheKey as int)\n"
			+ "//ijTimeSeriesJobResults.data[numVars][numDataPoints+1][numTimes] structure of ijTimeSeriesJobResults.data\n"
			+ "//ijTimeSeriesJobResults.data[varIndex][0][0...numTimes-1] contains the timePoints\n"
			+ "//ijTimeSeriesJobResults.data[varIndex][1...numDataPoints][0...numTimes-1] contains the data values at each index for each timePoint\n"
			+ "//Demo TimePoints ijTimeSeriesJobResults.data[0][0] == array of timePoints for var=\"r\", array has 1 value for timePoint=22.0\n"
			+ "//Demo DataPoints ijTimeSeriesJobResults.data[0][1..numDataPoints][0] iterate over dataPoints at each timePoint for each var to get data for var,times\n"
			+ "double[] chartTheseDataPoints = new double[dataIndexes.length]\n"
			+ "for(int i=0;i<dataIndexes.length;i++){\n"
			+ "	chartTheseDataPoints[i] =\n"
			+ "		ijTimeSeriesJobResults.data[0/*index of \"r\"*/][i+1/*data, skip 0 because it holds copy of times*/][0/*0 always because we had only 1 timePoint=22.0*/]\n"
			+ "}\n"
			+ "//Create JFreechart x,y axis arrays for plotting x=data indexes, y=dataPoint values\n"
			+ "double[][] data = [dataIndexes,chartTheseDataPoints];\n"
			+ "\n"
			+ "\n"
			+ "//Plot the Data\n"
			+ "\n"
			+ "String title = \"test\"\n"
			+ "String xAxisLabel = \"distance\"\n"
			+ "String yAxisLabel = \"val\"\n"
			+ "\n"
			+ "\n"
			+ "xyDataset = new DefaultXYDataset()\n"
			+ "xyDataset.addSeries((Comparable) \"data1\", data)\n"
			+ "\n"
			+ "chart = ChartFactory.createXYLineChart( title,  xAxisLabel,  yAxisLabel, xyDataset)\n"
			+ "chartPanel = new ChartPanel(chart)\n"
			+ "\n"
			+ "frame = new JFrame(\"Chart\");\n"
			+ "        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);\n"
			+ "frame.getContentPane().add(chartPanel)\n"
			+ "        //Display the window.\n"
			+ "frame.pack();\n"
			+ "frame.setVisible(true);\n"
			+ "\n"
			+ "";
	String combineFigText = "import ij.WindowManager\n"
			+ "\n"
			+ "simImageTitle = \"Sim fluor_0\"\n"
			+ "expImageTitle = \"abp\"\n"
			+ "simDupTitle = simImageTitle+\"-1\"\n"
			+ "expDupTitle = expImageTitle+\"-1\"\n"
			+ "\n"
			+ "if(WindowManager.getImage(simDupTitle) != null){\n"
			+ "	WindowManager.getImage(simDupTitle).close()\n"
			+ "}\n"
			+ "WindowManager.getImage(simImageTitle).duplicate().show()\n"
			+ "ij.IJ.getImage().setTitle(simDupTitle)\n"
			+ "\n"
			+ "if(WindowManager.getImage(expDupTitle) != null){\n"
			+ "	WindowManager.getImage(expDupTitle).close()\n"
			+ "}\n"
			+ "WindowManager.getImage(expImageTitle).duplicate().show()\n"
			+ "ij.IJ.getImage().setTitle(expDupTitle)\n"
			+ "ij.IJ.run(WindowManager.getImage(expDupTitle), \"32-bit\", \"\");\n"
			+ "\n"
			+ "\n"
			+ "ij.IJ.selectWindow(simDupTitle)\n"
			+ "imp = ij.IJ.getImage()\n"
			+ "ij.IJ.resetMinAndMax(imp)\n"
			+ "imp.setSlice(1)\n"
			+ "ij.IJ.setBackgroundColor(0, 0, 0);\n"
			+ "ij.IJ.run(imp, \"Select All\", \"\");\n"
			+ "ij.IJ.run(imp, \"Copy\", \"\");\n"
			+ "ij.IJ.run(imp, \"Add Slice\", \"\");\n"
			+ "ij.IJ.run(imp, \"Paste\", \"\");\n"
			+ "imp.setSlice(1);\n"
			+ "ij.IJ.run(imp, \"Clear\", \"slice\");\n"
			+ "\n"
			+ "ij.IJ.selectWindow(expDupTitle)\n"
			+ "ij.IJ.run(imp, \"32-bit\", \"\");\n"
			+ "expimp = ij.IJ.getImage()\n"
			+ "ij.IJ.resetMinAndMax(expimp)\n"
			+ "\n"
			+ "/*\n"
			+ "imp.setRoi(51,0,45,88);\n"
			+ "imp2 = imp.crop(\"stack\");\n"
			+ "imp2.setTitle(\"rhs\")\n"
			+ "imp2.show()\n"
			+ "ij.IJ.run(imp2, \"RGB Color\", \"\");\n"
			+ "\n"
			+ "ij.IJ.selectWindow(simDupTitle)\n"
			+ "ij.IJ.run(imp, \"Make Inverse\", \"\");\n"
			+ "imp3 = imp.crop(\"stack\");\n"
			+ "imp3.setTitle(\"lhs\")\n"
			+ "imp3.show()\n"
			+ "ij.IJ.run(imp3, \"ICA\", \"\");\n"
			+ "ij.IJ.run(imp3, \"RGB Color\", \"\");\n"
			+ "ij.IJ.run(\"Combine...\", \"stack1=lhs stack2=rhs\");\n"
			+ "ij.IJ.getImage().setTitle(\"fluorProc\")\n"
			+ "ij.IJ.run(expimp, \"RGB Color\", \"\");\n"
			+ "*/\n"
			+ "\n"
			+ "//imp3.setSlice(26);\n"
			+ "//ij.IJ.selectWindow(\"lhs\")\n"
			+ "//ij.IJ.setTool(\"wand\");\n"
			+ "//ij.IJ.doWand(imp3, 10, 13, 0.0, \"Legacy\");\n"
			+ "//ij.IJ.setBackgroundColor(0, 0, 0);\n"
			+ "//ij.IJ.run(imp3, \"Clear\", \"stack\");\n"
			+ "//if(true){return}\n"
			+ "//IJ.run(imp, \"Flip Horizontally\", \"stack\");\n"
			+ "\n"
			+ "//ij.IJ.run(expimp, \"RGB Color\", \"\");\n"
			+ "ij.IJ.selectWindow(simDupTitle)\n"
			+ "//ij.IJ.run(imp, \"RGB Color\", \"\");\n"
			+ "ij.IJ.run(imp, \"Translate...\", \"x=0 y=-5 interpolation=None stack\");\n"
			+ "ij.IJ.run(\"Combine...\", \"stack1='\"+simDupTitle+\"' stack2='\"+expDupTitle+\"'\");\n"
			+ "\n"
			+ "/*\n"
			+ "\n"
			+ "import ij.measure.ResultsTable\n"
			+ "import ij.plugin.frame.RoiManager\n"
			+ "import ij.gui.Roi\n"
			+ "\n"
			+ "	RoiManager roiManager = RoiManager.getRoiManager()\n"
			+ "	roiManager.reset()\n"
			+ "//roiManager.runCommand(\"set line width\", \"0\")\n"
			+ "lhsRoi = new Roi(0,0,96,88)\n"
			+ "temp = roiManager.addRoi(lhsRoi)\n"
			+ "rhsRoi = new Roi(96,0,96,88)\n"
			+ "temp = roiManager.addRoi(rhsRoi)\n"
			+ "\n"
			+ "//ij.IJ.selectWindow(\"Combined Stacks\")\n"
			+ "//roiManager.select(0);\n"
			+ "//ResultsTable rtlhs = roiManager.multiMeasure(ij.IJ.getImage());\n"
			+ "//rtlhs.show(\"Results\");\n"
			+ "//ij.IJ.run(\"Summarize\", \"\");\n"
			+ "//if(true){return}\n"
			+ "//ij.IJ.selectWindow(\"Combined Stacks\")\n"
			+ "//roiManager.select(1);\n"
			+ "//ResultsTable rtrhs = roiManager.multiMeasure(ij.IJ.getImage());\n"
			+ "//rtrhs.show(\"Results\");\n"
			+ "//ij.IJ.run(\"Summarize\", \"\");\n"
			+ "//if(true){return}\n"
			+ "max = findMax(roiManager,0)\n"
			+ "ij.IJ.run(ij.IJ.getImage(), \"Divide...\", \"value=\"+max+\" stack\");\n"
			+ "max = findMax(roiManager,1)\n"
			+ "ij.IJ.run(ij.IJ.getImage(), \"Divide...\", \"value=\"+max+\" stack\");\n"
			+ "\n"
			+ "ij.IJ.resetMinAndMax(ij.IJ.getImage());\n"
			+ "\n"
			+ "//116,56,2,2\n"
			+ "roiManager.reset()\n"
			+ "roix = 147-96\n"
			+ "roiy = 25\n"
			+ "roixs = 16;\n"
			+ "roiys = 10\n"
			+ "//roix = 147-96\n"
			+ "//roiy = 25\n"
			+ "//roixs = 16;\n"
			+ "//roiys = 10\n"
			+ "roi1 = new Roi(roix,roiy,roixs,roiys)\n"
			+ "temp = roiManager.addRoi(roi1)\n"
			+ "roi2 = new Roi(roix+96,roiy,roixs,roiys)\n"
			+ "temp = roiManager.addRoi(roi2)\n"
			+ "\n"
			+ "ij.IJ.selectWindow(\"Combined Stacks\")\n"
			+ "roiManager.select(0);\n"
			+ "ij.IJ.run(ij.IJ.getImage(), \"Plot Z-axis Profile\", \"\");\n"
			+ "\n"
			+ "ij.IJ.selectWindow(\"Combined Stacks\")\n"
			+ "roiManager.select(1);\n"
			+ "ij.IJ.run(ij.IJ.getImage(), \"Plot Z-axis Profile\", \"\");\n"
			+ "\n"
			+ "def double findMax(RoiManager roiManager,int roiIndex){\n"
			+ "ij.IJ.run(\"Clear Results\", \"\");\n"
			+ "ij.IJ.selectWindow(\"Combined Stacks\")\n"
			+ "roiManager.select(roiIndex);\n"
			+ "ij.IJ.run(ij.IJ.getImage(), \"Measure Stack...\", \"\");\n"
			+ "rt = ResultsTable.getResultsTable()\n"
			+ "rt.show(\"Results\");\n"
			+ "ij.IJ.run(\"Summarize\", \"\");\n"
			+ "rowLabels = rt.getRowLabels()\n"
			+ "for(int i=0;i<rowLabels.length;i++){\n"
			+ "	if(rowLabels[i] != null && rowLabels[i] instanceof String && rowLabels[i].equals(\"Max\")){\n"
			+ "		println(rt.getValue(\"Max\",i))\n"
			+ "		return rt.getValue(\"Max\",i)\n"
			+ "	}\n"
			+ "}\n"
			+ "}\n"
			+ "\n"
			+ "\n"
			+ "*/";
	public static void main(String[] args) {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
     }


	
	public static void scriptButton(String name, JFrame frame, String text, GridBagConstraints constraints) {
		final JButton button = new JButton(name);
		frame.add(button, constraints);		
		
		ActionListener buttonAction = new ActionListener() {
	         public void actionPerformed(ActionEvent event) {
	        	 JTextArea scriptText = new JTextArea(50,100);
	        	 scriptText.setSize(400,400); 
 	        	 scriptText.setText(text);
 	             scriptText.setWrapStyleWord(true);
 	             scriptText.setLineWrap(true);
                 scriptText.setCaretPosition(0);
 	             scriptText.setEditable(false);
 	            JScrollPane scrollPanel = new JScrollPane(scriptText);
 	            scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	        	 JOptionPane e = new JOptionPane(scrollPanel);
				JDialog dialog = e.createDialog(frame, text);
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
	     		e.setSize(500,200);            
	    	    e.setVisible(true);
	    		 
	         }
	      };
	      button.addActionListener(buttonAction);	
		
			
	 }  
	
	
	@Override
	public void run() {
		
		frame.setSize(new Dimension(1000,800));
		frame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		Image image;
		try {
			double scaleFactor = 0.5;
		    URL url = new URL("https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/Groovy-logo.svg/1200px-Groovy-logo.svg.png");
		    image = (ImageIO.read(url)).getScaledInstance((int)((ImageIO.read(url).getWidth())*scaleFactor), (int)((ImageIO.read(url).getHeight())*scaleFactor), Image.SCALE_DEFAULT);
		    JLabel picLabel = new JLabel(new ImageIcon(image));
		    frame.add(picLabel,c);
		} catch (Exception exp) {
		    exp.printStackTrace();
		} 
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 10;
		c.gridx = 0;
		c.gridy = 1;
		JEditorPane pane1 = new JEditorPane();
		 pane1.setBackground(frame.getBackground());
	     pane1.setContentType("text/html");
	     pane1.setText("<html>"+ pane1.getText() +"</html>");
	     pane1.setText("VCell infrastructure provides a way to run macros in order to communicate with VCell. Below are available macros written in <a href=\"https://groovy-lang.org/​\">Groovy</a> scripting language. To run them,  Fiji -> Plugins -> Macros -> Startup Macros ... Choose Grovy language and copy-paste the Macros into the text area. You may need to change some lines (see comments).\n"
	     		+ "<li> \n Macros to retrieve 2D sim results image into Fiji \n");
	     JEditorPane pane2 = new JEditorPane();
		 pane2.setBackground(frame.getBackground());
	     pane2.setContentType("text/html");
	     pane2.setText("<html>"+ pane2.getText() +"</html>");
	     pane2.setText("Macros to ...\n"
	     		+ "\n");
	     JEditorPane pane3 = new JEditorPane();
		 pane3.setBackground(frame.getBackground());
	     pane3.setContentType("text/html");
	     pane3.setText("<html>"+ pane3.getText() +"</html>");
	     pane3.setText("</ul>\n"
	     		+ "\n"
	     		+ "Please email <a mailto:blinov@uchc.edu>Michael Blinov</a> for questions.");
	     pane1.setEditable(false);
	     frame.add(pane1,c);
	     c.gridy = 2;
		scriptButton("Chart", frame, chartText, c);
		c.gridy = 3;
		frame.add(pane2,c);
		c.gridy = 4;
		scriptButton("CombineFig", frame, combineFigText, c);
		c.gridy = 5;
		frame.add(pane3,c);
		frame.setVisible(true);
		frame.setResizable(false);
	}
}
