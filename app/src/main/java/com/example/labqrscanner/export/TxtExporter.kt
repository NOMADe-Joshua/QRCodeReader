package com.example.labqrscanner.export

import com.example.labqrscanner.data.ScanRecord

object TxtExporter {

    fun toTxt(records: List<ScanRecord>): String {
        if (records.isEmpty()) return "No scan records.\r\n"

        val builder = StringBuilder()
        records.forEach { record ->
            builder.append("Date: ").append(record.dateDisplay).append("\r\n")
                .append("Time: ").append(record.time).append("\r\n")
                .append("Unique ID: ").append(record.uniqueId).append("\r\n")
                .append("Status: ").append(record.status.label).append("\r\n")
                .append("Comment: ").append(record.comment.ifBlank { "-" }).append("\r\n")
                .append("----------------------------------------\r\n")
        }
        return builder.toString()
    }
}
