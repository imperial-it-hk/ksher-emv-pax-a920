
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-ignorewarnings
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*


-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class * implements android.os.Parcelable{
    public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * implements java.io.Serializable {
   static final long serialVersionUID;
   private static final java.io.ObjectStreamField[] serialPersistentFields;
   !static !transient <fields>;
   private void writeObject(java.io.ObjectOutputStream);
   private void readObject(java.io.ObjectInputStream);
   java.lang.Object writeReplace();
   java.lang.Object readResolve();
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * {
    void *(**On*Event);
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application {*;}
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get* ();
}
-keep public class * extends android.view.animation.Animation {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    *** get* ();
}

-keepclassmembers class * extends android.app.Activity {
    public void * (android.view.View);
}

-keep public class javax.**

#过滤android.support.v4
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-dontwarn android.support.v7.**
-keep class android.support.v7.** {*;}
-keep interface android.support.v7.app.** {*;}
-keep class * extends android.support.v7.**

#过滤commons-httpclient-3.1.jar
-keep class org.apache.**{*;}
-keep interface org.apache.** {*;}

#gson
-dontwarn com.google.json.**
-keep class com.google.json.** {*;}
-keep interface com.google.json.** {*;}

#okhttp
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-keep interface com.squareup.okhttp.** { *; }
-dontwarn okio.**
-keep class okio.** {*;}
-keep interface okio.** {*;}

#glide
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.** {*;}
-keep interface com.bumptech.glide.** {*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#bga-banner
-dontwarn cn.bingoogolapple.bgabanner.**
-keep class cn.bingoogolapple.bgabanner.** {*;}

#httpclient
-keep class android.net.compatibility.** {*;}
-keep class android.net.http.** {*;}
-keep interface android.net.http.** {*;}
-keep class com.android.internal.http.multipart.** {*;}
-keep interface com.android.internal.http.multipart.** {*;}

-keep class org.slf4j.** {*;}
-keep interface org.slf4j.** {*;}

-keep class com.pax.market.api.sdk.** {*;}
-keep interface com.pax.market.api.sdk.** {*;}

-dontwarn com.pax.eemv.**
-keep class com.pax.eemv.** {*;}
-keep interface com.pax.eemv.** {*;}

-dontwarn com.pax.gl.**
-keep class com.pax.gl.** {*;}
-keep interface com.pax.gl.** {*;}

-dontwarn com.pax.dal.**
-keep class com.pax.dal.** {*;}
-keep interface com.pax.dal.** {*;}
-dontwarn com.pax.neptunelite.**
-keep class com.pax.neptuenlite.** {*;}
-keep interface com.pax.neptuenlite.** {*;}
-keep class com.pax.neptuneliteapi.** {*;}
-keep interface com.pax.neptuneliteapi.** {*;}

-dontwarn net.sqlcipher.**
-keep class net.sqlcipher.** {*;}
-keep interface net.sqlcipher.** {*;}

-dontwarn com.google.zxing.**
-keep class com.google.zxing.** {*;}
-keep interface com.google.zxing.** {*;}

-dontwarn com.pax.appstore.**
-keep class com.pax.appstore.** {*;}
-keep interface com.pax.appstore.** {*;}


-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

#---------------- PaxStore  ------------------------------------
#Gson
-dontwarn com.google.gson.**
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

#JJWT
-keepnames class com.fasterxml.jackson.databind.** { *; }
-dontwarn com.fasterxml.jackson.databind.*
-keepattributes InnerClasses
-keep class org.bouncycastle.** { *; }
-keepnames class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-keep class io.jsonwebtoken.** { *; }
-keepnames class io.jsonwebtoken.* { *; }
-keepnames interface io.jsonwebtoken.* { *; }
-dontwarn javax.xml.bind.DatatypeConverter
-dontwarn io.jsonwebtoken.impl.Base64Codec
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames interface com.fasterxml.jackson.** { *; }

#dom4j
-dontwarn org.dom4j.**
-keep class org.dom4j.**{*;}
-dontwarn org.xml.sax.**
-keep class org.xml.sax.**{*;}
-dontwarn com.fasterxml.jackson.**
-keep class com.fasterxml.jackson.**{*;}
-dontwarn com.pax.market.api.sdk.java.base.util.**
-keep class com.pax.market.api.sdk.java.base.util.**{*;}
-dontwarn org.w3c.dom.**
-keep class org.w3c.dom.**{*;}
-dontwarn javax.xml.**
-keep class javax.xml.**{*;}

#dto
-dontwarn com.pax.market.api.sdk.java.base.dto.**
-keep class com.pax.market.api.sdk.java.base.dto.**{*;}

-keep class com.gyf.immersionbar.* {*;}
-dontwarn com.gyf.immersionbar.**

-dontwarn com.google.**
-keep class com.google.** {*;}
-keep interface com.google.** {*;}

-dontwarn io.reactivex.**
-keep class io.reactivex.** {*;}
-keep interface io.reactivex.** {*;}

-dontwarn com.icg.vas.pay.**
-keep class com.icg.vas.pay.** {*;}
-keep interface com.icg.vas.pay.** {*;}