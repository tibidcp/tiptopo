package com.tibi.tiptopo.presentation.ui

import android.content.Context
import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolyline
import com.tibi.tiptopo.domain.Line
import com.tibi.tiptopo.domain.LineType
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.presentation.bitmapDescriptorFromVector

const val PatternDash = 20f
const val PatternGap = 10f

fun Polyline.setPolylinePattern(type: LineType): Polyline {
    pattern = when (type) {
        LineType.Continuous -> null
        LineType.Dashed -> listOf(Dash(PatternDash), Gap(PatternGap))
        LineType.Dotted -> listOf(Dot(), Gap(PatternGap))
        LineType.DashDotted ->  listOf(Dash(PatternDash), Gap(PatternGap), Dot(), Gap(PatternGap)
        )
    }
    return this
}

fun GoogleMap.drawAll(context: Context, measurements: List<Measurement>, lines: List<Line>) {
    measurements.forEach { measurement ->
        addMarker {
            position(LatLng(measurement.latitude, measurement.longitude))
            title(measurement.number.toString())
            icon(bitmapDescriptorFromVector(
                context,
                measurement.type.vectorResId,
                Color.BLACK
            ))
            anchor(measurement.type.anchorX, measurement.type.anchorY)
        }.tag = measurement.id
    }

    lines.forEach { line ->
        addPolyline {
            clickable(true)
            color(line.color)
            width(5f)

            line.vertices
                .sortedBy { it.index }
                .filter { vertex -> measurements.any {
                    it.id == vertex.measurementId}
                }
                .map { vertex ->
                    val measurement = measurements
                        .first { it.id == vertex.measurementId }
                    LatLng(measurement.latitude, measurement.longitude)
                }.forEach { add(it) }
        }.setPolylinePattern(line.type).tag = line.id
    }
}
