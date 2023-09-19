package com.sendprocess.config;

import java.io.*;
import java.util.*;
import com.sendprocess.log.LogJob;
import com.sendprocess.util.ErrorStatusCode;

public class Operation
{
	public static int
		Dir_Cleanning_Period ,
		Update_Period ,
		End_Time_Check_Period ,
		DNS_TimeOut;

	public static int Queue_Check_Period;

	public static String
		Sep ,
		File_Sep ,
		Log_Sep ,
		Ui_Sep ;

	public static int
		Merge_Index_Unit ,
		Log_Record_Period,
		Log_Insert_Size,
		Log_Write_Period ;

	public static boolean
		Access_Log;

	public static int
		Socket_Time,
		Listen_Socket_Time,
		Gen_Sort_Amount,
		OneDir_File_List_Amount,
		Send_Buffer,
		Receive_Buffer,
		DNSCache_Period ,
		Check_Period;

	public static boolean
		Network_File_Delete;

	//���� �߰� 2003.8.5 .. ������ �˻� ��õ� Ƚ��
	public static int domain_retry_cnt;
	//����͸� �˻� �Ⱓ
	public static int Monitoring_Period;

	public static synchronized boolean load()
    {
		try
        {
			Properties pro = new Properties();
			//pro.load(new FileInputStream("../config/net/operation.ini"));
			pro.load(new FileInputStream("./config/net/operation.ini"));

			Dir_Cleanning_Period= Integer.parseInt( (String)pro.get("Dir_Cleanning_Period") );
			Update_Period= Integer.parseInt( (String)pro.get("Update_Period") );
			End_Time_Check_Period= Integer.parseInt( (String)pro.get("End_Time_Check_Period") );

			Sep=(String)pro.get("Sep");
			File_Sep=(String)pro.get("File_Sep");
			Log_Sep=(String)pro.get("Log_Sep");
			Ui_Sep=(String)pro.get("Ui_Sep");

			Merge_Index_Unit=Integer.parseInt( (String)pro.get("Merge_Index_Unit") );
			Log_Record_Period=Integer.parseInt( (String)pro.get("Log_Record_Period") );
			Log_Insert_Size=Integer.parseInt( (String)pro.get("Log_Insert_Size") );
			Log_Write_Period=Integer.parseInt( (String)pro.get("Log_Write_Period") );

			Queue_Check_Period = Integer.parseInt((String)pro.get("Queue_Check_Period"));

			Access_Log=new Boolean((String)pro.get("Access_Log") ).booleanValue();

			Socket_Time=Integer.parseInt((String)pro.get("Socket_Time"));
			DNS_TimeOut=Integer.parseInt((String)pro.get("DNS_TimeOut"));

			//2003.8.5�� ���� �߰�
			domain_retry_cnt = Integer.parseInt((String)pro.get("DOMAIN_RETRY_CNT"));
			//����͸� �Ⱓ
			Monitoring_Period=Integer.parseInt( (String)pro.get("MONITORING_PERIOD") );

			Listen_Socket_Time=Integer.parseInt((String)pro.get("Listen_Socket_Time"));
			Gen_Sort_Amount=Integer.parseInt((String)pro.get("Gen_Sort_Amount"));
			OneDir_File_List_Amount=Integer.parseInt((String)pro.get("OneDir_File_List_Amount"));

			Send_Buffer=Integer.parseInt((String)pro.get("Send_Buffer"));
			Receive_Buffer=Integer.parseInt((String)pro.get("Receive_Buffer"));
			DNSCache_Period=Integer.parseInt((String)pro.get("DNSCache_Period"));
			Check_Period=Integer.parseInt((String)pro.get("Check_Period"));
			Network_File_Delete = new Boolean((String)pro.get("Network_File_Delete") ).booleanValue();

			if( Log_Write_Period >= 60 )
                Log_Write_Period = 59;

			if( Log_Record_Period > Log_Write_Period )
            {
				Log_Record_Period = 1;
			}

            return true;
		}
        catch(IOException e)
        {
			e.printStackTrace();
        	LogJob.errorLog("Operation","load()", ErrorStatusCode.OPERATION_INFO_NOT_VALID_MSG, e.toString());

            return false;
		}
	}
}
