package com.example.labqrscanner.export

import com.example.labqrscanner.data.ScanRecord

object CsvExporter {

    private const val DELIMITER = ";"

    fun toCsv(records: List<ScanRecord>): String {
        val builder = StringBuilder()
        builder.append("Date").append(DELIMITER)
            .append("Time").append(DELIMITER)
            .append("Unique ID").append(DELIMITER)
            .append("Status").append(DELIMITER)
            .append("Comment")
            .append("\r\n")

        records.forEach { record ->
            builder.append(escape(record.dateDisplay)).append(DELIMITER)
                .append(escape(record.time)).append(DELIMITER)
                .append(escape(record.uniqueId)).append(DELIMITER)
                .append(escape(record.status.label)).append(DELIMITER)
                .append(escape(record.comment))
                .append("\r\n")
        }
        return builder.toString()
    }

    private fun escape(value: String): String {
        return if (value.contains(DELIMITER) || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
