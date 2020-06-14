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

import android.accounts.Account
import android.accounts.AccountManager
import com.geekorum.geekdroid.security.SecretEncryption
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.network.TinyrssApiModule
import com.geekorum.ttrss.webapi.LoggedRequestInterceptorFactory
import com.geekorum.ttrss.webapi.TinyRssApi
import com.geekorum.ttrss.webapi.TokenRetriever
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Scope
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Dependency injection pieces for the account authenticator functionality.
 *
 * AuthenticatorService has a SubComponent of the ApplicationComponent.
 * The AuthenticatorNetworkComponent is a  SubSubComponent that allows to do network
 * request with the Account backend
 *
 */

@Retention(RUNTIME)
@MustBeDocumented
@Scope
annotation class PerAccount

@Module
@InstallIn(ApplicationComponent::class)
object AndroidTinyrssAccountManagerModule {

    private const val KEY_ALIAS = "com.geekorum.geekttrss.accounts.AccountManagerKey"

    @Provides
    fun providesAndroidTinyrssAccountManager(accountManager: AccountManager, secretEncryption: SecretEncryption): AndroidTinyrssAccountManager {
        val secretCipher = secretEncryption.getSecretCipher(KEY_ALIAS)
        return AndroidTinyrssAccountManager(accountManager, secretCipher)
    }

    // may be replaced by a @Bind but it complicates the syntax in kotlin
    @Provides
    fun providesTinyrssAccountManager(androidTinyrssAccountManager: AndroidTinyrssAccountManager): TinyrssAccountManager {
        return androidTinyrssAccountManager
    }
}


@Module
@DisableInstallInCheck
object NetworkLoginModule {

    @Provides
    @PerAccount
    fun providesTokenRetriever(dispatchers: CoroutineDispatchersProvider,
                               accountManager: AccountManager, account: Account): TokenRetriever {
        return TinyrssAccountTokenRetriever(dispatchers, accountManager, account)
    }

    @Provides
    @PerAccount
    fun providesLoggedRequestInterceptorFactory(tokenRetriever: TokenRetriever): LoggedRequestInterceptorFactory {
        return LoggedRequestInterceptorFactory(tokenRetriever)
    }

    @Provides
    fun providesServerInformation(accountManager: AndroidTinyrssAccountManager, account: Account): ServerInformation {
        return with(accountManager) {
            val ttRssAccount = fromAndroidAccount(account)
            getServerInformation(ttRssAccount)
        }
    }

}


@Module(subcomponents = [AuthenticatorNetworkComponent::class])
@InstallIn(ServiceComponent::class)
internal object AuthenticatorServiceModule

@Module(subcomponents = [AuthenticatorNetworkComponent::class])
@InstallIn(ApplicationComponent::class)
internal object AuthenticatorActivityModule

@Subcomponent(modules = [TinyRssServerInformationModule::class, TinyrssApiModule::class])
internal interface AuthenticatorNetworkComponent {
    fun getTinyRssApi(): TinyRssApi

    @Subcomponent.Builder
    interface Builder {
        fun build(): AuthenticatorNetworkComponent

        fun tinyRssServerInformationModule(module: TinyRssServerInformationModule): Builder
    }
}

@Module
@DisableInstallInCheck
internal class TinyRssServerInformationModule(val serverInformation: ServerInformation) {
    @Provides
    fun providesServerInformation(): ServerInformation {
        return serverInformation
    }

}

