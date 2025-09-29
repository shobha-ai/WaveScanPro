package com.wavescanpro

import android.telephony.CellInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CellularAdapter : RecyclerView.Adapter<CellularAdapter.ViewHolder>() {

    private var data: List<CellInfo> = emptyList()
    private var filteredData: List<CellInfo> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredData[position]
        val id = item.cellIdentity.toString().take(20)
        val strength = item.cellSignalStrength.dbm
        val type = when (item) {
            is CellInfo.Lte -> "LTE"
            is CellInfo.Nr -> "5G"
            is CellInfo.Gsm -> "GSM"
            is CellInfo.Cdma -> "CDMA"
            is CellInfo.Wcdma -> "WCDMA"
            else -> "Unknown"
        }
        holder.textView.text = "ID: $id - $strength dBm - $type"
    }

    override fun getItemCount(): Int = filteredData.size

    fun updateData(newData: List<CellInfo>) {
        data = newData
        filteredData = newData
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredData = if (query.isEmpty()) {
            data
        } else {
            data.filter { it.cellIdentity.toString().contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
