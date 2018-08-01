//
// Created by Sim.G on 2018/8/1.
//

#ifndef __COMMON_H__
#define __COMMON_H__

#include <jni.h>
#include "log.h"

int get_version();

jobject get_context(JNIEnv *env, jobject jobj);

char *get_package_name(JNIEnv *env, jobject jobj);

char *str_stitching(const char *str1, const char *str2, const char *str3);

void java_callback(JNIEnv *env, jobject jobj, char *method_name);

void start_service(char *package_name, char *service_name);

#endif
