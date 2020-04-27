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

package dev.cubxity.kraft.ui.accounts

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.steveice10.mc.auth.service.AuthenticationService
import dev.cubxity.kraft.db.entity.Account
import dev.cubxity.kraft.utils.UIUtils
import dev.cubxity.kraft.utils.clientToken
import dev.cubxity.kraft.utils.db
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountsViewModel : ViewModel() {
    companion object {
        private const val TAG = "AccountsViewModel"
    }

    val accounts = MutableLiveData<List<Account>>(emptyList())

    fun fetchAccounts(ctx: Context) = viewModelScope.launch(Dispatchers.IO) {
        accounts.postValue(ctx.db.accountsDao().getAccounts())
    }

    fun addAccount(ctx: Context) = viewModelScope.launch(Dispatchers.Default) {
        val (login, pass) = withContext(Dispatchers.Main) { UIUtils.requestCredential(ctx) }
            ?: return@launch
        val db = ctx.db

        val service = AuthenticationService(ctx.clientToken.toString())
        service.username = login
        service.password = pass

        withContext(Dispatchers.IO) {
            try {
                service.login()
                val profile = service.selectedProfile
                val account = Account(
                    profile.id.toString(),
                    profile.name,
                    login,
                    service.accessToken
                )
                db.accountsDao().addAccount(account)
                accounts.postValue(accounts.value!! + account)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to login", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(ctx, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}