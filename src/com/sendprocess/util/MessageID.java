package com.sendprocess.util;

import java.text.*;
import java.util.*;
import com.sendprocess.config.Config;
import com.sendprocess.config.Operation;

public class MessageID
{
	private static int serialNo = 1;

	public static String getMsgID()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmm", Locale.US );
		String msgID = fmt.format(new Date() ) + "_"+ Integer.toString(serialNo);  serialNo += 1;
		return msgID;
	}

	//Log File Format
	public static String getDay()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd", Locale.US );
		return  fmt.format(new Date() );
	}

	public static String getLogFileTime()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.US );
		String formatStr = fmt.format(new Date() );
		int pos = formatStr.lastIndexOf("_");
		String frontStr = formatStr.substring( 0 , pos );
		int minute = Integer.parseInt( formatStr.substring( pos+1 , formatStr.length() ) );
		if( minute % Operation.Log_Write_Period > 0 ) {
			minute = minute - (minute % Operation.Log_Write_Period);
		}

		if( minute < 10 ) {
			return  frontStr + "_0" + minute;
		}
		else {
			return  frontStr + "_" + minute;
		}
	}
	//

	//Display Format
	public static String getDisplayTime()
	{
		DateFormat fmt = DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.SHORT, Locale.getDefault());
		return fmt.format(new Date());
	}
	//

	public static String getLogTime()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
		return  fmt.format(new Date());
	}

	public static String getLogWriteTime()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
		return  fmt.format(new Date());
	}
}