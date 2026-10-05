# Proguard rules for NurPray
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.media3.**
