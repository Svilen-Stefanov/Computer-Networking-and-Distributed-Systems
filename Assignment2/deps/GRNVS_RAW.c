#include <stdio.h>
#include <errno.h>
#include "GRNVS_RAW.h"
#include "raw.h"
#include "hexdump.h"
#include <string.h>

#define max(x, y) (x) > (y) ? (x) : (y)

JNIEXPORT jint JNICALL Java_GRNVS_1RAW_getSocket
	(JNIEnv * env, jobject obj, jstring dev, jint level)
{
	const char * device = (*env)->GetStringUTFChars(env, dev, 0);
	int sock = grnvs_open(device, level);
	(*env)->ReleaseStringUTFChars(env, dev, device);
	if(sock < 0)
		(*env)->ThrowNew(env,
			(*env)->FindClass(env, "java/lang/Exception"),
			strerror(errno));
	return sock;
}

JNIEXPORT jint JNICALL Java_GRNVS_1RAW_write_1
	(JNIEnv * env, jobject obj, jint fd, jbyteArray buffer, jint length)
{
	char buf[1514];
	(*env)->GetByteArrayRegion(env, buffer, 0, length, buf);
	return grnvs_write(fd, buf, length);
}

JNIEXPORT void JNICALL Java_GRNVS_1RAW_hexdump_1
  (JNIEnv * env, jobject obj, jbyteArray buffer, jint length)
{
	char buf[1514];
	(*env)->GetByteArrayRegion(env, buffer, 0, length, buf);
	hexdump(buf, length);
}

JNIEXPORT jint JNICALL Java_GRNVS_1RAW_read_1
	(JNIEnv * env, jobject obj, jint fd, jbyteArray buffer, jobject time)
{
	char buf[1514];
	int length;
	int timeout;
	int * tp = NULL;
	jclass tclass;
	jmethodID get;
	jmethodID set;
	if(time) {
		tclass = (*env)->GetObjectClass(env, time);
		get = (*env)->GetMethodID(env, tclass, "getTimeout", "()I");
		set = (*env)->GetMethodID(env, tclass, "setTimeout", "(I)V");
		if(get == NULL || set == NULL) {
			fprintf(stderr, "Could not get one of the methods\n");
			return -1;
		}
		timeout = (int)(long long)(*env)->CallObjectMethod(env, time, get);
		fprintf(stderr, "Got timeout: %d\n", timeout);
		tp = &timeout;
	}

	memset(buf, 0, sizeof(buf));
	length = max(sizeof(buf), (*env)->GetArrayLength(env, buffer));
	length = grnvs_read(fd, buf, length, tp);
	if(time)
		(*env)->CallObjectMethod(env, time, set, timeout);
	(*env)->SetByteArrayRegion(env, buffer, 0, length, buf);
	return length;
}

JNIEXPORT jint JNICALL Java_GRNVS_1RAW_close_1
	(JNIEnv * env, jobject obj, jint fd)
{
	return grnvs_close(fd);
}

JNIEXPORT jbyteArray JNICALL Java_GRNVS_1RAW_mac_1
	(JNIEnv * env, jobject obj, jint fd)
{
	const unsigned char * mac = grnvs_get_hwaddr(fd);
	jbyteArray result;
	if(!mac)
		return NULL;
	result = (*env)->NewByteArray(env, 6);
	(*env)->SetByteArrayRegion(env, result, 0, 6, mac);
	return result;
}

JNIEXPORT jbyteArray JNICALL Java_GRNVS_1RAW_ip_1
	(JNIEnv * env, jobject obj, jint fd)
{
	const unsigned char * addr;
	struct in_addr ip;
	jbyteArray result;

	ip = grnvs_get_ipaddr(fd);
	addr = (unsigned char *)&ip;
	result = (*env)->NewByteArray(env, 4);
	(*env)->SetByteArrayRegion(env, result, 0, 4, addr);
	return result;
}
