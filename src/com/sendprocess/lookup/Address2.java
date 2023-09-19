package com.sendprocess.lookup;


import java.io.*;

public class Address2
    extends ResourceRecord2
{
    private int[] ipAddress = new int[4];

    protected void decode(DNSInputStream2 dnsIn)
        throws IOException {
        for (int i = 0; i < 4; i++) {
            ipAddress[i] = dnsIn.readByte();
        }
    }
}