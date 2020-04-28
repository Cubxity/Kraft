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

import android.app.Activity
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import dev.cubxity.kraft.R
import dev.cubxity.kraft.db.entity.Account
import dev.cubxity.kraft.db.entity.Session
import dev.cubxity.kraft.db.entity.SessionWithAccount
import kotlinx.android.synthetic.main.dialog_create_session.*
import kotlinx.android.synthetic.main.dialog_create_session.view.*
import kotlinx.android.synthetic.main.dialog_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object UIUtils {
    suspend fun requestCredential(ctx: Context): Pair<String, String>? = suspendCoroutine { c ->
        AlertDialog.Builder(ctx)
            .setTitle("Login")
            .setView(R.layout.dialog_login)
            .setMessage("Please enter your username and password")
            .setNegativeButton("Cancel") { i, _ -> i.cancel() }
            .setPositiveButton("Login") { i, _ ->
                val dialog = i as AlertDialog
                val user = dialog.username_field.text.toString()
                val pass = dialog.password_field.text.toString()
                c.resume(user to pass)
            }
            .setOnCancelListener { c.resume(null) }
            .create()
            .show()
    }

    suspend fun createSession(ctx: Activity): SessionWithAccount? {
        val accounts = withContext(Dispatchers.IO) { ctx.db.accountsDao().getAccounts() }

        return suspendCoroutine { c ->
            val view = ctx.layoutInflater.inflate(R.layout.dialog_create_session, null)
            val adapter = ArrayAdapter(ctx, R.layout.autocomplete_item, accounts)
            var selectedAccount: Account? = null

            val autocomplete: AutoCompleteTextView? = view.account_autocomplete
            autocomplete?.setAdapter(adapter)
            autocomplete?.setOnItemClickListener { _, _, i, _ ->
                selectedAccount = adapter.getItem(i)
            }

            AlertDialog.Builder(ctx)
                .setTitle("Create session")
                .setView(view)
                .setMessage("Please enter the sessions details below")
                .setNegativeButton("Cancel") { i, _ -> i.cancel() }
                .setPositiveButton("Create") { i, _ ->
                    val dialog = i as AlertDialog
                    val name = dialog.name_field.text.toString()
                    val host = dialog.host_field.text.toString()
                    val port = dialog.port_field.text.toString().toInt()

                    val account = selectedAccount

                    when {
                        account == null -> {
                            Toast.makeText(
                                ctx,
                                R.string.toast_no_account,
                                Toast.LENGTH_SHORT
                            ).show()
                            c.resume(null)
                        }
                        name.isEmpty() -> {
                            Toast.makeText(
                                ctx,
                                R.string.toast_empty_name,
                                Toast.LENGTH_SHORT
                            ).show()
                            c.resume(null)
                        }
                        host.isEmpty() -> {
                            Toast.makeText(
                                ctx,
                                R.string.toast_empty_host,
                                Toast.LENGTH_SHORT
                            ).show()
                            c.resume(null)
                        }
                        else -> c.resume(Session.create(name, account, host, port))
                    }
                }
                .setOnCancelListener { c.resume(null) }
                .create()
                .show()
        }
    }
}