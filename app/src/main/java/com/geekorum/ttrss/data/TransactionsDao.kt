/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.runBlocking

/**
 * Dao to read/modify transactions.
 */
@Dao
abstract class TransactionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTransaction(transaction: Transaction)

    @Delete
    abstract suspend fun deleteTransactions(transactions: Collection<Transaction>)


    @Query("SELECT * FROM transactions WHERE article_id=:articleId AND field=:field")
    abstract suspend fun getTransactionForArticleAndType(articleId: Long, field: String): List<Transaction>

    /**
     * Insert a unique transaction for a [Transaction.articleId] [Transaction.field] pair.
     *
     * @param transaction
     */
    @androidx.room.Transaction
    open fun insertUniqueTransaction(transaction: Transaction) = runBlocking {
        val existingTransactions = getTransactionForArticleAndType(transaction.articleId, transaction.field)
        deleteTransactions(existingTransactions)
        insertTransaction(transaction)
    }

}
