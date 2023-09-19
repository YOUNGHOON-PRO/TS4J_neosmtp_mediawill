package com.sendprocess.send;

import java.io.*;
import java.util.*;
import java.net.*;
import com.queue.MergeIndexQueue;
import com.sendprocess.log.LogJob;
import com.sendprocess.config.Config;
import com.sendprocess.config.Operation;
import com.sendprocess.send.NeoSMTP;
import com.sendprocess.data.SendData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UIListen extends Thread
{
	
	private static final Logger LOGGER = LogManager.getLogger(UIListen.class.getName());
	
	private ServerSocket serverSocket;
	private boolean listen = true;

	public UIListen(){}

	public void run()
	{
		try
		{
			serverSocket = new ServerSocket(Config.Interface_Port);
			while( listen ) {
				new SmtpSendUIData(serverSocket.accept()).start();
			}
		}
		catch(Exception e) {
                  LOGGER.error("PORT Open Failed.. please watch Interface_Port of /conf/NeoSMTP.conf.");
                  LOGGER.error(e);
			LogJob.errorLog("UIListener", "run()", "Network Error", "PORT Open Failed..");
		}
	}
}

class SmtpSendUIData extends Thread
{
	
	private static final Logger LOGGER = LogManager.getLogger(SmtpSendUIData.class.getName());
	
	private PrintWriter out;
	private BufferedReader in;
	private Socket socket;
	private String command;

	public SmtpSendUIData(Socket socket)
	{
		this.socket = socket;
	}

	public void run()
	{
		try
		{
			out = new PrintWriter(socket.getOutputStream(), true);
			in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			command = in.readLine().toUpperCase();

			//LOGGER.info(youwant);
			
			if( command.equals("STAT") )
			{
				StringBuffer sb = new StringBuffer("");
				// Queue / Success / JobOut / Active_Agent / All Thread
				sb.append(MergeIndexQueue.getSize())             // Current Queue Size
						.append("#")
						.append(MergeIndexQueue.getRetrySize())  // Current Retry Queue Size
						.append("#")
						.append(SendData.getTotalEmail())        // Total Email Count
						.append("#")
						.append(SendData.getSemail())            // Total Success Count
						.append("#")
						.append(SendData.getTotalEmail())        // Job In
						.append("#")
						.append(SendData.getJobOut())            // Job Out
						.append("#")
						.append(SendData.getActiveAgent())       // Active Agent Count
						.append("#")
						.append(getThreadGroup().activeCount()); // Total Agent Count

				out.println(sb.toString());
				out.flush();
			}
			else if( command.equals("RESET") ) {
				SendData.setClear();
			}
			else if( command.equals("PUSHLOG") || command.equals("SAVELOG") ) {
				NeoSMTP.isPushLog = true;
			}
			else if( command.startsWith("LOG") ) {
			}
//			else if( command.equals("PAUSE") ) {
//				NeoSMTP.isPause = true;
//			}
//			else if( command.equals("RESUME") ) {
//				NeoSMTP.isPause = false;
//			}
			else if( command.equals("CONFIGLOAD") || command.equals("LOADCONFIG") ) {
				Config.load();
			}
			else {
			}
		}
		catch(Exception e) {
			LOGGER.error(e);
		}
		finally
		{
			try {
				if( in != null ){ in.close(); in = null; }
			}
			catch(Exception e) {
				LOGGER.error(e);
			}

			try {
				if( out != null ){ out.close(); out = null; }
			}
			catch(Exception e) {
				LOGGER.error(e);
			}

			try {
				if( socket != null) { socket.close(); socket = null; }
			}
			catch(Exception e) {
				LOGGER.error(e);
			}
		}
	}
}
