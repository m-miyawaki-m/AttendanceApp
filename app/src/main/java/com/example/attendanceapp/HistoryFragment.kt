package com.example.attendanceapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 履歴一覧画面：勤怠記録を RecyclerView でカード表示する
 */
class HistoryFragment : Fragment() {

    private val viewModel: AttendanceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_history)
        val textEmpty = view.findViewById<TextView>(R.id.text_empty)
        val adapter = AttendanceAdapter()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // LiveData を observe してリストを更新
        viewModel.allRecordsLiveData.observe(viewLifecycleOwner) { records ->
            adapter.submitList(records)
            // データが空なら空メッセージを表示
            textEmpty.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
