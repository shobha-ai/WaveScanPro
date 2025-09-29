package com.wavescanpro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.net.wifi.ScanResult

class WifiAdapter : RecyclerView.Adapter<WifiAdapter.ViewHolder>() {

    private var data: List<ScanResult> = emptyList()
    private var filteredData: List<ScanResult> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredData[position]
        val ssid = item.SSID.takeIf { it.isNotEmpty() } ?: "Hidden SSID"
        holder.textView.text = "$ssid - ${item.level} dBm - ${item.capabilities}"
    }

    override fun getItemCount(): Int = filteredData.size

    fun updateData(newData: List<ScanResult>) {
        data = newData
        filteredData = newData
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredData = if (query.isEmpty()) {
            data
        } else {
            data.filter { it.SSID.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
