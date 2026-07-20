# ── Debugging: preserve line numbers in crash reports ─────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Supabase + Ktor ───────────────────────────────────────────────────────────
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn io.github.jan.supabase.**

# ── Kotlin Serialization ──────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.* <methods>;
}

# ── Room Database ─────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# ── Google Play Billing ───────────────────────────────────────────────────────
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ── Google AdMob ──────────────────────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.**

# ── Google Credentials / Sign-In ──────────────────────────────────────────────
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn androidx.credentials.**

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── ML Kit Barcode ────────────────────────────────────────────────────────────
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ── CameraX ───────────────────────────────────────────────────────────────────
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**