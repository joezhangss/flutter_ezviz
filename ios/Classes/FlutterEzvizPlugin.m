#import "FlutterEzvizPlugin.h"
#import <SystemConfiguration/CaptiveNetwork.h>
//#import <EZOpenSDKFramework/EZOpenSDKFramework.h>
//#import "MyEZOpenSDK/include/EZOpenSDK.h"
#import "EZOpenSDK.h"
//#import "MyEZOpenSDK/include/modules/EZProbeDeviceInfo.h"
#import "EZProbeDeviceInfo.h"
#import <NetworkExtension/NEHotspotConfigurationManager.h>
#import <CoreLocation/CoreLocation.h>

@interface FlutterEzvizPlugin ()<CLLocationManagerDelegate>

//@property (nonatomic, assign)FlutterResult result;
@property (nonatomic, strong) CLLocationManager *locationManager;
@property (nonatomic, strong) FlutterResult wifiResult;

@end

@implementation FlutterEzvizPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_ezviz"
            binaryMessenger:[registrar messenger]];
  FlutterEzvizPlugin* instance = [[FlutterEzvizPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
//    NSLog(@"call==%@",call.method);
//    NSLog(@"self.result==%@",self.result);
  if ([@"getPlatformVersion" isEqualToString: call.method])
  {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }
  else if([@"checkDevice" isEqualToString:call.method])
  {
      //查询设备是否存在
//      [self appToSystemSettings];
      [self checkDeviceWithDeviceSerial:call.arguments[0] andDeviceType:call.arguments[1] andResult:result];
  }
  else if([@"connectWifi" isEqualToString: call.method])
  {
      //用wifi跟设备连接
      [self connectWifiWithDeviceSerial:call.arguments[0] andPwd:call.arguments[1] andWifiName:call.arguments[2] andResult:result];
  }
  else if([@"stopWifi" isEqualToString:call.method])
  {
      //停止调用wifi
      [EZOpenSDK stopConfigWifi];
  }
  else if([@"stopApWifiConfig" isEqualToString:call.method])
  {
      //停止ap配网
      [EZOpenSDK stopAPConfigWifi];
  }
  else if([@"bindDevice" isEqualToString:call.method])
  {
      //绑定设备
      [self bindDeviceWithDeviceSerial:call.arguments[0] andVerifyCode:call.arguments[1] andResult:result];
  }
  else if([@"apWifiConfig" isEqualToString:call.method])
  {
      //ap配网
      [self apWifiConfigWithWifiName:call.arguments[3] password:call.arguments[0] deviceSerial:call.arguments[1] verifyCode:call.arguments[2] andResult:result];
  }
  else if([@"getWifiName" isEqualToString:call.method])
  {
      //获取当前的wifi名称
      [self getWifiNameWithResult:result];
  }
  else if([@"switchWifi" isEqualToString:call.method])
  {
      //切换wifi
      [self switchWifiWithWifiName:call.arguments[0] pwd:call.arguments[1] andResult:result];
  }
  else if([@"setDeviceVedioEncrypt" isEqualToString:call.method])
  {
      bool status = NO;
      if([call.arguments[2] isEqualToString:@"YES"]){
          status = YES;
          
      }else{
          status = NO;
      }
      [self setDeviceVedioEncryptWithDeviceSerial:call.arguments[0] verifyCode:call.arguments[1] isOpen:status andResult:result];//
  }
  else {
    result(FlutterMethodNotImplemented);
  }
}

#pragma mark - wifi连接设备
- (void) connectWifiWithDeviceSerial:(NSString *)deviceSerial andPwd:(NSString *)pwd andWifiName:(NSString *)ssid andResult:(FlutterResult)result
{
    
//EZWiFiConfigSmart | EZWiFiConfigWave
    NSLog(@"连接wifi设备。。。%@",deviceSerial);
    [EZOpenSDK startConfigWifi:ssid password:pwd deviceSerial:deviceSerial mode:EZWiFiConfigSmart|EZWiFiConfigWave deviceStatus:^(EZWifiConfigStatus status, NSString *deviceSerial) {
        NSLog(@"status==%ld",(long)status);
        
        if (status == DEVICE_WIFI_CONNECTING)
        {
//            NSLog(@"wifi 连接中。。");
            result(@(1));
//            weakSelf.enState = STATE_NONE;
//            [weakSelf createTimerWithTimeOut:60];
        }
        else if (status == DEVICE_WIFI_CONNECTED)
        {
//            NSLog(@"wifi 已连接。。。");
            result(@(2));
        }
        else if (status == DEVICE_PLATFORM_REGISTED)
        {
            //设备注册平台成功
//            NSLog(@"设备注册平台成功..");
            result(@(3));
        }else if(status == DEVICE_ACCOUNT_BINDED)
        {
            //设备已经绑定账户
//            NSLog(@"设备已经绑定账户..");
            result(@(4));
        }
    }];
//    NSLog(@"ssid==%@",ssid);
}

#pragma mark - 查询摄像头设备是否存在
- (void)checkDeviceWithDeviceSerial:(NSString *)deviceSerial andDeviceType:(NSString *)deviceType andResult:(FlutterResult)result {
    
//    NSLog(@"deviceSerial==%@, deviceType==%@",deviceSerial,deviceType);
    [EZOpenSDK probeDeviceInfo:deviceSerial deviceType:deviceType completion:^(EZProbeDeviceInfo *deviceInfo, NSError *error) {
//        NSLog(@"查询设备完成：%@",deviceInfo);
        NSLog(@"error==%ld",(long)[error code]);
         if (error)
         {
             if (error.code == EZ_HTTPS_DEVICE_OFFLINE_IS_ADDED ||
                 error.code == EZ_HTTPS_DEVICE_ADDED_MYSELF ||
                 error.code == EZ_HTTPS_DEVICE_ONLINE_ADDED )
             {
                 //已添加过此设备
//                 NSLog(@"已添加过此设备");
                 NSDictionary *dict = @{
                     @"deviceStatus": @"2",
                     @"deviceMessage": @"该设备已被添加",
//                     @"defaultPicPath":deviceInfo.defaultPicPath,
//                     @"displayName":deviceInfo.displayName,
//                     @"status": @(deviceInfo.status),
//                     @"subSerial" : deviceInfo.subSerial,
//                     @"supportConnectType": @([self supportTypeWithDeviceInfo:deviceInfo]),
                     @"supportConnectType": [NSString stringWithFormat:@"%d",[self supportTypeWithDeviceInfo:deviceInfo]]
//                     @"supportExt" : deviceInfo.supportExt,
                 };
                 result(dict);
             }
             else if (error.code == EZ_HTTPS_DEVICE_ONLINE_IS_ADDED)
             {
                 //此设备已被别人添加
//                 NSLog(@"此设备已被别人添加");
                 NSDictionary *dict = @{
                     @"deviceStatus": @"3",
                     @"deviceMessage": @"此设备已被别人添加",
                     
                 };
                  result(dict);
             }
             else if (error.code == EZ_HTTPS_DEVICE_OFFLINE_NOT_ADDED ||
                      error.code == EZ_HTTPS_DEVICE_NOT_EXISTS ||
                      error.code == EZ_HTTPS_DEVICE_OFFLINE_IS_ADDED_MYSELF)
             {
//                 NSLog(@"设备不在线,需连接网络");
                 //设备不在线,需连接网络
                 if (deviceInfo)
                 {
                    //根据设备能力选择合适的配网方式
                     NSDictionary *dict = @{
                         @"defaultPicPath":deviceInfo.defaultPicPath,
                         @"displayName":deviceInfo.displayName,
                         @"status": @(deviceInfo.status),
                         @"subSerial" : deviceInfo.subSerial,
                         @"deviceStatus": @"4",
                         @"deviceMessage": @"设备不在线,需连接网络。然后选择连接模式。如：声波配网或者AP配网，smartConfig配网",
//                         @"supportConnectType": @([self supportTypeWithDeviceInfo:deviceInfo]),
                         @"supportConnectType": [NSString stringWithFormat:@"%d",[self supportTypeWithDeviceInfo:deviceInfo]],
                         @"supportExt" : deviceInfo.supportExt,
                     };
                     result(dict);
                 }
                 else
                 {
                    //根据设备闪灯情况选择合适的配网方式
//                     NSLog(@"根据设备闪灯情况选择合适的配网方式");
                     NSDictionary *dict = @{
                         @"deviceStatus": @"4",
                         @"deviceMessage": @"设备不在线,需连接网络，根据设备闪灯情况选择合适的配网方式",
                         @"supportConnectType": @"0",
                     };
                      result(dict);
                     
                 }
             }
             else
             {
                 //查询失败，网络不给力,可进行重试
//                  NSLog(@"查询失败，网络不给力,可进行重试");
                 NSDictionary *dict = @{
                     @"deviceStatus": @"5",
                     @"deviceMessage": @"查询失败，网络不给力,可进行重试",
                     @"supportConnectType": @"0",
                 };
                  result(dict);
             }
         }
         else
         {
                //设备已在线，可进行添加
//             NSLog(@"设备已在线，可进行添加");
             NSDictionary *dict = @{
                 @"defaultPicPath":deviceInfo.defaultPicPath,
                 @"displayName":deviceInfo.displayName,
                 @"status": @(deviceInfo.status),
                 @"subSerial" : deviceInfo.subSerial,
                 @"deviceStatus": @"1",
                 @"deviceMessage": @"设备已在线，可进行添加设备",
//                 @"supportConnectType": @([self supportTypeWithDeviceInfo:deviceInfo]),
                 @"supportConnectType": [NSString stringWithFormat:@"%d",[self supportTypeWithDeviceInfo:deviceInfo]],
                 @"supportExt" : deviceInfo.supportExt,
             };
             result(dict);
        }
//        self.result(@"");
    }];
}

#pragma mark - 判断设备支持的类型
- (int)supportTypeWithDeviceInfo:(EZProbeDeviceInfo *)deviceInfo
{
    int connectType = 0;
    if(deviceInfo.supportWifi == 3){
        //支持smartConfig配网
//        NSLog(@"支持smartConfig配网..");
        connectType = 3;
    }
    
    if(deviceInfo.supportSoundWave == 1){
        //支持声波配网
//        NSLog(@"支持声波配网");
        connectType = 1;
    }
    
    if(deviceInfo.supportAP == 2){
         //支持AP配网
//        NSLog(@"支持AP配网。。");
        connectType = 2;
    }
    
    return connectType;
}

#pragma mark - 绑定设备成功
- (void)bindDeviceWithDeviceSerial:(NSString *)deviceSerial andVerifyCode:(NSString *)verifyCode andResult: (FlutterResult)result
{
//    NSLog(@"开始绑定设备。。。%@,%@",deviceSerial,verifyCode);
    [EZOpenSDK addDevice:deviceSerial verifyCode:verifyCode completion:^(NSError *error) {
        if(error){
            result([NSString stringWithFormat:@"%ld",(long)error.code]);
        }else{
//            NSLog(@"绑定成功");
            result(@"绑定成功！");
        }
//        NSLog(@"error==%@",error);
    }];
}

#pragma mark - 检查设备是否打开了图片加密
- (void)setDeviceVedioEncryptWithDeviceSerial:(NSString *)deviceSerial verifyCode:(NSString *)verifyCode isOpen:(BOOL)isOpen andResult:(FlutterResult)result
{
    [EZOpenSDK setDeviceEncryptStatus:deviceSerial verifyCode:verifyCode encrypt:isOpen completion:^(NSError *error) {
        NSLog(@"error==%@",error);
    }];
}

#pragma mark - ap配网
- (void)apWifiConfigWithWifiName:(NSString *)wifiName password:(NSString *)pwd deviceSerial:(NSString *) deviceSerial verifyCode:(NSString *)  verifyCode andResult: (FlutterResult)result
{
    
//    NSLog(@"这里是ap配网。ssid==%@  pwd=%@  deviceSerial=%@",wifiName,pwd,deviceSerial);
    [EZOpenSDK startAPConfigWifiWithSsid:wifiName password:pwd deviceSerial:deviceSerial verifyCode:verifyCode result:^(BOOL ret) {
        if(ret){
//            NSLog(@"联网成功。。");
            result(@"与摄像头连接网络成功！");
        }else{
//            NSLog(@"与摄像头连接网络失败，请检查您的网络！");
            result(@"与摄像头连接网络失败，请检查您的网络！");
        }
//        [EZOpenSDK stopAPConfigWifi];
    }];
}

#pragma mark - 获取当前wifi名称
- (void)getWifiNameWithResult: (FlutterResult)result
{
    NSString *ssid = @"Not Found";
    NSString* phoneVersion = [[UIDevice currentDevice] systemVersion];
    CGFloat version = [phoneVersion floatValue];
    
    if(version >= 13){
        self.wifiResult = result;
        self.locationManager = [[CLLocationManager alloc] init];
        self.locationManager.delegate = self;

//        NSLog(@"version==%f",version);
        // 如果是iOS13 未开启地理位置权限 需要提示一下
        if ([CLLocationManager authorizationStatus] == kCLAuthorizationStatusNotDetermined)
        {
           
           [self.locationManager requestWhenInUseAuthorization];
            
        }
    }else{
        CFArrayRef myArray = CNCopySupportedInterfaces();
        if (myArray != nil) {
            CFDictionaryRef myDict = CNCopyCurrentNetworkInfo(CFArrayGetValueAtIndex(myArray, 0));
            if (myDict != nil) {
                NSDictionary *dict = (NSDictionary*)CFBridgingRelease(myDict);
                ssid = [dict valueForKey:@"SSID"];
            }
            
        }
        result(ssid);
    }
 
    
    
//    NSLog(@"ssdi==%@",ssid);
    
}


//- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status
//{
//
//}

#pragma mark - 用户选择了是否授权地位的操作
- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status
{
    NSString *ssid = @"Not Found";
//    NSLog(@"status==%d",status);
    switch (status) {
        case kCLAuthorizationStatusAuthorizedWhenInUse:
        case kCLAuthorizationStatusAuthorizedAlways:
            [manager startUpdatingLocation];
//            NSLog(@"用户授权了。。");
            CFArrayRef myArrays = CNCopySupportedInterfaces();
            if (myArrays != nil) {
                CFDictionaryRef myDict = CNCopyCurrentNetworkInfo(CFArrayGetValueAtIndex(myArrays, 0));
                if (myDict != nil) {
                    NSDictionary *dict = (NSDictionary*)CFBridgingRelease(myDict);
                    ssid = [dict valueForKey:@"SSID"];
                }
                
            }
            self.wifiResult(ssid);
//            NSLog(@"222ssid:%@",ssid);
            break;
        case kCLAuthorizationStatusDenied:
            // 用户拒绝使用定位，可在此引导用户开启
//            NSLog(@"用户拒绝使用定位，可在此引导用户开启");
            self.wifiResult(@"未授权");
            break;
        case kCLAuthorizationStatusRestricted:
            // 权限受限，可引导用户开启
//            NSLog(@"权限受限，可引导用户开启");
            self.wifiResult(@"未授权");
            break;
        case kCLAuthorizationStatusNotDetermined:
            // 未选择，在代理方法里，一般不会有这个状态，如果有m，再次发起申请
            self.wifiResult(@"未授权");
            break;
        default:
            self.wifiResult(@"未授权");
            break;
    }

}


#pragma mark - 切换wifi（配合AP配网使用）
- (void)switchWifiWithWifiName:(NSString *)ssid pwd:pwd andResult:(FlutterResult)result
{
    //创建将要连接的wifi配置实例
    if (@available(iOS 11.0, *)) {
//        NSLog(@"ssid==%@, pwd=%@",ssid,pwd);
        NEHotspotConfiguration * hotspotConfig = [[NEHotspotConfiguration alloc] initWithSSID:ssid passphrase:pwd isWEP:NO];
        //开始连接（调用此方法系统会自动弹窗确认）
        [[NEHotspotConfigurationManager sharedManager] applyConfiguration:hotspotConfig completionHandler:^(NSError * _Nullable error) {
//            NSLog(@"切换wifi error==%@",error);
            if(!error){
//                NSLog(@"切换wifi成功。。");
                result(@"success");
            }else{
                result(@"failed");
            }
        }];
    }else {
        // Fallback on earlier versions
        //跳转到wifi设置页面
        [self appToSystemSettings];
        result(@"manualSet");
    }
    
    
}

#pragma mark - 到系统的wifi设置页面
-(void)appToSystemSettings
{
    if([[UIDevice currentDevice].systemVersion doubleValue] < 10.0) {
        if( [[UIApplication sharedApplication]canOpenURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]] )
        {
            [[UIApplication sharedApplication]openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
        }

    }else{// >= iOS10.0
        if( [[UIApplication sharedApplication]canOpenURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]] )
        {
            if (@available(iOS 10.0, *)) {
                [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]options:@{}completionHandler:^(BOOL success) {

                }];
            } else {
                // Fallback on earlier versions
            }

        }

    }

}

@end
