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
package com.geekorum.ttrss.accounts

import android.accounts.AccountManager
import android.content.ContentResolver
import android.os.Bundle
import android.os.StrictMode
import android.util.Base64
import com.geekorum.geekdroid.security.SecretCipher
import com.geekorum.ttrss.background_job.BackgroundJobManager
import com.geekorum.ttrss.debugtools.withStrictMode
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.sync.SyncContract
import timber.log.Timber
import java.security.GeneralSecurityException
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

/**
 * Represents a Tinyrss account
 */
data class Account(val username: String, val url: String)

/**
 * Holds information about the TinyTinyRss server we are connecting to.
 */
abstract class ServerInformation {
    abstract val apiUrl: String
    abstract val basicHttpAuthUsername: String?
    abstract val basicHttpAuthPassword: String?
}

/**
 * API of the AccountManager for Tinyrss
 */
interface TinyrssAccountManager {

    /**
     * Add an account.
     * @return true on success
     */
    fun addAccount(account: Account, password: String): Boolean

    /**
     * Initialize synchronisation jobs for an account.
     */
    fun initializeAccountSync(account: Account)

    fun updatePassword(account: Account, password: String)
}


/**
 * Implementation of [TinyrssAccountManager] on Android platform
 */
class AndroidTinyrssAccountManager @Inject constructor(
    private val accountManager: AccountManager,
    private val secretCipher: SecretCipher
) : TinyrssAccountManager {

    companion object {
        const val ACCOUNT_TYPE = AccountAuthenticator.TTRSS_ACCOUNT_TYPE
    }

    override fun addAccount(account: Account, password: String): Boolean {
        val androidAccount = android.accounts.Account(account.username, ACCOUNT_TYPE)
        val userdata = Bundle()
        userdata.putString(AccountAuthenticator.USERDATA_URL, account.url)
        val encryptedPassword = encrypt(password)
        return accountManager.addAccountExplicitly(androidAccount, encryptedPassword, userdata)
    }

    override fun updatePassword(account: Account, password: String) {
        val androidAccount = android.accounts.Account(account.username, ACCOUNT_TYPE)
        val encryptedPassword = encrypt(password)
        accountManager.setPassword(androidAccount, encryptedPassword)
    }

    override fun initializeAccountSync(account: Account) {
        val extras = Bundle()
        extras.putInt(SyncContract.EXTRA_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH, -1)
        extras.putBoolean(SyncContract.EXTRA_UPDATE_FEED_ICONS, true)
        android.accounts.Account(account.username, ACCOUNT_TYPE).also {
            ContentResolver.setSyncAutomatically(it, ArticlesContract.AUTHORITY, true)
            ContentResolver.addPeriodicSync(it, ArticlesContract.AUTHORITY, Bundle(),
                BackgroundJobManager.PERIODIC_REFRESH_JOB_INTERVAL_S)
            ContentResolver.addPeriodicSync(it, ArticlesContract.AUTHORITY, extras,
                BackgroundJobManager.PERIODIC_FULL_REFRESH_JOB_INTERVAL_S)
        }
    }

    @Throws(GeneralSecurityException::class, IllegalArgumentException::class)
    fun getPassword(account: Account): String? {
        val encryptedPassword: String? = android.accounts.Account(account.username, ACCOUNT_TYPE).let {
            accountManager.getPassword(it)
        }
        return encryptedPassword?.let { decrypt(it) }
    }

    fun fromAndroidAccount(androidAccount: android.accounts.Account): Account {
        return withStrictMode(StrictMode.allowThreadDiskReads()) {
            check(ACCOUNT_TYPE == androidAccount.type) { "Invalid account type ${androidAccount.type}" }
            val url = accountManager.getUserData(androidAccount, AccountAuthenticator.USERDATA_URL)
            Account(androidAccount.name, url)
        }
    }

    fun updateServerInformation(account: Account, serverInformation: ServerInformation) {
        val androidAccount = android.accounts.Account(account.username, ACCOUNT_TYPE)
        val encryptedPassword = serverInformation.basicHttpAuthPassword?.let { encrypt(it) }
        accountManager.run {
            setUserData(androidAccount, AccountAuthenticator.USERDATA_URL, serverInformation.apiUrl)
            setUserData(androidAccount, AccountAuthenticator.USERDATA_BASIC_HTTP_AUTH_USERNAME,
                serverInformation.basicHttpAuthUsername)
            setUserData(androidAccount, AccountAuthenticator.USERDATA_BASIC_HTTP_AUTH_PASSWORD, encryptedPassword)
        }
    }

    fun getServerInformation(account: Account): ServerInformation {
        val androidAccount = android.accounts.Account(account.username, ACCOUNT_TYPE)
        return object : ServerInformation() {
            override val apiUrl: String
                get() = accountManager.getUserData(androidAccount, AccountAuthenticator.USERDATA_URL)

            override val basicHttpAuthUsername: String?
                get() =  accountManager.getUserData(androidAccount,
                    AccountAuthenticator.USERDATA_BASIC_HTTP_AUTH_USERNAME)

            override val basicHttpAuthPassword: String?
                get() {
                    val encryptedPassword = accountManager.getUserData(androidAccount,
                        AccountAuthenticator.USERDATA_BASIC_HTTP_AUTH_PASSWORD)
                    return try {
                        encryptedPassword?.let { decrypt(it) }
                    } catch (e: Exception) {
                        Timber.w(e, "unable to decrypt basic http auth password")
                        null
                    }
                }
        }
    }


    private fun decrypt(encryptedPassword: String): String {
        val lines = encryptedPassword.lines()
        val encryptedPasswordPart = lines[0]
        val iv = Base64.decode(lines[1], Base64.NO_WRAP)
        val tlen = lines[2].toInt()
        val input = Base64.decode(encryptedPasswordPart, Base64.NO_WRAP)
        val gcmParameterSpec = GCMParameterSpec(tlen, iv)
        val output = secretCipher.decrypt(input, gcmParameterSpec)
        return output.toString(Charsets.UTF_8)
    }

    private fun encrypt(plaintextPassword: String): String {
        val input = plaintextPassword.toByteArray(Charsets.UTF_8)
        val output = secretCipher.encrypt(input)
        val base64EncryptedPassword = Base64.encodeToString(output, Base64.NO_WRAP)
        val gcmParameterSpec = secretCipher.parametersSpec
        val base64IV = Base64.encodeToString(gcmParameterSpec.iv, Base64.NO_WRAP)
        return "$base64EncryptedPassword\n$$base64IV\n${gcmParameterSpec.tLen}\n"
    }
}

