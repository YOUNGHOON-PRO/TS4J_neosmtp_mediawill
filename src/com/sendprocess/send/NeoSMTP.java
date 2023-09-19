package com.sendprocess.send;

public final class NeoSMTP
{
	public static String LocalIPAddress;
	public static boolean isPushLog = false;

	public static final int SUCCESS = 1;                   // �߼� ����
	public static final int NETWORK_ERROR = 2;             // ��Ʈ��ũ ����
	public static final int NOTEXIST_DOMAIN = 3;           // ȣ��Ʈ ����
	public static final int NOTEXIST_MAILSERVER = 4;       // ���ϼ��� ����
	public static final int LOCALHOST_MAILSERVER = 5;      // ����ȣ��Ʈ�� ���ϼ����� ����
	public static final int UNKNOWN_ACCOUNT = 6;           // ���� ����
	public static final int USER_STORAGEFULL = 7;          // ����� ������� ����
	public static final int SYSTEM_STORAGEFULL = 8;        // �ý��� ������� ����
	public static final int TRANSACTION_FAILED = 9;        // Ʈ����� ����
	public static final int PROCESSING_ERROR = 10;         // ���μ��� ����
	public static final int MAILBOX_SYNTAXERROR = 11;      // ���Ϲڽ� �������� �Ǵ� ����� ����
	public static final int SERVICE_NOT_AVAILABLE = 12;    // ���� ���� ����
	public static final int MAILBOX_NOT_AVAILABLE = 13;    // ���Ϲڽ� ����
	public static final int PROTOCOL_ERROR = 14;           // �������� ����
	public static final int COMMAND_SYNTAXERROR = 15;      // ����� ���� ����
	public static final int MAILFROM_TEMPFAIL = 16;
	public static final int MAILFROM_UNKNOWNERROR = 17;
	public static final int RCPTTO_TEMPFAIL = 18;
	public static final int RCPTTO_UNKNOWNERROR = 19;
	public static final int DATA_TEMPFAIL = 20;
	public static final int DATA_UNKNOWNERROR = 21;
	public static final int UNKNOWN_ERROR = 22;

	public static final int CONNECT = 0;
	public static final int HELO = 1;
	public static final int MAILFROM = 2;
	public static final int RCPTTO = 3;
	public static final int DATA = 4;
	public static final int DOT = 5;
}
