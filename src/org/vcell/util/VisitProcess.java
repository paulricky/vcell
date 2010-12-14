package org.vcell.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class VisitProcess {

	public static Process exportProcess;
	public static ProcessInfo myProcessInfo;
	public static BufferedOutputStream myBOS;
	public static StreamReader myNormalOutput;
	public static StreamReader myErrorOutput;
	
	public static class ProcessInfo{
		public StreamReader normalOutput;
		public StreamReader errorOutput;
		public Process execProcess;
	//	public int returnCode;
		public ProcessInfo(StreamReader normalOutput, StreamReader errorOutput, Process execProcess) {
			super();
			this.normalOutput = normalOutput;
			this.errorOutput = errorOutput;
//			this.returnCode = returnCode;
			this.execProcess = execProcess;
		}
		public Process getExecProcess(){
			return execProcess;
		}
	}

	public static class StreamReader extends Thread {
		private InputStream is;
		private StringBuilder stringBuilder;
		private Exception readerException;
		
		public StreamReader(InputStream is) {
			this.is = is;
			stringBuilder = new StringBuilder();
		}

		public synchronized void run() {
			try {
				int c;
				while ((c = is.read()) != -1){
					stringBuilder.append((char)c);
					//System.out.print((char)c);
				}
			} catch (Exception e) {
				readerException = e;
			}
		}

		public /*synchronized*/ String getString() {
			//System.out.println("Got Here");
			String s = stringBuilder.toString();
			stringBuilder = new StringBuilder();
			return s;
			
		}
		public synchronized Exception getReaderException(){
			return readerException;
		}
	}
	
	

	public static ProcessInfo spawnProcess(String spawnCommand) throws Exception{
		exportProcess = Runtime.getRuntime().exec(spawnCommand);
		//Listen for output
		StreamReader normalOutput = new StreamReader(exportProcess.getInputStream());
		normalOutput.start();
		StreamReader errorOutput = new StreamReader(exportProcess.getErrorStream());
		errorOutput.start();
		return new ProcessInfo(normalOutput, errorOutput, exportProcess);

	}

	
	public static void sendVisitCommand(String visitCmd){
		try {
			myBOS.write(visitCmd.getBytes());
			myBOS.flush();
			
			//myBOS = new BufferedOutputStream(myProcessInfo.getExecProcess().getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    return;
	}
	
	
	public static String sendAndListen(String visitCmd){
		sendVisitCommand(visitCmd);
		waitASec();
		return(myProcessInfo.normalOutput.getString());
	}
	
	public static void sendCommandAndNoteResponse(String visitCmd){
		sendVisitCommand(visitCmd);
		waitASec();
		System.out.println(myProcessInfo.normalOutput.getString());
		System.out.println(myProcessInfo.errorOutput.getString());
		return;
	}
	
	public static void waitASec(){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void waitNSecs(int n){for (int i=1;i<n;i++) {waitASec();}}
	
	/*public VisitProcess() {
		String execCommand = "python";
		 try {
			myProcessInfo = VisitProcess.spawnProcess(execCommand);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myBOS = new BufferedOutputStream(myProcessInfo.getExecProcess().getOutputStream());
	}*/

	public static void main(String[] args){
		
		//String execCommand = "env DYLD_LIBRARY_PATH=/Users/edboyce/visit/current/darwin-i386/lib python -i";
		String execCommand = "/Users/edboyce/visit/2.0.2/darwin-i386/bin/python.exe -i";
		//String execCommand = "python -i";
		 try {
			myProcessInfo = VisitProcess.spawnProcess(execCommand);
		} catch (Exception e) {
			e.printStackTrace();
		}
		myBOS = new BufferedOutputStream(myProcessInfo.getExecProcess().getOutputStream());
 
		waitASec();
		sendCommandAndNoteResponse("import sys\n");
		sendCommandAndNoteResponse("import os\n");
		sendCommandAndNoteResponse("sys.path.append(\"/Users/edboyce/visit/current/darwin-i386/lib/\")\n");
		sendCommandAndNoteResponse("import visit\n");
		sendCommandAndNoteResponse("from visit import *\n");
		sendCommandAndNoteResponse("visit.AddArgument(\"-cli\")\n");
		sendCommandAndNoteResponse("os.environ[\"PATH\"]=\"/Users/edboyce/visit/bin:/usr/bin:/bin:/usr/sbin:/sbin\"\n");
		waitASec();
		sendCommandAndNoteResponse("visit.Launch()\n");
		
		waitNSecs(10);
		sendCommandAndNoteResponse("isCLI = True\n");
		waitASec();
		sendCommandAndNoteResponse("visit.OpenDatabase(\"/Users/edboyce/visit/data/multi_rect3d.silo\")\n");
		waitNSecs(4);
		sendCommandAndNoteResponse("visit.AddPlot(\"Pseudocolor\", \"u\")\n");
		waitASec();
		sendCommandAndNoteResponse("visit.DrawPlots()\n");
		System.out.println("Done.");
		
	}
	}
	

