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

import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.data.message.Message
import dev.cubxity.kraft.db.entity.SessionWithAccount
import dev.cubxity.kraft.mc.entitiy.Entity
import dev.cubxity.kraft.mc.entitiy.SelfPlayer
import java.util.*

interface GameSession {
    val info: SessionWithAccount

    val state: State

    val log: LiveData<List<LogEntry>>

    val player: SelfPlayer?

    val entities: Map<Int, Entity>

    val modules: Map<String, Module>

    val isActive: Boolean

    fun connect(profile: GameProfile, clientToken: UUID)

    fun disconnect()

    fun sendMessage(message: String)

    fun addListener(listener: Listener)

    fun removeListener(listener: Listener)

    enum class State {
        DISCONNECTED,
        DISCONNECTING,
        CONNECTING,
        CONNECTED,
    }

    interface Listener {
        fun onTick() {}

        fun onStateChanged(state: State) {}

        fun onConnect() {}

        fun onDisconnect(reason: String?) {}

        fun onChat(message: Message) {}

        fun onHealthUpdate(player: SelfPlayer) {}

        fun onExpUpdate(player: SelfPlayer) {}

        fun onEntityVelocity(
            entity: Entity,
            velocityX: Double,
            velocityY: Double,
            velocityZ: Double
        ) {
        }

        fun onEntitySpawn(entity: Entity) {}

        fun onEntityUpdate(entity: Entity) {}

        fun onEntityDestroy(entity: Entity) {}

        /**
         * Called when a module is added or updated
         */
        fun onModuleUpdate(module: Module) {}

        /**
         * Send push notification
         */
        fun notify(
            session: GameSession,
            title: CharSequence,
            content: CharSequence,
            priority: Int = NotificationCompat.PRIORITY_DEFAULT
        ) {
        }
    }

    data class LogEntry(
        val scope: String?,
        val content: CharSequence,
        val level: LogLevel = LogLevel.INFO
    )

    enum class LogLevel {
        ERROR,
        WARNING,
        INFO,
        SUCCESS,
    }
}