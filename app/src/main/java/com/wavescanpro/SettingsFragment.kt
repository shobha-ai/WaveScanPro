package com.wavescanpro

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatDelegate

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val sharedPrefs = requireContext().getSharedPreferences("WaveScanProPrefs", Context.MODE_PRIVATE)
        val batterySwitch = view.findViewById<Switch>(R.id.switch_battery)
        val notificationsSwitch = view.findViewById<Switch>(R.id.switch_notifications)
        val themeSwitch = view.findViewById<Switch>(R.id.switch_theme)

        // Load saved states
        batterySwitch.isChecked = sharedPrefs.getBoolean("battery_optimization", true)
        notificationsSwitch.isChecked = sharedPrefs.getBoolean("notifications_enabled", true)
        themeSwitch.isChecked = sharedPrefs.getBoolean("high_contrast", false)

        // Handle toggle changes
        batterySwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("battery_optimization", isChecked).apply()
        }

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("high_contrast", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        return view
    }
}
