package com.quester.scard;

public class SCardManager {
	
	/* Smart Card Native Methods */
	
	/**
	 * See winscard.h SCardEstablishCotext(...)
	 */
	native int establishSCardContext(int dwScope);
	
	/**
	 * See winscard.h SCardReleaseContext(...)
	 */
	native int releaseSCardContext();
	
	/**
	 * See winscard.h SCardIsValidContext(...)
	 */
	native int isValidSCardContext();
	
	/**
	 * See winscard.h SCardConnect(...)
	 */
	native int connectSCard(String reader, int dwShareMode, int dwPreferredProtocols);
	
	/**
	 * See winscard.h SCardReconnect(...)
	 */
	native int reconnectSCard(int dwShareMode, int dwPreferredProtocols, int dwInitialization);
	
	/**
	 * See winscard.h SCardDisconnect(...)
	 */
	native int disconnectSCard(int dwDisposition);
	
	/**
	 * See winscard.h SCardBeginTransaction(...)
	 */
	native int beginSCardTransaction();
	
	/**
	 * See winscard.h SCardEndTransaction(...)
	 */
	native int endSCardTransaction(int dwDisposition);
	
	/**
	 * See winscard.h SCardGetStatus(...)
	 */
	native int getSCardStatus(byte[] reader, Integer readerLen, Integer state, 
			Integer prot, byte[] atr, Integer atrLen);
	
	
	/**
	 * See winscard.h SCardGetStatusChange(...)
	 */
	native int getSCardStatusChange(int dwTimeout, String reader, Integer eventState, 
			byte[] atr, Integer atrLen);
	
	/**
	 * See winscard.h SCardControl(...)
	 */
	native int controlSCard(int ctrlCode, byte[] sendBuf, int sendLen, 
			byte[] recvBuf, int recvLen, Integer lpBytesReturned);
	
	/**
	 * See winscard.h SCardTransmit(...)
	 */
	native int transmitSCard(byte[] sendBuf, int sendLen, byte[] recvBuf, Integer recvLen);
	
	/**
	 * See winscard.h SCardListReaderGroups(...)
	 */
	native String[] listSCardReaderGroups(int hGroups);
	
	/**
	 * See winscard.h SCardListReaders(...)
	 */
	native String[] listSCardReaders(String groups, int hReaders);
	
	/**
	 * See winscard.h SCardCancel(...)
	 */
	native int cancelSCard();
	
	/**
	 * See winscard.h SCardGetAttrib(...)
	 */
	native int getSCardAttrib(int attrId, byte[] attr, Integer attrLen);
	
	/**
	 * See winscard.h SCardSetAttrib(...)
	 */
	native int setScardAttrib(int attrId, byte[] attr, int attrLen);
	
	native String getPcscIfyError(int ret);
	/*-----------------------------------------------------------------------------------------*/
	
	public int scardEstablishContext(int dwScope) {
		return establishSCardContext(dwScope);
	}
	
	public int scardReleaseContext() {
		return releaseSCardContext();
	}
	
	public int scardIsValidContext() {
		return isValidSCardContext();
	}
	
	public int scardConnect(String reader, int dwShareMode, int dwPreferredProtocols) {
		return connectSCard(reader, dwShareMode, dwPreferredProtocols);
	}
	
	public int scardReconnect(int dwShareMode, int dwPreferredProtocols, int dwInitialization) {
		return reconnectSCard(dwShareMode, dwPreferredProtocols, dwInitialization);
	}
	
	public int scardDisconnect(int dwDisposition) {
		return disconnectSCard(dwDisposition);
	}
	
	public int scardBeginTransaction() {
		return beginSCardTransaction();
	}
	
	public int scardEndTransaction(int dwDisposition) {
		return endSCardTransaction(dwDisposition);
	}
	
	public int scardGetStatus(byte[] reader, Integer readerLen, Integer state, 
			Integer prot, byte[] atr, Integer atrLen) {
		return getSCardStatus(reader, readerLen, state, prot, atr, atrLen);
	}
	
	public int scardGetStatusChange(int dwTimeout, String reader, Integer eventState, 
			byte[] atr, Integer atrLen) {
		return getSCardStatusChange(dwTimeout, reader, eventState, atr, atrLen);
	}
	
	public int scardControl(int ctrlCode, byte[] sendBuf, int sendLen, 
			byte[] recvBuf, int recvLen, Integer lpBytesReturned) {
		return controlSCard(ctrlCode, sendBuf, sendLen, recvBuf, recvLen, lpBytesReturned);
	}
	
	public int scardTransmit(byte[] sendBuf, int sendLen, byte[] recvBuf, Integer recvLen) {
		return transmitSCard(sendBuf, sendLen, recvBuf, recvLen);
	}
	
	public String[] scardListReaderGroups(int hGroups) {
		return listSCardReaderGroups(hGroups);
	}
	
	public String[] scardListReaders(String groups, int hReaders) {
		return listSCardReaders(groups, hReaders);
	}
	
	public int scardCancel() {
		return cancelSCard();
	}
	
	public int scardGetAttrib(int attrId, byte[] attr, Integer attrLen) {
		return getSCardAttrib(attrId, attr, attrLen);
	}
	
	public int scardSetAttrib(int attrId, byte[] attr, int attrLen) {
		return setScardAttrib(attrId, attr, attrLen);
	}
	
	public String scardPcscIfyError(int ret) {
		return getPcscIfyError(ret);
	}
	/*-----------------------------------------------------------------------------------------*/
	
	static {
		System.loadLibrary("scard");
	}
	
}
