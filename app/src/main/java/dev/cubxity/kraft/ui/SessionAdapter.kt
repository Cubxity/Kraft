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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import dev.cubxity.kraft.R
import dev.cubxity.kraft.db.entity.Session
import dev.cubxity.kraft.db.entity.SessionWithAccount
import kotlinx.android.synthetic.main.list_item.view.*

class SessionAdapter(private val ctx: Context, private val handler: ActionHandler) :
    RecyclerView.Adapter<SessionAdapter.ViewHolder>() {
    val sessions = mutableListOf<SessionWithAccount>()

    private val layoutInflater = LayoutInflater.from(ctx)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = sessions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]

        Glide.with(ctx.applicationContext)
            .load("https://eu.mc-api.net/v3/server/favicon/${session.session.serverHost}")
            .placeholder(R.drawable.ic_baseline_dns_24)
            .into(holder.image)
        holder.card.setOnClickListener {
            handler.openSession(session)
        }
        holder.title.text = session.session.name
        holder.description.text = session.account.username
        holder.optionsButton.setOnClickListener {
            createMenu(session, it)
        }
    }

    private fun createMenu(session: SessionWithAccount, view: View) {
        val popup = PopupMenu(ctx, view)
        popup.inflate(R.menu.session_menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.remove_session -> handler.removeSession(session)
            }
            true
        }
        popup.show()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.item_card
        val image: ImageView = view.card_image
        val title: MaterialTextView = view.card_title
        val description: MaterialTextView = view.card_description
        val optionsButton: ImageButton = view.options_button
    }

    interface ActionHandler {
        fun removeSession(session: SessionWithAccount)

        fun openSession(session: SessionWithAccount)
    }
}