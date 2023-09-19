package com.sendprocess.lookup;

import java.io.*;
import java.util.*;
import java.net.*;
import com.sendprocess.config.Config;

import com.sendprocess.lookup.Lookup2;
import com.sendprocess.lookup.ResourceRecord2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Lookup
{
	private static final Logger LOGGER = LogManager.getLogger(Lookup.class.getName());
	
	public Look look;
	private String inDomain;
	private String realDomain = "";

	public Lookup(String inDomain)
    {
		this.inDomain = inDomain;
	}

	public Vector lookupList()
    {
		int mxPreference = 70000;
		int currentMxPreference = 0;
		String currentDomain = "";
		Vector mxDomainVector = new Vector();
		Hashtable mxLowDomainHash = new Hashtable();

		look = new Look();
		look.setMessage(inDomain);
	  	int c = look.sendMessage(Config.DNS_Server_IP);

		if( c == 0 )
        {
	      	c = look.receiveMessage();

	      	if( c == 0)
            {
	        	if( look.getReturnCode() == 3 )
                {
	        		return null;
	        	}

	      		int num = look.getNumberOfAnswers();
	       		if( num == 0 ) //Not MX record
                {
	   				return null;
	      		}

				for(int i=0; i < num; i++)
				{
					String answer = look.getAnswer(i);
					if(answer==null)
					{
						return null;
					}
					StringBuffer sb = new StringBuffer(answer);
					
					int sbLength = sb.length();

					try
					{
						if( sbLength-1 > 0 )
						{
							if(sb.charAt( sbLength-1 ) == '.' )
								sb.setCharAt( sbLength-1,' ' );

							currentDomain = new String(sb);
							currentMxPreference =  look.getMXPreference(i);

							if( mxPreference > currentMxPreference )
							{
								mxPreference = currentMxPreference;
							}

							mxLowDomainHash.put( currentDomain , new Integer( currentMxPreference ) );
						}
					}
					catch(java.lang.StringIndexOutOfBoundsException ex)
					{
						LOGGER.error(ex);
					}
				}

				if ( mxLowDomainHash.size() == 0 )
				{
					return null;
				}
				else
				{
					for( Enumeration en = mxLowDomainHash.keys() ;en.hasMoreElements();)
					{
						String addMxDomain = (String)en.nextElement();
						if ( mxPreference == ((Integer)mxLowDomainHash.get(addMxDomain)).intValue() )
						{
							mxDomainVector.add(addMxDomain.trim());
						}
					}

					return mxDomainVector;
				}
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
	
	public String getMXAddresses(Vector elist, String hostName) {
        if ( (elist == null) || (elist.size() == 0)) {
            return hostName;
        }
        else {
            ResourceRecord2 element;
            StringBuffer buf = new StringBuffer(15);
            for (int i = 0; i < elist.size(); i++) {
                element = (ResourceRecord2) elist.remove(0);
                if (element != null) {
                    if (element.mx != null) {
                        return element.mx;
                    }
                    if (element.ipAddress != null) {
                    	String ipAddress = "";
                    	for( int addr : element.ipAddress) { 
                    		ipAddress += (ipAddress.equals("") ? "" : ".") + addr;
                    	}
                    	return ipAddress;
                    }
                }
            }
        }
        return hostName;
    }
	
	public String lookupEMS()throws Exception {
		String resultHost = "";
		Vector exchangers_host = new Vector();
        Vector exchangers_ip = new Vector();
        /***************************************************************/
        Lookup2 lookMX = new Lookup2(Config.DNS_Server_IP, inDomain, 15);
        exchangers_host = lookMX.getResult();
        
        for( int i=0 ; i < exchangers_host.size(); i++) { 
        	String mx = ((ResourceRecord2) exchangers_host.get(i)).mx;
        	Lookup2 lookMX2 = new Lookup2(Config.DNS_Server_IP, mx, 1);
        	exchangers_ip = lookMX2.getResult();
        	/***********
             * DEBUG 출력
             * writed by 오범석
                             LOGGER.info("LookupColler.java : ");
                             LOGGER.info("exchangers ==> " + exchangers);
             */
            if ( (exchangers_ip == null) || (exchangers_ip.size() == 0)) {
            	resultHost = inDomain;
            }
            else {
                resultHost = this.getMXAddresses(exchangers_ip, inDomain);
            } 
        }
        return resultHost;
	}
	
	public String lookupAll() {
		String dns_address =  "";
		
		try { 
			dns_address = this.lookupEMS();
			
		} catch(Exception e) {
			LOGGER.error("Exception Message ::" + e.getMessage());
		}
		
		return dns_address; 
	}

	public String lookupAll_old()
	{
		int mxPreference = 70000;
		int currentMxPreference = 0;
		String currentDomain = "";

		look = new Look();
		look.setMessage( inDomain );
		int c = look.sendMessage( Config.DNS_Server_IP );

		if( c == 0 )
		{
			c = look.receiveMessage();

			if( c == 0 )
			{
				if( look.getReturnCode() == 3 )
				{
					return "4"; //Notexist
				}

				int num = look.getNumberOfAnswers();
				if( num == 0 ) //Not MX record
				{
					return this.inDomain;
				}

				for(int i=0; i < num; i++)
				{
					String answer = look.getAnswer(i);
					
					if(answer==null)
					{
						return "7";
					}
					StringBuffer sb = new StringBuffer(answer);
					int sbLength = sb.length();

					try
					{
						if( sbLength-1 > 0 )
						{
							if(sb.charAt( sbLength-1 ) == '.' )
								sb.setCharAt( sbLength-1,' ' );

							currentDomain = new String(sb);
							currentMxPreference =  look.getMXPreference(i);

							if( mxPreference > currentMxPreference )
							{
								mxPreference = currentMxPreference;
								realDomain = currentDomain;
							}
						}
					}
					catch(java.lang.StringIndexOutOfBoundsException ex)
					{
						LOGGER.error(ex);
					}
				}
				return realDomain;
			}
			else
			{ // DNS Server Error || Response Error
				return "7"; //UnclassifiedHost
			}
		}
		else
		{
			return "7"; //UnclassifiedHost
		}
	}
}
