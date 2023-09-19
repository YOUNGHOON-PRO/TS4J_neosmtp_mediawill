package com.sendprocess.log;

import java.io.*;
import java.util.*;
import com.sendprocess.util.MessageID;
import com.sendprocess.config.Config;
import com.sendprocess.config.Operation;
import com.sendprocess.send.NeoSMTP;

public class LogData
{
    public static boolean isLogSendNow = false;
    public static Vector logVector;

    public LogData(){}

    public static void allocateVector()
    {
        logVector = new Vector();
    }

    public static synchronized int getCount()
    {
        return logVector.size();
    }

	public static synchronized Vector getData()
	{
		Vector vtCopy = (Vector) logVector.clone();
		logVector.removeAllElements();
		return vtCopy;
	}

    public static synchronized void insertToSendVector(String messageID, String subID, String REFMID, String TID, String rID, String rName, String rEmail,
                    String sID, String sName, String sEmail, int code, String requestKey)
    {
        if( logVector.size() > Operation.Log_Insert_Size && !isLogSendNow )
        {
            isLogSendNow = true;
            FileRecorder.record(logVector, true);
            isLogSendNow = false;
        }

		StringBuffer sb = new StringBuffer();
		sb.append("225").append(Operation.Log_Sep)
				.append(messageID).append(Operation.Log_Sep)
				.append(subID).append(Operation.Log_Sep)
				.append(REFMID).append(Operation.Log_Sep)
				.append(TID).append(Operation.Log_Sep)
				.append(rID).append(Operation.Log_Sep)
				.append(rName).append(Operation.Log_Sep)
				.append(rEmail).append(Operation.Log_Sep)
				.append(sID).append(Operation.Log_Sep)
				.append(sName).append(Operation.Log_Sep)
				.append(sEmail).append(Operation.Log_Sep)
				.append(code).append(Operation.Log_Sep)
				.append(MessageID.getLogWriteTime()).append(Operation.Log_Sep)
				.append(NeoSMTP.LocalIPAddress).append(Operation.Log_Sep).append("0").append(Operation.Log_Sep)
				.append(requestKey);
		
        //2 25(with Mailchutev2.5)
        logVector.add(sb.toString());
    }

    //success
    public static synchronized void insertToSendVector(String messageID, String subID, String REFMID, String TID, String rID, String rName, String rEmail,
                    String sID, String sName, String sEmail, int code, int retry, String requestKey)
    {
        if( logVector.size() > Operation.Log_Insert_Size && !isLogSendNow )
        {
            isLogSendNow = true;
            FileRecorder.record(logVector, true);
            isLogSendNow = false;
        }

		StringBuffer sb = new StringBuffer();
		sb.append("225").append(Operation.Log_Sep)
				.append(messageID).append(Operation.Log_Sep)
				.append(subID).append(Operation.Log_Sep)
				.append(REFMID).append(Operation.Log_Sep)
				.append(TID).append(Operation.Log_Sep)
				.append(rID).append(Operation.Log_Sep)
				.append(rName).append(Operation.Log_Sep)
				.append(rEmail).append(Operation.Log_Sep)
				.append(sID).append(Operation.Log_Sep)
				.append(sName).append(Operation.Log_Sep)
				.append(sEmail).append(Operation.Log_Sep)
				.append(code).append(Operation.Log_Sep)
				.append(MessageID.getLogWriteTime()).append(Operation.Log_Sep)
				.append(NeoSMTP.LocalIPAddress).append(Operation.Log_Sep).append(retry).append(Operation.Log_Sep)
				.append(requestKey);
		
        //2 25(with Mailchutev2.5)
        logVector.add(sb.toString());
    }
}
