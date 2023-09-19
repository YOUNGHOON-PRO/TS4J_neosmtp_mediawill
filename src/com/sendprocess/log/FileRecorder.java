package com.sendprocess.log;

import java.io.*;
import java.util.*;
import com.sendprocess.util.MessageID;
import com.sendprocess.util.FileManager;
import com.sendprocess.config.Config;
import com.sendprocess.config.Operation;
import com.sendprocess.util.ErrorStatusCode;

public class FileRecorder
{
	public synchronized static int record(Vector vector, boolean bMainLog)
	{
		if( vector.isEmpty() ) {
			return 0;
		}

		String days = MessageID.getDay();
		String filePath = "";
		String fileName = "";

		if( bMainLog ) {
			filePath = (new StringBuffer(Config.Log_Dir).append(File.separator)
						.append(days)).toString();
		}
		else {
			filePath = (new StringBuffer(Config.SMTPLoggingDir).append(File.separator)
						.append(days)).toString();
		}

		FileManager.mkdir(filePath);
		int nResult = 0;
		PrintWriter logPr = null;
		try
		{
			if( bMainLog ) {
				fileName = (new StringBuffer(filePath).append(File.separator)
							.append(MessageID.getLogFileTime()).append(".log").toString());
			}
			else {
				StringBuffer sb = new StringBuffer();
				String fileNameTemp = sb.append(filePath).append(File.separator)
							.append(MessageID.getLogFileTime()).toString();

				// ������ �����ϸ�..
				if( new File(new StringBuffer(fileNameTemp).append(".xml").toString()).exists() )
				{
					int index = 0;
					while( true )
					{
						// ���ϸ� "_" ������ 0���� �����ϴ� ���·� ���ϸ��� ����� ������ �������� �ʴ�
						// �ε�����ȣ�� ã�� �̸� �̿��ؼ� ������ �����.
						StringBuffer sbTemp = new StringBuffer();
						if( new File(sbTemp.append(fileNameTemp).append("_").append(index)
									 .append(".xml").toString()).exists() ) {
							index++;
							continue;
						}
						else
						{
							fileName = (new StringBuffer(fileNameTemp).append("_").append(index)
										.append(".xml")).toString();
							break;
						}
					}
				}
				else {
					fileName = new StringBuffer(fileNameTemp).append(".xml").toString();
				}
			}

			logPr = new PrintWriter(new FileWriter(fileName, true));

			if( !bMainLog )
			{
				StringBuffer sb = new StringBuffer();
				sb.append("<?xml version=\"1.0\" encoding=\"euc-kr\"?>\r\n")
						.append("<?xml-stylesheet type='text/xsl' href='../../SMTPLogging.xsl'?>\r\n")
						.append("<SMTPLOGINFO>");
				logPr.println(sb.toString());
				logPr.flush();
			}

			while( vector.size() > 0 )
			{
				nResult++;
				logPr.println((String) vector.remove(0));
				logPr.flush();
			}

			if( !bMainLog )
			{
				logPr.println("</SMTPLOGINFO>");
				logPr.flush();
			}

			if( logPr != null )
			{
				logPr.close();
				logPr = null;
			}

			return nResult;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LogJob.errorLog("FileRecorder","record(Vector vector)",ErrorStatusCode.FILE_NOT_FOUND_MSG,e.toString());
			return nResult;
		}
		finally
		{
			if( logPr != null )
			{
				logPr.close();
				logPr = null;
			}
		}
	}
}
