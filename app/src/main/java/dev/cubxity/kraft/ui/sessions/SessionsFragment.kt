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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dev.cubxity.kraft.R
import dev.cubxity.kraft.SessionActivity
import dev.cubxity.kraft.db.entity.Session
import dev.cubxity.kraft.db.entity.SessionWithAccount
import dev.cubxity.kraft.ui.SessionAdapter
import kotlinx.android.synthetic.main.fragment_sessions.*

class SessionsFragment : Fragment(), SessionAdapter.ActionHandler {
    private val sessionsViewModel: SessionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_sessions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()

        val adapter = SessionAdapter(activity, this)

        val recyclerView = sessions_recycler
        recyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        sessionsViewModel.sessions.observe(viewLifecycleOwner, Observer {
            adapter.sessions.clear()
            adapter.sessions += it
            adapter.notifyDataSetChanged()
        })
        sessionsViewModel.fetchSessions(activity)

        create_session.setOnClickListener {
            sessionsViewModel.createSession(activity)
        }
    }

    override fun removeSession(session: SessionWithAccount) {
        sessionsViewModel.removeSession(session)
    }

    override fun openSession(session: SessionWithAccount) {
        val ctx = requireContext()

        val intent = Intent(ctx, SessionActivity::class.java)
        intent.putExtra("session", session)
        ctx.startActivity(intent)
    }
}