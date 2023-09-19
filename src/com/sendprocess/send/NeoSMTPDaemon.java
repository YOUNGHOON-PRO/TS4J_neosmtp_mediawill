package com.sendprocess.send;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NeoSMTPDaemon
    implements Daemon {

	private static final Logger LOGGER = LogManager.getLogger(NeoSMTPDaemon.class.getName());
	
  MergeSend mergeSend;

  public NeoSMTPDaemon() {
  }

  public void init(DaemonContext context) throws Exception {
    println("DBRecorderDaemon instance: init()");
  }

  public void start() {
    println("NeoSMTPDaemon instance: start(): in");
    String [] args = new String [1];
    args[0] = "12345";
    mergeSend = new MergeSend();
    mergeSend.main(args);

    println("NeoSMTPDaemon instance: start(): out");
  }

  public void stop() throws Exception {
    println("NeoSMTPDaemon instance: stop(): in");

    mergeSend.shutdown();

    println("NeoSMTPDaemon instance: stop(): out");
  }

  public void destroy() {
    println("NeoSMTPDaemon instance: destroy(): in");

    println("NeoSMTPDaemon instance: destroy(): out");
  }

  private String getCurrentTime() {
    java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(
        "yyyy/MM/dd HH:mm:ss", java.util.Locale.US);
    return fmt.format(new java.util.Date());
  }

  private void println(String msg) {
    LOGGER.info(getCurrentTime() + " : " + msg);
  }

}
