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
package com.geekorum.ttrss.session

/**
 * Simple interface for an Action that can be done and undone.
 */
interface Action {
    /**
     * Execute the action.
     */
    fun execute()

    /**
     * Undo the previously executed action.
     */
    fun undo()
}

/**
 * Allows to keep track of Actions that you may want to undo.
 */
class UndoManager<in T : Action> {

    private val actions = mutableListOf<T>()

    val nbActions get() = actions.size

    fun recordAction(action: T) {
        actions += action
    }

    fun undoAll() {
        for (action in actions.reversed()) {
            action.undo()
        }
        actions.clear()
    }

    fun clear() {
        actions.clear()
    }

}
