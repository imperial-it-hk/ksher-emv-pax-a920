#include <jni.h>
#include <string>

extern "C"
jstring
Java_th_co_crie_tron2_android_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
