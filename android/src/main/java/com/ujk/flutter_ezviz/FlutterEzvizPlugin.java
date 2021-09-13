package com.ujk.flutter_ezviz;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.videogo.debug.TestParams;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.EzvizAPI;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZProbeDeviceInfoResult;
import com.videogo.util.LogUtil;
import com.videogo.wificonfig.APWifiConfig;
import com.videogo.wificonfig.ConfigWifiErrorEnum;


import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.content.Context.WIFI_SERVICE;
import io.flutter.embedding.engine.plugins.FlutterPlugin;


/**
 * FlutterEzvizPlugin
 */
public class FlutterEzvizPlugin implements MethodCallHandler, FlutterPlugin, ActivityAware {

    private static final String API_URL = "https://open.ys7.com";
    private static final String WEB_URL = "https://openauth.ys7.com";

    private static final String TAG = "AutoWifiConnectingActivity";
//    private static Registrar registrar;
    private static MethodChannel channel;
    private Activity activity;
    private Application app;
    private Intent configParam;
    //    private Map<String, Object> maps;
    private static SurfaceViewPlatformFactory factory;
    private EZProbeDeviceInfoResult mEZProbeDeviceInfo = null;
    private  SurfaceView surfaceView;
    private  SurfaceHolder holder;
    private String serialNo;
    private EZPlayer mPlayer;
    private String deviceType;
    private String pwd;
    private String wifiName;
    private String verifyCode;
    private boolean encrypt;
    private Result result;
    private Thread mCurrentThread = null;
    private String deviceSerial;
    private int cameraNo;
    private int successStatus = 0;
    private int wifi_count = 0;
    private boolean add;
    private String errorCode = "0";
    private String apMsg = "";
    private Timer overTimeTimer;
    private static final int MSG_ADD_CAMERA_SUCCESS = 10;

    private static final int MSG_ADD_CAMERA_FAIL = 12;

    private static final int STATUS_WIFI_CONNETCTING = 100;

    private static final int STATUS_REGISTING = 101;

    private static final int STATUS_ADDING_CAMERA = 102;

    private static final int STATUS_ADD_CAMERA_SUCCESS = 103;

    private final static int MSG_OPEN_CLOUD_STORYED_SUCCESS = 104;

    private final static int MSG_OPEN_CLOUD_STORYED_FAIL = 105;

    private static final int ERROR_WIFI_CONNECT = 1000;

    private static final int ERROR_REGIST = 1001;

    private static final int ERROR_ADD_CAMERA = 1002;
    private static final int MAX_TIME_STEP_ONE_WIFI = 60;
    private static final int MAX_TIME_STEP_TWO_REGIST = 60;
    private static final int MAX_TIME_STEP_THREE_ADD = 15;
    private int searchErrorCode = 0;
    private int addCameraError = -1;
    private boolean isWifiConnected = false;
    private boolean isPlatConnected = false;
    private int errorStep = 0;

    private boolean isPlatBonjourget = false;
    private boolean isWifiOkBonjourget = false;
    private int fromPage;

    private boolean isSupportNetWork;

    private boolean isSupportWifi;

    private long t1 = 0;
    private long t2 = 0;
    private long t3 = 0;
    private long t4 = 0;
    private long t5 = 0;
    private boolean isLineConnecting;
    private static int ADD_CAMERA_TIMES = 3;
    public static final int FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY = 1;
    private boolean isUnbindDeviceError = false;
    private String mac;
    //线程池
    private ExecutorService service;

    private synchronized void stopWifiConfigOnThread() {

        // Stop configuration, stop bonjour service
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                EZOpenSDK.getInstance().stopConfigWiFi();
                LogUtil.debugLog(TAG,
                        "stopBonjourOnThread .cost time = " + (System.currentTimeMillis() - startTime) + "ms");
            }
        }).start();
        LogUtil.debugLog(TAG, "stopBonjourOnThread ..................");
    }

    private void stopApWifiConfig() {

        EZOpenSDK.getInstance().stopAPConfigWifiWithSsid();
    }

//    private FlutterEzvizPlugin(Activity activity) {
//        this.activity = activity;
//
//    }

    /**
     * Plugin registration.
     */
