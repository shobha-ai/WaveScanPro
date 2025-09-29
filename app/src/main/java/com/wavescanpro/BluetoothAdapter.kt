package com.wavescanpro

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BluetoothAdapter : RecyclerView.Adapter<BluetoothAdapter.ViewHolder>() {

    private var data: List<BluetoothDevice> = emptyList()
    private var filteredData: List<BluetoothDevice> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredData[position]
        val name = item.name?.takeIf { it.isNotEmpty() } ?: item.address
        val type = when (item.type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
            BluetoothDevice.DEVICE_TYPE_LE -> "LE"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual"
            else -> "Unknown"
        }
        holder.textView.text = "$name - Type: $type"
    }

    override fun getItemCount(): Int = filteredData.size

    fun updateData(newData: List<BluetoothDevice>) {
        data = newData
        filteredData = newData
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredData = if (query.isEmpty()) {
            data
        } else {
            data.filter { it.name?.contains(query, ignoreCase = true) == true || it.address.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
