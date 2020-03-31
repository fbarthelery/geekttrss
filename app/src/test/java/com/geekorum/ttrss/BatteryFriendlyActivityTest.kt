/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
 *
 * This file is part of Geekttrss.
 *
 * Geekttrss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Geekttrss is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Geekttrss.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.geekorum.ttrss

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PowerManager
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Module
import dagger.android.AndroidInjection
import dagger.android.ContributesAndroidInjector
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.Shadows
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class BatteryFriendlyActivityTest {
    val application: Application = ApplicationProvider.getApplicationContext()

    @BeforeTest
    fun setUp() {
        // declare BatteryFriendlyActivity to handle UI mode configuration change
        // this allows the test to not care about the activity being destroyed/recreated
        val packageManager = Shadows.shadowOf(application.packageManager)
        val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
        val batteryFriendly = ActivityInfo().apply {
            configChanges = ActivityInfo.CONFIG_UI_MODE
            name = BatteryFriendlyActivityRecordNightModeChanged::class.qualifiedName
            applicationInfo = packageInfo.applicationInfo
            packageName = packageInfo.packageName
        }
        packageManager.addOrUpdateActivity(batteryFriendly)
    }

    @Test
    fun testThatWhenPowerSaveIsOnNightModeConfigurationIsUsed() {
        val scenario = ActivityScenario.launch(BatteryFriendlyActivityRecordNightModeChanged::class.java)
        scenario.onActivity {
            assertThat(it.nightMode).isEqualTo(MODE_NIGHT_UNSPECIFIED)
        }

        val powerManager = Shadows.shadowOf(application.getSystemService<PowerManager>())
        powerManager.setIsPowerSaveMode(true)
        application.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))

        scenario.onActivity {
            assertThat(it.nightMode).isEqualTo(MODE_NIGHT_YES)
        }
    }

    @Test
    fun testThatWhenBatteryIsLowNightModeConfigurationIsUsed() {
        val scenario = ActivityScenario.launch(BatteryFriendlyActivityRecordNightModeChanged::class.java)
        scenario.onActivity {
            assertThat(it.nightMode).isEqualTo(MODE_NIGHT_UNSPECIFIED)
        }

        val application = ApplicationProvider.getApplicationContext<Application>()
        application.sendBroadcast(Intent(Intent.ACTION_BATTERY_LOW))

        scenario.onActivity {
            assertThat(it.nightMode).isEqualTo(MODE_NIGHT_YES)
        }
    }

}

class ForceNightModeViewModelTest {

    lateinit var viewModel: ForceNightModeViewModel
    lateinit var batterySaverLivedate: MutableLiveData<Boolean>
    lateinit var lowBatteryLiveData: MutableLiveData<Boolean>

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @BeforeTest
    fun setUp() {
        batterySaverLivedate = MutableLiveData()
        lowBatteryLiveData = MutableLiveData()
        viewModel = ForceNightModeViewModel(batterySaverLivedate, lowBatteryLiveData)
    }

    @Test
    fun testThatWhenBatteryIsLowWeForceNight() {
        val observer = mockk<Observer<Boolean>>(relaxed = true)
        batterySaverLivedate.value = false
        lowBatteryLiveData.value = true
        viewModel.forceNightMode.observeForever(observer)
        verifySequence {
            observer.onChanged(true)
        }
    }

    @Test
    fun testThatWhenSavingBatteryWeForceNight() {
        val observer = mockk<Observer<Boolean>>(relaxed = true)
        batterySaverLivedate.value = true
        lowBatteryLiveData.value = false
        viewModel.forceNightMode.observeForever(observer)
        verifySequence {
            observer.onChanged(true)
        }
    }

    @Test
    fun testThatWhenSavingBatteryAndLowBatteryWeForceNight() {
        val observer = mockk<Observer<Boolean>>(relaxed = true)
        batterySaverLivedate.value = true
        lowBatteryLiveData.value = true
        viewModel.forceNightMode.observeForever(observer)
        verifySequence {
            observer.onChanged(true)
        }
    }

    @Test
    fun testThatWhenNotSavingBatteryAndNotLowBatteryWeDontForceNight() {
        val observer = mockk<Observer<Boolean>>(relaxed = true)
        batterySaverLivedate.value = false
        lowBatteryLiveData.value = false
        viewModel.forceNightMode.observeForever(observer)
        verifySequence {
            observer.onChanged(false)
        }
    }
}

class BatteryFriendlyActivityRecordNightModeChanged : BatteryFriendlyActivity() {
    var nightMode: Int  = MODE_NIGHT_UNSPECIFIED

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onNightModeChanged(mode: Int) {
        nightMode = mode
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return ForceNightModeViewModel(application,
                    application.getSystemService()!!
                ) as T
            }
        }
    }
}


@Module
abstract class BatteryFriendlyActivityTestModule {

    @ContributesAndroidInjector
    internal abstract fun contributesBatteryFriendlyActivityInjector(): BatteryFriendlyActivityRecordNightModeChanged

}
