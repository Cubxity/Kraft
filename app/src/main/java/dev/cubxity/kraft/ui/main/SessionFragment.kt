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

package dev.cubxity.kraft.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import dev.cubxity.kraft.R
import dev.cubxity.kraft.db.entity.SessionWithAccount
import kotlinx.android.synthetic.main.fragment_session.*

class SessionFragment : Fragment() {
    private val sessionViewModel: SessionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_session, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionViewModel.log.observe(viewLifecycleOwner, Observer {
            logs.text = it
        })
        sessionViewModel.gameSession.observe(viewLifecycleOwner, Observer {
            it?.addListener(sessionViewModel)
        })

        val session: SessionWithAccount? = arguments?.getParcelable("session")
        session?.also { sessionViewModel.fetchGameSession(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionViewModel.gameSession.apply {
            value?.removeListener(sessionViewModel)
            value = null
        }
    }

    companion object {
        private const val ARG_SESSION = "session"

        @JvmStatic
        fun newInstance(session: SessionWithAccount) = SessionFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_SESSION, session)
            }
        }
    }
}