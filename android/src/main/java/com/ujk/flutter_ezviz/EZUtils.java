package com.ujk.flutter_ezviz;

import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;

/**
 * 文件名 EZUtils
 * 创建者  CT
 * 时 间  2019/8/27 13:39
 * TODO
 */
class EZUtils {

    static EZCameraInfo getCameraInfoFromDevice(EZDeviceInfo deviceInfo, int camera_index) {
        if (deviceInfo == null) {
            return null;
        }
        if (deviceInfo.getCameraNum() > 0 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() > camera_index) {
            return deviceInfo.getCameraInfoList().get(camera_index);
        }
        return null;
    }
}
