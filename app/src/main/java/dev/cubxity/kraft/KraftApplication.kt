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
import android.content.Intent
import android.util.Log
import dev.cubxity.kraft.mc.impl.local.LocalSessionManager
import dev.cubxity.kraft.service.KraftService

class KraftApplication : Application() {
    companion object {
        private const val TAG = "KraftApplication"
    }

    val sessionManager = LocalSessionManager(this)

    override fun onCreate() {
        super.onCreate()

        try {
            startService(Intent(this, KraftService::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred whilst starting the service", e)
        }

        sessionManager.start()
    }

    override fun onTerminate() {
        super.onTerminate()

        sessionManager.stop()
    }
}