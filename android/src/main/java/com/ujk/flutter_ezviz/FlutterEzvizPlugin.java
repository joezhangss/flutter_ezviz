package com.ujk.flutter_ezviz;

import android.content.IntentFilter;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.videogo.debug.TestParams;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.EzvizAPI;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import io.flutter.Log;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterEzvizPlugin
 */
public class FlutterEzvizPlugin implements MethodCallHandler {

    private static final String API_URL = "https://open.ys7.com";
    private static final String WEB_URL = "https://openauth.ys7.com";


    private static Registrar registrar;
    private static MethodChannel channel;

    private static SurfaceViewPlatformFactory factory;

    private EZPlayer mPlayer;

    private String deviceSerial;
    private int cameraNo;
    //线程池
    private ExecutorService service;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_ezviz");
        channel.setMethodCallHandler(new FlutterEzvizPlugin());
        FlutterEzvizPlugin.registrar = registrar;
        FlutterEzvizPlugin.channel = channel;

        factory = new SurfaceViewPlatformFactory();

        registrar.platformViewRegistry().registerViewFactory("com.ujk.flutter_ezviz.SurfaceViewPlatform", factory);

    }

    @Override

    public void onMethodCall(MethodCall call, Result result) {

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
                createPlayer(mapDevice.get("deviceSerial"), Integer.valueOf(noStr));
                startRealPlay();
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
                if(mPlayer != null){
                    mPlayer.openSound();
                }
                break;
            case "closeSound":
                if(mPlayer != null){
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

        EZOpenSDK.initLib(registrar.activity().getApplication(), appKey);
        EZOpenSDK.getInstance().setAccessToken(accessToken);

        if (TextUtils.isEmpty(apiUrl))
            apiUrl = API_URL;
        if (TextUtils.isEmpty(webUrl))
            webUrl = WEB_URL;

        EzvizAPI.getInstance().setServerUrl(apiUrl, webUrl);
    }


    /**
     * 根据deviceSerial 和 cameraNo 构造EZPlayer对象
     *
     * @param deviceSerial 设备序列号
     * @param cameraNo     通道号
     */

    private void createPlayer(String deviceSerial, int cameraNo) {
        Log.e("TAG", "deviceSerial获取数据:" + deviceSerial + " --- " + cameraNo);
        SurfaceView surfaceView = factory.getSurfaceView();
        if (surfaceView == null || TextUtils.isEmpty(deviceSerial))
            return;
        SurfaceHolder holder = surfaceView.getHolder();
        this.deviceSerial = deviceSerial;
        this.cameraNo = cameraNo;
        mPlayer = EZOpenSDK.getInstance().createPlayer(deviceSerial, cameraNo);
        mPlayer.setHandler(new Handler());

        mPlayer.setSurfaceHold(holder);
        onSurfaceTouch();

    }

    /**
     * 根据视频url构造EZPlayer对象，用于通过视频url进行播放
     */
    @Deprecated
    private void createPlayer(String url) {
        SurfaceView surfaceView = factory.getSurfaceView();
        if (surfaceView == null || TextUtils.isEmpty(url))
            return;
        SurfaceHolder holder = surfaceView.getHolder();
        mPlayer = EZOpenSDK.getInstance().createPlayerWithUrl(url);
        mPlayer.setHandler(new Handler());
        mPlayer.setSurfaceHold(holder);
    }

    /**
     * 开始实时预览
     */
    private void startRealPlay() {


        //获取设备相机信息
//        cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
//        if (cameraInfo == null)
//            return;
        //       mPlayer = EZOpenSDK.getInstance().createPlayer(cameraInfo.getDeviceSerial(), cameraInfo.getCameraNo());

        if (mPlayer != null)
            mPlayer.startRealPlay();

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
                    Log.e("云台错误","error=="+e.getLocalizedMessage()+ "   "+e.getMessage());
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
        Log.e("TAG","释放资源。。。");
        deviceSerial = null;
        cameraNo = 0;

        service.shutdownNow();
        service = null;
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
    private static void initReceive() {

        EzvizBroadcastReceiver receiver = new EzvizBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.videogo.action.OAUTH_SUCCESS_ACTION");
        registrar.activeContext().registerReceiver(receiver, filter);

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

        final GestureDetector detector = new GestureDetector(registrar.context(), new GestureDetector.SimpleOnGestureListener() {
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

        factory.getSurfaceView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                return detector.onTouchEvent(event);
            }
        });
    }

}
