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

import com.google.gson.annotations.SerializedName
import dev.cubxity.kraft.db.entity.Account
import java.util.*

data class WorldsResponse(val servers: List<Realm>)

data class Realm(
    val id: Int,
    @SerializedName("owner") val ownerName: String,
    @SerializedName("ownerUUID") override val owner: UUID,
    override val name: String,
    val motd: String,
    val state: String,
    val daysLeft: Int,
    val expired: Boolean,
    val expiredTrial: Boolean,
    val worldType: String,
    val players: List<String>,
    val maxPlayers: Int
) : Server {
    override val isLazy: Boolean
        get() = true

    override val description: String
        get() = motd

    override suspend fun getAddress(account: Account): Pair<String, Int>? = null
}