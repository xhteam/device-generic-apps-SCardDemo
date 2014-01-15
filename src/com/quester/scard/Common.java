package com.quester.scard;
import com.quester.android.platform_library.scard.*;

public class Common {
	
	public static final int ACCESS = 1;
	public static final int DONE = 2;

	public static final byte[] SELECT_MF = {(byte)0xa0, (byte)0xa4, 0x00, 0x00, 0x02, 0x3f, 0x00};
	public static final byte[] SELECT_ICCID = {(byte)0xa0, (byte)0xa4, 0x00, 0x00, 0x02, 0x2f, (byte)0xe2};
	public static final byte[] READ_ICCID = {(byte)0xa0, (byte)0xb0, 0x00, 0x00, 0x0a};
	public static final byte[] SELECT_GSM = {(byte)0xa0, (byte)0xa4, 0x00, 0x00, 0x02, 0x7f, 0x20};
	public static final byte[] SELECT_IMSI = {(byte)0xa0, (byte)0xa4, 0x00, 0x00, 0x02, 0x6f, 0x07};
	public static final byte[] READ_IMSI = {(byte)0xa0, (byte)0xb0, 0x00, 0x00, 0x09};
	public static final byte[] SELECT_TELECOM = {(byte)0xa0, (byte)0xa4, 0x00, 0x00, 0x02, 0x7f, 0x10};
	public static final byte[] SELECT_ADN = {(byte)0xa0, (byte)0xa4, 0x00, 0x00, 0x02, 0x6f, 0x3a};
	public static final byte[] SELECT_SMS = {(byte)0xa0, (byte)0xa4, 0x00, 0x00, 0x02, 0x6f, 0x3c};
	
	public static byte[] get_response = {(byte)0xa0, (byte)0xc0, 0x00, 0x00, 0x00};
	public static byte[] read_record = {(byte)0xa0, (byte)0xb2, 0x00, 0x04, 0x00};
	public static byte[] verify_chv1 = {(byte)0xa0, 0x20, 0x00, 0x01, 0x08, 0x31, 0x32, 0x33, 0x34, 
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
	public static int alpha_lgth = 0;
	
	public static SCardManager mgr;
	
	public static void init() {
		mgr = new SCardManager();
	}
	
	public static void printRecv(String tag, byte[] recv, int lgth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lgth; i++) {
			sb.append(String.format(" %02X", recv[i]));
		}
		System.out.println(tag + sb.toString());
	}
	
}
