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

package dev.cubxity.kraft.entity

import dev.cubxity.kraft.db.entity.Account
import java.util.*

interface Server {
    val name: String

    val description: String?
        get() = null

    /**
     * For static servers
     */
    val host: String?
        get() = null

    /**
     * For static servers
     */
    val port: Int?
        get() = null

    /**
     * @return the owner's uuid of the server (Realm)
     */
    val owner: UUID?
        get() = null

    /**
     * @return if getAddress is lazy (F.ex realm)
     */
    val isLazy: Boolean

    suspend fun getAddress(account: Account): Pair<String, Int>? {
        val host = host
        val port = port
        return if (host != null && port != null)
            return host to port
        else null
    }
}