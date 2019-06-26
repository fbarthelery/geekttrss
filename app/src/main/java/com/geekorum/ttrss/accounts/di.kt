/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2019 by Frederic-Charles Barthelery.
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
import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.geekorum.geekdroid.dagger.ViewModelKey
import com.geekorum.geekdroid.network.TokenRetriever
import com.geekorum.geekdroid.security.SecretEncryption
import com.geekorum.ttrss.network.TinyrssApiModule
import com.geekorum.ttrss.webapi.LoggedRequestInterceptorFactory
import com.geekorum.ttrss.webapi.TinyRssApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Scope
import kotlin.annotation.AnnotationRetention.RUNTIME
import com.geekorum.ttrss.webapi.TokenRetriever as WebapiTokenRetriever

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
class AndroidTinyrssAccountManagerModule {

    companion object {
        private const val KEY_ALIAS = "com.geekorum.geekttrss.accounts.AccountManagerKey"
    }

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

/**
 * Provides the Services injectors subcomponents.
 */
@Module
abstract class ServicesInjectorModule {

    @ContributesAndroidInjector(modules = [AuthenticatorServiceModule::class])
    internal abstract fun contributesAuthenticatorServiceInjector(): AuthenticatorService

    @ContributesAndroidInjector(modules = [AuthenticatorActivityModule::class, ViewModelsModule::class])
    internal abstract fun contributesLoginActivityInjector(): LoginActivity

}


@Module
class NetworkLoginModule {

    @Provides
    @PerAccount
    fun providesTokenRetriever(accountManager: AccountManager, account: Account): TokenRetriever {
        return TinyrssAccountTokenRetriever(accountManager, account)
    }

    @Provides
    @PerAccount
    fun providesWebApiTokenRetriever(tokenRetriever: TokenRetriever ): WebapiTokenRetriever {
        return object : WebapiTokenRetriever {
            override fun getToken(): String {
                return try {
                    tokenRetriever.token
                } catch (e: TokenRetriever.RetrieverException) {
                    throw WebapiTokenRetriever.RetrieverException(e.message, e)
                }
            }

            override fun invalidateToken() {
                tokenRetriever.invalidateToken()
            }
        }
    }


    @Provides
    @PerAccount
    fun providesLoggedRequestInterceptorFactory(tokenRetriever: WebapiTokenRetriever): LoggedRequestInterceptorFactory {
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
internal class AuthenticatorServiceModule {

    @Provides
    fun providesContext(service: AuthenticatorService): Context {
        return service
    }

}

@Module(subcomponents = [AuthenticatorNetworkComponent::class])
internal class AuthenticatorActivityModule {
    @Provides
    fun providesContext(activity: Activity): Context {
        return activity
    }
}


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
internal class TinyRssServerInformationModule(val serverInformation: ServerInformation) {
    @Provides
    fun providesServerInformation(): ServerInformation {
        return serverInformation
    }

}

@Module
private abstract class ViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun getLoginViewModel(loginViewModel: LoginViewModel): ViewModel

}
