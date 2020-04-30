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

import android.graphics.Typeface
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import dev.cubxity.kraft.R
import dev.cubxity.kraft.db.entity.SessionWithAccount
import dev.cubxity.kraft.mc.GameSession
import kotlinx.android.synthetic.main.fragment_session.*
import kotlin.properties.Delegates

class SessionFragment : Fragment() {
    private val sessionViewModel: SessionViewModel by activityViewModels()

    private var successColor by Delegates.notNull<Int>()
    private var warningColor by Delegates.notNull<Int>()
    private var errorColor by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_session, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ctx = requireContext()
        successColor = ctx.getColor(R.color.colorSuccess)
        warningColor = ctx.getColor(R.color.colorWarning)
        errorColor = ctx.getColor(R.color.colorError)

        installSession(sessionViewModel.gameSession.value)
        sessionViewModel.gameSession.observe(viewLifecycleOwner, Observer {
            installSession(it)
        })

        chat_input.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sessionViewModel.sendChat(v.text.toString())
                v.text = ""
                true
            } else false
        }
    }

    private fun installSession(session: GameSession?) {
        session?.addListener(sessionViewModel)
        session?.log?.apply {
            observe(viewLifecycleOwner, Observer { log ->
                updateLog(log)
            })
            value?.also { updateLog(it) }
        }
    }

    private fun updateLog(log: List<GameSession.LogEntry>) {
        val spannable = buildSpannedString {
            for (entry in log) {
                if (entry.scope != null) {
                    append("[${entry.scope}]", StyleSpan(Typeface.BOLD), 0)
                    append(' ')
                }
                if (entry.level != GameSession.LogLevel.INFO) {
                    val color = when (entry.level) {
                        GameSession.LogLevel.SUCCESS -> successColor
                        GameSession.LogLevel.WARNING -> warningColor
                        GameSession.LogLevel.ERROR -> errorColor
                        else -> errorColor
                    }
                    append(entry.content, ForegroundColorSpan(color), 0)
                } else {
                    append(entry.content)
                }
                append('\n')
            }
        }

        logs.text = spannable
    }
}