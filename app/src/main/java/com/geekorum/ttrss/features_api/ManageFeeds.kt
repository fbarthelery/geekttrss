/**
 * Apis that can be used by feature modules to access component of the application
 */
package com.geekorum.ttrss.features_api

import android.accounts.AccountManager
import android.os.PowerManager
import com.geekorum.ttrss.data.ArticlesDatabase

/**
 * Provides dependencies for the ManageFeeds feature.
 * All classes returned by this interface must be part of the public api of the application.
 * If from an external lib, the lib must be in the api gradle configuration
 */
interface ManageFeedsDependencies {

    fun getApplication(): android.app.Application

    fun getAccountManager(): AccountManager

    fun getPowerManager(): PowerManager

    fun getArticlesDatabase(): ArticlesDatabase

}
