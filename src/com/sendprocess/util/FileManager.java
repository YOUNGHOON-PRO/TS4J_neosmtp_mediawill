package com.sendprocess.util;

import java.io.*;
import java.util.*;
import java.text.*;

public class FileManager
{
	public static void mkdir(String dirName)
	{
		File dir = new File( dirName );
		if( !dir.exists() ) {
			dir.mkdirs();
		}
	}

	public static void ignoreDirDelete(File dir)
	{
		if( !dir.exists() ) {
			return;
		}

		String dirPath = dir.getAbsolutePath();
		String fileList[] = dir.list();

		if( fileList.length > 0 )
		{
			File file;
			for( int i = 0 ; i < fileList.length ; i++ )
			{
				file = new File(dirPath + File.separator + fileList[i]);
				if( file.isDirectory() ) {
					ignoreDirDelete(file);
				}
			}
		}
		dir.delete();
	}

	public static boolean isFileExists(String file)
	{
		File fileName = new File( file );
		if( !fileName.exists() ) {
			return false;
		}
		else {
			return true;
		}
	}
}

