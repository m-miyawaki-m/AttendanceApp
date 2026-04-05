package com.example.attendanceapp

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 勤怠記録を表すエンティティ
 */
@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,               // 例: "2026/04/04"
    val clockInTime: String,        // 例: "09:00"
    val clockInLocation: String,    // 出勤時の位置情報
    val clockOutTime: String? = null,       // 退勤時刻（未退勤なら null）
    val clockOutLocation: String? = null    // 退勤時の位置情報（未退勤なら null）
)
