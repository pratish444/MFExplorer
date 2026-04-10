# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions

# Gson
-keep class com.mfexplorer.app.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
