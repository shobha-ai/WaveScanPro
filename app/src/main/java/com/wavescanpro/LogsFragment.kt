package com.wavescanpro

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
import java.io.File

class LogsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var adapter: LogsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_logs, container, false)

        recyclerView = view.findViewById(R.id.recycler_logs)
        searchEditText = view.findViewById(R.id.search_logs)

        adapter = LogsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        loadLogs()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s.toString())
            }
        })

        return view
    }

    private fun loadLogs() {
        val logFile = File(requireContext().filesDir, "scan_logs.csv")
        if (logFile.exists()) {
            val logs = mutableListOf<LogEntry>()
            logFile.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val parts = line.split(",")
                    if (parts.size >= 4) {
                        logs.add(LogEntry(parts[0], parts[1], parts[2], parts[3]))
                    }
                }
            }
            adapter.updateData(logs)
        }
    }
}

data class LogEntry(val timestamp: String, val type: String, val id: String, val details: String)

class LogsAdapter : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {

    private var data: List<LogEntry> = emptyList()
    private var filteredData: List<LogEntry> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredData[position]
        holder.textView.text = "${item.timestamp} - ${item.type}: ${item.id} - ${item.details}"
    }

    override fun getItemCount(): Int = filteredData.size

    fun updateData(newData: List<LogEntry>) {
        data = newData
        filteredData = newData
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredData = if (query.isEmpty()) {
            data
        } else {
            data.filter { it.id.contains(query, ignoreCase = true) || it.details.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
