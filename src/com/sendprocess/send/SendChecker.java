package com.sendprocess.send;

import com.sendprocess.config.*;
import com.sendprocess.log.LogJob;
import com.sendprocess.util.ErrorStatusCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SendChecker extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(SendChecker.class.getName());
	
	private ThreadManager tm;

	public SendChecker(ThreadManager  tm)
    {
		this.tm = tm;
	}

	public synchronized void run()
    {
		while(true)
        {
			tm.send();
			try
            {
                sleep(Operation.Check_Period);
            }
            catch(InterruptedException e)
			{
            	LOGGER.error(e);
				//e.printStackTrace();
        		LogJob.errorLog("SendChecker","run()", ErrorStatusCode.THREAD_ERROR_MSG, e.toString());
			}
		}
	}
}
