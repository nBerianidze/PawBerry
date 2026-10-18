import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.pawberry"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.pawberry"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "HOST_IPV4", "\"${hostIpv4()}\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

fun hostIpv4(): String = try {
    val addresses = NetworkInterface.getNetworkInterfaces().toList()
        .filter { it.isUp && !it.isLoopback }
        .flatMap { it.inetAddresses.toList() }
        .filterIsInstance<Inet4Address>()
        .mapNotNull { it.hostAddress }
        .filterNot { it.startsWith("169.254.") }
    addresses.firstOrNull { it.startsWith("192.168.") }
        ?: addresses.firstOrNull()
        ?: ""
} catch (_: Exception) {
    ""
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

val adbReverseBackend = tasks.register<Exec>("adbReverseBackend") {
    isIgnoreExitValue = true
    val localProperties = Properties()
    rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use { localProperties.load(it) }
    val sdkDir = localProperties.getProperty("sdk.dir")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: System.getenv("ANDROID_HOME")
        ?: error("Android SDK location not found")
    val adbName = if (System.getProperty("os.name").startsWith("Windows")) "adb.exe" else "adb"
    commandLine(File(sdkDir, "platform-tools/$adbName"), "reverse", "tcp:3000", "tcp:3000")
}

tasks.configureEach {
    if (name == "installDebug" || name == "connectedDebugAndroidTest") {
        dependsOn(adbReverseBackend)
    }
}