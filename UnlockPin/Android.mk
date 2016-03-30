LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := UnlockPin
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_FLAG_FILES := proguard-project.txt
#LOCAL_PRIVILEGED_MODULE := true
#如果要预置进去可卸载,需要添加以下这行
#LOCAL_MODULE_PATH := $(TARGET_OUT_DATA_APPS)
include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PAT))

