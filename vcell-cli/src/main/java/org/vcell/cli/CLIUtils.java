package org.vcell.cli;

import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.SimpleSymbolTable;
import cbit.vcell.parser.SymbolTable;
import cbit.vcell.resource.OperatingSystemInfo;
import cbit.vcell.resource.PropertyLoader;
import cbit.vcell.solver.ode.ODESolverResultSet;
import cbit.vcell.util.ColumnDescription;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jlibsedml.*;
import org.vcell.stochtest.TimeSeriesMultitrialData;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CLIUtils {
    // Docker hardcode path
    // Note: Docker Working Directory and Singularity working directory works in different way.
    // Those paths are hardcoded because to run python code from a submodule path
    // Assuming Singularity image runs on HPC CRBMAPI service user
    private static final Path homeDir = Paths.get(String.valueOf(System.getProperty("user.dir")));
    private static final Path workingDirectory = Paths.get((Paths.get(String.valueOf(homeDir)).toString().equals("/")
            || Paths.get(String.valueOf(homeDir)).toString().startsWith("/home/FCAM/crbmapi/"))
            ? "/usr/local/app/vcell/installDir" : System.getProperty("user.dir"));
    // Submodule path for VCell_CLI_UTILS
    private static final Path utilPath = Paths.get(workingDirectory.toString(), "submodules", "vcell_cli_utils");
    private static final Path stdOutPath = Paths.get(workingDirectory.toString(), "stdOut.txt");
    private static final File stdOutFile = new File(String.valueOf(stdOutPath));
    private static final Path cliUtilPath = Paths.get(utilPath.toString(), "cli_util");
    private static final Path cliPath = Paths.get(cliUtilPath.toString(), "cli.py");
    private static final Path statusPath = Paths.get(cliUtilPath.toString(), "status.py");
    private static final Path requirementFilePath = Paths.get(utilPath.toString(), "requirements.txt");

    // Supported platforms
    public static boolean isWindowsPlatform = OperatingSystemInfo.getInstance().isWindows();
    public static boolean isMacPlatform = OperatingSystemInfo.getInstance().isMac();
    public static boolean isLinuxPlatform = OperatingSystemInfo.getInstance().isLinux();

    // private String tempDirPath = null;
    // private final String extractedOmexPath = null;


    // TODO: Implement this to remove System properties dynamically from Run time, Remove hardcoded from docker_run.sh
    private static void removeCliSystemProperties() throws IOException {
        if (System.getProperty("vcell.cli").equals("true")) {
            PropertyLoader.loadProperties(REQUIRED_CLIENT_PROPERTIES);
            for (int i = 0; i <= NOT_REQUIRED_LOCAL_CLI_PROPERTIES.length; i++)
            PropertyLoader.loadProperties((String[]) ArrayUtils.remove(NOT_REQUIRED_LOCAL_CLI_PROPERTIES, i));
        }
    }
    private static final String[] REQUIRED_CLIENT_PROPERTIES = {
            PropertyLoader.installationRoot
    };

    private static final String[] NOT_REQUIRED_LOCAL_CLI_PROPERTIES = {
            PropertyLoader.primarySimDataDirInternalProperty,
            PropertyLoader.secondarySimDataDirInternalProperty,
            PropertyLoader.dbPasswordValue,
            PropertyLoader.dbUserid,
            PropertyLoader.dbDriverName,
            PropertyLoader.dbConnectURL,
            PropertyLoader.vcellServerIDProperty,
            PropertyLoader.mongodbDatabase,
            PropertyLoader.mongodbHostInternal,
            PropertyLoader.mongodbPortInternal

    };


    // Simulation Status enum
    public enum Status {
        RUNNING("Simulation still running"),
        SKIPPED("Simulation skipped"),
        SUCCEEDED("Simulation succeeded"),
        FAILED("Simulation failed");

        Status(String desc) {
        }
    }


    // Breakline
    public static void drawBreakLine(String breakString, int times) {
        System.out.println(breakString + StringUtils.repeat(breakString, times));
    }

    public CLIUtils() {

    }

    public static boolean removeDirs(File f) {
        try {
            deleteRecursively(f);
        } catch (IOException ex) {
            System.err.println("Failed to delete the file: " + f);
            return false;
        }
        return true;
    }

    public static boolean makeDirs(File f) {
        if (f.exists()) {
            boolean isRemoved = removeDirs(f);
            if (!isRemoved)
                return false;
        }
        return f.mkdirs();
    }

    private static void deleteRecursively(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : Objects.requireNonNull(f.listFiles())) {
                deleteRecursively(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

    public static void saveTimeSeriesMultitrialDataAsCSV(TimeSeriesMultitrialData data, File outDir) {
        File outFile = Paths.get(outDir.toString(), data.datasetName + ".csv").toFile();
        int numberOfRows = data.times.length;
        int numberOfVariables = data.varNames.length;
        // Headers for CSV
        ArrayList<String> headersList = new ArrayList<>();
        headersList.add("times");
        Collections.addAll(headersList, data.varNames);

        // Complete rows for CSV
        ArrayList<ArrayList<Double>> allRows = new ArrayList<>();

        for (int rowCounter = 0; rowCounter < numberOfRows; rowCounter++) {
            ArrayList<Double> row = new ArrayList<>();
            row.add(data.times[rowCounter]);

            for (int varCounter = 0; varCounter < numberOfVariables; varCounter++) {
                row.add(data.data[varCounter][rowCounter][0]);
            }

            allRows.add(row);

        }

        // Writing CSV in string buffer
        StringBuilder headersBuilder = new StringBuilder();

        for (String headerName : headersList) {
            headersBuilder.append(headerName);
            headersBuilder.append(",");
        }


        String headers = headersBuilder.replace(headersBuilder.length() - 1, headersBuilder.length(), "\n").toString();

        StringBuilder allRowsBuilder = new StringBuilder(headers);

        for (ArrayList<Double> rowValues : allRows) {
            StringBuilder rowBuilder = new StringBuilder();
            for (Double val : rowValues) {
                rowBuilder.append(val);
                rowBuilder.append(",");
            }
            allRowsBuilder.append(rowBuilder.replace(rowBuilder.length() - 1, rowBuilder.length(), "\n").toString());
        }

        String csvAsString = allRowsBuilder.toString();

        try {
            PrintWriter out = new PrintWriter(outFile);
            out.print(csvAsString);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    public static void createCSVFromODEResultSet(ODESolverResultSet resultSet, File f) throws ExpressionException {
        ColumnDescription[] descriptions = resultSet.getColumnDescriptions();
        StringBuilder sb = new StringBuilder();


        int numberOfColumns = descriptions.length;
        int numberOfRows = resultSet.getRowCount();

        double[][] dataPoints = new double[numberOfColumns][];
        // Write headers
        for (ColumnDescription description : descriptions) {
            sb.append(description.getDisplayName());
            sb.append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("\n");


        // Write rows
        for (int i = 0; i < numberOfColumns; i++) {
            dataPoints[i] = resultSet.extractColumn(i);
        }

        for (int rowNum = 0; rowNum < numberOfRows; rowNum++) {
            for (int colNum = 0; colNum < numberOfColumns; colNum++) {
                sb.append(dataPoints[colNum][rowNum]);
                sb.append(",");
            }

            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("\n");
        }


        try {
            PrintWriter out = new PrintWriter(f);
            out.print(sb.toString());
            out.flush();
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find path, failed with err: " + e.getMessage());
        }

    }

    public static void removeIntermediarySimFiles(File path) {
        File[] files = path.listFiles();
        for (File f : files) {
            if (f.getName().endsWith(".csv")) {
                // Do nothing
                continue;
            } else {
                f.delete();
            }
        }
    }

    public static HashMap<String, File> generateReportsAsCSV(SedML sedml, HashMap<String, ODESolverResultSet> resultsHash, File outDirForCurrentSedml, String outDir, String sedmlLocation) {
        // finally, the real work
        HashMap<String, File> reportsHash = new HashMap<>();
        List<Output> ooo = sedml.getOutputs();
        for (Output oo : ooo) {
            if (!(oo instanceof Report)) {
                System.err.println("Ignoring unsupported output " + oo.getId());
            } else {
                System.out.println("Generating report " + oo.getId());
                try {
                    StringBuilder sb = new StringBuilder();
                    
                    // we go through each entry (dataset) in the list of datasets
                    // for each dataset, we use the data reference to obtain the data generator
                    // ve get the list of variables associated with the data reference
                    //   each variable has an id (which is the data reference above, the task and the sbml symbol urn
                    //   for each variable we recover the task, from the task we get the sbml model
                    //   we search the sbml model to find the vcell variable name associated with the urn
                    
                    List<DataSet> datasets = ((Report) oo).getListOfDataSets();
                    for (DataSet dataset : datasets) {
                        DataGenerator datagen = sedml.getDataGeneratorWithId(dataset.getDataReference());
                        ArrayList<String> varIDs = new ArrayList<>();
                        assert datagen != null;
                        ArrayList<Variable> vars = new ArrayList<>(datagen.getListOfVariables());
                        int mxlen = 0;
                        boolean supportedDataset = true;
                        // get target values
                        HashMap values = new HashMap<Variable, double[]>();
                        for (Variable var : vars) {
                            AbstractTask task = sedml.getTaskWithId(var.getReference());
                            Model model = sedml.getModelWithId(task.getModelReference());
                        	// must get variable ID from SBML model
                        	String sbmlVarId = "";
                        	if (var.getSymbol() != null) {
                        		// it is a predefined symbol
                        		sbmlVarId = var.getSymbol().name();
                        		// translate official symbols
                        		// TIME is time, etc.
                        		switch (sbmlVarId) {
								case "TIME":
									sbmlVarId = "time";
									break;
								}
                        		// TODO
                        		// check spec for other symbols
                        	} else {
                        		// it is an XPATH target in model
                        		String target = var.getTarget();
                        		// look for XPATH target element in model
                        		// use the Elements' attribute of SId
                        		// TODO
                        		// sbmlVarId = model.getsourcexml.getxpathelement.getsid
                        		// look for utils in jlibsedml
                        	}
                        		
                            if (task instanceof RepeatedTask) {
                                supportedDataset = false;
                            } else {
                                varIDs.add(var.getId());
                                assert task != null;
                                ODESolverResultSet results = resultsHash.get(task.getId());
                                int column = results.findColumn(sbmlVarId);
                                double[] data = results.extractColumn(column);
                                mxlen = Integer.max(mxlen, data.length);
                                values.put(var, data);
                            }
                            //String outDirRoot = outDirForCurrentSedml.toString().substring(0, outDirForCurrentSedml.toString().lastIndexOf(System.getProperty("file.separator")));
                            CLIUtils.updateDatasetStatusYml(sedmlLocation, oo.getId(), dataset.getId(), Status.SUCCEEDED, outDir);
                            CLIUtils.updateTaskStatusYml(sedmlLocation, task.getId(), Status.SUCCEEDED, outDir);
                        }
                        if (!supportedDataset) {
                            System.err.println("Dataset " + dataset.getId() + " references unsupported RepeatedTask and is being skipped");
                            continue;
                        }
                        //get math
                        String mathMLStr = datagen.getMathAsString();
                        Expression expr = new Expression(mathMLStr);
                        SymbolTable st = new SimpleSymbolTable(varIDs.toArray(new String[vars.size()]));
                        expr.bindExpression(st);
                        //compute and write result, padding with NaN if unequal length or errors
                        double[] row = new double[vars.size()];
                        sb.append(dataset.getLabel() + ",");
                        for (int i = 0; i < mxlen; i++) {
                            for (int j = 0; j < vars.size(); j++) {
                                double[] varVals = ((double[]) values.get(vars.get(j)));
                                if (i < varVals.length) {
                                    row[j] = varVals[i];
                                } else {
                                    row[j] = Double.NaN;
                                }
                            }
                            double computed = Double.NaN;
                            try {
                                computed = expr.evaluateVector(row);
                            } catch (Exception e) {
                                // do nothing, we leave NaN and don't warn/log since it could flood
                            }
                            sb.append(computed).append(",");
                        }
                        sb.deleteCharAt(sb.lastIndexOf(","));
                        sb.append("\n");
                    }
                    File f = new File(outDirForCurrentSedml, oo.getId() + ".csv");
                    PrintWriter out = new PrintWriter(f);
                    out.print(sb.toString());
                    out.flush();
                    reportsHash.put(oo.getId(), f);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    reportsHash.put(oo.getId(), null);
                }
            }
        }
        return reportsHash;
    }

    public static ODESolverResultSet interpolate(ODESolverResultSet odeSolverResultSet, UniformTimeCourse sedmlSim) throws ExpressionException {
        double outputStart = sedmlSim.getOutputStartTime();
        double outputEnd = sedmlSim.getOutputEndTime();

        // NOTE: In other plaecs we're adding 1 timepoint to numRows, not doing that here
        int numPoints = sedmlSim.getNumberOfPoints();


        ColumnDescription[] columnDescriptions = odeSolverResultSet.getColumnDescriptions();
        String[] columnNames = new String[columnDescriptions.length];

        for (int i = 0; i < columnDescriptions.length; i++) {
            columnNames[i] = columnDescriptions[i].getDisplayName();
        }

        // need to construct a new RowColumnResultSet instance
        ODESolverResultSet finalResultSet = new ODESolverResultSet();


        // use same column descriptions
        for (ColumnDescription cd : columnDescriptions) {
            finalResultSet.addDataColumn(cd);
        }


        double deltaTime = ((outputEnd - outputStart) / numPoints);
        double[] timepoints = new double[numPoints];

        timepoints[0] = outputStart;
        for (int i = 1; i < numPoints; i++) {
            timepoints[i] = timepoints[i - 1] + deltaTime;
        }

        double[] originalTimepoints = odeSolverResultSet.extractColumn(0);


        double[][] columnValues = new double[columnDescriptions.length][];
        columnValues[0] = timepoints;
        for (int i = 1; i < columnDescriptions.length; i++) {
            // each row uses the time index based on the params above and for each column descriptions interpolate the value from the original result set
            columnValues[i] = interpLinear(originalTimepoints, odeSolverResultSet.extractColumn(i), timepoints);
        }


        double[][] rowValues = new double[numPoints][columnDescriptions.length];

        for (int rowCount = 0; rowCount < numPoints; rowCount++) {
            for (int colCount = 0; colCount < columnDescriptions.length; colCount++) {
                rowValues[rowCount][colCount] = columnValues[colCount][rowCount];
            }
        }


        // add a numPoints number of rows one by one as double[]
        for (int rowCount = 0; rowCount < numPoints; rowCount++) {
            finalResultSet.addRow(rowValues[rowCount]);
        }

        return finalResultSet;
    }

    public static double[] interpLinear(double[] x, double[] y, double[] xi) throws IllegalArgumentException {

        if (x.length != y.length) {
            throw new IllegalArgumentException("X and Y must be the same length");
        }
        if (x.length == 1) {
            throw new IllegalArgumentException("X must contain more than one value");
        }
        double[] dx = new double[x.length - 1];
        double[] dy = new double[x.length - 1];
        double[] slope = new double[x.length - 1];
        double[] intercept = new double[x.length - 1];

        // Calculate the line equation (i.e. slope and intercept) between each point
        for (int i = 0; i < x.length - 1; i++) {
            dx[i] = x[i + 1] - x[i];
            if (dx[i] == 0) {
                throw new IllegalArgumentException("X must be montotonic. A duplicate " + "x-value was found");
            }
            if (dx[i] < 0) {
                throw new IllegalArgumentException("X must be sorted");
            }
            dy[i] = y[i + 1] - y[i];
            slope[i] = dy[i] / dx[i];
            intercept[i] = y[i] - x[i] * slope[i];
        }

        // Perform the interpolation here
        double[] yi = new double[xi.length];
        for (int i = 0; i < xi.length; i++) {
            if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
                yi[i] = Double.NaN;
            } else {
                int loc = Arrays.binarySearch(x, xi[i]);
                if (loc < -1) {
                    loc = -loc - 2;
                    yi[i] = slope[loc] * xi[i] + intercept[loc];
                } else {
                    yi[i] = y[loc];
                }
            }
        }

        return yi;
    }

    private static int execShellCommand(String[] args) {
        // NOTE: Magic number -10, simply means unassigned exit code
        int exitCode = -10;
        String joinArg = Joiner.on(" ").join(args);
        // Uncomment to debug the command execution
//        System.out.println("Executing the command: `" + joinArg + "`");
        File log = stdOutFile;
        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            // For debugging
//            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//            builder.redirectErrorStream(true);
            // STDOUT redirected to stdOut.txt file
//            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
            Process p = builder.start();
//            assert builder.redirectInput() == ProcessBuilder.Redirect.PIPE;
//            assert builder.redirectOutput().file() == log;
//            assert p.getInputStream().read() == -1;
            exitCode = p.waitFor();
            return exitCode;
        } catch (IOException | InterruptedException I) {
            I.printStackTrace();
            System.err.println("Failed executing the command: `" + joinArg + "`\n");
        }
        return exitCode;
    }

    public static void pipInstallRequirements() {
        // pip install the requirements
        String[] args;

        if (checkPythonInstallation() == 0 && checkPipInstallation() == 0) {
            System.out.println("Installing the required PIP packages..");
            if (isWindowsPlatform) {
                args = new String[]{"pip", "install", "-r", String.valueOf(requirementFilePath)};
            } else {
                args = new String[]{"pip3", "install", "-r", String.valueOf(requirementFilePath)};
            }
            execShellCommand(args);
        } else {
            System.err.println("Failed installing PIP packages.");
        }

    }

    public static int checkPythonInstallation() {
        int pyCheckIns;
        if (isWindowsPlatform) pyCheckIns = execShellCommand(new String[]{"python", "--version"});
        else pyCheckIns = execShellCommand(new String[]{"python3", "--version"});
        if (pyCheckIns != 0) System.err.println("Check Python installation...");
        return pyCheckIns;
    }

    public static int checkPipInstallation() {
        int pipCheckIns;
        if (isWindowsPlatform) pipCheckIns = execShellCommand(new String[]{"pip", "--version"});
        else pipCheckIns = execShellCommand(new String[]{"pip3", "--version"});
        if (pipCheckIns != 0) System.err.println("Check PIP installation...");
        return pipCheckIns;
    }

    public static void giveOpenPermissions(String PathStr) {
        // Give permissions to the file in the temp directory
        Path filePath = Paths.get(PathStr);
        // TODO: Make it work on Windows platform
        String[] permissionArgs = new String[]{"chmod", "777", filePath.toString()};
        CLIUtils.execShellCommand(permissionArgs);
    }

    public static void convertCSVtoHDF(String omexFilePath, String outputDir) {
        String[] cliArgs;
//        Path csvDirPath = Paths.get(csvDir);
//        Path sedmlFilePath = Paths.get(sedmlFilePathStr);
//        Path outDirPath = Paths.get(outDir);
//        CLIUtils.giveOpenPermissions(sedmlFilePathStr);

        // Convert CSV to HDF5
        // TODO: Add Metadata Directory to wrap H5(Simulation_1.sedml)
        /*
        Usage: cli.py SEDML_FILE_PATH WORKING_DIR BASE_OUT_PATH CSV_DIR <flags>
                    optional flags:        --rel_out_path | --apply_xml_model_changes |
                         --report_formats | --plot_formats | --log | --indent
        * */
        if (checkPythonInstallation() == 0) {
            if (isWindowsPlatform) {
                cliArgs = new String[]{"python", cliPath.toString(), "execSedDoc", omexFilePath, outputDir};
            } else {
                cliArgs = new String[]{"python3", cliPath.toString(), "execSedDoc", omexFilePath, outputDir};
            }
            execShellCommand(cliArgs);
            System.out.println("HDF conversion completed in '" + outputDir + "'\n");
        } else System.err.println("HDF5 conversion failed...");

    }

    // Sample STATUS YML
    /*
    sedDocuments:
      BIOMD0000000912_sim.sedml:
        outputs:
          BIOMD0000000912_sim:
            dataSets:
              data_set_E: SKIPPED
              data_set_I: PASSED
              data_set_T: SKIPPED
              data_set_time: SKIPPED
            status: SKIPPED
          plot_1:
            curves:
              plot_1_E_time: SKIPPED
              plot_1_I_time: SKIPPED
              plot_1_T_time: SKIPPED
            status: SKIPPED
        status: SUCCEEDED
        tasks:
          BIOMD0000000912_sim:
            status: SKIPPED
    status: SUCCEEDED
    * */
    public static void generateStatusYaml(String omexPath, String outDir) {
        // Note: by default every status is being skipped
        Path omexFilePath = Paths.get(omexPath);
        /*
         USAGE:

         NAME
         status.py

         SYNOPSIS
         status.py COMMAND

         COMMANDS
         COMMAND is one of the following:

         status_yml
        */
        if (checkPythonInstallation() == 0) {
            if (isWindowsPlatform)
                execShellCommand(new String[]{"python", statusPath.toString(), "genStatusYaml", String.valueOf(omexFilePath), outDir});
            else
                execShellCommand(new String[]{"python3", statusPath.toString(), "genStatusYaml", String.valueOf(omexFilePath), outDir});
        } else System.err.println("Failed generating status YAML...");
    }

    public static void updateTaskStatusYml(String sedmlName, String taskName, Status taskStatus, String outDir) {
        if (checkPythonInstallation() == 0) {
            if (isWindowsPlatform)
                execShellCommand(new String[]{"python", statusPath.toString(), "updateTaskStatus", sedmlName, taskName, taskStatus.toString(), outDir});
            else
                execShellCommand(new String[]{"python3", statusPath.toString(), "updateTaskStatus", sedmlName, taskName, taskStatus.toString(), outDir});
        } else System.err.println("Failed updating status YAML...");
    }

    public static void finalStatusUpdate(Status simStatus, String outDir) {
        if (checkPythonInstallation() == 0) {
//            System.out.println("Generating Status YAML...");
            if (isWindowsPlatform)
                execShellCommand(new String[]{"python", statusPath.toString(), "simStatus", simStatus.toString(), outDir});
            else
                execShellCommand(new String[]{"python3", statusPath.toString(), "simStatus", simStatus.toString(), outDir});
        } else System.err.println("Failed generating status YAML...");
    }

    public static void updateDatasetStatusYml(String sedmlName, String dataSet, String var, Status simStatus, String outDir) {
        if (checkPythonInstallation() == 0) {
            if (isWindowsPlatform)
                execShellCommand(new String[]{"python", statusPath.toString(), "updateDataSetStatus", sedmlName, dataSet, var, simStatus.toString(), outDir});
            else
                execShellCommand(new String[]{"python3", statusPath.toString(), "updateDataSetStatus", sedmlName, dataSet, var, simStatus.toString(), outDir});
        } else System.err.println("Failed updating DataSet to status YAML...");
    }

    public static void transposeVcmlCsv(String csvFilePath) {
        if (checkPythonInstallation() == 0) {
            if (isWindowsPlatform)
                execShellCommand(new String[]{"python", cliPath.toString(), "transposeVcmlCsv", csvFilePath});
            else execShellCommand(new String[]{"python3", cliPath.toString(), "transposeVcmlCsv", csvFilePath});
        } else System.err.println("Failed transposing VCML resultant CSV...");
    }


    private static ArrayList<File> listFilesForFolder(File dirPath) {
        File dir = new File(String.valueOf(dirPath));
        String[] extensions = new String[] { "csv" };
        ArrayList<File> csvFilesList = new ArrayList<>();
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        for (File file : files) {
            csvFilesList.add(file);
        }
        return csvFilesList;
    }

    public static void zipResFile(File dirPath) throws IOException {
        System.out.println("Generating zip Archive for reports");
        ArrayList<File> srcFiles = listFilesForFolder(dirPath);
        FileOutputStream fos = new FileOutputStream(Paths.get(dirPath.toString(), "reports.zip").toFile());
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (File srcFile : srcFiles) {
            FileInputStream fis = new FileInputStream(srcFile);

            // get relative path
            String relativePath = dirPath.toURI().relativize(srcFile.toURI()).toString();
            ZipEntry zipEntry = new ZipEntry(relativePath);
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();


    }


    @SuppressWarnings("UnstableApiUsage")
    public String getTempDir() {
        return Files.createTempDir().getAbsolutePath();
    }
}