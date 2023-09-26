package com.queue;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import com.sendprocess.log.LogData;
import com.sendprocess.config.*;
import com.sendprocess.log.LogJob;
import com.sendprocess.util.ErrorStatusCode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MergeQueueDirChecker extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(MergeQueueDirChecker.class.getName());
	
	private final int Merge_Memory_Limit = 1;
	private String mergeQueueDir;
	private long checkQueuePeriod = Operation.Queue_Check_Period * 1000;

	private HashMap msgSubDirMap;

	public MergeQueueDirChecker()
	{
		mergeQueueDir = Config.Merge_Queue_Dir;
		msgSubDirMap = new HashMap();
	}

	public synchronized void run()
	{
		File mergeQueueDirFile = new File(mergeQueueDir);

		while( true )
		{
			if( MergeIndexQueue.getSize() < Merge_Memory_Limit && mergeQueueDirFile.exists())
			{
				String[] messageIDArray = mergeQueueDirFile.list();

				//LOGGER.info(msgSubDirMap);
				// 일단 폴더가 다 지워지면.. 해쉬맵에 내용도 다 지워지도록 해놓았다.
				// (폴더가 지워져도 해쉬맵에 남아있는것이 있는듯하다.
				// 생각해보면 메일이 계속 쌓이면 폴더가 다 지워질 일이 없을거 같다.(ㅠ.ㅠ)
				if( messageIDArray.length == 0 ) {
					msgSubDirMap.clear();
				}

				for( int i = 0; i < messageIDArray.length; i++ )
				{
					String messageID = messageIDArray[i];

					File messageIDDirFile = new File(mergeQueueDirFile, messageID);
					String[] subDirList = messageIDDirFile.list();

					if( subDirList != null )
					{
						for( int j = 0; j < subDirList.length; j++ )
						{
							String subDir = subDirList[j];
							if( subDir.endsWith("$") ) {
								continue;    //현재 발송중인 디렉토리
							}
							if( subDir.endsWith("C") ) {
								continue;    //현재 Contents 생성중인 디렉토리
							}
							if( subDir.endsWith("Z") ) {
								continue;    //현재 압축해제중인 디렉토리
							}

							File subDirFile = new File(messageIDDirFile, subDir);
							if( subDirFile.isDirectory() )
							{
								String[] fileList = subDirFile.list();
								String key = new StringBuffer(messageID)
										.append("^").append(subDir).toString();
								String skey = new StringBuffer(messageID)
										.append(File.separator).append(subDir).toString();

								// messageID가 해쉬맵에 포함되어 있지 않을때
								if( !msgSubDirMap.containsKey(key) )
								{
									//그 안에 파일이 포함되어 있는 경우
									if( fileList.length > 0 )
									{
										LOGGER.info(new StringBuffer("ADD Queue Dir : ").append(skey).toString());
										MergeIndexQueue.addMergeIndexQueueArray(messageID, subDir, fileList);
										msgSubDirMap.put(key, subDir);
									}
									else
									{
										//그 안에 파일이 없다면... 그냥 폴더를 지워버린다.
										LOGGER.info(new StringBuffer("Delete Dir : ").append(skey).toString());

										//안지워지면 지워질때까지 계속 반복한다.
										while( true ) {
											if( subDirFile.delete() ) {
												break;
											}
										}
									}
								}
								else    //messageID가 해쉬맵에 포함되어 있을 경우
								{
									//그 안에 파일이 들어있지 않은 경우
									if( fileList.length == 0 )
									{
										LOGGER.info(new StringBuffer("Delete Dir : ").append(skey).toString());
										subDirFile.delete();
										LOGGER.info(new StringBuffer("Remove Dir : ").append(skey).toString());
										msgSubDirMap.remove(key);
									}
									else	//그 안에 파일이 들어있는 경우
									{
										//LOGGER.info("그 안에 파일은 있는데 해쉬맵안에 존재한다는것은 메일 전송을 아직 하지 않았던가 아니면 삭제가 되지 않았던가이다");
										//LOGGER.info("메시지아이디:"+messageID + File.separatorChar + subDir);
										//msgSubDirMap.remove(messageID + "^" + subDir);
										//해쉬맵에 포함되어 있는데... 그 안에 파일이 들어있다는것은...
										//아직 메일을 전송하지 않았던가.. 아니면 삭제가 되지 않았던가 둘중의 하나다..
										LOGGER.info("추가 ADD Queue Dir : " + messageID + File.separatorChar + subDir);
										//MergeIndexQueue.addMergeIndexQueueArray(messageID, subDir, fileList);
									}
								}
							}
						}//for
					} //if
				} // for
			} //if

			try {
				sleep(checkQueuePeriod);
			}
			catch(InterruptedException e)
			{
				LOGGER.error(e);
				//e.printStackTrace();
				LogJob.errorLog("MergeQueueDirChecker", "run()",
								ErrorStatusCode.THREAD_ERROR_MSG,e.toString());
			}
		} // while
	}
}