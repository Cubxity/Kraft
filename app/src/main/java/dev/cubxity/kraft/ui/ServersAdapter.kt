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
import dev.cubxity.kraft.entity.Server
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.coroutines.runBlocking

class ServersAdapter(private val ctx: Context, private val handler: ActionHandler) :
    RecyclerView.Adapter<ServersAdapter.ViewHolder>() {
    val servers = mutableListOf<Server>()

    private val layoutInflater = LayoutInflater.from(ctx)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = servers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val server = servers[position]

        val owner = server.owner
        when {
            owner != null -> Glide.with(ctx.applicationContext)
                .load("https://crafatar.com/avatars/$owner?overlay")
                .placeholder(R.drawable.ic_baseline_dns_24)
                .into(holder.image)
            !server.isLazy -> Glide.with(ctx.applicationContext)
                .load("https://eu.mc-api.net/v3/server/favicon/${server.host}")
                .placeholder(R.drawable.ic_baseline_dns_24)
                .into(holder.image)
            else -> holder.image.setImageDrawable(null)
        }
        holder.card.setOnClickListener {
            handler.openSession(server)
        }
        holder.title.text = server.name
        holder.description.text = server.description
        holder.optionsButton.setOnClickListener {
            createMenu(server, it)
        }
    }

    private fun createMenu(server: Server, view: View) {
        val popup = PopupMenu(ctx, view)
        popup.inflate(R.menu.server_menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.remove_server -> handler.removeServer(server)
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
        fun openSession(server: Server)

        fun removeServer(server: Server)
    }
}