//    public static void registerWith(Registrar registrar) {
//
//        MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_ezviz");
////        channel.setMethodCallHandler(new FlutterEzvizPlugin());
//        FlutterEzvizPlugin.registrar = registrar;
//        FlutterEzvizPlugin.channel = channel;
//
//        factory = new SurfaceViewPlatformFactory();
//
//        registrar.platformViewRegistry().registerViewFactory("com.ujk.flutter_ezviz.SurfaceViewPlatform", factory);
////        channel = new MethodChannel(registrar.messenger(), "com.ujk.flutter_ezviz.SurfaceViewPlatform");
//        FlutterEzvizPlugin instance = new FlutterEzvizPlugin(registrar.activity());
//        channel.setMethodCallHandler(instance);
//
//    }

    @Override
    public void onMethodCall(MethodCall call, Result results) {

        if (call == null)
            return;

        switch (call.method) {
            //初始化萤石云SDK
            case "initSDK":
                Map<String, String> mapSDk = call.arguments();
                if (mapSDk == null)
                    return;

                initSDK(mapSDk.get("appKey"), mapSDk.get("accessToken"), mapSDk.get("apiUrl"), mapSDk.get("webUrl"));
                break;
            //查询设备信息
            case "checkDevice":
                List<String> list = call.arguments();
                this.result = results;
                this.deviceSerial = list.get(0);
                this.deviceType = list.get(1);
                checkDevice(this.result);
                break;
            case "getWifiName":
                this.result = results;
                getConnectWifiSsid(this.result);
                break;
            case "connectWifi":
                List<String> params = call.arguments();
                this.result = results;
                this.deviceSerial = params.get(0);
                this.pwd = params.get(1);
                this.wifiName = params.get(2);
                startWifi(this.result);
                break;
            case "stopWifi":
                stopWifiConfigOnThread();
                break;
            case "stopApWifiConfig":
                stopApWifiConfig();
                break;
            case "switchWifi":
                this.result = results;
                List<String> wifi = call.arguments();
                String wifiName = wifi.get(0);
                String wifiPassword = wifi.get(1);
                switchWifi(this.result, wifiName, wifiPassword);
                break;
            case "apWifiConfig":
                this.result = results;
                List<String> zwifi = call.arguments();
                String zpwd = zwifi.get(0);
                String zdeviceSerial = zwifi.get(1);
                String zverifyCode = zwifi.get(2);
                String zwifiName = zwifi.get(3);
                apWifiConfig(this.result, zpwd, zdeviceSerial, zverifyCode, zwifiName);
                break;
            case "bindDevice":
                List<String> bind = call.arguments();
                this.result = results;
                this.deviceSerial = bind.get(0);
                this.verifyCode = bind.get(1);
                bindDevice(this.deviceSerial, this.verifyCode, this.result);
                break;
            case "setDeviceVedioEncrypt":
                List<String> encrypts = call.arguments();
                this.result = results;
                this.deviceSerial = encrypts.get(0);
                this.verifyCode = encrypts.get(1);
                this.encrypt = Boolean.parseBoolean(String.valueOf(encrypts.get(2)=="YES"?true:false));
                System.out.println("encrypt:"+encrypt);
                new setDeviceVedioEncrypt().execute(true);
//                setDeviceVedioEncrypt(this.result);
                break;
            case "cameraList":

                getCameraList();
                break;
            //通过deviceSerial 和 cameraNo 构造EZPlayer对象 并开启实时预览
            case "startRealPlay":
                Map<String, String> mapDevice = call.arguments();
                if (mapDevice == null)
                    return;
                String noStr = mapDevice.get("cameraNo");
                if (TextUtils.isEmpty(noStr))
                    return;

                createPlayer(mapDevice.get("deviceSerial"), Integer.valueOf(noStr), mapDevice.get("validateCode"));
//                startRealPlay(mapDevice.get("deviceSerial"), Integer.valueOf(noStr));
                break;
            //停止实时预览
            case "stopRealPlay":
                stopRealPlay();
                break;
            //开始旋转摄像头
            case "startDirection":
                int startDirection = call.arguments();
                directionController(0, startDirection);
                break;
            //停止旋转摄像头
            case "stopDirection":
                int stopDirection = call.arguments();
                directionController(1, stopDirection);
                break;
            case "shortDirection":
                int shortDirection = call.arguments();
                directionController(2, shortDirection);
                break;
            case "openSound":
                if (mPlayer != null) {
                    mPlayer.openSound();
                }
                break;
            case "closeSound":
                if (mPlayer != null) {
                    mPlayer.closeSound();
                }
                break;
            //释放资源
            case "release":
                release();
                break;
            default:
                result.notImplemented();
        }

    }

    private void apWifiConfig(final Result results, String pwd, String deviceSerial, String verifyCode, String wifiName) {
        EZOpenSDK.getInstance().startAPConfigWifiWithSsid(wifiName, pwd, deviceSerial, verifyCode, new APWifiConfig.APConfigCallback() {

            @Override
            public void onSuccess() {
                EZOpenSDK.getInstance().stopAPConfigWifiWithSsid();
                apMsg = "与摄像头连接网络成功！";
                System.out.println("apMsg0:"+apMsg);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                results.success(apMsg);
                            }
                        });
                    }
                }).start();

            }

            @Override
            public void OnError(int code) {
                EZOpenSDK.getInstance().stopAPConfigWifiWithSsid();
                switch (code) {
                    case 15:
                        // TODO: 2018/7/24 超时
                        apMsg = "超时";
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        results.success(apMsg);
                                    }
                                });
                            }
                        }).start();
                        break;
                    case 1:
                        // TODO: 2018/7/24 参数错误
                        apMsg = "参数错误";
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        results.success(apMsg);
                                    }
                                });
                            }
                        }).start();
                        break;
                    case 2:
                        // TODO: 2018/7/24 设备ap热点密码错误
                        apMsg = "设备ap热点密码错误";
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        results.success(apMsg);
                                    }
                                });
                            }
                        }).start();
                        break;
                    case 3:
                        // TODO: 2018/7/24  连接ap热点异常
                        apMsg = "连接ap热点异常";
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        results.success(apMsg);
                                    }
                                });
                            }
                        }).start();
                        break;
                    case 4:
                        // TODO: 2018/7/24 搜索WiFi热点错误
                        apMsg = "搜索WiFi热点错误";
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        results.success(apMsg);
                                    }
                                });
                            }
                        }).start();
                        break;
                    default:
                        // TODO: 2018/7/24 未知错误
                        apMsg = "未知错误";
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        results.success(apMsg);
                                    }
                                });
                            }
                        }).start();
                        break;

                }
            }

            @Override
            public void onErrorNew(ConfigWifiErrorEnum configWifiErrorEnum) {

            }
        });

    }

    private void switchWifi(Result results, String wifiName, String wifiPwd) {
        WifiUtil.getIns().init(activity);
        boolean res = WifiUtil.getIns().changeToWifi(wifiName, wifiPwd);
        if(res){
            results.success("success");
        }
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "flutter_ezviz");

        factory = new SurfaceViewPlatformFactory();
        binding.getPlatformViewRegistry().registerViewFactory("com.ujk.flutter_ezviz.SurfaceViewPlatform", factory);

