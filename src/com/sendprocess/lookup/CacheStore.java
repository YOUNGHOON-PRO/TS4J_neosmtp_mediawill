package com.sendprocess.lookup;

import java.io.*;
import java.net.*;
import java.util.*;

import com.sendprocess.util.ErrorStatusCode;
import com.sendprocess.log.LogJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CacheStore
{
	private static final Logger LOGGER = LogManager.getLogger(CacheStore.class.getName());
	
	private static boolean jobEnd = false;
	private static Vector domainMXVector = new Vector();
	private static Hashtable domainHash = new Hashtable();

	public static boolean isContainsDomain(String domain)
	{
		if( domainHash.containsKey(domain) ) {
			return true;
		}
		else {
			return false;
		}
	}

	public static boolean isJobEnd()
	{
		return jobEnd;
	}

	public static void setJobStatus(boolean isJobEnd)
	{
		jobEnd = isJobEnd;
	}

	public static void putDomainHash(String domain, Vector domainMXVector)
	{
		domainHash.put(domain, domainMXVector);
	}

	public static synchronized String getMxHost(String domain)
	{
		try
		{
			domainMXVector = (Vector)domainHash.get(domain);
			if( domainMXVector == null || domainMXVector.size() < 0 ) {
				return "";
			}

			String mxHost = (String) domainMXVector.remove(0);
			domainMXVector.add(mxHost);
			return mxHost;
		}
		catch(Exception e)
		{
			LOGGER.error(e);
			//e.printStackTrace();
			LogJob.errorLog("CacheStore","getMxHost(String domain)",ErrorStatusCode.GENERAL_ERROR_MSG,e.toString());
			return "";
		}
	}
}
