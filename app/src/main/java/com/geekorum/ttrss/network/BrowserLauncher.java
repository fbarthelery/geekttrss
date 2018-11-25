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
package com.geekorum.ttrss.network;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsService;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import android.text.TextUtils;
import com.geekorum.ttrss.R;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by darisk on 2/19/17.
 */

public class BrowserLauncher {

    private final Application application;
    private final PackageManager packageManager;
    private CustomTabsClient customTabsClient;

    private CustomTabsSession customTabsSession;
    private boolean serviceBinded;

    private final CustomTabsServiceConnection customTabsConnection = new CustomTabsServiceConnection() {
        @Override
        public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
            customTabsClient = client;
            customTabsClient.warmup(0);
            customTabsSession = customTabsClient.newSession(null);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            customTabsClient = null;
        }
    };

    @Inject
    public BrowserLauncher(Application application, PackageManager packageManager) {
        this.application = application;
        this.packageManager = packageManager;
    }

    public void warmUp() {
        String packageName = CustomTabsClient.getPackageName(application, getBrowserPackageNames());
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        serviceBinded = CustomTabsClient.bindCustomTabsService(application, packageName, customTabsConnection);
    }

    public void shutdown() {
        if (serviceBinded) {
            application.unbindService(customTabsConnection);
        }
    }

    private List<String> getBrowserPackageNames() {
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(activityIntent, PackageManager.MATCH_DEFAULT_ONLY);
        List<String> result = new LinkedList<>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            result.add(resolveInfo.activityInfo.packageName);
        }
        return result;
    }

    public void mayLaunchUri(Uri ... uris) {
        if (customTabsSession != null) {
            if (uris != null && uris.length > 0) {
                Uri uri = uris[0];
                List<Bundle> otherLikelyBundles = createLikelyBundles(uris);
                customTabsSession.mayLaunchUrl(uri, null, otherLikelyBundles);
            }
        }
    }

    private List<Bundle> createLikelyBundles(Uri... uris) {
        List<Bundle> result = new LinkedList<>();
        for (int i = 1; i < uris.length; i++) { // skip the first
            Uri uri = uris[i];
            Bundle b = new Bundle();
            b.putParcelable(CustomTabsService.KEY_URL, uri);
            result.add(b);
        }
        return result;
    }

    public void launchUrl(Context context, Uri uri, CustomTabIntentCustomizer customizer) {
        if (customTabsSession != null) {
            int tabColor = context.getColor(R.color.primary);
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(customTabsSession);
            builder
                    .setToolbarColor(tabColor)
                    .setShowTitle(true)
                    .enableUrlBarHiding();
            if (customizer != null) {
                customizer.customizeTabIntent(builder);
            }
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER_NAME,
                    Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + context.getPackageName()));
            customTabsIntent.launchUrl(context, uri);
        } else {
            launchUriInOtherApp(uri);
        }
    }

    public void launchUrl(Context context, Uri uri) {
        launchUrl(context, uri, null);
    }

    private void launchUriInOtherApp(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        application.startActivity(intent);
    }

    public interface CustomTabIntentCustomizer{
        void customizeTabIntent(CustomTabsIntent.Builder builder);
    }
}
