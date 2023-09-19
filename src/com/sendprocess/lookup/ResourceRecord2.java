package com.sendprocess.lookup;


import java.io.*;

public abstract class ResourceRecord2
{
    public int preference;
    public String mx;
    public int[] ipAddress = new int[4];

    public void initMailExchanger(DNSInputStream2 dnsIn)
        throws IOException {
        preference = dnsIn.readShort();
        mx = dnsIn.readDomainName();
    }

    public void initAddress(DNSInputStream2 dnsIn)
        throws IOException {
        for (int i = 0; i < 4; i++) {
            ipAddress[i] = dnsIn.readByte();
        }
    }

    protected abstract void decode(DNSInputStream2 dnsIn)
        throws IOException;
}
