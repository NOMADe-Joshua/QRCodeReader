@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.labqrscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.labqrscanner.data.ScanRecord
import com.example.labqrscanner.data.ScanRepository
import com.example.labqrscanner.data.ScanStatus
import com.example.labqrscanner.export.ExportRange
import com.example.labqrscanner.export.MailExporter
import com.example.labqrscanner.ui.theme.LabQRScannerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = ScanRepository(applicationContext)
        setContent {
            LabQRScannerTheme {
                InventoryScreen(repository = repository)
            }
        }
    }
}

@Composable
fun InventoryScreen(repository: ScanRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var records by remember { mutableStateOf<List<ScanRecord>>(emptyList()) }
    var pendingScan by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showDeleteHistoryDialog by remember { mutableStateOf(false) }
    var editingComment by remember { mutableStateOf<ScanRecord?>(null) }

    suspend fun refresh() {
        records = repository.getAll()
    }

    LaunchedEffect(Unit) { refresh() }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val scannedText = result.data?.getStringExtra(ScanActivity.EXTRA_SCANNED_TEXT)
        if (result.resultCode == android.app.Activity.RESULT_OK && scannedText != null) {
            pendingScan = scannedText
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lab QR Scanner") },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export / send by mail")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                TextButton(onClick = { showDeleteHistoryDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Text(" Delete history", modifier = Modifier.padding(start = 4.dp))
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { scanLauncher.launch(android.content.Intent(context, ScanActivity::class.java)) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Scan QR code")
            }
        }
    ) { padding ->
        ScanTable(
            records = records,
            modifier = Modifier.padding(padding),
            onCommentClick = { record -> editingComment = record }
        )
    }

    pendingScan?.let { scannedText ->
        StatusPickerDialog(
            scannedText = scannedText,
            onDismiss = { pendingScan = null },
            onStatusChosen = { status ->
                scope.launch {
                    repository.addScan(scannedText, status)
                    refresh()
                }
                pendingScan = null
            }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExportCsv = { range ->
                showExportDialog = false
                scope.launch {
                    val all = repository.getAll()
                    val filtered = range.filter(all)
                    MailExporter.shareAsCsv(context, filtered, range)
                }
            },
            onExportTxt = { range ->
                showExportDialog = false
                scope.launch {
                    val all = repository.getAll()
                    val filtered = range.filter(all)
                    MailExporter.shareAsTxt(context, filtered, range)
                }
            }
        )
    }

    if (showDeleteHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteHistoryDialog = false },
            title = { Text("Delete history?") },
            text = { Text("This will permanently delete all ${records.size} scan record(s). This cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    showDeleteHistoryDialog = false
                    scope.launch {
                        repository.deleteAll()
                        refresh()
                    }
                }) { Text("Delete all") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteHistoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    editingComment?.let { record ->
        CommentEditDialog(
            record = record,
            onDismiss = { editingComment = null },
            onSave = { comment ->
                scope.launch {
                    repository.updateComment(record.id, comment)
                    refresh()
                }
                editingComment = null
            }
        )
    }
}

@Composable
private fun ScanTable(
    records: List<ScanRecord>,
    modifier: Modifier = Modifier,
    onCommentClick: (ScanRecord) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            TableCell("Date", weight = 0.9f, isHeader = true)
            TableCell("Time", weight = 0.8f, isHeader = true)
            TableCell("Unique ID", weight = 1.4f, isHeader = true)
            TableCell("Status", weight = 0.9f, isHeader = true)
            TableCell("Comment", weight = 1.1f, isHeader = true)
        }
        HorizontalDivider()

        if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No scans yet. Tap + to scan a bottle's QR code.",
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn {
                items(records, key = { it.id }) { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCommentClick(record) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        TableCell(record.dateDisplay, weight = 0.9f)
                        TableCell(record.time, weight = 0.8f)
                        TableCell(record.uniqueId, weight = 1.4f)
                        TableCell(record.status.label, weight = 0.9f)
                        TableCell(record.comment.ifBlank { "Add..." }, weight = 1.1f)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = if (isHeader) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun StatusPickerDialog(
    scannedText: String,
    onDismiss: () -> Unit,
    onStatusChosen: (ScanStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scanned: $scannedText") },
        text = {
            Column {
                Text("What should this bottle be marked as?")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ScanStatus.entries.forEach { status ->
                        Button(onClick = { onStatusChosen(status) }) {
                            Text(status.label)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ExportDialog(
    onDismiss: () -> Unit,
    onExportCsv: (ExportRange) -> Unit,
    onExportTxt: (ExportRange) -> Unit
) {
    var selected by remember { mutableStateOf(ExportRange.TODAY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send scans by mail") },
        text = {
            Column {
                ExportRange.entries.forEach { range ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        RadioButton(selected = selected == range, onClick = { selected = range })
                        Text(range.label, modifier = Modifier.padding(start = 8.dp, top = 12.dp))
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onExportTxt(selected) }) { Text("Send as TXT") }
                Button(onClick = { onExportCsv(selected) }, modifier = Modifier.padding(start = 8.dp)) {
                    Text("Send as CSV")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CommentEditDialog(
    record: ScanRecord,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember(record.id) { mutableStateOf(record.comment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comment for ${record.uniqueId}") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Comment") },
                singleLine = false,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
