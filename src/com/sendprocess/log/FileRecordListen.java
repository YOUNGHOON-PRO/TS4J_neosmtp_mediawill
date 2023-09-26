package com.sendprocess.log;

import java.util.*;
import com.sendprocess.data.SendData;
import com.sendprocess.config.Config;
import com.sendprocess.config.Operation;
import com.sendprocess.send.NeoSMTP;
import com.sendprocess.log.LogJob;
import com.sendprocess.util.ErrorStatusCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileRecordListen extends Thread
{
	
	private static final Logger LOGGER = LogManager.getLogger(FileRecordListen.class.getName());
	
	public FileRecordListen(){}

	public void run()
	{
		while( true )
		{
			int nRecordCount = 0, nIndex;

			try {
				sleep(Operation.Log_Record_Period * 1000 * 60);
			}
			catch(java.lang.InterruptedException e) {
				LOGGER.error(e);
			}

			if( NeoSMTP.isPushLog ) //case Error
			{
				if( LogData.logVector.size() > 0 )
				{
					LogData.isLogSendNow = true;
					nRecordCount += FileRecorder.record(LogData.getData(), true);
					LogData.isLogSendNow = false;
				}

				if( Config.SMTPLOGGING )
				{
					if( SMTPMessageLog.getCount() > 0 )
					{
						SMTPMessageLog.isLogSendNow = true;
						nRecordCount += FileRecorder.record(SMTPMessageLog.getData(), false);
						SMTPMessageLog.isLogSendNow = false;
					}
				}

				NeoSMTP.isPushLog = false;
			}
			else
			{
				if( LogData.getCount() > 0 && !LogData.isLogSendNow && SendData.getActiveAgent() == 0 )
				{
					LogData.isLogSendNow = true;
					nRecordCount += FileRecorder.record(LogData.getData(), true);
					LogData.isLogSendNow = false;
				}

				if( Config.SMTPLOGGING )
				{
					if( SMTPMessageLog.getCount() > 0  && !SMTPMessageLog.isLogSendNow && SendData.getActiveAgent() == 0)
					{
						SMTPMessageLog.isLogSendNow = true;
						nRecordCount += FileRecorder.record(SMTPMessageLog.getData(), false);
						SMTPMessageLog.isLogSendNow = false;
					}
				}
			}
		}
	}
}