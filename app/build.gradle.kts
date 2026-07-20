import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nikeshparihar.smartcart"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nikeshparihar.smartcart"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load secrets from local.properties
        val props = Properties()
        props.load(rootProject.file("local.properties").inputStream())
        buildConfigField("String", "SUPABASE_URL", "\"${props["SUPABASE_URL"]}\"") 
        buildConfigField("String", "SUPABASE_KEY", "\"${props["SUPABASE_KEY"]}\"") 
        buildConfigField("String", "ADMOB_APP_ID", "\"${props.getProperty("ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")}\"") 
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${props.getProperty("GOOGLE_WEB_CLIENT_ID", "YOUR_GOOGLE_WEB_CLIENT_ID_HERE")}\"") 
        manifestPlaceholders["admobAppId"] = props.getProperty("ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")
    }

    signingConfigs {
        create("release") {
            val props = Properties()
            val localPropsFile = rootProject.file("local.properties")
            if (localPropsFile.exists()) props.load(localPropsFile.inputStream())
            val keystorePath = props.getProperty("KEYSTORE_PATH", "")
            if (keystorePath.isNotEmpty()) {
                storeFile = file(keystorePath)
                storePassword = props.getProperty("KEYSTORE_PASSWORD", "")
                keyAlias = props.getProperty("KEY_ALIAS", "")
                keyPassword = props.getProperty("KEY_PASSWORD", "")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // Align all Supabase modules (postgrest, auth, realtime) on the same version.
    implementation(platform(libs.supabase.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.engage.core)

    // Google Mobile Ads (AdMob)
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    // Google Play Billing
    implementation(libs.billing.ktx)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.credentials)
    implementation(libs.credentials.play)
    implementation(libs.googleid)

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // ML Kit Barcode Scanning
    implementation(libs.mlkit.barcode)

    // Accompanist Permissions
    implementation(libs.accompanist.permissions)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}