package com.sendprocess.send;

import java.io.*;
import java.util.*;

import com.sendprocess.config.Config;
import com.sendprocess.log.LogJob;
import com.sendprocess.util.ErrorStatusCode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 하루에 한번씩 정기적으로 삭제되지 않은 파일을 체크하여 지워준다.

public class CheckGarbageMail extends Thread
{
	
	private static final Logger LOGGER = LogManager.getLogger(CheckGarbageMail.class.getName());
	
	//하루는 24시간
	private static final int ONE_DAY= 24;

	public CheckGarbageMail()
	{
	}

	public void run()
	{
		runSpecifyTime();
	}

	//특정한 시간에 반복적으로 돌아가도록 처리한다.
	public void runSpecifyTime()
	{
		Calendar cal = Calendar.getInstance();
		//LOGGER.info(cal);
		//현재 날의 그 다음날로 세팅한다.
		cal.add(Calendar.DATE, 1);
		//LOGGER.info(cal);


		java.util.Date dDate = cal.getTime();
		dDate.setHours(Config.GARBAGE_START_TIME);//시작하는 시간을 적용해준다.
		dDate.setMinutes(0);
		dDate.setSeconds(0);


		Timer time = new Timer();
		time.schedule(new TimerTaskJob(), dDate, ONE_DAY*1000*60*60/2);
	}
}

//반복적으로 처리할 일의 내용을 정의한다.
class TimerTaskJob extends TimerTask
{
	private static final Logger LOGGER = LogManager.getLogger(TimerTaskJob.class.getName());
	
	public void run()
	{
		deleteFile();
	}

	//폴더를 체크한다.
	private void deleteFile()
	{
		LOGGER.info("삭제 로직이 돌아간다");
		if( Config.msgNotDeleteMap != null )
		{
			int delSize = Config.msgNotDeleteMap.size();
			for( int i = 0; i < delSize; i++ )
			{
				if( ((File)(Config.msgNotDeleteMap).get(i)).isFile() ) { //파일이 있는 경우
					if( ((File)(Config.msgNotDeleteMap).get(i)).delete() ) { //파일이 삭제가 성공한 경우
						//삭제안된 리스트에서 빼준다.
						Config.msgNotDeleteMap.remove(i);
					}
					else {
						//파일이 삭제가 안된 경우는 그냥... 이 해쉬맵에 남겨준다.
					}
				}
				else //해쉬맵에는 있는데 실제로 파일이 없는경우
				{
					//해쉬맵에서 빼준다.
					Config.msgNotDeleteMap.remove(i);
				}
			}
		}
	}
}