//        FlutterEzvizPlugin instance = new FlutterEzvizPlugin(activity);
        channel.setMethodCallHandler(this);
        app = (Application)binding.getApplicationContext();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    private class setDeviceVedioEncrypt extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            try {
                System.out.println("encrypt:"+encrypt);
                EZOpenSDK.getInstance().setDeviceEncryptStatus(deviceSerial, verifyCode, encrypt);
                return true;
            } catch (BaseException e) {
                e.printStackTrace();
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            System.out.println("result:" + result);
        }
    }

    private void bindDevice(final String deviceNo, final String code, final Result results) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("deviceSerial:" + deviceNo);
                    System.out.println("verifyCode:" + code);
                    add = EZOpenSDK.getInstance().addDevice(deviceNo, code);
                } catch (BaseException e) {
                    errorCode = String.valueOf(e.getErrorCode());
                    e.printStackTrace();

                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        results.success("绑定成功！");
//                        Thread.currentThread().interrupt();
                        if (add) {
                            results.success("绑定成功！");
                            Thread.currentThread().interrupt();
                        }else{
                            if (!errorCode.equals("0")) {
                                System.out.println(errorCode);
                                results.success(errorCode);
                                Thread.currentThread().interrupt();
                            }
                        }

                    }
                });

            }
        }).start();

    }
    private void cancelOvertimeTimer() {
        LogUtil.i(TAG, "Enter cancelOvertimeTimer: ");
        if (overTimeTimer != null) {
            LogUtil.i(TAG, " cancelOvertimeTimer: " + overTimeTimer);
            overTimeTimer.cancel();
        }
    }
    private static class DeviceOnlineStatusMonitor{

        private static EZOpenSDKListener.EZStartConfigWifiCallback mCallback = null;
        private static String mDeviceSerial = null;
        private static boolean isMonitoring = false;
        private static Thread mCurrentThread = null;

        static void start(final String deviceSerial, EZOpenSDKListener.EZStartConfigWifiCallback callback){
            if (isMonitoring){
                return;
            }
            LogUtil.d(TAG, "start to monitor device status");
            isMonitoring = true;
            mCallback = callback;
            mDeviceSerial = deviceSerial;
            mCurrentThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isMonitoring){
                        boolean isOnline =false;
                        EZProbeDeviceInfoResult result = EZOpenSDK.getInstance().probeDeviceInfo(mDeviceSerial,null);
                        // online && not added by anyone
                        if (result.getBaseException() == null){
                            isOnline = true;
                            // online && added by current account
                        }else if(result.getBaseException().getErrorCode() == 120020){
                            isOnline = true;
                        }
                        LogUtil.d(TAG, "device is online? " + isOnline);
                        if (isOnline){
                            mCallback.onStartConfigWifiCallback(deviceSerial, EZConstants.EZWifiConfigStatus.DEVICE_PLATFORM_REGISTED);
                            stop();
                        }else{
                            try {
                                Thread.sleep(10 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    LogUtil.d(TAG, "finish to monitor device status");
                }
            });
            mCurrentThread.start();
        }

        static void stop(){
            if (!isMonitoring){
                return;
            }
            LogUtil.d(TAG, "stop to monitor device status");
            if (mCurrentThread != null){
                mCurrentThread.interrupt();
                mCurrentThread = null;
            }
            isMonitoring = false;
            mDeviceSerial = null;
            mCallback = null;
        }

    }

    private void startOvertimeTimer(long time, final Runnable run) {
        LogUtil.i(TAG, "Enter startOvertimeTimer: " + run);

        if (overTimeTimer != null) {
            LogUtil.i(TAG, " overTimeTimer.cancel: " + overTimeTimer);
            overTimeTimer.cancel();
            overTimeTimer = null;
        }
        overTimeTimer = new Timer();
        overTimeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.debugLog(TAG, "startOvertimeTimer");
                activity.runOnUiThread(run);
                DeviceOnlineStatusMonitor.stop();
            }
        }, time);
        LogUtil.i(TAG, " startOvertimeTimer: timer:" + overTimeTimer + " runnable:" + run);
    }
    private void recordConfigTimeAndError() {
        // 非有线连接，不是来自添加页面，不是来自设置界面，不是解绑错误
        if (!isLineConnecting && fromPage != FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY && !isUnbindDeviceError) {
        }
    }
    private void addCameraFailed(int errorStep, int errorCode) {
        this.errorStep = errorStep;
        addCameraError = errorCode;
        switch (errorStep) {
            case ERROR_WIFI_CONNECT:

                // stopBonjourOnThread();
                recordConfigTimeAndError();
                break;
            case ERROR_REGIST:

                recordConfigTimeAndError();
                break;
            case ERROR_ADD_CAMERA:
                if (errorCode == ErrorCode.ERROR_WEB_DEVICE_EXCEPTION) {
                    // Device exception
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_ADD_OWN_AGAIN) {
                    // The device has been added by itself
                    // showToast(R.string.query_camera_fail_repeat_error);
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_ADDED) {
                    // TODO
                    // The device has been added
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_NOT_ONLINE) {
                    // The device is not online

                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_VERIFY_CODE_ERROR) {
                    // Verification code error

                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_NOT_EXIT) {
                    // The device does not exist

                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_ADDED_BT_OTHER) {
                    // The device has been added by others

                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_OFFLINE_NOT_ADD) {
                    // The device is not online and is not added

                } else if (errorCode > 0) {

                } else {

                }
                recordConfigTimeAndError();
                break;
            default:
                break;
        }
    }
    private int probeDeviceInfo(String deviceNo) {
        mEZProbeDeviceInfo = EZOpenSDK.getInstance().probeDeviceInfo(deviceSerial,deviceType);
        if (mEZProbeDeviceInfo != null) {
            if (mEZProbeDeviceInfo.getBaseException() != null){
                return mEZProbeDeviceInfo.getBaseException().getErrorCode();
            }
            return 0;
        }
        return 1;//unknown error
    }
    private void startWifi(final Result results) {

        EZOpenSDK.getInstance().startConfigWifi(activity, deviceSerial, wifiName, pwd, new EZOpenSDKListener.EZStartConfigWifiCallback(){
            @Override
            public void onStartConfigWifiCallback(String s, final EZConstants.EZWifiConfigStatus ezWifiConfigStatus) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ezWifiConfigStatus == EZConstants.EZWifiConfigStatus.DEVICE_WIFI_CONNECTING) {
                            //设备wifi正在连接
                            System.out.println("设备wifi正在连接");
//                                    successStatus = 1;


                        } else if (ezWifiConfigStatus == EZConstants.EZWifiConfigStatus.DEVICE_WIFI_CONNECTED) {
                            //设备wifi连接成功
                            System.out.println("设备wifi连接成功");
                            if (isWifiConnected) {
                                LogUtil.i(TAG, "defiveFindHandler: receiver WIFI while isWifiConnected is true");
                                return;
                            }
                            System.out.println("isWifiConnected:"+isWifiConnected);
                            LogUtil.debugLog(TAG, "Received WIFI on device connection  " + deviceSerial);
                            isWifiOkBonjourget = true;
                            isWifiConnected = true;
                            t2 = System.currentTimeMillis();
                            cancelOvertimeTimer();
                            LogUtil.i(TAG, "in STATUS_REGISTING: startOvertimeTimer");
                            startOvertimeTimer((MAX_TIME_STEP_TWO_REGIST - 5) * 1000, new Runnable() {
                                public void run() {
                                    EZOpenSDK.getInstance().stopConfigWiFi();
                                    final Runnable success = new Runnable() {
                                        public void run() {
                                            isPlatConnected = false;
                                            if (isPlatConnected) {
                                                return;
                                            }
                                            // save wifipassword
                                            if (!isLineConnecting && !TextUtils.isEmpty(mac) && !"NULL".equals(mac)) {
                                                //                                    LocalInfo.getInstance().setWifiPassword(mac, wifiPassword);
                                            }
                                            isPlatConnected = true;
                                            t4 = System.currentTimeMillis();
                                            results.success(3);
                                            isWifiConnected = false;
                                            LogUtil.debugLog(TAG,
                                                    "STATUS_REGISTING Timeout from the server to obtain the device information is successful");
                                        }
                                    };
                                    final Runnable fail = new Runnable() {
                                        public void run() {
                                            t4 = System.currentTimeMillis();
                                            LogUtil.debugLog(TAG, "Timeout from the server to get device information failed");
                                            stopWifiConfigOnThread();
                                            addCameraFailed(isWifiOkBonjourget ? ERROR_REGIST : ERROR_WIFI_CONNECT, searchErrorCode);
                                        }
                                    };
                                    Thread thr = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            LogUtil.i(TAG, "in change status STATUS_REGISTING, begin probeDeviceInfo");
                                            int result = probeDeviceInfo(deviceSerial);

                                            LogUtil.i(TAG, "in start, got probeDeviceInfo");
                                            if (result == 0 && mEZProbeDeviceInfo != null) {
                                                LogUtil.i(TAG, "in start, probeDeviceInfo success," + mEZProbeDeviceInfo);
                                                activity.runOnUiThread(success);
//                                                        activity.runOnUiThread(new Runnable() {
//                                                            @Override
//                                                            public void run() {
//                                                                results.success(3);
//                                                            }
//                                                        });

                                                isWifiConnected = false;
                                                // TODO
                                            } else if (result == ErrorCode.ERROR_WEB_DEVICE_ONLINE_NOT_ADD) {
                                                LogUtil.i(TAG, "in start, probeDeviceInfo error:ERROR_WEB_DIVICE_ONLINE_NOT_ADD");
                                                activity.runOnUiThread(success);
                                            } else {
                                                LogUtil.i(TAG, "in start, probeDeviceInfo camera not online");
                                                activity.runOnUiThread(fail);
                                            }

                                        }
                                    });
                                    thr.start();

                                }
                            });

                        } else if (ezWifiConfigStatus == EZConstants.EZWifiConfigStatus.DEVICE_PLATFORM_REGISTED) {
//                                    EZOpenSDK.getInstance().stopConfigWiFi();
                            //设备注册到平台成功，可以调用添加设备接口添加设备
                            if (isPlatConnected) {
                                LogUtil.i(TAG, "defiveFindHandler: receiver PLAT while isPlatConnected is true");
                                return;
                            }
                            isPlatBonjourget = true;
                            isPlatConnected = true;
                            t3 = System.currentTimeMillis();
                            cancelOvertimeTimer();
                            System.out.println("设备注册到平台成功，可以调用添加设备接口添加设备");
                            successStatus = 3;
//                                    cancelOvertimeTimer();

                            results.success(successStatus);
                            isWifiConnected = false;

                            stopWifiConfigOnThread();



                        }
                        System.out.println("wifi_count:"+wifi_count);
