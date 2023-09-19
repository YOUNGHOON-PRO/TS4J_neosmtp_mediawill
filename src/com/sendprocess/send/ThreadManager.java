package com.sendprocess.send;

import com.sendprocess.config.*;
import com.sendprocess.data.SendData;
import com.sendprocess.log.LogData;
import com.queue.MergeIndexQueue;


public class ThreadManager
{
	private SendChecker sc;
	public ThreadManager(){}

	public synchronized boolean isFull()
	{
		if( SendData.getActiveAgent() < Config.Active_Agent ) {
			return false;
		}
		else {
			return true;
		}
	}

	public void send()
	{
		if( !isFull() )
		{
			if( MergeIndexQueue.getSize() > 0 )
			{
				String info = MergeIndexQueue.getMergeMemoryIndexDequeue();
				new Send( info ).start();
			}else if(LogData.getCount() > 0 && SendData.getActiveAgent() != 0 && !LogData.isLogSendNow ){
				NeoSMTP.isPushLog = true;
			}
		}
	}
}
