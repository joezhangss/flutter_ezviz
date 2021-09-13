import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_ezviz/flutter_ezviz.dart';

void main() => runApp(MaterialApp(
  title: "",
  home: MyApp(),
));

class MyApp extends StatefulWidget {
  bool isLand = false;

  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Widget surfaceView;

  @override
  void initState() {
    super.initState();
    print("initState");
  }

  @override
  Widget build(BuildContext context) {
    double videoHeight =
    widget.isLand ? MediaQuery.of(context).size.height : 150.0;

    surfaceView = AndroidView(viewType: FlutterEzviz.getSurfaceView());

    List<Widget> mList = [];
    //创建视频直播 和 控制模块
    Stack videoStack = Stack(
      children: <Widget>[
        Container(
          child: surfaceView,
          padding: const EdgeInsets.only(top: 24),
          height: videoHeight,
        ),
        Container(
            height: 36,
            width: MediaQuery.of(context).size.width,
            color: Colors.red,
            child: Row(
              children: <Widget>[
                GestureDetector(
                  onTap: () {
                    FlutterEzviz.stopRealPlay();

                    widget.isLand
                        ? SystemChrome.setPreferredOrientations(
                        [DeviceOrientation.portraitUp])
                        : SystemChrome.setPreferredOrientations(
                        [DeviceOrientation.landscapeLeft]);

                    setState(() {
                      widget.isLand = !widget.isLand;
                      surfaceView = null;
                    });
                  },
                  child: Text(
                    "全屏",
                    textAlign: TextAlign.center,
                  ),
                ),
                RaisedButton(
                    onPressed: () async {
                      FlutterEzviz.initSDK("52e47a5d4fe941e18b787fbf6f881757", "at.14tv5bg247zezy39c9fvmd09dc6say46-7sl52sp66o-0txba50-nd1iz2mfe");
                      FlutterEzviz.onTapCallback();
                    },
                    child: Text("登录")),
                RaisedButton(
                    onPressed: () async {
                      FlutterEzviz.getCameraList();
                    },
                    child: Text("设备列表")),
                RaisedButton(
                  onPressed: () {
                    FlutterEzviz.startRealPlay("600848031","1");
                  },
                  child: Text("播放"),
                ),
                RaisedButton(
                  onPressed: () {
                    FlutterEzviz.stopRealPlay();
                  },
                  child: Text("停止"),
                ),
              ],
            ))
      ],
    );

    Container controllerContainer = _buildControllerContainer();

    mList.add(videoStack);
    if (!widget.isLand) mList.add(controllerContainer);



    return widget.isLand
        ? Scaffold(body: Column(children: mList))
        : Scaffold(
      appBar: AppBar(title: Text("萤石云")),
      body: Column(
        children: mList,
      ),
    );
  }

  Widget _buildControllerContainer() => Container(
    child: Column(
      children: <Widget>[
        RaisedButton(
            onPressed: () async {
              FlutterEzviz.initSDK( "52e47a5d4fe941e18b787fbf6f881757", "at.14tv5bg247zezy39c9fvmd09dc6say46-7sl52sp66o-0txba50-nd1iz2mfe");
            },
            child: Text("登录")),
        RaisedButton(
            onPressed: () async {
              FlutterEzviz.getCameraList();
            },
            child: Text("设备列表")),
        RaisedButton(
          onPressed: () {
            FlutterEzviz.startRealPlay("600848031","1");
          },
          child: Text("播放"),
        ),
        RaisedButton(
          onPressed: () {
            FlutterEzviz.stopRealPlay();
          },
          child: Text("停止"),
        ),
        Row(
          children: <Widget>[
            RaisedButton(
              onPressed: () {
                FlutterEzviz.startDirection(0);
              },
              child: Text("左"),
            ),
            RaisedButton(
              onPressed: () {
                FlutterEzviz.stopDirection(0);
              },
              child: Text("停止左"),
            ),
          ],
        ),
        Row(

          children: <Widget>[

            RaisedButton(
              onPressed: () {
                FlutterEzviz.startDirection(2);
              },
              child: Text("右"),
            ),
            RaisedButton(
              onPressed: () {
                FlutterEzviz.stopDirection(2);
              },
              child: Text("停止右"),
            )
          ],
        )
      ],
    ),
  );
}
