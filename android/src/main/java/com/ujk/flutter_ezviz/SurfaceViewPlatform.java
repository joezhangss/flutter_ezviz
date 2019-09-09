package com.ujk.flutter_ezviz;

import android.content.Context;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import io.flutter.plugin.platform.PlatformView;

/**
 * 文件名 SurfaceViewPlatfrom
 * 创建者  CT
 * 时 间  2019/8/27 10:19
 * TODO
 */
public class SurfaceViewPlatform implements PlatformView {

    private SurfaceView surfaceView;

    public SurfaceViewPlatform(Context context) {
        surfaceView = new SurfaceView(context);
    }

    @Override
    public View getView() {
        return surfaceView;
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    @Override
    public void dispose() {

    }
}
