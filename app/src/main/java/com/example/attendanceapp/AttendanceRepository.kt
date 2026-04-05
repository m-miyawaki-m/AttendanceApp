package com.example.attendanceapp

import kotlinx.coroutines.flow.Flow

/**
 * 勤怠記録のリポジトリ：DAO をラップしてデータアクセスを提供
 */
class AttendanceRepository(private val dao: AttendanceDao) {

    /** 全件取得（id の降順） */
    val allRecords: Flow<List<AttendanceRecord>> = dao.getAll()

    /** 勤務中（未退勤）のレコードを取得 */
    suspend fun getActiveRecord(): AttendanceRecord? {
        return dao.getActiveRecord()
    }

    /** 出勤レコードを挿入 */
    suspend fun insertClockIn(record: AttendanceRecord) {
        dao.insertClockIn(record)
    }

    /** 退勤情報を更新 */
    suspend fun updateClockOut(id: Int, clockOutTime: String, clockOutLocation: String) {
        dao.updateClockOut(id, clockOutTime, clockOutLocation)
    }
}
