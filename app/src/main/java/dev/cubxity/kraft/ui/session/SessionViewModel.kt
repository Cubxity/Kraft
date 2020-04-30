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

package dev.cubxity.kraft.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.cubxity.kraft.KraftApplication
import dev.cubxity.kraft.db.entity.SessionWithAccount
import dev.cubxity.kraft.mc.GameSession
import dev.cubxity.kraft.mc.entitiy.SelfPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionViewModel(app: Application) : AndroidViewModel(app), GameSession.Listener {
    val gameSession = MutableLiveData<GameSession>()
    val health = MutableLiveData(0F)
    val experience = MutableLiveData(0F)
    val experienceLevel = MutableLiveData(0)

    fun fetchGameSession(session: SessionWithAccount) = viewModelScope.launch(Dispatchers.IO) {
        val app: KraftApplication = getApplication()
        val s = app.sessionManager.getSession(session)

        gameSession.postValue(s)

        val player = s?.player ?: return@launch
        health.postValue(player.health)
        experience.postValue(player.experienceBar)
        experienceLevel.postValue(player.level)
    }

    fun sendChat(text: String) = viewModelScope.launch(Dispatchers.IO) {
        gameSession.value?.sendMessage(text)
    }

    override fun onExpUpdate(player: SelfPlayer) {
        experience.postValue(player.experienceBar)
        experienceLevel.postValue(player.level)
    }

    override fun onHealthUpdate(player: SelfPlayer) {
        health.postValue(player.health)
    }
}