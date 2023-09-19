package com.queue;

import java.util.*;
import com.sendprocess.config.Operation;
import com.sendprocess.data.SendData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MergeIndexQueue
{
	private static final Logger LOGGER = LogManager.getLogger(MergeIndexQueue.class.getName());
	
	private static Vector Merge_Memory_Index_QueueVector;
	private static Vector Merge_Memory_Index_RetryVector;

	public static void allocate()
	{
		Merge_Memory_Index_QueueVector = new Vector();
		Merge_Memory_Index_RetryVector = new Vector();
	}

	public synchronized static int getSize() {
		return Merge_Memory_Index_QueueVector.size();
	}

	public synchronized static int getRetrySize() {
		return Merge_Memory_Index_RetryVector.size();
	}

	public synchronized static void addMergeMemoryIndexRetryQueueVector(String index) {
		Merge_Memory_Index_RetryVector.add(index);
	}

	public synchronized static void addMergeIndexQueueArray(String messageID,
			String subDir, String[] array)
	{
		String prefixStr = (new StringBuffer().append(messageID).append(Operation.File_Sep)
							.append(subDir).append(Operation.File_Sep)).toString();
		String suffixStr = new StringBuffer(Operation.File_Sep).append("0").toString(); //retry 0

		for( int i = 0; i < array.length; i++ )
		{
			String fileName = array[i];
			int dotInx = fileName.lastIndexOf('.');
			if( dotInx > 0 )
			{
				String fileInx = fileName.substring(0, dotInx);
				SendData.plusTotalEmail();
				Merge_Memory_Index_QueueVector.add(prefixStr + fileInx + suffixStr);
				//LOGGER.info("Add Queue : " + prefixStr + fileInx + suffixStr);
				
			}
		}
	}

	public synchronized static void addMergeMemoryIndexQueueVector(String index)
	{
		StringTokenizer st = new StringTokenizer(index, Operation.File_Sep);
		String messageID = st.nextToken();
		String subDir = st.nextToken();
		int startMsgNo = Integer.parseInt(st.nextToken());
		int endMsgNo = Integer.parseInt(st.nextToken());

		StringBuffer sb = null;

		for( int i = startMsgNo; i <= endMsgNo; i++ )
		{
			sb = new StringBuffer("");
			sb.append(messageID)
					.append(Operation.File_Sep)
					.append(subDir)
					.append(Operation.File_Sep)
					.append(i)
					.append(Operation.File_Sep)
					.append("0");  //retry 0

			Merge_Memory_Index_QueueVector.add(sb.toString());
			sb.delete(0, sb.length());
		}
	}

	public synchronized static void swapRetryToMergeIndex()
	{
		if( Merge_Memory_Index_RetryVector.size() == 0 ) {
			return;
		}

		Merge_Memory_Index_QueueVector.addAll(Merge_Memory_Index_RetryVector);
		Merge_Memory_Index_RetryVector.removeAllElements();
		Merge_Memory_Index_RetryVector.clear();
	}

	public synchronized static String getMergeMemoryIndexDequeue()
	{
		return (String)Merge_Memory_Index_QueueVector.remove(0);
	}
}