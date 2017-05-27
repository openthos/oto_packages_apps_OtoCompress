LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-appcompat

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    $(call all-renderscript-files-under, src) \
    $(call all-proto-files-under, protos) \
    $(call all-subdir-Java-files)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_SDK_VERSION := current

LOCAL_PACKAGE_NAME := OtoCompress
LOCAL_CERTIFICATE := platform

LOCAL_JNI_SHARED_LIBRARIES := libp7zip

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE := libp7zip
LOCAL_SRC_FILES := libs/x86_64/libp7zip.so
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
include $(BUILD_PREBUILT)

