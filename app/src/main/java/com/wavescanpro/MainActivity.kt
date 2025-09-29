package com.wavescanpro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)

        val fragments = listOf(
            WifiFragment(),
            BluetoothFragment(),
            CellularFragment(),
            LogsFragment(),
            SettingsFragment()
        )
        val adapter = ViewPagerAdapter(this, fragments)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Wi-Fi"
                1 -> "Bluetooth"
                2 -> "Cellular"
                3 -> "Logs"
                4 -> "Settings"
                else -> null
            }
        }.attach()
    }
}
