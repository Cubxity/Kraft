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

import com.github.steveice10.mc.protocol.data.message.Message
import dev.cubxity.kraft.db.entity.Session
import dev.cubxity.kraft.mc.entitiy.Entity
import dev.cubxity.kraft.mc.entitiy.SelfPlayer
import java.util.*

interface GameSession {
    val info: Session

    val player: SelfPlayer?

    val entities: Map<Int, Entity>

    val isActive: Boolean

    fun connect(clientToken: UUID)

    fun disconnect()

    fun sendMessage(message: String)

    fun addListener(listener: Listener)

    fun removeListener(listener: Listener)

    interface Listener {
        fun onTick() {}

        fun onConnect() {}

        fun onDisconnect(reason: String?) {}

        fun onChat(message: Message) {}

        fun onEntitySpawn(entity: Entity) {}

        fun onEntityUpdate(entity: Entity) {}

        fun onEntityDestroy(entity: Entity) {}
    }
}