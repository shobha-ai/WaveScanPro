package com.wavescanpro

import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
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
import android.telephony.CellInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CellularFragment : ScanFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chart: LineChart
    private lateinit var searchEditText: EditText
    private lateinit var adapter: CellularAdapter
    private lateinit var telephonyManager: TelephonyManager
    private val handler = Handler(Looper.getMainLooper())
    private val signalData = mutableMapOf<String, MutableList<Entry>>()
    private var time = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cellular, container, false)

        recyclerView = view.findViewById(R.id.recycler_cellular)
        chart = view.findViewById(R.id.chart_cellular)
        searchEditText = view.findViewById(R.id.search_cellular)

        telephonyManager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        adapter = CellularAdapter()
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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION), 3)
            return
        }
        val cellInfo = telephonyManager.allCellInfo ?: emptyList()
        adapter.updateData(cellInfo)
        updateGraph(cellInfo)
        logScanResults(cellInfo)
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
    }

    private fun updateGraph(cellInfo: List<CellInfo>) {
        time += 5f
        cellInfo.forEach { info ->
            val id = info.cellIdentity.toString().take(20)
            val strength = when {
                info.isRegistered -> info.cellSignalStrength.dbm.toFloat()
                else -> -100f
            }
            signalData.getOrPut(id) { mutableListOf() }.add(Entry(time, strength))
        }
        if (cellInfo.isNotEmpty()) {
            val entries = signalData.values.firstOrNull() ?: mutableListOf()
            val dataSet = LineDataSet(entries, "Signal Strength")
            dataSet.color = R.color.colorAccent
            dataSet.setDrawCircles(false)
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    }

    private fun logScanResults(cellInfo: List<CellInfo>) {
        val logFile = File(requireContext().filesDir, "scan_logs.csv")
        val isNewFile = !logFile.exists()
        logFile.appendText(
            buildString {
                if (isNewFile) append("Timestamp,Type,ID,Details\n")
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                cellInfo.forEach { info ->
                    val id = info.cellIdentity.toString().take(20)
                    val strength = info.cellSignalStrength.dbm
                    val type = when (info) {
                        is CellInfo.Lte -> "LTE"
                        is CellInfo.Nr -> "5G"
                        is CellInfo.Gsm -> "GSM"
                        is CellInfo.Cdma -> "CDMA"
                        is CellInfo.Wcdma -> "WCDMA"
                        else -> "Unknown"
                    }
                    append("$timestamp,Cellular,$id,$strength dBm, $type\n")
                }
            }
        )
    }
}
