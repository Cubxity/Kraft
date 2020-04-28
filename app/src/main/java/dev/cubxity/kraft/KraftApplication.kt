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

package dev.cubxity.kraft

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import dev.cubxity.kraft.mc.impl.local.LocalSessionManager
import dev.cubxity.kraft.service.KraftService

class KraftApplication : Application() {
    companion object {
        private const val TAG = "KraftApplication"
    }

    val sessionManager = LocalSessionManager(this)

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, KraftService::class.java))
            } else {
                startService(Intent(this, KraftService::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred whilst starting the service", e)
        }

        sessionManager.start()
    }

    override fun onTerminate() {
        super.onTerminate()

        sessionManager.stop()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            KraftService.CHANNEL_ID,
            "Kraft Sessions Service",
            NotificationManager.IMPORTANCE_HIGH
        )

        service.createNotificationChannel(channel)
    }
}