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

  /**
   * 跟设备联网成功后，需要调用stopWifi。
   * 这里是声波连接和wifi连接
   *
   */
  static Future<int> connectWifi(String deviceSerial, String pwd, String wifiName) async{//, String currentDeviceType, int supportType
    /**
     * 1, 设备正在连接WiFi
     * 2, 设备连接WiFi成功
     * 3, 设备注册平台成功
     * 4, 设备已经绑定账户
     *
     */
    return _channel.invokeMethod('connectWifi',[deviceSerial, pwd, wifiName]);
  }

  //停止连接wifi
  static Future<void> stopWifi() async{
    _channel.invokeMethod("stopWifi");
  }


  //检查设备，传入序列号和设备型号
  static Future<Map> checkDevice(String deviceSerial, String deviceType){
    /**
     * 根据设备状态来拿其他数据
     *  NSDictionary *dict = @{
        @"defaultPicPath":deviceInfo.defaultPicPath,
        @"displayName":deviceInfo.displayName,
        @"status": @(deviceInfo.status),
        @"supportExt" : deviceInfo.supportExt,
        @"subSerial" : deviceInfo.subSerial,
        @"deviceStatus": @(1),
        @"deviceMessage": @"",
        @"supportConnectType": 0//0:是根据闪灯来选择配网(红蓝闪烁为普通的配网方式，蓝灯闪烁为AP配网方式)；1：支持声波配网；2：支持AP配网；3：支持smartConfig配网
        };
        deviceStatus:
        //1：设备已在线，可进行添加；
        //2：已添加过此设备；
        //3：此设备已被别人添加；
        //4：查询到设备信息，根据根据设备闪灯情况选择合适的配网方式；
        //5：查询失败，网络不给力,可进行重试：
        //6：根据设备能力选择合适的配网方式
     */
    return _channel.invokeMethod('checkDevice', [deviceSerial, deviceType]);
//    map["defaultPicPath"];
  }

  //绑定设备
  static Future<String> bindDevice(String deviceSerial, String verifyCode){
    return _channel.invokeMethod('bindDevice',[deviceSerial, verifyCode]);
  }
  
  //ap配网,deviceSerial：设备序列号， verifyCode：验证码
  static Future<String> apWifiConfig(String deviceSerial,String verifyCode, String pwd, String wifiName)
  {
    return _channel.invokeMethod('apWifiConfig', [pwd, deviceSerial, verifyCode, wifiName]);
  }

  //停止ap配网
  static Future<void> stopApWifiConfig()
  {
    _channel.invokeMethod('stopApWifiConfig');
  }

  //获取wifi名称
  static Future<String> getWifiName()
  {
    return _channel.invokeMethod('getWifiName');
  }

  //切换wifi
  static Future<String> switchWifi(String wifiName, String wifiPwd) {
    return _channel.invokeMethod('switchWifi',[wifiName, wifiPwd]);
  }

  //设置设备的视频图片是否加密
  static Future<void> setDeviceVedioEncrypt(String verifyCode, String deviceSerial, {bool isOpen = false}){
    String status = "NO";
    if(isOpen){
      status = "YES";
    }else{
      status = "NO";
    }
    _channel.invokeMethod('setDeviceVedioEncrypt', [deviceSerial, verifyCode, status]);
  }


  ///获取一个SurfaceView
  static String getSurfaceView() => "com.ujk.flutter_ezviz.SurfaceViewPlatform";
}
