package com.example.attendanceapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 勤怠記録の ViewModel：UI とデータ層を仲介する
 */
class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    /** 全勤怠記録（Flow） */
    val allRecords: Flow<List<AttendanceRecord>>

    /** 全勤怠記録（LiveData）：UI から observe 用 */
    val allRecordsLiveData: LiveData<List<AttendanceRecord>>

    init {
        val dao = AttendanceDatabase.getInstance(application).attendanceDao()
        repository = AttendanceRepository(dao)
        allRecords = repository.allRecords
        allRecordsLiveData = allRecords.asLiveData()
    }

    /** 出勤を記録する */
    fun clockIn(date: String, time: String, location: String) {
        viewModelScope.launch {
            val record = AttendanceRecord(
                date = date,
                clockInTime = time,
                clockInLocation = location
            )
            repository.insertClockIn(record)
        }
    }

    /** 退勤を記録する */
    fun clockOut(time: String, location: String) {
        viewModelScope.launch {
            val activeRecord = repository.getActiveRecord()
            if (activeRecord != null) {
                repository.updateClockOut(activeRecord.id, time, location)
            }
        }
    }
}
