# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# keep annotated by NotProguard
-keep @cn.weijing.demo.comm.NotProguard class *{*;}
-keep class * {
@cn.weijing.demo.comm.NotProguard <fields>;
}
-keepclassmembers class * {
@cn.weijing.demo.comm.NotProguard <methods>;
}

#加解密签名
-dontwarn org.bouncycastle.util.**
-keep public class org.bouncycastle.**{*;}

-keep class cn.dabby.demo.sdk.bean.**{*;}

#-keep public class cn.weijing.**{*;}


#接入方需要添加配置1微警sdk混淆规则
-keep class cn.weijing.sdk.**{*;}
-keep class cn.weijing.framework.gson.** { *;}
#可能因为一所活检是lib包引入所以需要额外配置，云从aar引入不需要
-keep class com.sensetime.** { *; }

-dontwarn cn.cloudwalk.**
-dontwarn cloudwalk.**
-keep class cn.cloudwalk.**{*;}
-keep class cloudwalk.**{*;}

