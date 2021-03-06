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

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.auth.service.AuthenticationService
import dev.cubxity.kraft.db.entity.Account
import dev.cubxity.kraft.db.entity.Session
import dev.cubxity.kraft.db.entity.SessionWithAccount
import dev.cubxity.kraft.mc.GameSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

suspend fun Context.refresh(account: Account): GameProfile? {
    val service = AuthenticationService("$clientToken")
    service.username = account.username
    service.accessToken = account.accessToken

    withContext(Dispatchers.IO) {
        try {
            service.login() // Refreshing the token
        } catch (e: Exception) {
            Log.e(null, "An error occurred whilst logging in", e)
        }
    }

    account.accessToken = service.accessToken
    account.username = service.selectedProfile.name

    withContext(Dispatchers.IO) {
        try {
            db.accountsDao().updateAccount(account)
        } catch (e: Exception) {
            Log.e(null, "An error occurred whilst updating account", e)
        }
    }

    return service.selectedProfile
}

/**
 * @param configure configure [GameSession] before connecting
 */
suspend fun Context.refreshAndConnect(
    session: SessionWithAccount,
    createSession: suspend (SessionWithAccount) -> GameSession,
    configure: GameSession.() -> Unit = {}
) {
    val profile = refresh(session.account) ?: return

    createSession(session)
        .apply(configure)
        .connect(profile, clientToken)
}