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
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.steveice10.mc.auth.util.UUIDSerializer
import dev.cubxity.kraft.KraftApplication
import dev.cubxity.kraft.entity.Server
import dev.cubxity.kraft.entity.WorldsResponse
import dev.cubxity.kraft.utils.UIUtils
import dev.cubxity.kraft.utils.db
import dev.cubxity.kraft.utils.refresh
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ServersViewModel(app: Application) : AndroidViewModel(app) {
    private val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                registerTypeAdapter(UUID::class.java, UUIDSerializer())
            }
        }
    }

    companion object {
        private const val TAG = "ServersViewModel"
    }

    val servers = MutableLiveData<List<Server>>(emptyList())

    fun fetchServers(ctx: Context) = viewModelScope.launch(Dispatchers.IO) {
        servers.postValue(ctx.db.serversDao().getServers())

        ctx.db.accountsDao().getAccounts().forEach { account ->
            ctx.refresh(account)
            try {
                val cookies = "sid=token:${account.accessToken}:${account.uuid.replace("-", "")}" +
                        ";user=${account.username};version=1.15.2"
                val res: WorldsResponse = client.get("https://pc.realms.minecraft.net/worlds") {
                    header("Cookie", cookies)
                }
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

    fun removeServer(server: dev.cubxity.kraft.db.entity.Server) = viewModelScope.launch(Dispatchers.IO) {
        val app: KraftApplication = getApplication()

        app.db.serversDao().deleteServer(server)
        servers.postValue(servers.value!! - server)
    }
}