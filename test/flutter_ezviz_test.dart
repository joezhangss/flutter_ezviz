import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_ezviz/flutter_ezviz.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_ezviz');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

//  test('getPlatformVersion', () async {
//    expect(await FlutterEzviz.platformVersion, '42');
//  });
}
