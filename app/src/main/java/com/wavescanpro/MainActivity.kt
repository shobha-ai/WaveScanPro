package com.wavescanpro

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var fabRefresh: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        fabRefresh = findViewById(R.id.fab_refresh)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_wifi)
                1 -> getString(R.string.tab_bluetooth)
                2 -> getString(R.string.tab_cellular)
                3 -> getString(R.string.tab_logs)
                4 -> getString(R.string.tab_settings)
                else -> ""
            }
        }.attach()

        fabRefresh.setOnClick {
            animateRefresh()
            val currentFragment = adapter.getFragment(viewPager.currentItem)
            currentFragment?.refreshScan()
        }
    }

    private fun animateRefresh() {
        val rotation = PropertyValuesHolder.ofFloat("rotation", 0f, 360f)
        ObjectAnimator.ofPropertyValuesHolder(fabRefresh, rotation).apply {
            duration = 500
            start()
        }
    }
}
