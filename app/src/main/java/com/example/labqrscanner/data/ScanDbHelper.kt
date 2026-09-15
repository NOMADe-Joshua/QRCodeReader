package com.example.labqrscanner.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScanDbHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_SCANS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DATE_ISO TEXT NOT NULL,
                $COL_DATE_DISPLAY TEXT NOT NULL,
                $COL_TIME TEXT NOT NULL,
                $COL_UNIQUE_ID TEXT NOT NULL,
                $COL_STATUS TEXT NOT NULL,
                $COL_COMMENT TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_SCANS ADD COLUMN $COL_COMMENT TEXT NOT NULL DEFAULT ''")
        }
    }

    fun insert(
        dateIso: String,
        dateDisplay: String,
        time: String,
        uniqueId: String,
        status: ScanStatus,
        comment: String = ""
    ): Long {
        val values = ContentValues().apply {
            put(COL_DATE_ISO, dateIso)
            put(COL_DATE_DISPLAY, dateDisplay)
            put(COL_TIME, time)
            put(COL_UNIQUE_ID, uniqueId)
            put(COL_STATUS, status.name)
            put(COL_COMMENT, comment)
        }
        return writableDatabase.insert(TABLE_SCANS, null, values)
    }

    fun updateComment(id: Long, comment: String) {
        val values = ContentValues().apply {
            put(COL_COMMENT, comment)
        }
        writableDatabase.update(TABLE_SCANS, values, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun deleteAll() {
        writableDatabase.delete(TABLE_SCANS, null, null)
    }

    fun getAll(): List<ScanRecord> {
        val records = mutableListOf<ScanRecord>()
        readableDatabase.rawQuery(
            "SELECT $COL_ID, $COL_DATE_ISO, $COL_DATE_DISPLAY, $COL_TIME, $COL_UNIQUE_ID, $COL_STATUS, $COL_COMMENT " +
                "FROM $TABLE_SCANS ORDER BY $COL_ID DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                records.add(
                    ScanRecord(
                        id = cursor.getLong(0),
                        dateIso = cursor.getString(1),
                        dateDisplay = cursor.getString(2),
                        time = cursor.getString(3),
                        uniqueId = cursor.getString(4),
                        status = ScanStatus.valueOf(cursor.getString(5)),
                        comment = cursor.getString(6) ?: ""
                    )
                )
            }
        }
        return records
    }

    companion object {
        private const val DATABASE_NAME = "scan_records.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_SCANS = "scans"
        private const val COL_ID = "id"
        private const val COL_DATE_ISO = "date_iso"
        private const val COL_DATE_DISPLAY = "date_display"
        private const val COL_TIME = "time"
        private const val COL_UNIQUE_ID = "unique_id"
        private const val COL_STATUS = "status"
        private const val COL_COMMENT = "comment"
    }
}
