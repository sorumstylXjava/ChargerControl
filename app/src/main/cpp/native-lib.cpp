#include <jni.h>
#include <stdlib.h>
#include <string>

extern "C" JNIEXPORT jint JNICALL
Java_com_chargercontrol_utils_BatteryControl_executeRoot(JNIEnv* env, jobject obj, jstring command) {
    const char* cmd = env->GetStringUTFChars(command, 0);
    int result = system(cmd); 
    env->ReleaseStringUTFChars(command, cmd);
    return result;
}
