package com.geekorum.ttrss.debugtools

import android.content.ContentResolver
import android.provider.Settings

/**
 * Test if the application is running on a Firebase test device
 */
fun isFirebaseTestDevice(contentResolver: ContentResolver): Boolean {
    val testLabSetting = Settings.System.getString(contentResolver, "firebase.test.lab")
    return "true" == testLabSetting
}
