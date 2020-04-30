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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import dev.cubxity.kraft.R
import dev.cubxity.kraft.mc.GameSession
import dev.cubxity.kraft.mc.Module

class ModuleFragment : PreferenceFragmentCompat() {
    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val module = MutableLiveData<Module>()
    private var populatedPreference = false

    companion object {
        private const val MODULE_ARG = "module"

        @JvmStatic
        fun createInstance(module: Module) = ModuleFragment().apply {
            arguments = Bundle().apply {
                putString(MODULE_ARG, module.id)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retrieveModule(sessionViewModel.gameSession.value)
        sessionViewModel.gameSession.observe(viewLifecycleOwner, Observer {
            retrieveModule(it)
        })
    }

    private fun retrieveModule(session: GameSession?) {
        val moduleId = requireArguments().getString(MODULE_ARG)
        if (moduleId != null) {
            val module = session?.modules?.get(moduleId)
            if (module != null) this.module.postValue(module)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = requireArguments().getString(MODULE_ARG)
        setPreferencesFromResource(R.xml.module_preferences, rootKey)

        val enableSwitch: SwitchPreference? = findPreference("enabled")
        enableSwitch?.isChecked = module.value?.isEnabled == true
        createModulePreferences(module.value)

        enableSwitch?.setOnPreferenceClickListener {
            module.value?.isEnabled = enableSwitch.isChecked
            true
        }
        this.module.observe(this, Observer {
            enableSwitch?.isChecked = it?.isEnabled == true
            createModulePreferences(it)
        })
    }

    private fun createModulePreferences(module: Module?) {
        module ?: return
        if (!populatedPreference) {
            val category: PreferenceCategory = findPreference("module") ?: return
            module.buildPreferences(requireContext(), viewLifecycleOwner, category)
            populatedPreference = true
        }
    }
}