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

package dev.cubxity.kraft.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dev.cubxity.kraft.R
import dev.cubxity.kraft.ui.AccountAdapter
import kotlinx.android.synthetic.main.fragment_accounts.*

class AccountsFragment : Fragment() {
    private val accountsViewModel: AccountsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_accounts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ctx = requireContext()

        val adapter = AccountAdapter(ctx)

        val recyclerView = accounts_recycler
        recyclerView.layoutManager =
            LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        add_account.setOnClickListener {
            accountsViewModel.addAccount(ctx)
        }

        accountsViewModel.accounts.observe(viewLifecycleOwner, Observer {
            adapter.accounts.clear()
            adapter.accounts += it
            adapter.notifyDataSetChanged()
        })
        accountsViewModel.fetchAccounts(ctx)
    }
}