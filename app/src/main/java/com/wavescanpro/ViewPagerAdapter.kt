package com.wavescanpro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WifiFragment()
            1 -> BluetoothFragment()
            2 -> CellularFragment()
            3 -> LogsFragment()
            4 -> SettingsFragment()
            else -> Fragment()
        }
    }

    fun getFragment(position: Int): ScanFragment? {
        return when (position) {
            0 -> WifiFragment()
            1 -> BluetoothFragment()
            2 -> CellularFragment()
            else -> null
        } as? ScanFragment
    }
}

abstract class ScanFragment : Fragment() {
    abstract fun refreshScan()
}
