
# GAM
#pre GAMv20.0
-keep class com.google.android.gms.ads.doubleclick.PublisherAdRequest {
    public android.os.Bundle getCustomTargeting();
}

-keep class com.google.android.gms.ads.doubleclick.PublisherAdRequest$Builder {
    public com.google.android.gms.ads.doubleclick.PublisherAdRequest build();
    public com.google.android.gms.ads.doubleclick.PublisherAdRequest$Builder addCustomTargeting(java.lang.String, java.lang.String);
}

#GAMv20.0
-keep class com.google.android.gms.ads.admanager.AdManagerAdRequest {
    public android.os.Bundle getCustomTargeting();
}

-keep class com.google.android.gms.ads.admanager.AdManagerAdRequest$Builder {
    public com.google.android.gms.ads.admanager.AdManagerAdRequest build();
    public com.google.android.gms.ads.admanager.AdManagerAdRequest$Builder addCustomTargeting(java.lang.String, java.lang.String);
}

# MoPub Just to be sure
-keep class com.mopub.mobileads.MoPubView {
    public java.lang.String getKeywords();
    public void setKeywords(java.lang.String);
}

-keep class com.mopub.mobileads.MoPubInterstitial {
    public java.lang.String getKeywords();
    public void setKeywords(java.lang.String);
}


# From rendering

# This ProGuard configuration file illustrates how to process a program
# library, such that it remains usable as a library.
# Usage:
#     java -jar proguard.jar @library.pro
#

# Specify the input jars, output jars, and library jars.
# In this case, the input jar is the program library that we want to process.


# Save the obfuscation mapping to a file, so we can de-obfuscate any stack
# traces later on. Keep a fixed source file attribute and all line number
# tables to get line numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.

# -printmapping out.map
-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod

# Preserve all annotations.

-keepattributes *Annotation*

# Preserve all public classes, and their public and protected fields and
# methods.

-keep public class * {
    public protected *;
}

# Preserve all .class method names.

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

# Preserve all native method names and the names of their classes.

-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve the special static methods that are required in all enumeration
# classes.

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your library doesn't use serialization.
# If your code contains serializable classes that have to be backward
# compatible, please refer to the manual.

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}



