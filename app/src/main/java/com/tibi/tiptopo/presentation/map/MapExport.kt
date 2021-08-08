package com.tibi.tiptopo.presentation.map

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

@Composable
fun ExportMapFiles(mapViewModel: MapViewModel) {
    val rawText = mapViewModel.rawText
    val measurementJson = mapViewModel.measurementJsonText
    val linearJson = mapViewModel.linearJsonText

    if (rawText.isNotBlank() && measurementJson.isNotBlank() && linearJson.isNotBlank()) {

        val context = LocalContext.current

        val docPath = File(context.filesDir, "docs")
        val docFile = File(docPath, "raw.rdf")
        val docUri = FileProvider.getUriForFile(context, "com.tibi.tiptopo.fileprovider", docFile)

        val measurementFile = File(docPath, "measurements.json")
        val measurementUri = FileProvider.getUriForFile(context, "com.tibi.tiptopo.fileprovider", measurementFile)

        val linearFile = File(docPath, "lines.json")
        val linearUri = FileProvider.getUriForFile(context, "com.tibi.tiptopo.fileprovider", linearFile)

        val uris = arrayListOf(docUri, measurementUri, linearUri)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            type = "text/*"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        val dir = context.filesDir.absolutePath + File.separator + "docs"
        val projDir = File(dir)
        if (!projDir.exists()) {
            projDir.mkdirs()
        }
        val file = File(projDir, "raw.rdf")
        val stream = FileOutputStream(file)
        stream.use {
            stream.write(rawText.toByteArray())
        }

        val fileMeasurement = File(projDir, "measurements.json")
        val streamMeasurement = FileOutputStream(fileMeasurement)
        streamMeasurement.use {
            streamMeasurement.write(measurementJson.toByteArray())
        }

        val fileLinear = File(projDir, "lines.json")
        val streamLinear = FileOutputStream(fileLinear)
        streamLinear.use {
            streamLinear.write(linearJson.toByteArray())
        }

        context.startActivity(shareIntent)
        mapViewModel.onResetExportTexts()
    }
}