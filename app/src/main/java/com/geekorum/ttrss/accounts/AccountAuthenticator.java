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
package com.geekorum.ttrss.accounts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.geekorum.ttrss.BuildConfig;
import com.geekorum.ttrss.R;
import com.geekorum.ttrss.network.ApiCallException;
import com.geekorum.ttrss.network.impl.LoginRequestPayload;
import com.geekorum.ttrss.network.impl.LoginResponsePayload;
import com.geekorum.ttrss.network.impl.TinyRssApi;
import kotlinx.coroutines.future.FutureKt;
import timber.log.Timber;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    public static final String TTRSS_ACCOUNT_TYPE = BuildConfig.APPLICATION_ID;
    public static final String TTRSS_AUTH_TOKEN_SESSION_ID = "session_id";
    public static final int ERROR_CODE_AUTHENTICATOR_FAILURE = 500;
    public static final int ERROR_CODE_NOT_SUPPORTED = 501;
    public static final String USERDATA_URL = "url";

    private final Context context;
    private final AndroidTinyrssAccountManager accountManager;
    private final AuthenticatorNetworkComponent.Builder authenticatorBuilder;

    @Inject
    public AccountAuthenticator(Context context,
                                AndroidTinyrssAccountManager accountManager,
                                AuthenticatorNetworkComponent.Builder authenticatorBuilder) {
        super(context);
        this.context = context;
        this.accountManager = accountManager;
        this.authenticatorBuilder = authenticatorBuilder;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return new Bundle();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
                             String[] requiredFeatures, Bundle options) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.setAction(LoginActivity.ACTION_ADD_ACCOUNT);
        Bundle result = new Bundle();
        result.putParcelable(AccountManager.KEY_INTENT, intent);
        return result;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.setAction(LoginActivity.ACTION_CONFIRM_CREDENTIALS);
        intent.putExtra(LoginActivity.EXTRA_ACCOUNT, account);
        Bundle result = new Bundle();
        result.putParcelable(AccountManager.KEY_INTENT, intent);
        return result;
    }

    @Override
    @SuppressLint("MissingPermission")
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                               String authTokenType, Bundle options) {
        Bundle result = new Bundle();
        com.geekorum.ttrss.accounts.Account ttRssAccount = accountManager.fromAndroidAccount(account);
        String password;
        try {
            password = accountManager.getPassword(ttRssAccount);
        } catch (Exception e) {
            Timber.w(e, "Unable to get encrypted password");
            return getRevalidateCredentialResponse(account);
        }
        try {
            LoginResponsePayload responsePayload = login(ttRssAccount.getUrl(), ttRssAccount.getUsername(), password);
            if (responsePayload.isStatusOk()) {
                String sessionId = responsePayload.getSessionId();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, sessionId);
                return result;
            }
            ApiCallException.ApiError error = responsePayload.getError();
            if (error == ApiCallException.ApiError.LOGIN_FAILED) {
                Timber.w("Login failed: Invalid credentials");
                return getRevalidateCredentialResponse(account);
            }
        } catch (InterruptedException | ExecutionException e) {
            Timber.e(e, "Unable to login");
        }
        // if we got there an error happened, probably network
        result.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_NETWORK_ERROR);
        result.putString(AccountManager.KEY_ERROR_MESSAGE, "Unable to login");
        return result;
    }

    @NonNull
    private Bundle getRevalidateCredentialResponse(Account account) {
        Bundle result = new Bundle();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setAction(LoginActivity.ACTION_CONFIRM_CREDENTIALS);
        intent.putExtra(LoginActivity.EXTRA_ACCOUNT, account);
        result.putParcelable(AccountManager.KEY_INTENT, intent);
        return result;
    }

    private LoginResponsePayload login(String url, String user, String password) throws ExecutionException, InterruptedException {
        TinyRssUrlModule urlModule = new TinyRssUrlModule(url);
        AuthenticatorNetworkComponent authenticatorNetworkComponent = authenticatorBuilder
                .tinyRssUrlModule(urlModule)
                .build();
        TinyRssApi api = authenticatorNetworkComponent.getTinyRssApi();
        LoginRequestPayload payload = new LoginRequestPayload(user, password);
        CompletableFuture<LoginResponsePayload> future = FutureKt.asCompletableFuture(api.login(payload));
        return future.get();
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return context.getResources().getString(R.string.ttrss_account);
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                    String authTokenType, Bundle options) {
        // let's just say it is not supported yet
        return makeNotSupportedResponse();
    }

    @NonNull
    private Bundle makeNotSupportedResponse() {
        Bundle result = new Bundle();
        result.putInt(AccountManager.KEY_ERROR_CODE, ERROR_CODE_NOT_SUPPORTED);
        result.putString(AccountManager.KEY_ERROR_MESSAGE, "Not supported");
        return result;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
        // let's say no for now unless features is empty
        boolean supported = false;
        if (features.length == 0) {
            supported = true;
        }
        Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, supported);
        return result;
    }
}
