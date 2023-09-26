package com.queue;

import com.sendprocess.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>Title: NeoSMTP version 2.5</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2000-2004 Neocast Co.,Ltd</p>
 * <p>Company: Neocast Co.,Ltd</p>
 * @author Myungjae Lee
 * @version 1.0
 */

public class RetryManager extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(RetryManager.class.getName());
	
	public RetryManager() {
	}

	public void run()
	{
		while( true )
		{
			MergeIndexQueue.swapRetryToMergeIndex();

			try {
				sleep(1000 * 60 * Config.Retry_Period);
			}
			catch(Exception e) {LOGGER.error(e);}
		}
	}
}
