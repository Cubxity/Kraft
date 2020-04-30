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

package dev.cubxity.kraft

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import dev.cubxity.kraft.db.entity.SessionWithAccount
import dev.cubxity.kraft.mc.GameSession
import dev.cubxity.kraft.ui.session.ModuleFragment
import dev.cubxity.kraft.ui.session.SectionsPagerAdapter
import dev.cubxity.kraft.ui.session.SessionViewModel
import kotlinx.android.synthetic.main.activity_session.*

class SessionActivity : AppCompatActivity(), GameSession.Listener {
    private val sessionViewModel: SessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session: SessionWithAccount? = intent.getParcelableExtra("session")
        if (session == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_session)

        val pagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

        val viewPager = view_pager
        viewPager.adapter = pagerAdapter

        val tabs = tabs
        tabs.setupWithViewPager(viewPager)

        sessionViewModel.gameSession.observe(this, Observer {
            it?.addListener(this)
            updateModules(pagerAdapter, it)
        })
        session.also { sessionViewModel.fetchGameSession(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionViewModel.gameSession.apply {
            value?.removeListener(this@SessionActivity)
            value = null
        }
    }

    private fun updateModules(pagerAdapter: SectionsPagerAdapter, session: GameSession?) {
        if (session == null) return

        pagerAdapter.tabs = pagerAdapter.baseTabs.toMutableList()
        val moduleTabs = session.modules.map { (name, module) ->
            name to { ModuleFragment.createInstance(module) }
        }
        pagerAdapter.tabs.addAll(moduleTabs)
        pagerAdapter.notifyDataSetChanged()
    }
}