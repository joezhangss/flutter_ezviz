import 'dart:async';

import 'package:flutter/services.dart';

///萤石云插件
class FlutterEzviz {
  static const int DIRECTION_LEFT = 0;
  static const int DIRECTION_UP = 1;
  static const int DIRECTION_RIGHT = 2;
  static const int DIRECTION_DOWN = 3;
  static const MethodChannel _channel = const MethodChannel('flutter_ezviz');

  ///登录到萤石云
  ///[appKey] 萤石云AppKey
  ///[accessToken] 萤石云AccessToken
  static Future<void> initSDK(String appKey, String accessToken,
      {String apiUrl = "", String webUrl = ""}) async {
    _channel.invokeMethod("initSDK", {
      "appKey": appKey,
      "accessToken": accessToken,
      "apiUrl": apiUrl,
      "webUrl": webUrl
    });
  }

  ///获取设备列表
  static Future<void> getCameraList() async {
    _channel.invokeMethod("cameraList");
  }

  /// 开启设备的实时预览
  ///[deviceSerial] camera对应的设备数字序列号
  ///[cameraNo]  camera在对应设备上的通道号
  static Future<void> startRealPlay(
      String deviceSerial, String cameraNo) async {
    _channel.invokeMethod(
        "startRealPlay", {"deviceSerial": deviceSerial, "cameraNo": cameraNo});
  }

  /// 关闭设备的实时预览
  static Future<void> stopRealPlay() async {
    _channel.invokeMethod("stopRealPlay");
  }

  /// 控制设备方向
  /// [direction] 要旋转的方向
  /// see
  /// [DIRECTION_LEFT]
  /// [DIRECTION_UP]
  /// [DIRECTION_RIGHT]
  /// [DIRECTION_DOWN]
  static Future<String> startDirection(int direction) async {
    return _channel.invokeMethod("startDirection", direction);
  }

  static Future<String> stopDirection(int direction) async {
    return _channel.invokeMethod("stopDirection", direction);
  }

  ///短暂的移动摄像头
  static Future<void> shortDirection(int direction) async {
    _channel.invokeMethod("shortDirection", direction);
  }

  ///打开声音
  static Future<void> openSound() async {
    _channel.invokeMethod("openSound");
  }

  ///关闭声音
  static Future<void> closeSound() async {
    _channel.invokeMethod("closeSound");
  }

  ///释放资源
  static Future<void> release() async {
    _channel.invokeMethod("release");
  }

  ///回调
  static Future<void> onTapCallback() async {
    _channel.setMethodCallHandler((handler) async {
      switch (handler.method) {
        case "onSingleTap":
          print("onSingleTap");
          break;
        case "onDoubleTap":
          print("onDoubleTap");
          break;
      }
    });
  }

  ///获取一个SurfaceView
  static String getSurfaceView() => "com.ujk.flutter_ezviz.SurfaceViewPlatform";
}
