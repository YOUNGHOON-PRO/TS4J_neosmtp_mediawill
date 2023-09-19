package com.sendprocess.lookup;

import java.io.*;
import java.net.*;
import com.sendprocess.config.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Look
{
	
	private static final Logger LOGGER = LogManager.getLogger(Look.class.getName());
	
    int msglen = 1024;
    byte header[] = { 0, 2, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0 };
    int head_len = 12;
    int OP_ST_QUERY;
    public int QT_MX = 15;

    public int pos, id, RR_total, f_QR, f_AA, f_TC, f_RD, f_RA, f_opcode, f_rcode, q_num , aRR_num, aaRR_num, aadRR_num;
    public String RR_dnames[];
    public String RR_rdata[];
    public int RR_type[];
    public int RR_dlength[];
    public int RR_mxpref[];

    public DatagramSocket dgs;
    public int query_id;
    public byte q_msg[];
    public int q_msg_len;
    public int query_type;

  	public Look()
    {
        f_RD = 1;
        f_opcode = OP_ST_QUERY;
        q_msg = new byte[msglen];

        try
        {
            dgs = new DatagramSocket();
            dgs.setSoTimeout(Operation.DNS_TimeOut*1000);

        }
        catch (Exception e)
        {
        	LOGGER.error(e);
        }

  	    query_id = 4096 + (int)(28671.0 * Math.random());
  	}

    private int getMessage(byte ab[], int i)
    {
        int j;
        id = Bytes2Int(ab[0], ab[1]);
        f_QR = fromByte(ab[2], 0, 1);
        f_opcode = fromByte(ab[2], 1, 4);
        f_AA = fromByte(ab[2], 5, 1);
        f_TC = fromByte(ab[2], 6, 1);
        f_RD = fromByte(ab[2], 7, 1);
        f_RA = fromByte(ab[3], 0, 1);
        f_rcode = fromByte(ab[3], 4, 4);
        q_num = Bytes2Int(ab[4], ab[5]);
        aRR_num = Bytes2Int(ab[6], ab[7]);
        aaRR_num = Bytes2Int(ab[8], ab[9]);
        aadRR_num = Bytes2Int(ab[10], ab[11]);
        RR_total = aRR_num + aaRR_num + aadRR_num;

        if (RR_total > 0)
        {
            RR_dnames = new String[RR_total];
            RR_type = new int[RR_total];
            RR_rdata = new String[RR_total];
            RR_dlength = new int[RR_total];
            RR_mxpref = new int[RR_total];
            outDNSdomain(ab, head_len, i, 0, -1);
            pos = pos + 4;

            for (j = 0; j < RR_total && (RR_dnames[j] = outDNSdomain(ab, pos + 1, i, 0, -1)) != null; j++)
            {
                RR_type[j] = Bytes2Int(ab[pos + 1], ab[pos + 2]);
                RR_dlength[j] = Bytes2Int(ab[pos + 9], ab[pos + 10]);
                if (RR_type[j] == 1)
                {
                    RR_rdata[j] = new String(Byte2Int(ab[pos + 11]) +"." + Byte2Int(ab[pos + 12]) + "." + Byte2Int(ab[pos + 13]) + "." + Byte2Int(ab[pos + 14]));
                    pos = pos + 14;
                }
                else if (RR_type[j] == 15)
                {
                    RR_mxpref[j] = Bytes2Int(ab[pos + 11], ab[pos + 12]);
                    pos = pos + 2;
                    RR_rdata[j] = outDNSdomain(ab, pos + 11, i, RR_type[j], RR_dlength[j]);
                }
                else
                    RR_rdata[j] = outDNSdomain(ab, pos + 11, i, RR_type[j], RR_dlength[j]);

                if (RR_rdata[j] == null)
                    pos = pos + 10 + RR_dlength[j];
            }
        }

        return 0;
    }

    public int setMessage(String string1)
    {
		String string2;
        query_id = (query_id == 32767) ? 4096 : (query_id + 1);

        string2 = new String(string1);

        for (int k = 0; k < head_len; k++)
        {
            q_msg[k] = header[k];
        }

        byte ab[] = new byte[2];
	    toByte(Integer.toHexString(query_id), ab, 0);
        q_msg[0] = ab[0];
        q_msg[1] = ab[1];
        int j1 = toDNSdomain(q_msg, head_len, string2.length(), string2.getBytes()) + head_len;
        q_msg[j1] = 0;
        q_msg[j1 + 1] = (byte)QT_MX;
        q_msg[j1 + 2] = 0;
        q_msg[j1 + 3] = 1;
        q_msg_len = j1 + 4;
        return q_msg_len;
    }

    private int toDNSdomain(byte ab1[], int i1, int j, byte ab2[])
    {
        int i2;
        int k = j - 1;
        for (i2 = j - 1; i2 > -1; i2--)
        {
            if (ab2[i2] != 46)
            {
                ab1[i1 + i2 + 1] = ab2[i2];
            }

            if (ab2[i2] == 46 && i2 != j - 1)
            {
                ab1[i1 + i2 + 1] = (byte)(k - i2);
                k = i2 - 1;
            }
        }
        ab1[i1] = (byte)(k - i2);
        ab1[i1 + j + 1] = 0;
        return j + 2;
    }

    private String outDNSdomain(byte ab[], int i1, int j1, int k1, int i2)
    {
        int j2;
        int k2;
        StringBuffer stringBuffer;
        int i3;
        Exception e;
        j2 = i1;
        stringBuffer = new StringBuffer();
        i3 = 0;

    	try
        {
            for (pos = 0; j2 < j1 && ab[j2] != 0 && (i3 < i2 || i2 < 0 || pos > 0); j2 = k2)
            {
                if (fromByte(ab[j2], 0, 2) == 3)
                {
                    if (pos == 0)
                        pos = j2 + 1;
                    j2 = fromByte(ab[j2], 2, 6, true) + Byte2Int(ab[j2 + 1]);
                    i3++;
                }
                for (k2 = j2 + 1; k2 < j2 + Byte2Int(ab[j2]) + 1; k2++)
                    stringBuffer.append((char)ab[k2]);

                if (k1 == 13 && j2 == i1)
                    stringBuffer.append("; OS = ");
                else if (k1 != 16 && k1 != 13)
                    stringBuffer.append(".");

                if (pos == 0 && i2 >= 0)
                    i3 = i3 + k2 - j2;
            }
    	}
        catch(ArrayIndexOutOfBoundsException e1){
        	LOGGER.error(e1);
    	}

        if (j2 >= ab.length)
        {
        	//LOGGER.info(stringBuffer.length() + " " + stringBuffer.toString());
            return null;
        }

        if (i3 >= i2 && pos == 0 && i2 >= 0)
            pos = j2 - 1;
        else if (pos == 0)
            pos = j2;

        return stringBuffer.toString();
    }

    private void append(Object object, String string) throws IOException
    {
        if (object instanceof FileOutputStream)
        {
            ((FileOutputStream)object).write(string.getBytes());
            return;
        }

        if (object instanceof PrintStream)
            ((PrintStream)object).print(string);
    }

    public int sendMessage(String string)
    {
        try
        {
            DatagramPacket datagramPacket = new DatagramPacket(q_msg, q_msg_len, InetAddress.getByName(string), 53);
            dgs.send(datagramPacket);
            return 0;
        }
        catch (UnknownHostException e1)
        {
        	LOGGER.error(e1);
            return -1;
        }
        catch (IOException e2)
        {
        	LOGGER.error(e2);
            return -2;
        }
    }

    public int receiveMessage()
    {
        int i = -1;
        DatagramPacket datagramPacket = new DatagramPacket(q_msg, msglen);
        try
        {
            dgs.receive(datagramPacket);
            i = Bytes2Int(datagramPacket.getData()[0], datagramPacket.getData()[1]);
        }
        catch (InterruptedIOException e1)
        {
        	LOGGER.error(e1);
            return -1;
        }
        catch (IOException e2)
        {
        	LOGGER.error(e2);
            return -2;
        }

        if (i == query_id)
        {
            getMessage(datagramPacket.getData(), datagramPacket.getLength());
        	i = 0;
        }
        return i;
    }

    public int getReturnCode(){ return f_rcode; }


    public String getAnswer(int i)
    {
		try{
        if (i >= 0 && i < RR_total)
        {
            if(RR_total%2 == 0)
            {
                for(int z = i ; RR_total > z ; z ++)
                {
                    if(('0' <= RR_rdata[RR_total/2+z].charAt(0) ) && (RR_rdata[RR_total/2+z].charAt(0)  <= '9'))
                        return RR_rdata[RR_total/2+z];
                }
                return null;
            }
            else
            {
                for(int z = i ; RR_total > z ; z ++)
                {
                    if(('0' <= RR_rdata[RR_total/2-1+z].charAt(0) ) && (RR_rdata[RR_total/2-1+z].charAt(0)  <= '9'))
                        return RR_rdata[RR_total/2-1+z];
                }
                return null;
            }
        }
        else
            return null;
		}catch(ArrayIndexOutOfBoundsException e){
			LOGGER.error(e);
			return null;
		}
    }

    public String getDomainName(int i)
    {
        if (i >= 0 && i < RR_total)
            return RR_dnames[i];
        else
            return null;
    }

    public int getMXPreference(int i)
    {
        if (i < RR_total && RR_type[i] == QT_MX)
            return RR_mxpref[i];
        else
            return -1;
    }

    public int getType(int i)
    {
        if (i >= 0 && i < RR_total)
            return RR_type[i];
        else
            return -1;
    }

    public int getAnswersTotal(){ return RR_total; }
    public int getNumberOfAnswers(){ return aRR_num; }
    public int getNumberOfAuthorityRec(){ return aaRR_num; }
    public int getNumberOfAdditionalRec(){ return aadRR_num; }

    private int fromByte(byte b, int i1, int j1, boolean flag)
    {
        int k1 = Byte2Int(b);
        int i2 = flag ? 256 : 1;
        String string = Integer.toBinaryString(k1);
        int j2 = 7;

        while (string.length() <  8 )
        {
            string = new StringBuffer(String.valueOf('0')).append(string).toString();
            j2--;
        }

        string = string.substring(i1, i1 + j1);
        k1 = 0;
        for (int k2 = string.length() - 1; k2 >= 0; k2--)
        {
            if (string.charAt(k2) == 49)
                k1 += i2;
            i2 *= 2;
        }
        return k1;
    }

    private int fromByte(byte b, int i, int j){ return fromByte(b, i, j, false); }

    private int Bytes2Int(byte b1, byte b2)
    {
        int i = 0;
        if (b1 == 0 && b2 == 0)
            return 0;
        if (b2 != 0)
            i = Byte2Int(b2);
        if (b1 == 0)
            return i;
        else
            return fromByte(b1, 0, 8, true) + i;
    }

    private int Byte2Int(byte b)
    {
        if (b < 0)
            return b + 256;
        else
            return b;
    }

    private byte hextoByte(char ch)
    {
        if (ch <= 57 && ch >= 48)
            return (byte)(ch - 48);
        if (ch >= 97 || ch <= 102)
            return (byte)(ch - 97 + 10);
        if (ch >= 65 || ch <= 70)
            return (byte)(ch - 65 + 10);
        else
            return 0;
    }

    private char halfBytetoHex(int i)
    {
        if (i <= 9)
        {
            return (char)(48 + i);
        }
        if (i <= 15)
        {
            return (char)(65 + i - 10);
        }
        else
        {
            return '-';
        }
    }

    private byte[] toByte(String string, byte ab[], int i)
    {
        for (int j = 0; j < (string.length() + 1) / 2; j++)
        {
            ab[j + i] = (byte)(16 * hextoByte(string.charAt(2 * j)));
            if (j != string.length() - 1)
                ab[j + i] = (byte)(ab[j + i] + hextoByte(string.charAt(2 * j + 1)));
        }
        return ab;
    }

    private String tohexString(byte ab[], int i1, int j)
    {
        StringBuffer stringBuffer = new StringBuffer(i1 * 2);
        for (int i2 = 0; i2 < i1; i2++)
        {
            int k;
            if (ab[i2 + j] < 0)
                k = ab[i2 + j] + 256;
            else
                k = ab[i2 + j];
            stringBuffer.insert(2 * i2, halfBytetoHex(k / 16));
            stringBuffer.insert(2 * i2 + 1, halfBytetoHex(k - 16 * (k / 16)));
        }
        return stringBuffer.toString();
    }

    private String tohexString(byte ab[], int i)
    {
        return tohexString(ab, i, 0);
    }

    private String tohexString(byte ab[])
    {
        return tohexString(ab, ab.length, 0);
    }
}
