/*
 *     Kraft: Lightweight Minecraft client for Android featuring modules support and other task automation
 *     Copyright (C) 2020  Cubxity
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cubxity.kraft.mc

import dev.cubxity.kraft.db.entity.Session
import dev.cubxity.kraft.db.entity.SessionWithAccount

interface SessionManager {
    /**
     * @return active game sessions
     */
    suspend fun getSessions(): List<GameSession>

    /**
     * @return active game session from [session] spec
     */
    suspend fun getSession(session: SessionWithAccount): GameSession?

    /**
     * Creating a session, this does not connect
     * @return the created game session from [session] spec
     */
    suspend fun createSession(session: SessionWithAccount): GameSession

    /**
     * Disconnecting from a session and removing it
     */
    suspend fun removeSession(session: SessionWithAccount)

    /**
     * Starts the connection to the daemon
     */
    fun start()

    /**
     * Stops the connection to the daemon
     */
    fun stop()
}