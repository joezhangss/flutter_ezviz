#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'flutter_ezviz'
  s.version          = '0.0.1'
  s.summary          = '萤石云插件'
  s.description      = <<-DESC
萤石云插件
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
#  s.ios.vendored_frameworks = 'Frameworks/EZOpenSDKFramework.framework'
#  s.vendored_libraries = 'Classes/*/*.a'
#  s.libraries = "z", "c++", "iconv.2.4.0", "bz2", "sqlite3.0", "crypto", "EZOpenSDK", "ssl", "flutter_ezviz"
#  s.frameworks = "OpenAL", "CoreMedia", "GLKit", "AudioToolbox", "VideoToolbox", "MobileCoreServices", "SystemConfiguration"
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
#  s.dependency 'EZOpenSDK'

  s.ios.deployment_target = '8.0'
end

