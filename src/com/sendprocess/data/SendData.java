package com.sendprocess.data;

import java.text.NumberFormat;

public class SendData
{
	public static int var = 0;
	public static int BASE_THREAD = 0;

	private static int sEmail = 0;
	private static int jobOut = 0;
	private static int active_Agent = 0;
	private static int totalEmail = 0;

	public static synchronized void setClear(){ sEmail = 0; jobOut = 0; }
	public static synchronized void plusSemail(){ sEmail++; }
	public static synchronized void plusJobOut(){ jobOut++; }
	public static synchronized void plusTotalEmail() { totalEmail++; }
	public static synchronized void setEmailCount(int size) {
		totalEmail = totalEmail + size;
	}

	public static synchronized int getSemail(){ return sEmail; }
	public static synchronized int getJobOut(){ return jobOut; }
	public static synchronized int getTotalEmail() { return totalEmail; }

	public static synchronized void setActiveAgent(int active_Agent_) {
		active_Agent = active_Agent_;
	}

	public static synchronized int getActiveAgent() {
		return active_Agent;
	}
}
