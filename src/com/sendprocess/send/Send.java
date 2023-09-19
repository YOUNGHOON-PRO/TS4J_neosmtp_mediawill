package com.sendprocess.send;

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;
import java.lang.*;

import com.sendprocess.send.SmtpProtocolException;
import com.sendprocess.log.*;
import com.sendprocess.config.*;
import com.sendprocess.lookup.*;
import com.queue.MergeIndexQueue;
import com.sendprocess.data.SendData;
import com.sendprocess.send.ThreadManager;
import com.sendprocess.util.ErrorStatusCode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Send implements Runnable
{
	
	private static final Logger LOGGER = LogManager.getLogger(Send.class.getName());
	
	private Thread thread;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private InputStream iStream;
	private OutputStream oStream;
	private String responseCode = null;

	private boolean isOK = false;
	private int retry;
	private String subDirNo, msgNo ,msgFilePath;
	private String messageID, subID, rID, rName, rEmail, sID, sName, sEmail, TID, REFMID, requestKey;;
	private String domain, mxHost;

	private String errorMessage = "";
	private String smtpCommand = "";

	private boolean dotAction = false;
	private String checkLine = "";
	private String lt = "&lt;";
	private String gt = "&gt;";
	private String dataTag = "<!--DATAPART-->";

	private String smtpHeader = "";
	private String smtpBody = "";

	private static PrintStream debugStream;
	static {
		String debugFile = System.getProperty("smtp.debug.file");
		if( debugFile != null )
		{
			try {
				debugStream = new PrintStream(
						new BufferedOutputStream(new FileOutputStream(debugFile)));
			}
			catch(IOException ex) {
				LOGGER.error(ex);
				ex.printStackTrace();
			}
		}
	}

	public Send(String info )
	{
		DataInputStream dis = null;

		try
		{
			StringTokenizer st = new StringTokenizer(info,Operation.File_Sep);
			messageID = st.nextToken();
			subDirNo = st.nextToken();
			msgNo = st.nextToken();

			msgFilePath = (new StringBuffer(Config.Merge_Queue_Dir).append(File.separator)
						   .append(messageID).append(File.separator).append(subDirNo)
						   .append(File.separator).append(msgNo).append(".eml")).toString();
			retry = Integer.parseInt(st.nextToken());

			dis = new DataInputStream(new FileInputStream(msgFilePath));
			byte[] data = new byte[dis.available()];
			dis.readFully(data);
			String files = new String(data);
			smtpHeader = files.substring(0, files.indexOf(dataTag));
			smtpBody = files.substring(files.indexOf(dataTag) + dataTag.length());
			smtpBody = smtpBody.trim();

			StringTokenizer stHeader = new StringTokenizer(smtpHeader, "\n");
			this.sEmail = stHeader.nextToken().trim();
			this.rEmail = stHeader.nextToken().trim();
			this.sID = stHeader.nextToken().trim();
			this.sName = stHeader.nextToken().trim();
			this.rID = stHeader.nextToken().trim();
			this.rName = stHeader.nextToken().trim();
			this.TID = stHeader.nextToken().trim();
			this.REFMID = stHeader.nextToken().trim();
			this.subID = stHeader.nextToken().trim();
			this.requestKey = stHeader.nextToken().trim();

			int pos = this.rEmail.lastIndexOf("@");
			domain = this.rEmail.substring(pos + 1, this.rEmail.length()).trim();
			domain = domain.toLowerCase();

			///////////////////////////////////////////////
			if( smtpBody.startsWith("2") ) {
				isOK = false;
			}
			else {
				isOK = true;
			}
		}
		catch(Exception e)
		{
			LOGGER.error(e);
			e.printStackTrace();
			LogJob.errorLog("Send", "Send(String info)",
							ErrorStatusCode.SMTP_INFO_NOT_VALID_MSG, e.toString());
			isOK = false;
		}
		finally
		{
			try {
				dis.close();
				dis = null;
			}
			catch(Exception e) {LOGGER.error(e);}
		}
	}

	public void start()
	{
		thread = new Thread(this);
		Config.threadListVector.add(messageID);

		if( thread != null )
		{
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
		return;
	}

	public void end()
	{
		if( thread != null )
		{
			SendData.plusJobOut();
			thread.interrupt();
			thread = null;
			Config.threadListVector.remove(messageID);
		}
		return;
	}

	private void delete()
	{
		try {
			new File(this.msgFilePath).delete();
		}
		catch(Exception e) {
			LOGGER.error(e);
		}
	}

	private void setOut()
	{
		try
		{
			if( in != null ){ in.close(); in = null; }
			if( out != null ){ out.close(); out= null; }
			if( socket != null ){ socket.close(); socket = null; }
		}
		catch(IOException e) {
			LOGGER.error(e);
		}
		finally {
			end();
		}
		return;
	}

	public synchronized void run()
	{
		String strSendDoamin = "";
		///////////////////////////////////////////////////////
		if( !isOK )
		{
			delete();
			setOut();
			return;
		}

		if( CacheStore.isContainsDomain(domain.toLowerCase()) )
		{
			mxHost = CacheStore.getMxHost(domain.toLowerCase());

			if( mxHost.equals(""))
			{
				Lookup lookup = new Lookup(domain.toLowerCase());
				mxHost =  lookup.lookupAll().trim();
			}
		}
		else
		{
			Lookup lookup = new Lookup(domain.toLowerCase());
			mxHost =  lookup.lookupAll().trim();
			
			if(mxHost.equals("")) { 
				mxHost = "4";
			}
		}

//        Lookup lookup = new Lookup( domain );
//        mxHost =  lookup.lookupAll().trim();

		String strRetCode = "";
		/////////////////////////////////////////////////////////////

		// messageID,rEmail,NOTEXISTS
		if( mxHost.equals("4") )
		{
			LogData.insertToSendVector(messageID, subID, REFMID, TID, rID, rName, rEmail,
									   sID, sName, sEmail, NeoSMTP.NOTEXIST_DOMAIN, requestKey);
			delete();
			setOut();
			return;
		}
		else if( mxHost.equals("7") )
		{
			LogData.insertToSendVector(messageID, subID, REFMID, TID, rID, rName, rEmail,
									   sID, sName, sEmail, NeoSMTP.NOTEXIST_MAILSERVER, requestKey);
			delete();
			setOut();
			return;
		}

		if( mxHost.equals("127.0.0.1") || mxHost.equalsIgnoreCase("localhost") || mxHost.equals(".") )
		{
			LogData.insertToSendVector(messageID, subID, REFMID, TID, rID, rName, rEmail,
									   sID, sName, sEmail, NeoSMTP.LOCALHOST_MAILSERVER, requestKey);
			delete();
			setOut();
			return;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss a z", java.util.Locale.US);
		String dateStr = sdf.format(new java.util.Date());
		StringBuffer sb = null;

		try
		{
			sb = new StringBuffer();
			this.smtpCommand = sb.append("CONNECT:").append(mxHost).toString();
			socket = new Socket();

			socket.setReuseAddress(true);
			socket.setSoTimeout(Operation.Socket_Time * 1000);
			socket.setTcpNoDelay(true);
			socket.setSoLinger(true, 1);
			socket.setKeepAlive(true);

			LOGGER.info("mxHost:"+mxHost);
			long startTime = System.currentTimeMillis();

			socket.connect(new InetSocketAddress(mxHost, 25), Operation.Socket_Time * 1000);
			if( debugStream != null )
			{
				long endTime = System.currentTimeMillis();

				debugStream.println((new StringBuffer("Connection:")
									 .append(endTime - startTime)
									 .append("msec [").append(mxHost).append("] Success mid : ").append(messageID).toString()));
				debugStream.flush();
			}

			this.iStream = socket.getInputStream();
			this.oStream = socket.getOutputStream();

			in = new BufferedReader(new InputStreamReader(this.iStream));
			out = new PrintWriter(this.oStream, true);

			socket.setSendBufferSize(Operation.Send_Buffer);
			socket.setReceiveBufferSize(Operation.Receive_Buffer);

			strRetCode = getResponse();
			if( strRetCode == "" ) {
				throw new SmtpProtocolException(NeoSMTP.CONNECT);
			}
			if( !strRetCode.equals("220") )
			{
				retry(NeoSMTP.SERVICE_NOT_AVAILABLE);
				return;
			}
			sb = new StringBuffer();

			if(Config.SEND_DOMAIN == null)
			{
				strSendDoamin = InetAddress.getLocalHost().getHostName();
			}else{
				strSendDoamin = Config.SEND_DOMAIN;
			}

			this.smtpCommand = (sb.append("HELO ").append(strSendDoamin)).toString();
			
			sendLine(this.smtpCommand);
			strRetCode = getResponse();
			if( strRetCode == "" ) {
				throw new SmtpProtocolException(NeoSMTP.HELO);
			}

			if( !strRetCode.equals("250"))
			{
				retry();
				return;
			}

			sb = new StringBuffer();
			this.smtpCommand = (sb.append("MAIL FROM: &lt;").append(sEmail).append("&gt;")).toString();
			sb = new StringBuffer();
			sendLine(sb.append("MAIL FROM: <").append(sEmail).append(">").toString());
			strRetCode = getResponse();
			
			if( strRetCode == "" ) {
				throw new SmtpProtocolException(NeoSMTP.MAILFROM);
			}

			if( !strRetCode.equals("250") )
			{
				int nCode = -1;

				if( strRetCode == "421" ) {
					retry(NeoSMTP.SERVICE_NOT_AVAILABLE);
					return;
				}
				else if( strRetCode.charAt(0) == '4' ) {
					retry(NeoSMTP.MAILFROM_TEMPFAIL);
					return;
				}
				else if( strRetCode == "501" ) {
					nCode =  NeoSMTP.COMMAND_SYNTAXERROR;
				}
				else if( strRetCode == "553" ) {
					nCode = NeoSMTP.MAILBOX_SYNTAXERROR;
				}
				else if( strRetCode == "552" ) {
					retry(NeoSMTP.SYSTEM_STORAGEFULL);
					return;
				}
				else if( strRetCode.charAt(0) == '5' ) {
					nCode = NeoSMTP.MAILFROM_UNKNOWNERROR;
				}

				LogData.insertToSendVector(messageID, subID, REFMID, TID,
						rID, rName, rEmail, sID, sName, sEmail, nCode, requestKey);
				SMTPMessageLog.insertMessageInfo(messageID, subID, this.rEmail, this.rName,
						this.rID, this.smtpCommand, this.errorMessage);
				delete();
				return;
			}

			sb = new StringBuffer();
			this.smtpCommand = (sb.append("RCPT TO: &lt;")
						   .append(rEmail).append("&gt;")).toString();
			sb = new StringBuffer();
			sendLine(sb.append("RCPT TO: <").append(rEmail)
					 .append(">").toString());
			strRetCode = getResponse();
			if( strRetCode == "" ) {
				throw new SmtpProtocolException(NeoSMTP.RCPTTO);
			}

			if( strRetCode.equals("250") || strRetCode.equals("251") ) {
			}
			else
			{
				int nCode = -1;

				if( strRetCode.equals("500") || strRetCode.equals("501") ) {
					nCode = NeoSMTP.COMMAND_SYNTAXERROR;
				}
				else if( strRetCode.equals("550" ) || strRetCode.equals("551") || strRetCode.equals("553") ) {
					nCode = NeoSMTP.UNKNOWN_ACCOUNT;
				}
				else if( strRetCode.equals("552") ) {    // Too many recipients
					retry();
					return;
				}
				else if( strRetCode.charAt(0) == '5' ) {
					nCode = NeoSMTP.RCPTTO_UNKNOWNERROR;
				}
				else if( strRetCode.equals("450") || strRetCode.equals("451") ) {
					retry(NeoSMTP.MAILBOX_NOT_AVAILABLE);
					return;
				}
				else if( strRetCode.equals("452") ) {
					nCode = NeoSMTP.SYSTEM_STORAGEFULL;
				}
				else if( strRetCode.equals("421") ) {
					retry(NeoSMTP.SERVICE_NOT_AVAILABLE);
					return;
				}
				else if( strRetCode.charAt(0) == '4' ) {
					retry(NeoSMTP.RCPTTO_TEMPFAIL);
					return;
				}

				LogData.insertToSendVector(messageID, subID, REFMID, TID,
						rID, rName, rEmail, sID, sName, sEmail, nCode, requestKey);
				SMTPMessageLog.insertMessageInfo(messageID, subID, this.rEmail, this.rName,
						this.rID, this.smtpCommand, this.errorMessage);
				delete();
				return;
			}

			if( Config.Is_Real )
			{
				this.smtpCommand = "DATA";
				sendLine(this.smtpCommand);
				strRetCode = getResponse();
				if( strRetCode == "" ) {
					throw new SmtpProtocolException(NeoSMTP.DATA);
				}

				if( !strRetCode.equals("354") )
				{
					if( strRetCode.equals("451") )
					{
						retry(NeoSMTP.PROCESSING_ERROR);
						return;
					}
					else
					{
						retry();
						return;
					}
				}

				sb = new StringBuffer();
				sb.append("Return-Path: <").append(sEmail).append(">\r\n")
						.append("Received: <").append(messageID).append(" ")
						.append(retry).append("> NeoSMTP version 2.5\r\n");

				// Date Field �˻�..
				if( this.smtpBody.indexOf("\nDate: ") == -1 ) {
					sb.append("Date: ").append(dateStr).append("\r\n");
				}
				sb.append(this.smtpBody.trim()).append("\r\n");

				sendData(sb.toString().getBytes());
				this.smtpCommand = "\r\n.";
				sendLine(this.smtpCommand);

				/*******************************************************************************************/
				//���⼭���ʹ� �����ΰ�
				/////////////////
				dotAction = true;

				strRetCode = getResponse();
				if( strRetCode == "" ) {
					throw new SmtpProtocolException(NeoSMTP.DOT);
				}

				if( strRetCode.equals("250") ) {
				}
				else if( strRetCode.equals("NeoSMTP_Success") ) {
				}
				else
				{
					int nCode = 0;
					if( strRetCode.equals("552") ) {
						retry(NeoSMTP.USER_STORAGEFULL);
					}
					else if( strRetCode.equals("554") ) {
						retry(NeoSMTP.TRANSACTION_FAILED);
						return;
					}
					else if( strRetCode.charAt(0) == '5' ) {
						nCode = NeoSMTP.DATA_UNKNOWNERROR;
					}
					else if( strRetCode.equals("451") ) {
						retry(NeoSMTP.PROCESSING_ERROR);
						return;
					}
					else if( strRetCode.equals("452") ) {
						retry(NeoSMTP.SYSTEM_STORAGEFULL);
						return;
					}
					else if( strRetCode.charAt(0) == '4' ) {
						retry(NeoSMTP.DATA_TEMPFAIL);
						return;
					}

					LogData.insertToSendVector(messageID, subID, REFMID, TID,
							rID, rName, rEmail, sID, sName, sEmail, nCode, requestKey);
					SMTPMessageLog.insertMessageInfo(messageID, subID, this.rEmail, this.rName,
							this.rID, this.smtpCommand, this.errorMessage);
					delete();
					return;
				}

				/////////////////
				dotAction = false;

				/*******************************************************************************************/

				// messageID, rEmail, retry, SUCCESS;
				SendData.plusSemail();
				LogData.insertToSendVector(messageID, subID, REFMID, TID,
						rID, rName, rEmail, sID, sName, sEmail, NeoSMTP.SUCCESS, retry, requestKey);
			}
			else
			{
				// messageID, rEmail, retry, SUCCESS;
				SendData.plusSemail();
				LogData.insertToSendVector(messageID, subID, REFMID, TID,
						rID, rName, rEmail, sID, sName, sEmail, NeoSMTP.SUCCESS, retry, requestKey);
			}

			sendLine("QUIT");
			getResponse();

			delete();
		}
		catch (Exception ex)
		{
			//ex.printStackTrace();

			if( ex instanceof UnknownHostException )
			{
				LOGGER.info("UnknownHostException");
				LogJob.errorLog("Send", "run()", ErrorStatusCode.DOMAIN_NOT_FOUND_MSG, ex.toString());
				LogData.insertToSendVector(messageID, subID, REFMID, TID,
						rID, rName, rEmail, sID, sName, sEmail, NeoSMTP.NOTEXIST_MAILSERVER, requestKey);
				SMTPMessageLog.insertMessageInfo(messageID, subID, this.rEmail, this.rName,
						this.rID, this.smtpCommand, this.errorMessage);
				delete();
				return;
			}
			else if( ex instanceof ConnectException )
			{
				this.smtpCommand = "CONNECT";
				this.errorMessage = ex.toString();
				retry(NeoSMTP.NETWORK_ERROR);
				return;
			}
			else if( ex instanceof NoRouteToHostException )
			{
				this.smtpCommand = "CONNECT";
				this.errorMessage = ex.toString();
				retry(NeoSMTP.NETWORK_ERROR);
				return;
			}
			else if( ex instanceof SmtpProtocolException )
			{
				LOGGER.info("SmtpProtocolException");
				LogJob.errorLog("Send", "run()", ErrorStatusCode.PROTOCOL_ERROR_MSG, ex.toString());
				int nCode = -1;
				switch( ((SmtpProtocolException) ex).nCurrent )
				{
					case NeoSMTP.CONNECT:
					case NeoSMTP.HELO:
					case NeoSMTP.MAILFROM:
					case NeoSMTP.RCPTTO:
					case NeoSMTP.DATA:
						retry(NeoSMTP.PROTOCOL_ERROR);
						return;
					case NeoSMTP.DOT:
						nCode = NeoSMTP.SUCCESS;
						break;
					default:
						nCode = NeoSMTP.UNKNOWN_ERROR;
					break;
				}

				LogData.insertToSendVector(messageID, subID, REFMID, TID,
						rID, rName, rEmail, sID, sName, sEmail, nCode, requestKey);
				delete();
				return;
			}

			retry();
		}
		finally {
			setOut();
		}
	}

	private void retry(int nCode)
	{
		if( retry < Config.Retry_Limit ) {
			createSpool();
		}
		else
		{
			if( Operation.Network_File_Delete ) {
				delete();
			}

			LogData.insertToSendVector(messageID, subID, REFMID, TID, rID, rName,
									   rEmail, sID, sName, sEmail, nCode, requestKey);

			this.smtpCommand.trim();
			this.errorMessage.trim();

			if( smtpCommand.length() != 0 || errorMessage.length() != 0 ) {
				SMTPMessageLog.insertMessageInfo(messageID, subID, this.rEmail, this.rName,
						this.rID, this.smtpCommand, this.errorMessage);
			}
		}
	}

	private void sendData(byte[] data)
	{
		ByteArrayInputStream  bis = null;
		DataOutputStream dos = null;
		int buffsize = 2048;

		try
		{
			bis = new ByteArrayInputStream(data);
			dos = new DataOutputStream(oStream);
			byte[] sendbuff = new byte[buffsize];
			int readcnt = 0;
			while( (readcnt = bis.read(sendbuff, 0, buffsize)) != -1 )
			{
				if( readcnt < buffsize )
				{
					byte[] sTemp = new byte[readcnt];
					System.arraycopy(sendbuff, 0, sTemp, 0, readcnt);
					dos.write(sTemp);
					dos.flush();
					sTemp = null;
					break;
				}

				dos.write(sendbuff);
				dos.flush();
			}

			sendbuff = null;
		}
		catch(Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		finally
		{
			try {
				bis.close();
				bis = null;
			}
			catch(Exception e) {LOGGER.error(e);}
		}
	}

	private void retry()
	{
		if( retry < Config.Retry_Limit ) {
			createSpool();
		}
		else
		{
			if( Operation.Network_File_Delete ) {
				delete();
			}

			LogData.insertToSendVector(messageID, subID, REFMID, TID, rID, rName,
									   rEmail, sID, sName, sEmail, NeoSMTP.NETWORK_ERROR, requestKey);

			this.smtpCommand.trim();
			this.errorMessage.trim();

			if( smtpCommand.length() != 0 || errorMessage.length() != 0 ) {
				SMTPMessageLog.insertMessageInfo(messageID, subID, this.rEmail, this.rName,
						this.rID, this.smtpCommand, this.errorMessage);
			}
		}
	}

	private void sendLine(String line)
	{

		long startTime = System.currentTimeMillis();

		try
		{
			out.print(line);
			out.print("\r\n");
			out.flush();
			if( debugStream != null )
			{
				long endTime = System.currentTimeMillis();
				debugStream.println((new StringBuffer("Send: mid=>").append(messageID).append("   ").append(endTime - startTime)
						.append("msec [").append(mxHost).append("] ").append(line).toString()));
				debugStream.flush();
			}

		}
		catch(Exception e)
		{
			LOGGER.error(e);
			e.printStackTrace();
			LogJob.errorLog("Send", "sendLine(String line)",
							ErrorStatusCode.IO_ERROR_MSG, e.toString());
			if( debugStream != null )
			{
				long endTime = System.currentTimeMillis();
				debugStream.println((new StringBuffer("Send:mid=>").append(messageID).append("   ").append(endTime - startTime)
						.append("msec [").append(mxHost).append("] ")
						.append(line).append(" => ").append(e.getMessage()).toString()));
				debugStream.flush();
			}

			return;
		}

	}

	private String getResponse()
	{
		StringBuffer sb = new StringBuffer();
		this.errorMessage = "";
		String returnVal = "";

		boolean bResult = readFully(sb);

		if( bResult )
		{
			this.errorMessage = replaceBraket(sb.toString());
			String temp = "";

			StringTokenizer st = new StringTokenizer(this.errorMessage, "\n");
			int nCount = st.countTokens();
			int nIndex = 0;
			String[] buffResp = new String[nCount];
			while( st.hasMoreElements() ) {
				buffResp[nIndex++] = st.nextToken().trim();
			}
			returnVal = buffResp[nCount - 1].substring(0, 3);
		}
		else {
			returnVal = "";
		}

		return returnVal;
		/*
		try
		{
			responseCode = "";
			String line = in.readLine();

			if( line == null ) {
				return "";
			}

			responseCode = line.substring(0, 3);

			while( line.charAt(3) == '-' ) {
				line = in.readLine();
			}

			return responseCode;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LogJob.errorLog("Send","getResponse()", ErrorStatusCode.IO_ERROR_MSG, e.toString());

			if( e instanceof InterruptedIOException ) {
				if( dotAction ) {
					return "NeoSMTP_Success";
				}
				else {
					return "";
				}
			}
			else {
				return "";
			}
		}
		*/
	}

 public boolean readFully(StringBuffer sb)
 {
	if(sb == null) {
		sb = new StringBuffer();
	}

	boolean bResult = true;
	DataInputStream dis = null;
	long startTime = System.currentTimeMillis();

	try {
		dis = new DataInputStream(this.iStream);
		byte[] data = new byte[2048];
		int readcnt = 0;

		while((readcnt = dis.read(data)) != -1) {
			if(readcnt < 2048) {
				byte[] tmp = new byte[readcnt];
				System.arraycopy(data, 0, tmp, 0, readcnt);
				sb.append(new String(tmp));
				if(tmp[readcnt - 1] != 10) { // Line Feed Check..
					try {
						Thread.sleep(10);
					}
					catch(Exception ex) {
						LOGGER.error(ex);
					}
					continue;
				}
				 break;
			}
			sb.append(new String(data));
		}
			if( debugStream != null )
			{
				long endTime = System.currentTimeMillis();
				debugStream.println((new StringBuffer("Read: mid=>").append(messageID).append("   ").append(endTime - startTime)
									 .append("msec [").append(mxHost).append("] ")
									 .append(smtpCommand).append("<=>").append(sb.toString()).toString()));
				debugStream.flush();
			}

	}
	catch(Exception e) {
		LOGGER.error(e);
		bResult = false;
			if( debugStream != null )
			{
				long endTime = System.currentTimeMillis();
				debugStream.println((new StringBuffer("Read: Exception mid=>").append(messageID).append("   ").append(endTime - startTime)
									 .append("msec [").append(mxHost).append("] ")
									 .append(smtpCommand).append("<=>").append(e.getMessage()).toString()));
			}


	}
	return bResult;
 }
/*
	public boolean readFully(StringBuffer sb)
	{
		if( sb == null ) {
			sb = new StringBuffer();
		}

		boolean bResult = true;
		DataInputStream dis = null;
		try
		{
			dis = new DataInputStream(this.iStream);
			byte[] data = new byte[2000];
			int readcnt = 0;

			while( (readcnt = dis.read(data)) != -1 )
			{
				if( readcnt < 2000 )
				{
					byte[] tmp = new byte[readcnt];
					System.arraycopy(data, 0, tmp, 0, readcnt);
					sb.append(new String(tmp));
					break;
				}
				sb.append(new String(data));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			bResult = false;
		}

		return bResult;
	}
*/
	public int getResponseEx()
			throws Exception
	{
		String iRCode = "";
		this.responseCode = "";
		this.responseCode = this.in.readLine();

		if( this.responseCode == null ) {
			return -1;
		}

		if( this.responseCode.trim().length() <= 3 )
		{
			iRCode = this.responseCode.trim();
			return Integer.parseInt(iRCode);
		}

		while( this.responseCode.charAt(3) == '-' ) {
			this.responseCode = this.in.readLine();
		}

		iRCode = this.responseCode.substring(0, 3);
		int iCode = Integer.parseInt(iRCode);
		return iCode;
	}

	private void createSpool()
	{
		MergeIndexQueue.addMergeMemoryIndexRetryQueueVector((new StringBuffer(messageID)
				.append(Operation.File_Sep).append(subDirNo).append(Operation.File_Sep)
				.append(msgNo).append(Operation.File_Sep).append(retry + 1).toString()));
	}

	private String replaceBraket(String msg)
	{
		msg = msg.trim();
		StringBuffer sb = null;

		int startPos = 0;
		int nPos = msg.indexOf("<", startPos);
		if( nPos != -1 )
		{
			sb = new StringBuffer();
			while( nPos != -1 )
			{
				sb.append(msg.substring(startPos, nPos)).append(this.lt);
				startPos = nPos + 1;
				nPos = msg.indexOf("<", startPos);
				if( nPos == -1 ) {
					sb.append(msg.substring(startPos));
				}
			}
			msg = sb.toString();
		}

		startPos = 0;
		nPos = msg.indexOf(">", startPos);

		if( nPos != -1 )
		{
			sb = new StringBuffer();
			while( nPos != -1 )
			{
				sb.append(msg.substring(startPos, nPos)).append(this.gt);
				startPos = nPos + 1;
				nPos = msg.indexOf(">", startPos);
				if( nPos == -1 ) {
					sb.append(msg.substring(startPos));
				}
			}
			msg = sb.toString();
		}

		return msg;
	}
}
