package com.startup;

/**
 * Daemon 을 구동시키는 작업을 한다.
 * 모듈별로 작업종료를 위한 ShutdownListener 의 포트를 다르게 하여 구동한다.
 */

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class Bootstrap {

  private Logger logger = Logger.getLogger("com.startup.Bootstrap");

  public Bootstrap() {
    logger.setLevel(Level.FINE);
    try {
      File logDir = new File("./Bootlog");
      logDir.mkdirs();
      FileHandler handler = new FileHandler(logDir.getAbsolutePath()+"/Bootstrap.log");
      handler.setFormatter(new SimpleFormatter());
      logger.addHandler(handler);
      logger.setUseParentHandlers(false);
    }
    catch (IOException ex) {
      ex.printStackTrace();
      // FileHandler 생성 실패
    }
  }

  public void doStart() throws IOException {
    logger.fine("Bootstrap instance: doStart() : 진입");

    try {
      NeoDaemonLoader cd
          = new NeoDaemonLoader("com.sendprocess.send.NeoSMTPDaemon",Integer.parseInt(System.getProperty("neosmtp.shutdown","3102")));
      cd.start();
    }
    catch (Exception ex) {
      logger.log(Level.SEVERE, "Bootstrap instance: doStart()", ex);
    }

    logger.fine("Bootstrap instance: doStart() : 종료");
  }

  public void doStop() throws IOException {
    logger.fine("Bootstrap instance: doCenterStop() : 진입");

    Socket socket = null;
    PrintWriter pw = null;
    try {
      socket = new Socket("localhost", Integer.parseInt(System.getProperty("neosmtp.shutdown","3102")));
      pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
      pw.println("com.sendprocess.send.NeoSMTPDaemon");
      pw.flush();
    }
    catch (IOException ex) {
      logger.log(Level.SEVERE, "Bootstrap instance: doStop()", ex);
    }
    finally {
      if (pw != null) pw.close();
      if (socket != null) socket.close();
    }

    logger.fine("Bootstrap instance: doStop() : 종료");
  }


  /**
   * Daemon startup of Neocast@Messager v3.0 for The service of windows system
   * @param args String[]
   * @throws Throwable
   */
  public static void main(String[] args) throws Throwable {
    try {
      if (args.length > 0) {
        if (args[0].equals("neosmtp_start")) {
          Bootstrap boot = new Bootstrap();
          boot.doStart();
        }
        else if (args[0].equals("neosmtp_stop")) {
          Bootstrap boot = new Bootstrap();
          boot.doStop();
        }
        else {
          System.out.println("Invalid Paramter: " + args[0]);
          System.exit(1);
        }
      }
      else {
        System.out.println("Parameter not found!!");
        System.exit(2);
      }
    }
    catch (Throwable e) {
      e.printStackTrace(System.out);
    }
  }
}
