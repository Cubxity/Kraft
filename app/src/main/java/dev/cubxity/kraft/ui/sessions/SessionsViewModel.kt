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

package dev.cubxity.kraft.ui.sessions

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.cubxity.kraft.KraftApplication
import dev.cubxity.kraft.SessionActivity
import dev.cubxity.kraft.db.entity.SessionWithAccount
import dev.cubxity.kraft.utils.UIUtils
import dev.cubxity.kraft.utils.db
import dev.cubxity.kraft.utils.refreshAndConnect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class SessionsViewModel(app: Application) : AndroidViewModel(app) {
    companion object {
        private const val TAG = "SessionsViewModel"
    }

    val sessions = MutableLiveData<List<SessionWithAccount>>(emptyList())

    fun fetchSessions(ctx: Context) = viewModelScope.launch(Dispatchers.IO) {
        sessions.postValue(ctx.db.sessionsDao().getSessionsWithAccount())
    }

    fun createSession(ctx: Activity) = viewModelScope.launch(Dispatchers.Default) {
        val session = withContext(Dispatchers.Main) { UIUtils.createSession(ctx) } ?: return@launch
        val app: KraftApplication = getApplication()

        withContext(Dispatchers.IO) { app.db.sessionsDao().addSession(session.session) }

        sessions.postValue(sessions.value!! + session)

        try {
            ctx.refreshAndConnect(session, app.sessionManager::createSession)

            val intent = Intent(ctx, SessionActivity::class.java)
            intent.putExtra("session", session)
            ctx.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to start the session", e)
        }
    }

    fun removeSession(session: SessionWithAccount) = viewModelScope.launch(Dispatchers.IO) {
        val app: KraftApplication = getApplication()

        app.db.sessionsDao().deleteSession(session.session)
        try {
            app.sessionManager.removeSession(session)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to remove the session", e)
        }

        sessions.postValue(sessions.value!! - session)
    }
}