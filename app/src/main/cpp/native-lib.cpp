#include <jni.h>
#include <stdlib.h>
#include <string>
#include <fcntl.h>
#include <unistd.h>

extern "C" JNIEXPORT jint JNICALL
Java_com_chargercontrol_utils_BatteryControl_executeRoot(JNIEnv* env, jobject obj, jstring command) {
    const char* cmd = env->GetStringUTFChars(command, 0);
    int result = system(cmd); 
    env->ReleaseStringUTFChars(command, cmd);
    return result;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_chargercontrol_utils_BatteryControl_getEngineVersion(JNIEnv* env, jobject obj) {
    return env->NewStringUTF("C++ Native Engine v2.1-Stable");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_chargercontrol_utils_BatteryControl_optimizeKernel(JNIEnv* env, jobject obj, jboolean powerSave) {
    const char* cmd = powerSave ? "su -c 'echo powersave > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor'" 
                               : "su -c 'echo schedutil > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor'";
    return system(cmd);
}
