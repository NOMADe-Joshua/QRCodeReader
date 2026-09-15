package com.example.labqrscanner.export

import com.example.labqrscanner.data.ScanRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

enum class ExportRange(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This week"),
    ALL("All records");

    fun filter(records: List<ScanRecord>): List<ScanRecord> {
        if (this == ALL) return records
        val today = LocalDate.now()
        return records.filter { record ->
            val recordDate = LocalDate.parse(record.dateIso, DateTimeFormatter.ISO_LOCAL_DATE)
            when (this) {
                TODAY -> recordDate == today
                THIS_WEEK -> {
                    val weekFields = WeekFields.of(Locale.getDefault())
                    recordDate.get(weekFields.weekOfWeekBasedYear()) == today.get(weekFields.weekOfWeekBasedYear()) &&
                        recordDate.get(weekFields.weekBasedYear()) == today.get(weekFields.weekBasedYear())
                }
                ALL -> true
            }
        }
    }
}
