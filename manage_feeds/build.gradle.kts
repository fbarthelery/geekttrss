import com.geekorum.build.dualTestImplementation
import com.geekorum.build.enforcedDaggerPlatform

plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
    kotlin("kapt")
    id("com.geekorum.build.android-tests")
    id("com.geekorum.build.android-genymotion")
    id("com.geekorum.build.source-license-checker")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    val compileSdkVersion: String by rootProject.extra
    setCompileSdkVersion(compileSdkVersion)

    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(28)
    }

    dataBinding {
        isEnabled = true
    }

    flavorDimensions("distribution")
    productFlavors {
        register("free") {
            dimension = "distribution"
        }

        register("google") {
            dimension = "distribution"
        }
    }

    buildTypes {
        named("release") {
            proguardFile("proguard-rules.pro")
        }
    }
}


dependencies {
    implementation(project(":app"))

    val kotlinVersion: String by rootProject.extra
    implementation(enforcedPlatform(kotlin("bom", kotlinVersion)))
    implementation(kotlin("stdlib-jdk8"))

    val daggerVersion: String by rootProject.extra
    implementation(enforcedDaggerPlatform(daggerVersion))
    kapt(enforcedDaggerPlatform(daggerVersion))
    implementation("com.google.dagger:dagger:$daggerVersion")
    implementation("com.google.dagger:dagger-android:$daggerVersion")
    implementation("com.google.dagger:dagger-android-support:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    kapt("com.google.dagger:dagger-android-processor:$daggerVersion")
    kaptTest("com.google.dagger:dagger-compiler:$daggerVersion")
    kaptTest("com.google.dagger:dagger-android-processor:$daggerVersion")

    val GEEKDROID_PROJECT_DIR: String? by project
    val geekdroidExt = GEEKDROID_PROJECT_DIR?.let { "" } ?: "aar"
    implementation(group = "com.geekorum", name = "geekdroid", version = "0.0.1", ext = geekdroidExt)

    implementation("androidx.activity:activity-ktx:1.0.0-beta01")

    // androidx UI
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.0.0")
    implementation("com.google.android.material:material:1.1.0-alpha07")

    implementation("androidx.core:core-ktx:1.0.2")

    val lifecycleVersion: String by rootProject.extra
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    implementation(project(":htmlparsers"))
    implementation(project(":webapi"))

    androidTestImplementation("androidx.work:work-testing:2.1.0")
    dualTestImplementation("androidx.arch.core:core-testing:2.0.1")

    implementation("androidx.navigation:navigation-fragment-ktx:2.1.0-alpha06")
    debugImplementation("androidx.fragment:fragment-testing:1.2.0-alpha01")

}
