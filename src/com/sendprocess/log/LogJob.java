package com.sendprocess.log;

import java.io.*;
import com.sendprocess.util.MessageID;
import com.sendprocess.config.Config;
import com.sendprocess.config.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class LogJob
{
	private static final Logger LOGGER = LogManager.getLogger(LogJob.class.getName());
	
	/**Root Folder*/
	public static String ROOT_DIR = ".";

	/**�α� ����*/
	public static String LOG_FOLDER = "Log";

	/**���� �α� ����*/
	public static String ERROR_LIST_FOLDER = "SMTPLog";

	/**�α� ���� Ȯ����*/
	public static String LOG_EXT = ".log";

	/**������ϸ�*/
	public static String BACK_EXT  =".bak";

	/**�����αװ� ���̴� ����*/
	public static String ERROR_LOG_FOLDER = (new StringBuffer(ROOT_DIR)
			.append(File.separator).append(LOG_FOLDER).append(File.separator)
			.append(ERROR_LIST_FOLDER).append(File.separator)).toString();

	/**
	 * �Ķ��Ÿ�� ������
	 *  1. Ŭ������
	 *  2. �޼ҵ��
	 *  3. ��������
	 *  4. ��������
	 *  �� ������ ����.
	 */
	public synchronized static void errorLog(String className, String methodName,
			String errorType, String log)
	{
		String errorLogName = MessageID.getDay() + LOG_EXT;
		String errorLogFullPath = ERROR_LOG_FOLDER + errorLogName;

		StringBuffer sb = new StringBuffer();

		sb.append(MessageID.getLogTime()).append(Operation.Log_Sep).append(className)
				.append(Operation.Log_Sep).append(methodName).append(Operation.Log_Sep)
				.append(errorType).append(Operation.Log_Sep).append(log);

		PrintWriter pw = null;

		try
		{
			pw = new PrintWriter(new FileWriter(errorLogFullPath,true));
			pw.println(sb.toString());
			pw.flush();
		}
		catch(IOException e)
		{
			LOGGER.error(e);
			//e.printStackTrace();
		}
		finally
		{
			if( pw != null ) {
				pw.close();
			}
		}
	}

//	public synchronized static void errorLog(String log)
//    {
//		 try
//         {
//	  	    PrintWriter errorPrint = new PrintWriter(new FileWriter( Config.Log_Dir + File.separator+ "Error.log",true));
//	    	errorPrint.print(MessageID.getLogTime() + Operation.Log_Sep + log + Config.lineSeparator );
//			errorPrint.flush();
//	    	if(errorPrint!=null)
//            {
//                errorPrint.close();
//                errorPrint = null;
//            }
//    	}
//        catch(IOException e)
//        {
//    	}
//	}
}