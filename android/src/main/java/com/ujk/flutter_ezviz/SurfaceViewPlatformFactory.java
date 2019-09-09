package com.ujk.flutter_ezviz;

import android.content.Context;
import android.view.SurfaceView;

import io.flutter.Log;
import io.flutter.plugin.common.MessageCodec;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

/**
 * 文件名 SurfaceViewPlatformFactory
 * 创建者  CT
 * 时 间  2019/8/27 10:25
 * TODO
 */
public class SurfaceViewPlatformFactory extends PlatformViewFactory {


    private SurfaceViewPlatform surfaceViewPlatform;

    public SurfaceViewPlatformFactory() {
        super(StandardMessageCodec.INSTANCE);
    }

    @Override
    public PlatformView create(Context context, int i, Object o) {

        surfaceViewPlatform = new SurfaceViewPlatform(context);
        Log.e("TAG","开始创建一个SurfaceView");
        return surfaceViewPlatform;
    }


    public SurfaceView getSurfaceView() {
        if (surfaceViewPlatform != null)
            return surfaceViewPlatform.getSurfaceView();
        return null;
    }

}
