package com.example.attendanceapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * 勤怠記録一覧の RecyclerView アダプター
 */
class AttendanceAdapter :
    ListAdapter<AttendanceRecord, AttendanceAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val dotClockOut: View = itemView.findViewById(R.id.dot_clock_out)
        private val textClockInTime: TextView = itemView.findViewById(R.id.text_clock_in_time)
        private val textClockInLocation: TextView = itemView.findViewById(R.id.text_clock_in_location)
        private val textClockOutTime: TextView = itemView.findViewById(R.id.text_clock_out_time)
        private val textClockOutLocation: TextView = itemView.findViewById(R.id.text_clock_out_location)

        fun bind(record: AttendanceRecord) {
            textDate.text = record.date
            textClockInTime.text = "出勤 ${record.clockInTime}"
            textClockInLocation.text = record.clockInLocation

            if (record.clockOutTime != null) {
                // 退勤済み：緑の丸を表示
                dotClockOut.setBackgroundResource(R.drawable.circle_green)
                textClockOutTime.text = "退勤 ${record.clockOutTime}"
                textClockOutLocation.text = record.clockOutLocation ?: ""
            } else {
                // 退勤未登録：グレー枠線の丸を表示
                dotClockOut.setBackgroundResource(R.drawable.circle_gray_outline)
                textClockOutTime.text = "退勤 --:--"
                textClockOutLocation.text = "退勤未登録"
            }
        }
    }

    /** リスト差分計算用コールバック */
    private object DiffCallback : DiffUtil.ItemCallback<AttendanceRecord>() {
        override fun areItemsTheSame(old: AttendanceRecord, new: AttendanceRecord): Boolean {
            return old.id == new.id
        }

        override fun areContentsTheSame(old: AttendanceRecord, new: AttendanceRecord): Boolean {
            return old == new
        }
    }
}
