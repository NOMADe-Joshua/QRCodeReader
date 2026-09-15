package com.example.labqrscanner.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.labqrscanner.data.ScanRecord
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MailExporter {

    private val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

    fun shareAsCsv(context: Context, records: List<ScanRecord>, range: ExportRange) {
        val csv = CsvExporter.toCsv(records)
        share(context, records, range, csv, extension = "csv", mimeType = "text/csv")
    }

    fun shareAsTxt(context: Context, records: List<ScanRecord>, range: ExportRange) {
        val txt = TxtExporter.toTxt(records)
        share(context, records, range, txt, extension = "txt", mimeType = "text/plain")
    }

    private fun share(
        context: Context,
        records: List<ScanRecord>,
        range: ExportRange,
        content: String,
        extension: String,
        mimeType: String
    ) {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val file = File(exportsDir, "qr_scans_$timestamp.$extension")

        FileOutputStream(file).use { stream ->
            stream.write(UTF8_BOM)
            stream.write(content.toByteArray(Charsets.UTF_8))
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Lab QR scan export (${range.label})")
            putExtra(
                Intent.EXTRA_TEXT,
                "Attached: ${records.size} scan record(s) - ${range.label}."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Send scan export via"))
    }
}
