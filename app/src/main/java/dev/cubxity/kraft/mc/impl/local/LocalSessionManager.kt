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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.cubxity.kraft.db.entity.Session
import dev.cubxity.kraft.db.entity.SessionWithAccount
import dev.cubxity.kraft.mc.GameSession
import dev.cubxity.kraft.mc.SessionManager
import dev.cubxity.kraft.service.KraftService

class LocalSessionManager(private val ctx: Context) : SessionManager {
    private var binder: KraftService.KraftBinder? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            binder = service as KraftService.KraftBinder
        }

        override fun onServiceDisconnected(className: ComponentName) {
            binder = null
        }
    }

    override suspend fun getSessions() =
        binder?.service?.sessions?.values?.toList() ?: error("Service not bound")

    override suspend fun getSession(session: SessionWithAccount) =
        binder?.service?.sessions?.get(session.session.id)

    override suspend fun createSession(session: SessionWithAccount) =
        binder?.service?.createSession(session) ?: error("Service not bound")

    override suspend fun removeSession(session: SessionWithAccount) =
        binder?.service?.removeSession(session) ?: error("Service not bound")

    override fun start() {
        Intent(ctx, KraftService::class.java).also { intent ->
            ctx.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun stop() {
        binder?.also {
            ctx.unbindService(serviceConnection)
            binder = null
        }
    }
}