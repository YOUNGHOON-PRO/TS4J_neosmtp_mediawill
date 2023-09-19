package com.sendprocess.lookup;


import java.io.*;

public class MailExchanger2
    extends ResourceRecord2
{
    private int preference;
    private String mx;

    protected void decode(DNSInputStream2 dnsIn)
        throws IOException {
        preference = dnsIn.readShort();
        mx = dnsIn.readDomainName();
    }
}