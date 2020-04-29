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

package dev.cubxity.kraft.utils

import com.github.steveice10.mc.auth.util.UUIDSerializer
import dev.cubxity.kraft.db.entity.Account
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import java.util.*

val client = HttpClient(OkHttp) {
    install(JsonFeature) {
        serializer = GsonSerializer {
            registerTypeAdapter(UUID::class.java, UUIDSerializer())
        }
    }
}

fun buildRealmCookie(account: Account) =
    "sid=token:${account.accessToken}:${account.uuid.replace("-", "")}" +
            ";user=${account.username};version=1.15.2"