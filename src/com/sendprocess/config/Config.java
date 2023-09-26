package com.sendprocess.config;

import java.io.*;
import java.util.*;
import com.sendprocess.log.LogJob;
import com.sendprocess.util.ErrorStatusCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config
{
	
	private static final Logger LOGGER = LogManager.getLogger(Config.class.getName());
	
	public static String Root_Dir;
	public static String DNS_Server_IP;
	public static String Receive_Server_IP;
	public static int SMTP_Port;
	public static int Interface_Port;
	public static int Mcast_Receive_Port;
	public static int Gen_Transfer_Port;
	public static int Merge_Transfer_Port;
	public static int Active_Agent;
	public static int Retry_Limit;
	public static boolean Is_Real;
	public static boolean Is_Standard;
	public static boolean SMTPLOGGING;
	public static String Log_Send_SMTP_IP;
	public static String SenderEmail;
	public static String lineSeparator;
	public static String Log_Dir;
	public static String Gen_Queue_Dir;
	public static String Merge_Queue_Dir;
	public static String SMTPLoggingDir;
	public static String SEND_DOMAIN;
	//������ �̸����� �����ִ� �ֱ�
	public static int GARBAGE_START_TIME;
	//�������� ���� �޽����� �����ϰ� �ִ� ��ü
	public static ArrayList msgNotDeleteMap;
	public static int Retry_Period;

	public static int Mcast_Send_Port = 11000;

	public static Vector threadListVector;

	public static synchronized boolean load()
	{
		msgNotDeleteMap = new ArrayList();
		threadListVector = new Vector();

		try
		{
			Properties pro = new Properties();
			pro.load(new FileInputStream("../config/NeoSMTP.conf"));
			//pro.load(new FileInputStream("./config/NeoSMTP.conf"));

			Root_Dir=(String)pro.get("Root_Dir");
			Receive_Server_IP=(String)pro.get("Receive_Server_IP");
			DNS_Server_IP=(String)pro.get("DNS_Server_IP");
			SEND_DOMAIN=(String)pro.get("SEND_DOMAIN");
			SMTP_Port= Integer.parseInt( (String)pro.get("SMTP_Port") );
			Interface_Port= Integer.parseInt( (String) pro.getProperty("Interface_Port", "10001") );
			Mcast_Receive_Port=Integer.parseInt( (String)pro.get("Mcast_Receive_Port") );
			Gen_Transfer_Port=Integer.parseInt((String)pro.get("Gen_Transfer_Port"));
			Merge_Transfer_Port=Integer.parseInt((String)pro.get("Merge_Transfer_Port"));

			Active_Agent=Integer.parseInt((String)pro.get("Active_Agent"));
			Retry_Limit=Integer.parseInt((String)pro.get("Retry_Limit"));
			Is_Real = new Boolean((String) pro.get("Is_Real")).booleanValue();
			Is_Standard = new Boolean((String) pro.get("Is_Standard")).booleanValue();
			Retry_Period = Integer.parseInt(pro.getProperty("RETRY_PERIOD", "5"));
			SMTPLOGGING = new Boolean((String) pro.getProperty("SMTPLOGGING", "true")).booleanValue();

			Log_Send_SMTP_IP = (String) pro.get("Log_Send_SMTP_IP");
			SenderEmail = (String) pro.get("SenderEmail");

			lineSeparator  = System.getProperty("line.separator");
			Log_Dir = Config.Root_Dir + File.separator + "Log";
			Gen_Queue_Dir = Config.Root_Dir + File.separator + "Gen_Queue";
			Merge_Queue_Dir = Config.Root_Dir + File.separator + "Merge_Queue";
			SMTPLoggingDir = Config.Root_Dir + File.separator + "SMTP_LOGGING";

			GARBAGE_START_TIME = Integer.parseInt((String)pro.get("GARBAGE_START_TIME"));
			return true;
		}
		catch(IOException e)
		{
			LOGGER.error(e);
			//e.printStackTrace();
			LogJob.errorLog("Config","load()",ErrorStatusCode.CFG_INFO_NOT_VALID_MSG,e.toString());
			return false;
		}
	}
}