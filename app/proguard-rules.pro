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

-dontwarn org.eclipse.jetty.**
-dontwarn org.fourthline.cling.**
-dontwarn org.seamless.**
-keep class org.fourthline.cling.** {*;}
-keepattributes Annotation

# for media render state machine
-keep class org.seamless.statemachine.** {*;}
-keepclassmembers class * implements org.fourthline.cling.support.avtransport.impl.state.AbstractState {*;}