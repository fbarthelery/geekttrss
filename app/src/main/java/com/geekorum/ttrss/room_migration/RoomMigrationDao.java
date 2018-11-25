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
package com.geekorum.ttrss.room_migration;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import com.geekorum.ttrss.data.Article;
import com.geekorum.ttrss.data.Category;
import com.geekorum.ttrss.data.Feed;
import com.geekorum.ttrss.data.Transaction;

/**
 * Dao to perform Room migration.
 */
@Dao
public interface RoomMigrationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticles(Article... articles);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(Category... categories);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFeeds(Feed... feeds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTranscations(Transaction... transactions);


}
