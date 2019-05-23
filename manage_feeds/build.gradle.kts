import com.geekorum.build.configureJavaVersion
import com.geekorum.build.enforcedDaggerPlatform

plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
    kotlin("kapt")
    id("com.geekorum.build.android-tests")
    id("com.geekorum.build.android-genymotion")
    id("com.geekorum.build.source-license-checker")
}

android {
    val compileSdkVersion: String by rootProject.extra
    setCompileSdkVersion(compileSdkVersion)

    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(28)

        missingDimensionStrategy("distribution", "free", "google")
    }

    configureJavaVersion()

    dataBinding {
        isEnabled = true
    }
}


dependencies {
    val kotlinVersion: String by rootProject.extra
    implementation(enforcedPlatform(kotlin("bom", kotlinVersion)))
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":app"))

}
