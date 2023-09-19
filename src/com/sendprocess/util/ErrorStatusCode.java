package com.sendprocess.util;
/*
* 에러 코드 정리
*/

/**
* 에러 코드 클래스
* @version 1.0
* @author ymkim
*/
public class ErrorStatusCode
{
//	/*------------------에러 종류 --------------*/
//	/**파일 에러*/
//	public static String FILE_NOT_FOUND = "FILE_ERROR";
//	/**도메인 에러*/
//	public static String DOMAIN_NOT_FOUND = "DOMAIN_ERROR";
//	/**SMTP 정보 에러*/
//	public static String SMTP_INFO_NOT_VALID = "SMTP_INFO_NOT_VALID_FOUND";
//	/**Config 정보 에러*/
//	public static String CFG_INFO_NOT_VALID = "CFG_INFO_NOT_VALID_FOUND";
	
	
	/*------------------에러 메시지 --------------*/
	/**FILE 없음 에러 메시지*/
	public static String FILE_NOT_FOUND_MSG = "파일이 없거나 파일경로명이 틀립니다";
	/**도메인 없음 에러 메시지*/
	public static String DOMAIN_NOT_FOUND_MSG = "도메인명이 틀리거나 없는 도메인입니다";
	/**SMTP 정보 이상 에러 메시지*/
	public static String SMTP_INFO_NOT_VALID_MSG = "EML파일에 있는 SMTP정보가 틀렸습니다.";
	/**Config 정보 이상 에러 메시지*/
	public static String CFG_INFO_NOT_VALID_MSG = "Config 환경 정보가 틀리거나 경로가 틀립니다";
	/**Operation 정보 이상 에러 메시지*/
	public static String OPERATION_INFO_NOT_VALID_MSG = "OPERATION 환경 정보가 틀리거나 경로가 틀립니다";
	/**Thread 에러 메시지*/
	public static String THREAD_ERROR_MSG = "Thread 에러가 발생했습니다.";
	/**Socket 에러 메시지*/
	public static String SOCKET_ERROR_MSG = "SOCKET 에러가 발생했습니다.";
	/**IO 에러 메시지*/
	public static String IO_ERROR_MSG = "IO 에러가 발생했습니다.";
	/**PROTOCOL 에러 메시지*/
	public static String PROTOCOL_ERROR_MSG = "PROTOCOL 에러가 발생했습니다.";
	/**일반적인 에러 메시지*/
	public static String GENERAL_ERROR_MSG = "일반적인 에러가 발생했습니다.";
}