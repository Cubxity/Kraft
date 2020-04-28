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

package dev.cubxity.kraft.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import dev.cubxity.kraft.R
import dev.cubxity.kraft.db.entity.Account
import kotlinx.android.synthetic.main.list_item.view.*

class AccountAdapter(private val ctx: Context, private val handler: ActionHandler) :
    RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    val accounts = mutableListOf<Account>()

    private val layoutInflater = LayoutInflater.from(ctx)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = accounts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = accounts[position]

        Glide.with(ctx.applicationContext)
            .load("https://crafatar.com/avatars/${account.uuid}?overlay")
            .into(holder.image)

        holder.title.text = account.username
        holder.description.text = account.uuid
        holder.optionsButton.setOnClickListener {
            createMenu(account, it)
        }
    }

    private fun createMenu(account: Account, view: View) {
        val popup = PopupMenu(ctx, view)
        popup.inflate(R.menu.account_menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.remove_account -> handler.removeAccount(account)
            }
            true
        }
        popup.show()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.card_image
        val title: MaterialTextView = view.card_title
        val description: MaterialTextView = view.card_description
        val optionsButton: ImageButton = view.options_button
    }

    interface ActionHandler {
        fun removeAccount(account: Account)
    }
}