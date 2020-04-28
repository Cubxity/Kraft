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

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import dev.cubxity.kraft.R
import dev.cubxity.kraft.db.entity.SessionWithAccount

private val TAB_TITLES = arrayOf(
    R.string.tab_chat
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(
    private val context: Context,
    private val session: SessionWithAccount,
    fm: FragmentManager
) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int) = when (position) {
        0 -> SessionFragment.newInstance(session)
        else -> error("Invalid position $position")
    }

    override fun getPageTitle(position: Int) =
        context.resources.getString(TAB_TITLES[position])

    override fun getCount() = 1
}