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

package dev.cubxity.kraft.ui.servers

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.cubxity.kraft.KraftApplication
import dev.cubxity.kraft.SessionActivity
import dev.cubxity.kraft.db.entity.Session
import dev.cubxity.kraft.entity.Server
import dev.cubxity.kraft.entity.WorldsResponse
import dev.cubxity.kraft.utils.*
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ServersViewModel(app: Application) : AndroidViewModel(app) {
    companion object {
        private const val TAG = "ServersViewModel"
    }

    val servers = MutableLiveData<List<Server>>(emptyList())

    fun fetchServers(ctx: Context) = viewModelScope.launch(Dispatchers.IO) {
        servers.postValue(ctx.db.serversDao().getServers())

        ctx.db.accountsDao().getAccounts().forEach { account ->
            try {
                ctx.refresh(account)
                val res: WorldsResponse = client.get("https://pc.realms.minecraft.net/worlds") {
                    header("Cookie", buildRealmCookie(account))
                }
                res.servers.forEach { it.account = UUID.fromString(account.uuid) }
                servers.postValue(res.servers + servers.value!!)
            } catch (e: Exception) {
                Log.e(TAG, "An error occurred whilst fetching realms", e)
            }
        }
    }

    fun addServer(ctx: Activity) = viewModelScope.launch(Dispatchers.Default) {
        val server = withContext(Dispatchers.Main) { UIUtils.addServer(ctx) } ?: return@launch
        val app: KraftApplication = getApplication()

        withContext(Dispatchers.IO) { app.db.serversDao().addServer(server) }
        servers.postValue(servers.value!! + server)
    }

    fun removeServer(server: dev.cubxity.kraft.db.entity.Server) =
        viewModelScope.launch(Dispatchers.IO) {
            val app: KraftApplication = getApplication()

            app.db.serversDao().deleteServer(server)
            servers.postValue(servers.value!! - server)
        }

    fun openSession(ctx: Activity, server: Server) = viewModelScope.launch(Dispatchers.Default) {
        val app: KraftApplication = getApplication()

        val accountUUID = server.account
        val account = if (accountUUID != null) {
            withContext(Dispatchers.IO) { app.db.accountsDao().getAccount("$accountUUID") }
        } else {
            withContext(Dispatchers.Main) { UIUtils.selectAccount(ctx) }
        }

        if (account != null) {
            try {
                val address = withContext(Dispatchers.IO) { server.getAddress(account) }
                val (host, port) = address
                val session = Session.create(server.name, account, host, port)

                session.session.id =
                    withContext(Dispatchers.IO) {
                        app.db.sessionsDao().addSession(session.session)
                    }.toInt()

                try {
                    ctx.refreshAndConnect(session, app.sessionManager::createSession)

                    val intent = Intent(ctx, SessionActivity::class.java)
                    intent.putExtra("session", session)
                    ctx.startActivity(intent)
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "Unable to start the session", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unable to start a session", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(ctx, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}