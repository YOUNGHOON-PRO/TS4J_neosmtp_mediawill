package com.sendprocess.util;
/*
* ���� �ڵ� ����
*/

/**
* ���� �ڵ� Ŭ����
* @version 1.0
* @author ymkim
*/
public class ErrorStatusCode
{
//	/*------------------���� ���� --------------*/
//	/**���� ����*/
//	public static String FILE_NOT_FOUND = "FILE_ERROR";
//	/**������ ����*/
//	public static String DOMAIN_NOT_FOUND = "DOMAIN_ERROR";
//	/**SMTP ���� ����*/
//	public static String SMTP_INFO_NOT_VALID = "SMTP_INFO_NOT_VALID_FOUND";
//	/**Config ���� ����*/
//	public static String CFG_INFO_NOT_VALID = "CFG_INFO_NOT_VALID_FOUND";
	
	
	/*------------------���� �޽��� --------------*/
	/**FILE ���� ���� �޽���*/
	public static String FILE_NOT_FOUND_MSG = "������ ���ų� ���ϰ�θ��� Ʋ���ϴ�";
	/**������ ���� ���� �޽���*/
	public static String DOMAIN_NOT_FOUND_MSG = "�����θ��� Ʋ���ų� ���� �������Դϴ�";
	/**SMTP ���� �̻� ���� �޽���*/
	public static String SMTP_INFO_NOT_VALID_MSG = "EML���Ͽ� �ִ� SMTP������ Ʋ�Ƚ��ϴ�.";
	/**Config ���� �̻� ���� �޽���*/
	public static String CFG_INFO_NOT_VALID_MSG = "Config ȯ�� ������ Ʋ���ų� ��ΰ� Ʋ���ϴ�";
	/**Operation ���� �̻� ���� �޽���*/
	public static String OPERATION_INFO_NOT_VALID_MSG = "OPERATION ȯ�� ������ Ʋ���ų� ��ΰ� Ʋ���ϴ�";
	/**Thread ���� �޽���*/
	public static String THREAD_ERROR_MSG = "Thread ������ �߻��߽��ϴ�.";
	/**Socket ���� �޽���*/
	public static String SOCKET_ERROR_MSG = "SOCKET ������ �߻��߽��ϴ�.";
	/**IO ���� �޽���*/
	public static String IO_ERROR_MSG = "IO ������ �߻��߽��ϴ�.";
	/**PROTOCOL ���� �޽���*/
	public static String PROTOCOL_ERROR_MSG = "PROTOCOL ������ �߻��߽��ϴ�.";
	/**�Ϲ����� ���� �޽���*/
	public static String GENERAL_ERROR_MSG = "�Ϲ����� ������ �߻��߽��ϴ�.";
}