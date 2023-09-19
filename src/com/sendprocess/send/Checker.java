package com.sendprocess.send;

import java.io.*;
import java.net.*;
import com.queue.MergeIndexQueue;
import com.sendprocess.log.LogData;
import com.sendprocess.config.Config;
import com.sendprocess.log.LogJob;
import com.sendprocess.util.ErrorStatusCode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Checker extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(Checker.class.getName());
	
	private final int Merge_Memory_Limit = 1;

	public Checker() {
	}

	public synchronized void run()
	{
		Socket sock = null;
		BufferedReader in = null;
		PrintStream out = null;
		InputStream iStream = null;
		OutputStream oStream = null;
		String info;

		while( true )
		{
			try
			{
				sock = new Socket(Config.Receive_Server_IP, Config.Merge_Transfer_Port);
				iStream = sock.getInputStream();
				oStream = sock.getOutputStream();

				out = new PrintStream(oStream);
				in = new BufferedReader(new InputStreamReader(iStream));

				while( true )
				{
					if( MergeIndexQueue.getSize() < Merge_Memory_Limit )
					{
						try
						{
							out.println("GETINDEX");
							out.flush();

							info = in.readLine().trim();

							if( !info.equals("") ) {
								MergeIndexQueue.addMergeMemoryIndexQueueVector(info);
							}
						}
						catch(Exception e)
						{
							LOGGER.error(e);
							e.printStackTrace();
							LogJob.errorLog("Checker","run()",ErrorStatusCode.SOCKET_ERROR_MSG,e.toString());
							LOGGER.error("Cannot read queue from NeoSMTPv2.5..retry!");
						}
						finally
						{
						}
					}

					yield();

					try {
						sleep(1000);
					}
					catch(InterruptedException e) {
						LOGGER.error(e);
					}
				}
			}
			catch(Exception e)
			{
				LOGGER.error(e);
				LOGGER.error("Cannot Access Receive Process of the NeoSMTPv2.5..retry!");
			}
			finally
			{
				if( in != null )
				{
					try
					{
						in.close();
					}
					catch(Exception e) {LOGGER.error(e);}
					finally { in = null; }
				}

				if( out != null )
				{
					try
					{
						out.close();
					}
					catch(Exception e) {LOGGER.error(e);}
					finally { out = null; }
				}

				if( sock != null )
				{
					try
					{
						sock.close();
					}
					catch(Exception e) {LOGGER.error(e);}
					finally { sock = null; }
				}
			}

			yield();

			try
			{
				sleep(1000);
			}
			catch(InterruptedException e)
			{
				LOGGER.error(e);
				e.printStackTrace();
				LogJob.errorLog("Checker", "run()",
								ErrorStatusCode.THREAD_ERROR_MSG, e.toString());
			}
		}
	}
}
