package com.wavescanpro

import android.content.Context
import android.net.wifi.WifiManager
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
import android.widget.Toast

class WifiFragment : ScanFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chart: LineChart
    private lateinit var searchEditText: EditText
    private lateinit var adapter: WifiAdapter
    private lateinit var wifiManager: WifiManager
    private val handler = Handler(Looper.getMainLooper())
    private val signalData = mutableMapOf<String, MutableList<Entry>>()
    private var time = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_wifi, container, false)

        recyclerView = view.findViewById(R.id.recycler_wifi)
        chart = view.findViewById(R.id.chart_wifi)
        searchEditText = view.findViewById(R.id.search_wifi)

        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        adapter = WifiAdapter()
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

        handler.postDelayed(object : Runnable {
            override fun run() {
                refreshScan()
                handler.postDelayed(this, 5000)
            }
        }, 5000)

        return view
    }

    override fun refreshScan() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        wifiManager.startScan()
        val results = wifiManager.scanResults
        adapter.updateData(results)
        updateGraph(results)
        logScanResults(results)
        analyzeInterference(results)
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
    }

    private fun updateGraph(results: List<android.net.wifi.ScanResult>) {
        time += 5f
        results.forEach { result ->
            val ssid = result.SSID.takeIf { it.isNotEmpty() } ?: "Hidden SSID"
            val strength = result.level.toFloat()
            signalData.getOrPut(ssid) { mutableListOf() }.add(Entry(time, strength))
        }
        if (results.isNotEmpty()) {
            val entries = signalData.values.firstOrNull() ?: mutableListOf()
            val dataSet = LineDataSet(entries, "Signal Strength")
            dataSet.color = R.color.colorAccent
            dataSet.setDrawCircles(false)
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    }

    private fun logScanResults(results: List<android.net.wifi.ScanResult>) {
        val logFile = File(requireContext().filesDir, "scan_logs.csv")
        val isNewFile = !logFile.exists()
        logFile.appendText(
            buildString {
                if (isNewFile) append("Timestamp,Type,ID,Details\n")
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                results.forEach { result ->
                    val ssid = result.SSID.takeIf { it.isNotEmpty() } ?: "Hidden SSID"
                    val details = "${result.level} dBm, ${result.capabilities}"
                    append("$timestamp,WiFi,$ssid,$details\n")
                }
            }
        )
    }

    private fun analyzeInterference(results: List<android.net.wifi.ScanResult>) {
        val channelUsage = IntArray(12) { 0 } // Channels 1-11
        results.forEach { result ->
            val channel = getChannelFromFrequency(result.frequency)
            if (channel in 1..11) channelUsage[channel]++
        }
        val leastCrowded = channelUsage.indices.filter { it > 0 }.minByOrNull { channelUsage[it] } ?: 1
        if (results.isNotEmpty()) {
            Toast.makeText(context, "Suggested Wi-Fi channel: $leastCrowded (least crowded)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getChannelFromFrequency(freq: Int): Int {
        return when {
            freq in 2412..2484 -> (freq - 2412) / 5 + 1
            else -> -1
        }
    }
}
