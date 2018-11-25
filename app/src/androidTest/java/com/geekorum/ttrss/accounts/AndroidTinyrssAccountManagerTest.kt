/**
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.accounts

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.PeriodicSync
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geekorum.geekdroid.security.SecretCipher
import com.geekorum.geekdroid.security.SecretEncryption
import com.geekorum.ttrss.BackgroundJobManager
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.sync.SyncContract
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AndroidTinyrssAccountManagerTest {

    lateinit var accountManager: AndroidTinyrssAccountManager
    lateinit var androidAccountManager: AccountManager
    lateinit var secretCipher: SecretCipher

    private val modelAccount = Account("test", "https://exemple.com")
    private val androidAccount = android.accounts.Account("test", AndroidTinyrssAccountManager.ACCOUNT_TYPE)

    @Before
    fun setup() {
        androidAccountManager = AccountManager.get(ApplicationProvider.getApplicationContext())
        secretCipher = SecretEncryption().getSecretCipher("instrumented test")
        accountManager = AndroidTinyrssAccountManager(androidAccountManager, secretCipher)
    }


    @After
    fun tearDown() {
        androidAccountManager.removeAccountExplicitly(androidAccount)
    }

    @Test
    fun testThatAddAccountWorks() {
        val account = modelAccount
        val result = accountManager.addAccount(account, "password")
        assertThat(result).isTrue()
        assertThat(androidAccountManager.getAccountsByType(AndroidTinyrssAccountManager.ACCOUNT_TYPE)).asList().contains(
            androidAccount)
    }


    @Test
    fun testThatInitializeAccountSyncWorks() {
        val urlBundle = Bundle().apply {
            putString(AccountAuthenticator.USERDATA_URL, "https://exemple.com")
        }
        androidAccountManager.addAccountExplicitly(androidAccount, "password", urlBundle)
        val periodicRefreshSync = PeriodicSync(androidAccount, ArticlesContract.AUTHORITY, Bundle.EMPTY,
            BackgroundJobManager.PERIODIC_REFRESH_JOB_INTERVAL_S)

        val fullExtra = Bundle().apply {
            putInt(SyncContract.EXTRA_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH, -1)
        }
        val periodicFullRefreshSync = PeriodicSync(androidAccount, ArticlesContract.AUTHORITY, fullExtra,
            BackgroundJobManager.PERIODIC_FULL_REFRESH_JOB_INTERVAL_S)

        accountManager.initializeAccountSync(modelAccount)
        // the system needs some time to initialize
        runBlocking{ delay(500) }

        val syncAutomatically = ContentResolver.getSyncAutomatically(androidAccount, ArticlesContract.AUTHORITY)
        assertThat(syncAutomatically).isTrue()
        val periodicSyncs = ContentResolver.getPeriodicSyncs(androidAccount, ArticlesContract.AUTHORITY)
        assertThat(periodicSyncs).hasSize(2)
        assertThat(periodicSyncs).containsExactly(periodicFullRefreshSync, periodicRefreshSync)
    }
}
