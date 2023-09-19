package com.sendprocess.send;

import com.sendprocess.config.*;
import com.sendprocess.log.LogJob;
import com.sendprocess.util.ErrorStatusCode;
import com.sendprocess.log.*;
import com.sendprocess.lookup.*;
import com.queue.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Monitoring extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(Monitoring.class.getName());
	
	private DNSCache dc;
	private MergeQueueDirChecker mqdc;
	private SendChecker  sc;
	private ThreadManager tm;
	private FileRecordListen frl;
	private UISender us;

	public void setThread(Object dc, Object mqdc, Object sc, Object tm, Object frl,Object us)
	{
		this.dc = (DNSCache) dc;
		this.mqdc = (MergeQueueDirChecker) mqdc;
		this.sc = (SendChecker) sc;
		this.tm = (ThreadManager) tm;
		this.frl = (FileRecordListen) frl;
		this.us = (UISender) us;
	}

	public Monitoring() {
	}

	public synchronized void run()
	{
		while( true )
		{
			if( !dc.isAlive() )
			{
				dc = new DNSCache();
				dc.start();
				LOGGER.info("DNSCache가 죽엇다");
				try
				{
					Thread.sleep(5000);
					LOGGER.info("DNSCache가 죽엇다");	
				}
				catch(Exception e) {
					LOGGER.error(e);
				}
			}

			if(!(mqdc.isAlive()))
			{
				mqdc = new MergeQueueDirChecker();
				mqdc.start();
				LOGGER.info("MergeQueueDirChecker가 죽엇다");

				try
				{
					Thread.sleep(5000);
					LOGGER.info("MergeQueueDirChecker가 죽엇다");					
				}
				catch(Exception e) {
					LOGGER.error(e);
				}
			}

			if( !sc.isAlive() )
			{
				tm = new ThreadManager();
				sc = new SendChecker(tm);
				sc.start();
				LOGGER.info("SendChecker가 죽엇다");

				try
				{
					Thread.sleep(5000);
					LOGGER.info("SendChecker가 죽엇다");
				}
				catch(Exception e) {
					LOGGER.error(e);
				}
			}

			if( !frl.isAlive() )
			{
				frl = new FileRecordListen();
				frl.start();
				LOGGER.info("FileRecordListen가 죽엇다");

				try
				{
					Thread.sleep(5000);
					LOGGER.info("FileRecordListen가 죽엇다");
				}
				catch(Exception e) {
					LOGGER.error(e);
				}
			}

			if( !us.isAlive() )
			{
				us = new UISender();
				us.start();
				LOGGER.info("UISender가 죽엇다");

				try
				{
					Thread.sleep(5000);
					LOGGER.info("UISender가 죽엇다");
				}
				catch(Exception e) {
					LOGGER.error(e);
				}
			}

			try {
				sleep(1000 * 60 * Operation.Monitoring_Period);
			}
			catch(Exception e)
			{
				LOGGER.error(e);
				e.printStackTrace();
				LogJob.errorLog("ThreadManager", "send()",
								ErrorStatusCode.THREAD_ERROR_MSG, e.toString());
			}
		}
	}
}