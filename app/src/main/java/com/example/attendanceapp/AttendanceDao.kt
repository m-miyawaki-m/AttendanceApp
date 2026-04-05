package com.example.attendanceapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 勤怠記録へのデータアクセスオブジェクト
 */
@Dao
interface AttendanceDao {

    /** 全件取得（id の降順） */
    @Query("SELECT * FROM attendance_records ORDER BY id DESC")
    fun getAll(): Flow<List<AttendanceRecord>>

    /** 勤務中（未退勤）のレコードを取得 */
    @Query("SELECT * FROM attendance_records WHERE clockOutTime IS NULL LIMIT 1")
    suspend fun getActiveRecord(): AttendanceRecord?

    /** 出勤レコードを挿入 */
    @Insert
    suspend fun insertClockIn(record: AttendanceRecord)

    /** 退勤情報を更新（id を指定して退勤時刻・位置情報をセット） */
    @Query("UPDATE attendance_records SET clockOutTime = :clockOutTime, clockOutLocation = :clockOutLocation WHERE id = :id")
    suspend fun updateClockOut(id: Int, clockOutTime: String, clockOutLocation: String)
}
