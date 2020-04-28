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

package dev.cubxity.kraft.service

import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.github.steveice10.mc.auth.service.AuthenticationService
import com.github.steveice10.mc.protocol.data.message.Message
import dev.cubxity.kraft.R
import dev.cubxity.kraft.db.entity.Session
import dev.cubxity.kraft.db.entity.SessionWithAccount
import dev.cubxity.kraft.mc.GameSession
import dev.cubxity.kraft.mc.impl.local.LocalGameSession
import dev.cubxity.kraft.utils.clientToken
import dev.cubxity.kraft.utils.db
import dev.cubxity.kraft.utils.refreshAndConnect
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class KraftService : Service(), CoroutineScope, GameSession.Listener {
    override val coroutineContext = Dispatchers.IO + Job()

    companion object {
        private const val TAG = "KraftService"
        private const val NOTIFICATION_ID = 1

        const val CHANNEL_ID = "sessions_service"
    }

    private val binder = KraftBinder()
    private var isForeground: Boolean = false

    val sessions = HashMap<SessionWithAccount, LocalGameSession>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        launch {
            val sessions = db.sessionsDao().getSessionsWithAccount()

            if (sessions.isNotEmpty()) startForeground()

            Log.i(TAG, "Resuming ${sessions.size} session(s)")
            sessions.forEach {
                try {
                    refreshAndConnect(it, ::createSession)
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to connect to the server", e)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        launch {
            sessions.map { (_, session) ->
                async(Dispatchers.Default) {
                    try {
                        session.disconnect()
                    } catch (e: Exception) {
                        Log.e(TAG, "An error occurred whilst disconnecting", e)
                    }
                    session.removeListener(this@KraftService)
                }
            }.awaitAll()
        }
    }

    override fun onBind(intent: Intent?) = binder

    override fun onStateChanged(state: GameSession.State) {
        Log.d(TAG, "State changed: $state")

        if (state == GameSession.State.DISCONNECTED && sessions.none { (_, session) -> session.isActive }) {
            // If there are no sessions active and the service is currently running in foreground
            if (isForeground) {
                Log.i(TAG, "Running service as background")
                stopForeground(true)
            }
        } else {
            // Update the current notification
            if (isForeground) {
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, createNotification())
            } else {
                // Run service as foreground
                startForeground()
            }
        }
    }

    override fun onChat(message: Message) {
        println(message.fullText)
    }

    suspend fun createSession(session: SessionWithAccount): LocalGameSession =
        sessions.getOrPut(session) { LocalGameSession(session).apply { addListener(this@KraftService) } }

    fun removeSession(session: SessionWithAccount) {
        val gameSession = sessions.remove(session) ?: return
        gameSession.disconnect()
        gameSession.removeListener(this)
    }

    private fun startForeground() {
        Log.i(TAG, "Running service as foreground")
        isForeground = true
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val activeSessions = sessions.count { (_, session) -> session.isActive }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_dashboard_24)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentTitle("Kraft")
            .setContentText("$activeSessions sessions active")
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    inner class KraftBinder internal constructor() : Binder() {
        val service: KraftService
            get() = this@KraftService
    }
}