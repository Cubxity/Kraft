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
import androidx.appcompat.app.AlertDialog
import dev.cubxity.kraft.R
import kotlinx.android.synthetic.main.dialog_login.*
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
}