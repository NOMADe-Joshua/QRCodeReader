package com.example.labqrscanner.data

data class ScanRecord(
    val id: Long,
    val dateDisplay: String,
    val dateIso: String,
    val time: String,
    val uniqueId: String,
    val status: ScanStatus,
    val comment: String = ""
)
