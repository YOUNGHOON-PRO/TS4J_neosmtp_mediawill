package com.sendprocess.send;

import java.io.*;
import java.util.*;
import java.net.*;
import com.sendprocess.log.LogJob;
import com.sendprocess.config.Config;
import com.sendprocess.data.SendData;
import com.sendprocess.util.ErrorStatusCode;

public class SenderUIListen extends Thread
{
	private ServerSocket serverSocket;
	private boolean listen = true;

	public SenderUIListen(){}

	public void run()
	{
		try
		{
			serverSocket = new ServerSocket( Config.Mcast_Send_Port );
			while(listen)
			{
				new SendUIData(serverSocket.accept()).start();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
        	LogJob.errorLog("SenderUIListen","run()", ErrorStatusCode.SOCKET_ERROR_MSG, e.toString());
		}
	}
}

class SendUIData extends Thread
{
	private PrintWriter out;
	private BufferedReader in;
	private Socket socket;

	public SendUIData(Socket socket)
	{
		this.socket = socket;
	}

	private String getThreadSessionList()
	{
		try
		{
			Hashtable ht = new Hashtable();

			for(int i=0; i< Config.threadListVector.size(); i++)
			{
				ht.put( Config.threadListVector.elementAt(i) , "1" );
			}

			String retString = "";
			for(Enumeration en = ht.keys() ;en.hasMoreElements();)
			{
				retString += (String)en.nextElement() + ";";
			}
			return retString;
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
        	LogJob.errorLog("SenderUIListen","getThreadSessionList()", ErrorStatusCode.THREAD_ERROR_MSG, e.toString());
			return "error";
		}
	}

	public void run()
	{
		try
		{
			out = new PrintWriter(socket.getOutputStream(),true);
			in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String youwant = in.readLine().toUpperCase();

			if( youwant.equals("RESET") )
			{
				SendData.setClear();
			}
			else if( youwant.equals("ACTIVE_SESSION") )
			{
				out.print( getThreadSessionList() + "\r\n");
				out.flush();
			}
			else if( youwant.equals("SHUTDOWN_SENDER"))
			{
				System.exit(1);
			}
			else if( youwant.equals("PUSHLOG"))
			{
				NeoSMTP.isPushLog = true;
			}
			else if( youwant.equals("CONFIGLOAD"))
			{
				Config.load();
			}

			if( out != null )
			{
				out.close();
				out = null;
			}

			if( in != null )
			{
				in.close();
				in = null;
			}

			if( socket != null )
			{
				socket.close();
				socket = null;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
        	LogJob.errorLog("SenderUIListen","run()", ErrorStatusCode.SOCKET_ERROR_MSG, e.toString());
			try
			{
				if( out != null ){ out.close(); out = null; }
				if( in != null ){ in.close(); in = null; }
				if( socket != null ){ socket.close(); socket = null; }
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}
}
