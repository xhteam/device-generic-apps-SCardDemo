/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "scardJni"
#include "winscard.h"
#include "wintypes.h"
#include "pcsclite.h"
#include "JNIHelp.h"
#include "jni.h"
#include "utils/Log.h"
#include "android_runtime/AndroidRuntime.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

namespace android {

static SCARDCONTEXT hContext;
static SCARDHANDLE hCard;
static DWORD dwActiveProtocol;

static int establishSCardContext(JNIEnv* env, jobject thiz, jint dwScope) {
	LONG ret;
	ret = SCardEstablishContext((DWORD)dwScope, NULL, NULL, &hContext);
	return ret;
}

static int releaseSCardContext(JNIEnv* env, jobject thiz) {
	LONG ret;
	ret = SCardReleaseContext(hContext);
	return ret;
}

static int isValidSCardContext(JNIEnv* env, jobject thiz) {
	LONG ret;
	ret = SCardIsValidContext(hContext);
	return ret;
}

static int connectSCard(JNIEnv* env, jobject thiz, jstring reader, jint dwShareMode, jint dwPreferredProtocols) {
	LONG ret;
	LPCSTR szReader;

	szReader = env->GetStringUTFChars(reader, NULL);
	dwActiveProtocol = -1;

	ret = SCardConnect(hContext, szReader, (DWORD)dwShareMode, (DWORD)dwPreferredProtocols,
			&hCard, &dwActiveProtocol);
	if (szReader) {
		env->ReleaseStringUTFChars(reader, szReader);
	}

	return ret;
}

static int reconnectSCard(JNIEnv* env, jobject thiz, jint dwShareMode, jint dwPreferredProtocols, 
						jint dwInitialization) {
	LONG ret;
	ret = SCardReconnect(hCard, (DWORD)dwShareMode, (DWORD)dwPreferredProtocols,
		(DWORD)dwInitialization, &dwActiveProtocol);
	return ret;
}

static int disconnectSCard(JNIEnv* env, jobject thiz, jint dwDisposition) {
	LONG ret;
	ret = SCardDisconnect(hCard, (DWORD)dwDisposition);
	return ret;
}

static int beginSCardTransaction(JNIEnv* env, jobject thiz) {
	LONG ret;
	ret = SCardBeginTransaction(hCard);
	return ret;
}

static int endSCardTransaction(JNIEnv* env, jobject thiz, jint dwDisposition) {
	LONG ret;
	ret = SCardEndTransaction(hCard, (DWORD)dwDisposition);
	return ret;
}

static int getSCardStatus(JNIEnv* env, jobject thiz, jbyteArray reader, jobject readerLen, jobject state, 
						jobject prot, jbyteArray atr, jobject atrLen) {
	LONG ret;
	DWORD pcchReaderLen, dwState, dwProt, cbAtrLen;
	jclass clazzInt;
	jfieldID fieldId;

	clazzInt = env->FindClass("java/lang/Integer");
	if (NULL == clazzInt) {
		ALOGE("SCardGetStatus: FindClass failed");
		return -1;
	}
	fieldId = env->GetFieldID(clazzInt, "value", "I");
	if (NULL == fieldId) {
		ALOGE("SCardGetStatus: GetFieldID failed");
		return -1;
	}

	LPSTR mszReaderName = (LPSTR)env->GetByteArrayElements(reader, NULL);
	LPBYTE pbAtr = (LPBYTE)env->GetByteArrayElements(atr, NULL);
	pcchReaderLen = (DWORD)env->GetIntField(readerLen, fieldId);
	cbAtrLen = (DWORD)env->GetIntField(atrLen, fieldId);

	ret = SCardStatus(hCard, mszReaderName, &pcchReaderLen, &dwState, &dwProt, pbAtr, &cbAtrLen);
	env->ReleaseByteArrayElements(reader, (jbyte*)mszReaderName, JNI_COMMIT);
	env->SetIntField(readerLen, fieldId, pcchReaderLen);
	env->SetIntField(state, fieldId, dwState);
	env->SetIntField(prot, fieldId, dwProt);
	env->SetIntField(atrLen, fieldId, cbAtrLen);
	env->ReleaseByteArrayElements(atr, (jbyte*)pbAtr, JNI_COMMIT);

	return ret;
}

static int getSCardStatusChange(JNIEnv* env, jobject thiz, jint dwTimeout, jstring reader, jobject eventState, 
								jbyteArray atr, jobject atrLen) {
	LONG ret;
	SCARD_READERSTATE rgReaderStates[1];
	jclass clazzInt;
	jfieldID fieldId;

	clazzInt = env->FindClass("java/lang/Integer");
	if (NULL == clazzInt) {
		ALOGE("SCardGetStatusChange: FindClass failed");
		return -1;
	}
	fieldId = env->GetFieldID(clazzInt, "value", "I");
	if (NULL == fieldId) {
		ALOGE("SCardGetStatusChange: GetFieldID failed");
		return -1;
	}

	LPCSTR szReader = (LPCSTR)env->GetStringUTFChars(reader, NULL);
	LPBYTE rgbAtr = (LPBYTE)env->GetByteArrayElements(atr, NULL);
	rgReaderStates[0].szReader = szReader;
	rgReaderStates[0].dwCurrentState = SCARD_STATE_UNAWARE;

	ret = SCardGetStatusChange(hContext, (DWORD)dwTimeout, rgReaderStates, 1);
	if (szReader) {
		env->ReleaseStringUTFChars(reader, szReader);
	}
	env->SetIntField(eventState, fieldId, rgReaderStates[0].dwEventState);
	env->ReleaseByteArrayElements(atr, (jbyte*)rgbAtr, JNI_COMMIT);
	env->SetIntField(atrLen, fieldId, rgReaderStates[0].cbAtr);

	return ret;
}

static int controlSCard(JNIEnv* env, jobject thiz, jint dwControlCode, jbyteArray sendBuffer, jint sendLen, 
						jbyteArray recvBuffer, jint recvLen, jobject lpBytesReturned) {
	LONG ret;
	DWORD lp;
	jclass clazzInt;
	jfieldID fieldId;

	clazzInt = env->FindClass("java/lang/Integer");
	if (NULL == clazzInt) {
		ALOGE("SCardControl: FindClass failed");
		return -1;
	}
	fieldId = env->GetFieldID(clazzInt, "value", "I");
	if (NULL == fieldId) {
		ALOGE("SCardControl: GetFieldID failed");
		return -1;
	}

	LPBYTE pbSendBuffer = (LPBYTE)env->GetByteArrayElements(sendBuffer, NULL);
	LPBYTE pbRecvBuffer = (LPBYTE)env->GetByteArrayElements(recvBuffer, NULL);

	ret = SCardControl(hCard, (DWORD)dwControlCode, pbSendBuffer, (DWORD)sendLen,
		pbRecvBuffer, (DWORD)recvLen, &lp);
	env->ReleaseByteArrayElements(sendBuffer, (jbyte*)pbSendBuffer, JNI_ABORT);
	env->ReleaseByteArrayElements(recvBuffer, (jbyte*)pbRecvBuffer, JNI_COMMIT);
	env->SetIntField(lpBytesReturned, fieldId, lp);

	return ret;
}

static int transmitSCard(JNIEnv* env, jobject thiz, jbyteArray sendBuffer, jint sendLen, jbyteArray recvBuffer, 
						jobject recvLen) {
	LONG ret;
	DWORD cbRecvLen;
	const SCARD_IO_REQUEST* pioSendPci;
	SCARD_IO_REQUEST pioRecvPci;
	jclass clazzInt;
	jfieldID fieldId;

	clazzInt = env->FindClass("java/lang/Integer");
	if (NULL == clazzInt) {
		ALOGE("SCardTransmit: FindClass failed");
		return -1;
	}
	fieldId = env->GetFieldID(clazzInt, "value", "I");
	if (NULL == fieldId) {
		ALOGE("SCardTransmit: GetFieldID failed");
		return -1;
	}

	switch (dwActiveProtocol) {
	case SCARD_PROTOCOL_T0:
		pioSendPci = SCARD_PCI_T0;
		break;
	case SCARD_PROTOCOL_T1:
		pioSendPci = SCARD_PCI_T1;
		break;
	default:
		return -1;
	}

	LPBYTE pbSendBuffer = (LPBYTE)env->GetByteArrayElements(sendBuffer, NULL);
	LPBYTE pbRecvBuffer = (LPBYTE)env->GetByteArrayElements(recvBuffer, NULL);
	cbRecvLen = (DWORD)env->GetIntField(recvLen, fieldId);

	ret = SCardTransmit(hCard, pioSendPci, pbSendBuffer, (DWORD)sendLen,
			&pioRecvPci, pbRecvBuffer, &cbRecvLen);
	env->ReleaseByteArrayElements(sendBuffer, (jbyte*)pbSendBuffer, JNI_ABORT);
	env->ReleaseByteArrayElements(recvBuffer, (jbyte*)pbRecvBuffer, JNI_COMMIT);
	env->SetIntField(recvLen, fieldId, cbRecvLen);

	return ret;
}

static jobjectArray listSCardReaderGroups(JNIEnv* env, jobject thiz, jint hGroups) {
	LONG ret;
	DWORD cchGroups;
	LPSTR ptr;
	LPSTR mszGroups;
	int nbGroups;

	jclass clazzString;
	jmethodID methodId;
	jobject jgroup;
	jobjectArray jgroups;

	clazzString = env->FindClass("java/lang/String");
	if (NULL == clazzString) {
		ALOGE("SCardListReaderGroups: FindClass failed");
		return NULL;
	}
	methodId = env->GetMethodID(clazzString, "<init>", "()V");
	if(NULL == methodId) {
		ALOGE("SCardListReaderGroups: GetMethodID failed");
		return NULL;
	}

	cchGroups = (DWORD)hGroups;

	ret = SCardListReaderGroups(hContext, (LPSTR)&mszGroups, &cchGroups);
	if (ret != SCARD_S_SUCCESS) {
		ALOGE("SCardListReaderGroups: %s (0x%lX)", pcsc_stringify_error(ret), ret);
		if (mszGroups) {
			SCardFreeMemory(hContext, mszGroups);
		}
		return NULL;
	}

	nbGroups = 0;
	ptr = mszGroups;
	while (*ptr != '\0') {
		ptr += strlen(ptr) + 1;
		nbGroups++;
	}
	if (nbGroups == 0) {
		ALOGE("SCardListReaderGroups: No reader found");
		if (mszGroups) {
			SCardFreeMemory(hContext, mszGroups);
		}
		return NULL;
	}

	jgroup = env->NewObject(clazzString, methodId);
	jgroups = env->NewObjectArray(nbGroups, clazzString, jgroup);

	nbGroups = 0;
	ptr = mszGroups;
	while (*ptr != '\0')
	{
		env->SetObjectArrayElement(jgroups, nbGroups, (jobject)env->NewStringUTF(ptr));
		ptr += strlen(ptr)+1;
		nbGroups++;
	}

	SCardFreeMemory(hContext, mszGroups);
	return jgroups;
}

static jobjectArray listSCardReaders(JNIEnv* env, jobject thiz, jstring groups, jint hReaders) {
	LONG ret;
	LPCSTR mszGroups;
	DWORD cchReaders;
	LPSTR ptr;
	LPSTR mszReaders;
	int nbReaders;

	jclass clazzString;
	jmethodID methodId;
	jobject jreader;
	jobjectArray jreaders;

	clazzString = env->FindClass("java/lang/String");
	if (NULL == clazzString) {
		ALOGE("SCardListReaders: FindClass failed");
		return NULL;
	}
	methodId = env->GetMethodID(clazzString, "<init>", "()V");
	if(NULL == methodId) {
		ALOGE("SCardListReaders: GetMethodID failed");
		return NULL;
	}

	mszGroups = (LPCSTR)env->GetStringUTFChars(groups, NULL);
	cchReaders = (DWORD)hReaders;

	ret = SCardListReaders(hContext, mszGroups, (LPSTR)&mszReaders, &cchReaders);
	if (mszGroups) {
		env->ReleaseStringUTFChars(groups, mszGroups);
	}
	if (ret != SCARD_S_SUCCESS) {
		ALOGE("SCardListReaders: %s (0x%lX)", pcsc_stringify_error(ret), ret);
		if (mszReaders) {
			SCardFreeMemory(hContext, mszReaders);
		}
		return NULL;
	}

	nbReaders = 0;
	ptr = mszReaders;
	while (*ptr != '\0') {
		ptr += strlen(ptr) + 1;
		nbReaders++;
	}
	if (nbReaders == 0) {
		ALOGE("SCardListReaders: No reader found");
		if (mszReaders) {
			SCardFreeMemory(hContext, mszReaders);
		}
		return NULL;
	}

	jreader = env->NewObject(clazzString, methodId);
	jreaders = env->NewObjectArray(nbReaders, clazzString, jreader);

	nbReaders = 0;
	ptr = mszReaders;
	while (*ptr != '\0')
	{
		env->SetObjectArrayElement(jreaders, nbReaders, (jobject)env->NewStringUTF(ptr));
		ptr += strlen(ptr)+1;
		nbReaders++;
	}

	SCardFreeMemory(hContext, mszReaders);
	return jreaders;
}

static int cancelSCard(JNIEnv* env, jobject thiz) {
	LONG ret;
	ret = SCardCancel(hContext);
	return ret;
}

static int getSCardAttrib(JNIEnv* env, jobject thiz, jint attrId, jbyteArray attr, jobject attrLen) {
	LONG ret;
	DWORD cbAttrLen;
	jclass clazzInt;
	jfieldID fieldId;

	clazzInt = env->FindClass("java/lang/Integer");
	if (clazzInt == NULL) {
		ALOGE("SCardGetAttrib: FindClass failed");
		return -1;
	}
	fieldId = env->GetFieldID(clazzInt, "value", "I");
	if (fieldId == NULL) {
		ALOGE("SCardGetAttrib: GetFieldID failed");
		return -1;
	}

	LPBYTE pbAttr = (LPBYTE)env->GetByteArrayElements(attr, NULL);
	cbAttrLen = (DWORD)env->GetIntField(attrLen, fieldId);

	ret = SCardGetAttrib(hCard, attrId, pbAttr, &cbAttrLen);
	env->ReleaseByteArrayElements(attr, (jbyte*)pbAttr, JNI_COMMIT);
	env->SetIntField(attrLen, fieldId, cbAttrLen);

	return ret;
}

static int setScardAttrib(JNIEnv* env, jobject thiz, jint attrId, jbyteArray attr, jint attrLen) {
	LONG ret;
	LPCBYTE pbAttr = (LPCBYTE)env->GetByteArrayElements(attr, NULL);
	ret = SCardSetAttrib(hCard, attrId, pbAttr, attrLen);
	env->ReleaseByteArrayElements(attr, (jbyte*)pbAttr, JNI_ABORT);
	return ret;
}

static jstring getPcscIfyError(JNIEnv* env, jobject thiz, jint ret) {
	LPSTR stringify;
	stringify = pcsc_stringify_error(ret);
	return (env->NewStringUTF(stringify));
}

static JNINativeMethod sMethods[] = {
		{"establishSCardContext", "(I)I", (void*) establishSCardContext},
		{"releaseSCardContext", "()I", (void*) releaseSCardContext},
		{"isValidSCardContext", "()I", (void*) isValidSCardContext},
		{"connectSCard", "(Ljava/lang/String;II)I", (void*) connectSCard},
		{"reconnectSCard", "(III)I", (void*) reconnectSCard},
		{"disconnectSCard", "(I)I", (void*) disconnectSCard},
		{"beginSCardTransaction", "()I", (void*) beginSCardTransaction},
		{"endSCardTransaction", "(I)I", (void*) endSCardTransaction},
		{"getSCardStatus", "([BLjava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;[BLjava/lang/Integer;)I",
			(void*) getSCardStatus},
		{"getSCardStatusChange", "(ILjava/lang/String;Ljava/lang/Integer;[BLjava/lang/Integer;)I",
			(void*) getSCardStatusChange},
		{"controlSCard", "(I[BI[BILjava/lang/Integer;)I", (void*) controlSCard},
		{"transmitSCard", "([BI[BLjava/lang/Integer;)I", (void*) transmitSCard},
		{"listSCardReaderGroups", "(I)[Ljava/lang/String;", (void*) listSCardReaderGroups},
		{"listSCardReaders", "(Ljava/lang/String;I)[Ljava/lang/String;", (void*) listSCardReaders},
		{"cancelSCard", "()I", (void*) cancelSCard},
		{"getSCardAttrib", "(I[BLjava/lang/Integer;)I", (void*) getSCardAttrib},
		{"setScardAttrib", "(I[BI)I", (void*) setScardAttrib},
		{"getPcscIfyError", "(I)Ljava/lang/String;", (void*) getPcscIfyError},
};

int register_com_quester_demo_scard_SCardManager(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "com/quester/scard/SCardManager",
                                    sMethods, NELEM(sMethods));
}

}/* namespace android */

/*
 * JNI Initialization
 */
jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
   JNIEnv *e;
   int status;

   ALOGV("SCardManager : loading JNI\n");

   // Check JNI version
   if(jvm->GetEnv((void **)&e, JNI_VERSION_1_6)) {
       ALOGE("JNI version mismatch error");
      return JNI_ERR;
   }

   if ((status = android::register_com_quester_demo_scard_SCardManager(e)) < 0) {
       ALOGE("jni scard manager registration failure, status: %d", status);
      return JNI_ERR;
   }

   return JNI_VERSION_1_6;
}