//                                if(wifi_count>1){
//                                    results.success(successStatus);
//                                }

                    }
                });
//                        System.out.println(123);



            }
        });
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (successStatus != 0) {
//                    System.out.println("3是否过来");
//                    System.out.println("3是否过来"+successStatus);
//                    results.success(successStatus);
//                }
//            }
//        });



    }

    private void getConnectWifiSsid(Result results) {
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replace("\"", "");
        System.out.println("ssid:" + ssid);
        results.success(ssid);
    }


    /**
     * 初始化萤石云SDK
     *
     * @param appKey      萤石云AppKey
     * @param accessToken 萤石云accessToken
     */
    private void initSDK(String appKey, String accessToken, String apiUrl, String webUrl) {
//        Log.e("TAG", "获取数据:" + appKey + " --- " + accessToken);
        TestParams.setUse(true);
        /*
         * sdk日志开关，正式发布需要去掉
         */
        EZOpenSDK.showSDKLog(true);

        /*
         * 设置是否支持P2P取流,详见api
         */
        EZOpenSDK.enableP2P(true);

//        EZOpenSDK.initLib(registrar.activity().getApplication(), appKey);
//        EZOpenSDK.initLib(activity.getApplication(), appKey);
        EZOpenSDK.initLib(app, appKey);
        EZOpenSDK.getInstance().setAccessToken(accessToken);

        if (TextUtils.isEmpty(apiUrl))
            apiUrl = API_URL;
        if (TextUtils.isEmpty(webUrl))
            webUrl = WEB_URL;

        EzvizAPI.getInstance().setServerUrl(apiUrl, webUrl);
    }

    private void checkDevice(final Result results) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final EZProbeDeviceInfoResult probeDeviceInfoResult = EZOpenSDK.getInstance().probeDeviceInfo(deviceSerial, deviceType);
                final Map<String, String> maps = new HashMap<>();
                if (probeDeviceInfoResult.getBaseException() == null) {
                    //查询成功，添加设备
                    maps.put("defaultPicPath", probeDeviceInfoResult.getEZProbeDeviceInfo().getDefaultPicPath());
                    maps.put("displayName", probeDeviceInfoResult.getEZProbeDeviceInfo().getDisplayName());
                    maps.put("deviceStatus", "1");
                    maps.put("deviceMessage", "直接添加");
//                    bindDevice(results);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (probeDeviceInfoResult.getEZProbeDeviceInfo() == null) {
                                // 未查询到设备信息，不确定设备支持的配网能力,需要用户根据指示灯判断
                                //若设备指示灯红蓝闪烁，请选择smartconfig配网
                                //若设备指示灯蓝色闪烁，请选择设备热点配网
                                maps.put("deviceStatus", "5");
                                maps.put("deviceMessage", "未查询到设备信息");
                            } else {
                                if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportAP() == 2) {
                                    //选择设备热单配网
                                    maps.put("supportConnectType", "2");

                                }
                                if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportWifi() == 3) {
                                    //选择smartconfig配网
                                    maps.put("supportConnectType", "3");

                                }
                                if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportSoundWave() == 1) {
                                    //选择声波配网
                                    maps.put("supportConnectType", "1");

                                }
                            }

                        }
                    });
                } else {
                    switch (probeDeviceInfoResult.getBaseException().getErrorCode()) {
                        case 120023:
                            System.out.println("进来120023");
                            // TODO: 2018/6/25  设备不在线，未被用户添加 （这里需要网络配置）
                            maps.put("defaultPicPath", probeDeviceInfoResult.getEZProbeDeviceInfo().getDefaultPicPath());
                            maps.put("displayName", probeDeviceInfoResult.getEZProbeDeviceInfo().getDisplayName());
                            maps.put("deviceStatus", "4");
                            maps.put("deviceMessage", "设备不在线，未被用户添加");
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (probeDeviceInfoResult.getEZProbeDeviceInfo() == null) {
                                        // 未查询到设备信息，不确定设备支持的配网能力,需要用户根据指示灯判断
                                        //若设备指示灯红蓝闪烁，请选择smartconfig配网
                                        //若设备指示灯蓝色闪烁，请选择设备热点配网
                                        maps.put("deviceStatus", "5");
                                        maps.put("deviceMessage", "未查询到设备信息");
                                    } else {
                                        if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportAP() == 2) {
                                            //选择设备热单配网
                                            maps.put("supportConnectType", "2");

                                        }
                                        if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportWifi() == 3) {
                                            //选择smartconfig配网
                                            maps.put("supportConnectType", "3");

                                        }
                                        if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportSoundWave() == 1) {
                                            //选择声波配网
                                            maps.put("supportConnectType", "1");

                                        }
                                    }

                                }
                            });
                            break;
                        case 120002:
                            // TODO: 2018/6/25  设备不存在，未被用户添加 （这里需要网络配置）
                            maps.put("defaultPicPath", probeDeviceInfoResult.getEZProbeDeviceInfo().getDefaultPicPath());
                            maps.put("displayName", probeDeviceInfoResult.getEZProbeDeviceInfo().getDisplayName());
                            maps.put("deviceStatus", "4");
                            maps.put("deviceMessage", "设备不存在，未被用户添加");
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (probeDeviceInfoResult.getEZProbeDeviceInfo() == null) {
                                        // 未查询到设备信息，不确定设备支持的配网能力,需要用户根据指示灯判断
                                        //若设备指示灯红蓝闪烁，请选择smartconfig配网
                                        //若设备指示灯蓝色闪烁，请选择设备热点配网
                                        maps.put("deviceStatus", "5");
                                        maps.put("deviceMessage", "未查询到设备信息");
                                    } else {
                                        if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportAP() == 2) {
                                            //选择设备热单配网
                                            maps.put("supportConnectType", "2");

                                        }
                                        if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportWifi() == 3) {
                                            //选择smartconfig配网
                                            maps.put("supportConnectType", "3");

                                        }
                                        if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportSoundWave() == 1) {
                                            //选择声波配网
                                            maps.put("supportConnectType", "1");

                                        }
                                    }
                                }
                            });
                            break;
                        case 120029:
                            // TODO: 2018/6/25  设备不在线，已经被自己添加 (这里需要网络配置)
                            maps.put("defaultPicPath", probeDeviceInfoResult.getEZProbeDeviceInfo().getDefaultPicPath());
                            maps.put("displayName", probeDeviceInfoResult.getEZProbeDeviceInfo().getDisplayName());
                            maps.put("deviceStatus", "4");
                            maps.put("deviceMessage", "设备不在线，已经被自己添加");
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (probeDeviceInfoResult.getEZProbeDeviceInfo() == null) {
                                        // 未查询到设备信息，不确定设备支持的配网能力,需要用户根据指示灯判断
                                        //若设备指示灯红蓝闪烁，请选择smartconfig配网
                                        //若设备指示灯蓝色闪烁，请选择设备热点配网
                                        maps.put("deviceStatus", "5");
                                        maps.put("deviceMessage", "未查询到设备信息");
                                    } else {
                                        if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportAP() == 2) {
                                            //选择设备热单配网
                                            maps.put("supportConnectType", "2");

                                        }
                                        if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportWifi() == 3) {
                                            //选择smartconfig配网
                                            maps.put("supportConnectType", "3");

                                        }
                                        if (probeDeviceInfoResult.getEZProbeDeviceInfo().getSupportSoundWave() == 1) {
                                            //选择声波配网
                                            maps.put("supportConnectType", "1");

                                        }
                                    }

                                }
                            });
                        case 120020:
                            // TODO: 2018/6/25 设备在线，已经被自己添加 (给出提示)
