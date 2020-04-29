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

package dev.cubxity.kraft.ui.servers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dev.cubxity.kraft.R
import dev.cubxity.kraft.entity.Server
import dev.cubxity.kraft.ui.ServersAdapter
import kotlinx.android.synthetic.main.fragment_servers.*

class ServersFragment : Fragment(), ServersAdapter.ActionHandler {
    private val serversViewModel: ServersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_servers, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()

        val adapter = ServersAdapter(activity, this)

        val recyclerView = servers_recycler
        recyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        serversViewModel.servers.observe(viewLifecycleOwner, Observer {
            adapter.servers.clear()
            adapter.servers += it
            adapter.notifyDataSetChanged()
        })
        serversViewModel.fetchServers(activity)

        add_server.setOnClickListener {
            serversViewModel.addServer(activity)
        }
    }

    override fun removeServer(server: Server) {
        (server as? dev.cubxity.kraft.db.entity.Server)
            ?.also { serversViewModel.removeServer(it) }
    }

    override fun openSession(server: Server) {
        // TODO
    }
}