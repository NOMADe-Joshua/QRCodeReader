package com.example.labqrscanner.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ScanRepository(context: Context) {

    private val dbHelper = ScanDbHelper(context)

    suspend fun addScan(uniqueId: String, status: ScanStatus, comment: String = ""): Unit = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        val dateIso = now.toLocalDate().format(ISO_DATE_FORMAT)
        val dateDisplay = now.toLocalDate().format(DISPLAY_DATE_FORMAT)
        val time = now.toLocalTime().format(TIME_FORMAT)
        dbHelper.insert(dateIso, dateDisplay, time, uniqueId, status, comment)
    }

    suspend fun updateComment(id: Long, comment: String): Unit = withContext(Dispatchers.IO) {
        dbHelper.updateComment(id, comment)
    }

    suspend fun deleteAll(): Unit = withContext(Dispatchers.IO) {
        dbHelper.deleteAll()
    }

    suspend fun getAll(): List<ScanRecord> = withContext(Dispatchers.IO) {
        dbHelper.getAll()
    }

    companion object {
        val ISO_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val DISPLAY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
