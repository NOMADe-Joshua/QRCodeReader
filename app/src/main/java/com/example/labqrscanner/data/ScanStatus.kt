package com.example.labqrscanner.data

enum class ScanStatus(val label: String) {
    OPENED("Opened"),
    TRASH("Trash"),
    USED("Used");

    companion object {
        fun fromLabel(label: String): ScanStatus =
            entries.firstOrNull { it.label == label } ?: OPENED
    }
}
