package com.sendprocess.send;

/**
 * <p>Title: NeoSMTP version 2.5</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Neocast Co.,Ltd</p>
 * @author unascribed
 * @version 1.0
 */

public class SmtpProtocolException extends Exception
{
	public int nCurrent = -1;

	public SmtpProtocolException()
	{
		super();
	}

	public SmtpProtocolException(String errmsg)
	{
		super(errmsg);
	}

	public SmtpProtocolException(int nProtocol)
	{
		super();
		nCurrent = nProtocol;
	}
}

