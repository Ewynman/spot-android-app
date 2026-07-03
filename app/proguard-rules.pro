# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep BuildConfig
-keep class com.spot.android.BuildConfig { *; }

# Supabase / Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.spot.android.**$$serializer { *; }
-keepclassmembers class com.spot.android.** {
    *** Companion;
}
-keepclasseswithmembers class com.spot.android.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes used for API
-keep class com.spot.android.core.model.** { *; }

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