//                        maps.put("defaultPicPath", probeDeviceInfoResult.getEZProbeDeviceInfo().getDefaultPicPath());
//                        maps.put("displayName", probeDeviceInfoResult.getEZProbeDeviceInfo().getDisplayName());
//                        maps.put("supportConnectType", "1");
                            maps.put("deviceStatus", "2");
                            maps.put("deviceMessage", "设备在线，已经被自己添加");

                            break;
                        case 120022:
                            // TODO: 2018/6/25 设备在线，已经被别的用户添加 (给出提示)
//                        maps.put("defaultPicPath", probeDeviceInfoResult.getEZProbeDeviceInfo().getDefaultPicPath());
//                        maps.put("displayName", probeDeviceInfoResult.getEZProbeDeviceInfo().getDisplayName());
//                        maps.put("supportConnectType", "1");
                            maps.put("deviceStatus", "3");
                            maps.put("deviceMessage", "设备在线，已经被别的用户添加");
                            break;
                        case 120024:
                            // TODO: 2018/6/25 设备不在线，已经被别的用户添加 (给出提示)
//                        maps.put("defaultPicPath", probeDeviceInfoResult.getEZProbeDeviceInfo().getDefaultPicPath());
//                        maps.put("displayName", probeDeviceInfoResult.getEZProbeDeviceInfo().getDisplayName());
//                        maps.put("supportConnectType", "1");
                            maps.put("deviceStatus", "3");
                            maps.put("deviceMessage", "设备不在线，已经被别的用户添加");
                            break;
                        default:
                            // TODO: 2018/6/25 请求异常
                            maps.put("deviceStatus", "5");
                            maps.put("deviceMessage", "设备查询失败");

                            break;
                    }
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        results.success(maps);
                    }
                });

            }
        }).start();

    }


    /**
     * 根据deviceSerial 和 cameraNo 构造EZPlayer对象
     *
     * @param deviceSerial 设备序列号
     * @param cameraNo     通道号
     */

    private void createPlayer(String deviceSerial, int cameraNo, String validateCode) {
        Log.e("TAG", "deviceSerial获取数据:" + deviceSerial + " --- " + cameraNo);
        surfaceView = factory.getSurfaceView();

        mPlayer = EZOpenSDK.getInstance().createPlayer(deviceSerial, cameraNo);

        if (surfaceView == null || TextUtils.isEmpty(deviceSerial))
            return;

        mPlayer.setHandler(new Handler());
        holder = surfaceView.getHolder();
        this.deviceSerial = deviceSerial;
        this.cameraNo = cameraNo;
//        mPlayer.setPlayVerifyCode();


        mPlayer.setSurfaceHold(holder);
        System.out.println("mPlayer:"+mPlayer);
        System.out.println("holder:"+holder);

//        mPlayer.setPlayVerifyCode(validateCode);
        mPlayer.startRealPlay();
        onSurfaceTouch();

    }

    /**
     * 根据视频url构造EZPlayer对象，用于通过视频url进行播放
     */
