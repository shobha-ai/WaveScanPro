package com.wavescanpro

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.os.Handler
import android.os.Looper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BluetoothFragment : ScanFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chart: LineChart
    private lateinit var searchEditText: EditText
    private lateinit var adapter: BluetoothAdapter
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val signalData = mutableMapOf<String, MutableList<Entry>>()
    private var time = 0f

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                    device?.let { updateDeviceData(it, rssi) }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bluetooth, container, false)

        recyclerView = view.findViewById(R.id.recycler_bluetooth)
        chart = view.findViewById(R.id.chart_bluetooth)
        searchEditText = view.findViewById(R.id.search_bluetooth)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return view
        adapter = BluetoothAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        setupChart()
        refreshScan()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s.toString())
            }
        })

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        requireContext().registerReceiver(receiver, filter)

        handler.postDelayed(object : Runnable {
            override fun run() {
                refreshScan()
                handler.postDelayed(this, 5000)
            }
        }, 5000)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(receiver)
    }

    override fun refreshScan() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), 2)
            return
        }
        val pairedDevices = bluetoothAdapter.bondedDevices ?: emptySet()
        adapter.updateData(pairedDevices.toList())
        logScanResults(pairedDevices.toList())
        if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()
        bluetoothAdapter.startDiscovery()
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
    }

    private fun updateDeviceData(device: BluetoothDevice, rssi: Int) {
        time += 5f
        val name = device.name?.takeIf { it.isNotEmpty() } ?: device.address
        signalData.getOrPut(name) { mutableListOf() }.add(Entry(time, rssi.toFloat()))
        if (signalData.isNotEmpty()) {
            val entries = signalData.values.firstOrNull() ?: mutableListOf()
            val dataSet = LineDataSet(entries, "Signal Strength")
            dataSet.color = R.color.colorAccent
            dataSet.setDrawCircles(false)
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
        logDeviceData(device, rssi)
    }

    private fun logScanResults(devices: List<BluetoothDevice>) {
        val logFile = File(requireContext().filesDir, "scan_logs.csv")
        val isNewFile = !logFile.exists()
        logFile.appendText(
            buildString {
                if (isNewFile) append("Timestamp,Type,ID,Details\n")
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                devices.forEach { device ->
                    val id = device.name?.takeIf { it.isNotEmpty() } ?: device.address
                    val type = when (device.type) {
                        BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
                        BluetoothDevice.DEVICE_TYPE_LE -> "LE"
                        BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual"
                        else -> "Unknown"
                    }
                    append("$timestamp,Bluetooth,$id,Type: $type\n")
                }
            }
        )
    }

    private fun logDeviceData(device: BluetoothDevice, rssi: Int) {
        val logFile = File(requireContext().filesDir, "scan_logs.csv")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val id = device.name?.takeIf { it.isNotEmpty() } ?: device.address
        val type = when (device.type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
            BluetoothDevice.DEVICE_TYPE_LE -> "LE"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual"
            else -> "Unknown"
        }
        logFile.appendText("$timestamp,Bluetooth,$id,Type: $type, RSSI: $rssi\n")
    }
}
