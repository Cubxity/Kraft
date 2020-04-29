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

package dev.cubxity.kraft.db.dao

import androidx.room.*
import dev.cubxity.kraft.db.entity.Session
import dev.cubxity.kraft.db.entity.SessionWithAccount

@Dao
interface SessionsDao {
    @Query("SELECT * FROM sessions")
    suspend fun getSessions(): List<Session>

    @Transaction
    @Query("SELECT * FROM sessions")
    suspend fun getSessionsWithAccount(): List<SessionWithAccount>

    @Update
    suspend fun updateSession(session: Session)

    @Insert
    suspend fun addSession(session: Session): Long

    @Delete
    suspend fun deleteSession(session: Session)
}