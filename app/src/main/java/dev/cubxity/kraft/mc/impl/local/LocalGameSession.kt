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

package dev.cubxity.kraft.mc.impl.local

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.MinecraftProtocol
import com.github.steveice10.mc.protocol.data.game.MessageType
import com.github.steveice10.mc.protocol.data.game.entity.type.`object`.ObjectType
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityDestroyPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityMetadataPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityVelocityPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerSetExperiencePacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket
import com.github.steveice10.packetlib.Client
import com.github.steveice10.packetlib.event.session.*
import com.github.steveice10.packetlib.packet.Packet
import com.github.steveice10.packetlib.tcp.TcpSessionFactory
import dev.cubxity.kraft.db.entity.SessionWithAccount
import dev.cubxity.kraft.mc.GameSession
import dev.cubxity.kraft.mc.Module
import dev.cubxity.kraft.mc.entitiy.Entity
import dev.cubxity.kraft.mc.entitiy.SelfPlayer
import dev.cubxity.kraft.mc.impl.entity.*
import dev.cubxity.kraft.mc.impl.local.modules.AutoFishModule
import dev.cubxity.kraft.utils.ChatUtils
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class LocalGameSession(override val info: SessionWithAccount) : SessionAdapter(), GameSession,
    CoroutineScope {
    companion object {
        private const val TAG = "LocalGameSession"
    }

    override val coroutineContext = Dispatchers.Default + Job()
    var client: Client? = null
        private set
    private var job: Job? = null

    override var state: GameSession.State = GameSession.State.DISCONNECTED
        private set(value) {
            listeners.forEach { it.onStateChanged(value) }
            field = value
        }

    override val log: MutableLiveData<List<GameSession.LogEntry>> = MutableLiveData(emptyList())

    private var entityId: Int? = null
    override var player: SelfPlayer? = null
    override val entities = mutableMapOf<Int, Entity>()
    override val modules = mutableMapOf<String, Module>()
    override val isActive: Boolean
        get() = state != GameSession.State.DISCONNECTED || client?.session?.isConnected == true

    private val listeners = CopyOnWriteArrayList<GameSession.Listener>()

    override fun connect(profile: GameProfile, clientToken: UUID) {
        if (client != null) disconnect()
        state = GameSession.State.CONNECTING

        val account = info.account
        val protocol = MinecraftProtocol(profile, "$clientToken", account.accessToken)

        val client =
            Client(info.session.serverHost, info.session.serverPort, protocol, TcpSessionFactory())
        client.session.addListener(this)
        client.session.connect()
        registerModules()

        job = launch {
            try {
                while (true) {
                    listeners.forEach { it.onTick() }
                    delay(50)
                }
            } catch (_: CancellationException) {
                // Ignore
            } catch (e: Exception) {
                Log.e(TAG, "An error occurred whilst ticking", e)
            }
        }

        this.client = client
    }

    override fun disconnect() {
        client?.apply {
            state = GameSession.State.DISCONNECTING
            if (session.isConnected) session.disconnect("Disconnected.")
            session.removeListener(this@LocalGameSession)
            client = null

            entityId = null
            player = null
            job?.cancel()
            job = null
            entities.clear()
            unregisterModules()
        }
    }

    override fun connected(event: ConnectedEvent) {
        state = GameSession.State.CONNECTED
        success("Client", "Connected to ${event.session.remoteAddress}")
        listeners.forEach { it.onConnect() }
    }

    override fun disconnecting(event: DisconnectingEvent?) {
        warn("Client", "Disconnecting...")
    }

    override fun disconnected(event: DisconnectedEvent) {
        state = GameSession.State.DISCONNECTED
        warn("Client", "Disconnected: ${event.reason}")
        listeners.forEach { it.onDisconnect(event.reason) }
    }

    override fun sendMessage(message: String) {
        client?.session?.send(ClientChatPacket(message))
    }

    override fun addListener(listener: GameSession.Listener) {
        listeners += listener
    }

    override fun removeListener(listener: GameSession.Listener) {
        listeners -= listener
    }

    override fun packetReceived(event: PacketReceivedEvent) {
        when (val packet: Packet = event.getPacket()) {
            is ServerChatPacket -> if (packet.type != MessageType.NOTIFICATION) {
                log(null, ChatUtils.buildSpan(packet.message))
                listeners.forEach { it.onChat(packet.message) }
            }
            is ServerJoinGamePacket -> {
                entityId = packet.entityId
                player = SelfPlayerImpl(packet.entityId, UUID.fromString(info.account.uuid))
            }
            is ServerPlayerHealthPacket -> {
                player?.apply {
                    health = packet.health
                    food = packet.food
                    foodSaturation = packet.saturation
                    listeners.forEach { it.onHealthUpdate(this) }
                }
            }
            is ServerPlayerPositionRotationPacket -> {
                client?.session?.send(
                    ClientPlayerPositionRotationPacket(
                        false,
                        packet.x,
                        packet.y,
                        packet.z,
                        packet.yaw,
                        packet.pitch
                    )
                )
                client?.session?.send(ClientTeleportConfirmPacket(packet.teleportId))
                player?.apply {
                    x = packet.x
                    y = packet.y
                    z = packet.z
                    yaw = packet.yaw
                    pitch = packet.pitch
                }
            }
            is ServerSpawnPlayerPacket -> {
                val player = PlayerImpl(packet.entityId, packet.uuid)
                player.x = packet.x
                player.y = packet.y
                player.z = packet.z
                player.pitch = packet.pitch
                player.yaw = packet.yaw

                entities[packet.entityId] = player
            }
            is ServerSpawnObjectPacket -> {
                val entity = when (packet.type) {
                    ObjectType.ITEM -> Item(packet.entityId, packet.uuid)
                    ObjectType.FISHING_BOBBER -> FishBobber(packet.entityId, packet.uuid)
                    else -> BaseEntity(packet.entityId, packet.uuid)
                }
                entity.data = packet.data

                entity.x = packet.x
                entity.y = packet.y
                entity.z = packet.z
                entity.pitch = packet.pitch
                entity.yaw = packet.yaw
                entity.velocityX = packet.motionX
                entity.velocityY = packet.motionY
                entity.velocityZ = packet.motionZ

                entities[packet.entityId] = entity

                listeners.forEach { it.onEntitySpawn(entity) }
            }
            is ServerPlayerSetExperiencePacket -> {
                player?.apply {
                    experienceBar = packet.experience
                    totalExperience = packet.totalExperience
                    level = packet.level
                    listeners.forEach { it.onExpUpdate(this) }
                }
            }
            is ServerEntityDestroyPacket -> {
                packet.entityIds.forEach { entityId ->
                    val entity = entities.remove(entityId) ?: return@forEach
                    listeners.forEach { it.onEntityDestroy(entity) }
                }
            }
            is ServerEntityMetadataPacket -> {
                val entity = entities[packet.entityId] ?: return
                entity.metadata = packet.metadata
                listeners.forEach { it.onEntityUpdate(entity) }
            }
            is ServerEntityVelocityPacket -> {
                val entity = entities[packet.entityId] ?: return
                listeners.forEach {
                    it.onEntityVelocity(
                        entity,
                        packet.motionX,
                        packet.motionY,
                        packet.motionZ
                    )
                }
            }
        }
    }

    private fun registerModules() {
        register(AutoFishModule(this))
    }

    private fun register(module: Module) {
        modules[module.id] = module
        if (module is GameSession.Listener)
            addListener(module)
    }

    private fun unregisterModules() {
        modules.forEach { (_, module) ->
            if (module is GameSession.Listener)
                removeListener(module)
        }
        modules.clear()
    }

    private fun log(
        scope: String?,
        content: CharSequence,
        level: GameSession.LogLevel = GameSession.LogLevel.INFO
    ) {
        val log = log.value ?: emptyList()
        val new = log.let { if (it.size > 100) it.drop(1) else it } +
                GameSession.LogEntry(scope, content, level)
        this.log.postValue(new)
    }

    private fun warn(scope: String?, content: CharSequence) =
        log(scope, content, GameSession.LogLevel.WARNING)

    private fun error(scope: String?, content: CharSequence) =
        log(scope, content, GameSession.LogLevel.ERROR)

    private fun success(
        scope: String?,
        content: CharSequence
    ) = log(scope, content, GameSession.LogLevel.SUCCESS)
}