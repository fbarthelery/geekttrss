# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.groovy.
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

# Picasso contains Downloader with okhttp2 but we use okhttp3
-dontwarn com.squareup.okhttp.*
-dontnote com.squareup.okhttp.*

# Dagger-android has errorprone annotations
-dontwarn com.google.errorprone.annotations.*

# okio and retrofit have some animal-sniffer annotations
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Don't warn on javax.annotation annotations include in lot of libraries
-dontwarn javax.annotation.**

# okhttp support conscrypt platform not available on android
-dontwarn org.conscrypt.**

# geekdroid: we don't use firebase stuff
-dontwarn com.geekorum.geekdroid.firebase.**

# coroutines
# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.geekorum.ttrss.**$$serializer { *; }
-keepclassmembers class com.geekorum.ttrss.** {
    *** Companion;
}
-keepclasseswithmembers class com.geekorum.ttrss.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# can't find reference field in program class for these
-dontwarn com.geekorum.ttrss.network.impl.ListContent$ListContentSerializer
-dontwarn  com.geekorum.ttrss.network.impl.ListResponsePayload$ListResponsePayloadSerializer

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
