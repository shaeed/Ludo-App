# ── kotlinx.serialization ─────────────────────────────────────────
# Keep @Serializable classes and their generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.shaeed.ludo.**$$serializer { *; }
-keepclassmembers class com.shaeed.ludo.** {
    *** Companion;
}
-keepclasseswithmembers class com.shaeed.ludo.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── General ───────────────────────────────────────────────────────
# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
