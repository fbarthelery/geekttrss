/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


/**
 * Wait for completion of this [ViewModel.viewModelScope] children coroutines
 */
suspend fun ViewModel.waitForChildrenCoroutines() {
    viewModelScope.coroutineContext[Job]!!.children.forEach { it.join() }
}

/**
 * Wait for this LiveData to obtain a value
 */
fun <T> LiveData<T>.waitForValue(numberOfChanged: Int = 1, timeout: Long = 2000, unit: TimeUnit = TimeUnit.MILLISECONDS) {
    val latch = CountDownLatch(numberOfChanged)
    val observer: Observer<T> = Observer { latch.countDown() }
    try {
        observeForever(observer)
        if (!latch.await(timeout, unit)) {
            throw TimeoutException("LiveData value was never set")
        }
    } finally {
        removeObserver(observer)
    }
}
