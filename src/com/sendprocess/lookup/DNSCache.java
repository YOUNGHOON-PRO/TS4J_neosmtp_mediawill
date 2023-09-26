package com.sendprocess.lookup;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import com.sendprocess.config.*;
import com.sendprocess.log.LogJob;
import com.sendprocess.util.ErrorStatusCode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DNSCache extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(DNSCache.class.getName());

	
	Vector cacheDomainListVector = new Vector();

	public DNSCache()
	{
		//mailserver customizing set loading
		printMsg("DNSCache","Fixed Domain Search..");
		LineNumberReader domainListLn = null;
		try
		{
			File mailListFile = new File("../config/net/mailserver");
			//File mailListFile = new File("./config/net/mailserver");

			String dirPath = mailListFile.getAbsolutePath();
			String fileList[] = mailListFile.list();

			if( fileList.length > 0 )
			{
				File file;
				for( int i = 0 ; i < fileList.length ; i++ )
				{
					file = new File(dirPath + File.separator + fileList[i]);
					String fileName = file.getName();
					String domainName = fileName.substring(0,fileName.length()-4);

					domainListLn = new LineNumberReader(new FileReader(file));

					String domainStr = "";
					Vector setDomainListVector = new Vector();

					while( ( domainStr=domainListLn.readLine() ) != null )
					{
						printMsg("DNSCache", domainName + " --> " + domainStr);
						setDomainListVector.add(domainStr.trim());
					}

					CacheStore.putDomainHash(domainName.trim(), setDomainListVector);
					if(domainListLn !=null) domainListLn.close();
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.error(e);
			//e.printStackTrace();
			LogJob.errorLog("DNSCache","DNSCache()",ErrorStatusCode.FILE_NOT_FOUND_MSG,e.toString());
		}finally
		{
			try
			{
				if(domainListLn !=null) domainListLn.close();
			}catch(Exception e)
			{
				LOGGER.error(e);
				//e.printStackTrace();
			}
		}

		//DNS cache loading
		printMsg("DNSCache", "DNS Cache Domain Search..");

		try
		{
			domainListLn = new LineNumberReader(
								new FileReader("../config/net/cache.ini" ));
								//new FileReader("./config/net/cache.ini" ));

			String domainStr = "";
			while( ( domainStr = domainListLn.readLine() ) != null ) {
				if(domainStr.equals(""))
				{

				}else
				{
					cacheDomainListVector.add( domainStr.trim() );
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.error(e);
			//e.printStackTrace();
			LogJob.errorLog("DNSCache","DNSCache()",ErrorStatusCode.FILE_NOT_FOUND_MSG,e.toString());
		}finally
		{
			try
			{
				if(domainListLn !=null) domainListLn.close();
			}catch(Exception e)
			{
				LOGGER.error(e);
				//e.printStackTrace();
			}
		}
	}

	private void printMsg(String className, String msg)
	{
		LOGGER.info(className + " : " + msg);
	}

	public void run()
	{
		while( true )
		{
			for( int i = 0; i < cacheDomainListVector.size(); i++ )
			{
				String cacheDomain = (String) cacheDomainListVector.elementAt(i);
				Vector lookupListVector = null;
				printMsg("DNSCache", cacheDomain + " --> Query" );

				int retryCount = 0;
				while( lookupListVector == null )
				{
					Lookup lookup = new Lookup(cacheDomain);
//					lookupListVector = lookup.lookupList();
					String dns_address = lookup.lookupAll();
					if (!dns_address.equals("") && lookupListVector == null) {
						lookupListVector = new Vector();
						lookupListVector.add(dns_address);
					}

					if( lookupListVector != null)
					{
						printMsg("DNSCache", cacheDomain + " --> OK!" );
						CacheStore.putDomainHash( cacheDomain , lookupListVector );
						break;
					}
					else
					{
						printMsg("DNSCache", cacheDomain + " --> Fail!" );
					}

					try
					{
						sleep(500);
					}
					catch(InterruptedException e)
					{
						LOGGER.error(e);
						//e.printStackTrace();
						LogJob.errorLog("DNSCache","DNSCache()",ErrorStatusCode.FILE_NOT_FOUND_MSG,e.toString());
					}

					//5�� ��õ� ���Ŀ� ����������.
					retryCount++;
					if(retryCount == Operation.domain_retry_cnt)
					{
						break;
					}
				}
			}

			CacheStore.setJobStatus(true);
			printMsg("DNSCache", "DNS Cache Successful..." );

			try
			{
				sleep(1000*60*Operation.DNSCache_Period);
			}
			catch(InterruptedException e)
			{
				LOGGER.error(e);
				//e.printStackTrace();
				LogJob.errorLog("DNSCache","run()",ErrorStatusCode.FILE_NOT_FOUND_MSG,e.toString());
			}
		}
	}
}
