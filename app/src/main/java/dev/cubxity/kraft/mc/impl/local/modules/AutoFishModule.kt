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

package dev.cubxity.kraft.mc.impl.local.modules

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand
import com.github.steveice10.mc.protocol.data.game.entity.type.`object`.ProjectileData
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerUseItemPacket
import com.github.steveice10.packetlib.packet.Packet
import dev.cubxity.kraft.R
import dev.cubxity.kraft.mc.GameSession
import dev.cubxity.kraft.mc.entitiy.Entity
import dev.cubxity.kraft.mc.impl.entity.FishBobber
import dev.cubxity.kraft.mc.impl.local.LocalGameSession
import dev.cubxity.kraft.mc.impl.local.LocalModule
import kotlin.math.abs

class AutoFishModule(private val session: LocalGameSession) : LocalModule("autofish", "AutoFish"),
    GameSession.Listener {

    companion object {
        private const val TAG = "AutoFishModule"
    }

    private val recasts = MutableLiveData(0)
    private val ticks = MutableLiveData(0)
    private var currentBobber: FishBobber? = null
    private var waitingTicks = 0
    private var queuedRecast = false

    override fun buildPreferences(
        ctx: Context,
        lifecycleOwner: LifecycleOwner,
        preferences: PreferenceCategory
    ) {
        val recastsPref = Preference(ctx)
        recastsPref.setIcon(R.drawable.ic_baseline_refresh_24)
        recastsPref.title = "Recasts"
        recasts.observe(lifecycleOwner, Observer {
            recastsPref.summary = "$it"
        })

        val timerPref = Preference(ctx)
        timerPref.setIcon(R.drawable.ic_baseline_timelapse_24)
        timerPref.title = "Last recast"
        ticks.observe(lifecycleOwner, Observer {
            timerPref.summary = "%.1fs".format(it / 20.0)
        })

        preferences.addPreference(recastsPref)
        preferences.addPreference(timerPref)
    }

    override fun onTick() {
        if (waitingTicks == 0)
            if (isEnabled) {
                when {
                    currentBobber == null -> {
                        // Cast
                        session.client?.session?.send(ClientPlayerUseItemPacket(Hand.MAIN_HAND))
                        waitingTicks += 15
                    }
                    queuedRecast -> {
                        session.client?.session?.send(ClientPlayerUseItemPacket(Hand.MAIN_HAND))
                        queuedRecast = false
                        waitingTicks = 5
                        ticks.postValue(0)
                    }
                    else -> {
                        // Bobber is active
                        ticks.postValue(ticks.value!! + 1)
                    }
                }
            } else {
                if (currentBobber != null) {
                    // Retract
                    session.client?.session?.send(ClientPlayerUseItemPacket(Hand.MAIN_HAND))
                    waitingTicks = 5
                    ticks.postValue(0)
                }
            }
        else
            waitingTicks--
    }

    override fun onEntityUpdate(entity: Entity) {
        if (entity is FishBobber) {
            if ((entity.data as? ProjectileData)?.ownerId == session.player?.entityId)
                currentBobber = entity
        }
    }

    override fun onEntityDestroy(entity: Entity) {
        val bobber = currentBobber
        if (entity == bobber)
            currentBobber = null
    }

    override fun onEntityVelocity(
        entity: Entity,
        velocityX: Double,
        velocityY: Double,
        velocityZ: Double
    ) {
        // Wait until the bobber lands on the water
        if (waitingTicks == 0 && entity == currentBobber && !queuedRecast) {
            val velocity = abs(velocityY)
            if (velocity > 0.07) {
                Log.d(TAG, "Recasting, velocity: $velocity")
                queuedRecast = true
                recasts.postValue(recasts.value!! + 1)
                waitingTicks = 5
            }
        }
    }
}