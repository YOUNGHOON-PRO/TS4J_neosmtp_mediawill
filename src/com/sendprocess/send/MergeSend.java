package com.sendprocess.send;

import java.io.*;
import java.net.*;
import com.sendprocess.log.*;
import com.sendprocess.lookup.*;
import com.queue.*;
import com.sendprocess.config.*;
import com.sendprocess.send.Monitoring;
import com.sendprocess.util.MessageID;
import com.sendprocess.util.FileManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MergeSend
{
	
	private static final Logger LOGGER = LogManager.getLogger(MergeSend.class.getName());
	
	public static void main(String[] args)
	{
		LOGGER.info("");
		LOGGER.info("NeoSMTP Version 2.5 MergeSender Module Copyright (C) 1999-2002 Neocast Co.,Ltd..");

		if( !Config.load() )
		{
			LOGGER.info("Configuration file load failure.. Terminate Process..");
			System.exit(0);
		}

		if( !Operation.load() )
		{
			LOGGER.info("Operation file load failure.. Terminate Process..");
			System.exit(0);
		}

		if( args.length == 1)
		{
			if( args[0].equals("-version") )
			{
				LOGGER.info("NeoSMTPv2.5_001");
				System.exit(1);
			}
			else
			{
				try
				{
					int port = Integer.parseInt(args[0]);

					if( port > 0 && port < 65536 ) {
						Config.Mcast_Send_Port = Integer.parseInt(args[0]);
					}
					else
					{
						LOGGER.info("500 Argument Error. ");
						System.exit(1);
					}
				}
				catch(Exception e)
				{
					LOGGER.error("501 Argument Error : " + e);
					System.exit(1);
				}
			}
		}

		try {
			NeoSMTP.LocalIPAddress = (InetAddress.getLocalHost()).getHostAddress();
		}
		catch(UnknownHostException e) {
			LOGGER.error(e);
			NeoSMTP.LocalIPAddress = "127.0.0.1";
		}

		//���� ��ħ 2003.8.6 ������ ��ü�� �����Ѵ�.
		DNSCache dc = new DNSCache();
		dc.start();

		//dnscache���� true�� ���� ������ �̰����� ��� ���� �ݺ��ɼ� �ִ�.
		while( !CacheStore.isJobEnd() ){
			try{
				Thread.sleep(100);
			}	catch(InterruptedException e){LOGGER.error(e);}
		}

		MergeIndexQueue.allocate();
		LogData.allocateVector();

		//������ üũ�Ѵ�.
		MergeQueueDirChecker mqdc = new MergeQueueDirChecker();
		mqdc.start();

		ThreadManager tm = new ThreadManager();
		SendChecker sc = new SendChecker(tm);
		sc.start();

		FileRecordListen frl = new FileRecordListen();
		frl.start();

		new CheckGarbageMail().start();
		new RetryManager().start();
		new UIListen().start();

		UISender us = new UISender();
		us.start();

		//���� ������ ����
		Monitoring monitor = new Monitoring();
		monitor.setThread(dc, mqdc, sc, tm, frl, us);
		monitor.start();
	}

        public static void shutdown() {
          LOGGER.info("NeoSMTP shutdown.");
          System.exit(0);
        }

}