//    @Deprecated
//    private void createPlayer(String url) {
//        SurfaceView surfaceView = factory.getSurfaceView();
//        if (surfaceView == null || TextUtils.isEmpty(url))
//            return;
//        SurfaceHolder holder = surfaceView.getHolder();
//        mPlayer = EZOpenSDK.getInstance().createPlayerWithUrl(url);
//        mPlayer.setHandler(new Handler());
//        mPlayer.setSurfaceHold(holder);
//    }

    /**
     * 开始实时预览
     */
    private void startRealPlay(String deviceSerial, int cameraNo) {

        System.out.println("mPlayer1:" + mPlayer);
        if (mPlayer != null) {
            mPlayer.startRealPlay();
        }


    }


    /**
     * 停止播放
     */
    private void stopRealPlay() {
        mPlayer.stopRealPlay();
    }


    /**
     * 控制摄像头
     *
     * @param type      类型  0 表示开始旋转 1 表示停止旋转
     * @param direction 旋转方向
     */
    private void directionController(int type, int direction) {

        EZConstants.EZPTZCommand command = null;

        switch (direction) {
            case 0:
                command = EZConstants.EZPTZCommand.EZPTZCommandLeft;
                break;
            case 1:
                command = EZConstants.EZPTZCommand.EZPTZCommandUp;
                break;
            case 2:
                command = EZConstants.EZPTZCommand.EZPTZCommandRight;
                break;
            case 3:
                command = EZConstants.EZPTZCommand.EZPTZCommandDown;
                break;
        }

        if (type == 0)
            ptzOption(command, EZConstants.EZPTZAction.EZPTZActionSTART);
        else if (type == 1)
            ptzOption(command, EZConstants.EZPTZAction.EZPTZActionSTOP);
        else if (type == 2) {
            ptzOption(command, EZConstants.EZPTZAction.EZPTZActionSTART);
            ptzOption(command, EZConstants.EZPTZAction.EZPTZActionSTOP);
        }

    }

    private void ptzOption(final EZConstants.EZPTZCommand command, final EZConstants.EZPTZAction action) {
//        Log.e("TAG","ptzOption==="+deviceSerial +", cameraNo=="+cameraNo+",command=="+command+",action=="+action);
        if (service == null)
            service = Executors.newSingleThreadExecutor();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    EZOpenSDK.getInstance().controlPTZ(deviceSerial, cameraNo, command,
                            action, EZConstants.PTZ_SPEED_DEFAULT);
                } catch (BaseException e) {
                    Log.e("云台错误", "error==" + e.getLocalizedMessage() + "   " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };


        service.execute(runnable);


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                boolean ptz_result = false;
//                try {
//                    ptz_result = EZOpenSDK.getInstance().controlPTZ(cameraInfo.getDeviceSerial(), cameraInfo.getCameraNo(), command,
//                            action, EZConstants.PTZ_SPEED_DEFAULT);
//
//
//                } catch (BaseException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//        }).start();
    }


    /**
     * 释放资源
     */
    private void release() {
        Log.e("TAG", "释放资源。。。");
        deviceSerial = null;
        cameraNo = 0;
        if(service != null){
            service.shutdownNow();
            service = null;
        }
        
        mPlayer.closeSound();
        mPlayer.stopRealPlay();
        mPlayer.release();

        mPlayer = null;

    }


    private void getCameraList() {
        new Thread() {
            @Override
            public void run() {
                try {
                    List<EZDeviceInfo> list = EZOpenSDK.getInstance().getDeviceList(0, 20);


                    for (EZDeviceInfo item : list) {


                        EZCameraInfo camera = EZUtils.getCameraInfoFromDevice(item, 0);

                        Log.e("TAG", item.getDeviceSerial() + " ----- " + camera.getDeviceSerial() + "----" + camera.getCameraNo());

                    }

                } catch (BaseException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Deprecated
    private void initReceive() {

        EzvizBroadcastReceiver receiver = new EzvizBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.videogo.action.OAUTH_SUCCESS_ACTION");
//        registrar.activeContext().registerReceiver(receiver, filter);
        activity.registerReceiver(receiver, filter);
    }

    /**
     * 通过调用服务接口判断AppKey和AccessToken且有效
     *
     * @return 是否依旧有效
     */
    @Deprecated
    private boolean checkAppKeyAndAccessToken() {
        boolean isValid = false;
        try {
            EzvizAPI.getInstance().getUserName();
            isValid = true;
        } catch (BaseException e) {
            e.printStackTrace();
            int errCode = e.getErrorCode();
            String errMsg;
            if (errCode == 400031) {
                errMsg = "400031";
            } else {
                errMsg = "default";
            }
            Log.e("TAG", "ERROR:" + errMsg);
        }


        Log.e("TAG", "是否有效:" + isValid);
        return isValid;
    }

    /**
     * 当SurfaceView被触摸
     */
    private void onSurfaceTouch() {

//        final GestureDetector detector = new GestureDetector(registrar.context(), new GestureDetector.SimpleOnGestureListener() {
        final GestureDetector detector = new GestureDetector(activity.getBaseContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                // Log.e("TAG", "onSingleTapConfirmed");
                channel.invokeMethod("onSingleTap", "");
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Log.e("TAG", "onDoubleTap");
                channel.invokeMethod("onDoubleTap", "");
                return super.onDoubleTap(e);
            }


            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                double x1 = e1.getX();
                double y1 = e1.getY();

                double x2 = e2.getX();
                double y2 = e2.getY();

                double moveX = x1 - x2;
                double moveY = y1 - y2;

                if (Math.abs(moveX) >= Math.abs(moveY)) {
                    //左右移动摄像头
                    if (moveX > 0)
                        //向左移动摄像头
                        directionController(2, 0);
                    else
                        directionController(2, 2);
                } else {
                    //上线移动摄像头
                    if (moveY > 0)
                        //向下移动摄像头
                        directionController(2, 1);
                    else
                        directionController(2, 3);
                }


                //Log.e("TAG", "onFling"+"e1:"+x1+"--"+y1+"=== e2:"+x2+"--"+y2);


                return super.onFling(e1, e2, velocityX, velocityY);
            }


        });

    }

}
