package com.sendprocess.send;

import java.net.*;
import java.util.*;
import com.queue.MergeIndexQueue;
import com.sendprocess.data.SendData;
import com.sendprocess.config.Config;
import com.sendprocess.config.Operation;
import com.sendprocess.log.LogJob;
import com.sendprocess.util.ErrorStatusCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UISender extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(UISender.class.getName());
	
	public UISender() {
		SendData.BASE_THREAD = getThreadGroup().activeCount();
	}

	public void run()
	{
		while( true )
		{
			yield();

			try {
				sleep(Operation.Update_Period * 1000);
			}
			catch(InterruptedException e) {
				LOGGER.error(e);
			}
			

			SendData.setActiveAgent(getThreadGroup().activeCount() - SendData.BASE_THREAD - 1);
		}
	}
}