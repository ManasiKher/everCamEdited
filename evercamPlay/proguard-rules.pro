# Don't show warnings for the following libraries
-dontwarn io.evercam.**
-dontwarn okio.**
-dontwarn org.joda.time.**
-dontwarn org.simpleframework.xml.**
-dontwarn com.mixpanel.android.**
-dontwarn com.google.android.gms.**
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry

# Fix the MenuBuilder NoClassDefFoundError https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.view.menu.**,!android.support.design.internal.NavigationMenu,!android.support.design.internal.NavigationMenuPresenter,!android.support.design.internal.NavigationSubMenu,** {*;}

# Keep line numbers for Crashlytics bug reports
-keepattributes SourceFile,LineNumberTable

-ignorewarnings

-keep class * {
    public private *;
}

