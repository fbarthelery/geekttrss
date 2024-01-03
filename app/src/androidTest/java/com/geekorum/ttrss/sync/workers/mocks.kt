/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.sync.workers

import android.accounts.Account
import android.app.Application
import androidx.room.Room
import com.geekorum.ttrss.accounts.AndroidTinyrssAccountManager
import com.geekorum.ttrss.accounts.ServerInformation
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleWithAttachments
import com.geekorum.ttrss.data.ArticlesDatabase
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.migrations.ALL_MIGRATIONS
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.network.ServerInfo
import com.geekorum.ttrss.providers.ArticlesContract
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.hilt.migration.DisableInstallInCheck
import okio.BufferedSource
import java.util.concurrent.Executor
import java.util.concurrent.Executors


internal open class MockApiService : ApiService {

    override suspend fun getArticles(feedId: Long, sinceId: Long, offset: Int, showExcerpt: Boolean, showContent: Boolean, includeAttachments: Boolean): List<ArticleWithAttachments> {
        return if (offset == 0) {
            val article = Article(id = 1, isUnread = true)
            listOf(ArticleWithAttachments(article, emptyList()))
        } else {
            emptyList()
        }
    }

    override suspend fun getArticlesOrderByDateReverse(feedId: Long, sinceId: Long, offset: Int, showExcerpt: Boolean, showContent: Boolean, includeAttachments: Boolean): List<ArticleWithAttachments> {
        return getArticles(feedId, sinceId, offset, showExcerpt, showContent, includeAttachments)
    }

    override suspend fun getCategories(): List<Category> {
        TODO("not implemented")
    }

    override suspend fun getFeeds(): List<Feed> {
        TODO("not implemented")
    }

    override suspend fun getServerInfo(): ServerInfo {
        TODO("not implemented")
    }

    override suspend fun updateArticleField(id: Long, field: ArticlesContract.Transaction.Field, value: Boolean) {
        TODO("not implemented")
    }

    override suspend fun getFeedIcon(feedId: Long): BufferedSource {
        TODO("Not yet implemented")
    }

    override suspend fun markFeedAsRead(feedId: Long) {
        TODO("Not yet implemented")
    }

}


@Subcomponent(modules = [
    FakeNetworkLoginModule::class
])
interface FakeSyncWorkerComponent : SyncWorkerComponent {
    @Subcomponent.Builder
    interface Builder : SyncWorkerComponent.Builder
}

@Module(subcomponents = [FakeSyncWorkerComponent::class])
@DisableInstallInCheck
abstract class FakeSyncWorkersModule {
    @Binds
    abstract fun bindsSyncWorkerComponentBuilder(builder: FakeSyncWorkerComponent.Builder): SyncWorkerComponent.Builder
}

@Module
@DisableInstallInCheck
object FakeNetworkLoginModule {

    @Provides
    fun providesServerInformation(accountManager: AndroidTinyrssAccountManager, account: Account): ServerInformation {
        return object : ServerInformation() {
            override val apiUrl: String = "https://test.exemple.com/"
            override val basicHttpAuthUsername: String? = null
            override val basicHttpAuthPassword: String? = null
        }
    }

}

internal fun buildInMemoryDatabase(application: Application,
                                   queryExecutor: Executor,
                                   transactionExecutor: Executor = Executors.newSingleThreadExecutor()
): ArticlesDatabase {
    /*
     * Due to a deadlock in room we need to provide a transactionExecutor running on separate
     * thread. See
     * https://medium.com/@eyalg/testing-androidx-room-kotlin-coroutines-2d1faa3e674f
     * https://github.com/Kotlin/kotlinx.coroutines/pull/1206
     * https://issuetracker.google.com/issues/135334849
     */
    return Room.inMemoryDatabaseBuilder(application, ArticlesDatabase::class.java)
        .fallbackToDestructiveMigrationOnDowngrade()
        .setQueryExecutor(queryExecutor)
        .setTransactionExecutor(transactionExecutor)
        .addMigrations(*ALL_MIGRATIONS.toTypedArray())
        .build()
}
