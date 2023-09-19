package com.sendprocess.log;

import com.sendprocess.util.MessageID;
import com.sendprocess.config.*;
import java.util.*;

/**
 *
 * <p>Title: NeoSMTP version 2.5</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2000-2004 Neocast Co.,Ltd</p>
 * <p>Company: Neocast Co.,Ltd</p>
 * @author Myungjae Lee
 * @version 1.0
 */
public class SMTPMessageLog
{
	public static Vector vtData;
	public static boolean isLogSendNow = false;

	static {
		vtData = new Vector();
	}

	public SMTPMessageLog() {
	}

	public static synchronized Vector getData() {
		Vector vtCopy = (Vector) vtData.clone();
		vtData.removeAllElements();
		return vtCopy;
	}

	public static synchronized void insertMessageInfo(String msgid, String subID, String email,
			String name, String userid, String smtpCommand, String message)
	{
		if( !Config.SMTPLOGGING ) {
			return;
		}

		if( vtData.size() > Operation.Log_Insert_Size && !isLogSendNow )
		{
			isLogSendNow = true;
			FileRecorder.record(vtData, false);
			isLogSendNow = false;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("\t<MAILINFO>\r\n")
				.append("\t\t<MSGID>").append(msgid).append("</MSGID>\r\n")
				.append("\t\t<SUBID>").append(subID).append("</SUBID>\r\n")
				.append("\t\t<EMAIL>").append(email).append("</EMAIL>\r\n")
				.append("\t\t<NAME>").append(name).append("</NAME>\r\n")
				.append("\t\t<USERID>").append(userid).append("</USERID>\r\n")
				.append("\t\t<LOGTIME>").append(MessageID.getLogTime()).append("</LOGTIME>\r\n")
				.append("\t\t<PROCEED>").append(smtpCommand.trim()).append("</PROCEED>\r\n")
				.append("\t\t<SMTPMSG>").append(message.trim()).append("\r\n\t\t</SMTPMSG>\r\n")
				.append("\t</MAILINFO>");
		vtData.addElement(sb.toString());
	}

	public static synchronized int getCount() {
		return vtData.size();
	}
}
