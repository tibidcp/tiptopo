package com.tibi.tiptopo.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.domain.TotalStation
import com.tibi.tiptopo.presentation.di.CurrentTotalStation
import com.tibi.tiptopo.presentation.parser.*
import org.osgeo.proj4j.BasicCoordinateTransform
import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.ProjCoordinate
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

const val SCALE = 10.0

fun String.tsParser(totalStation: TotalStation): IDataParser {
    return when (totalStation) {
        TotalStation.Nikon -> NikonRawParser(this)
        TotalStation.Trimble -> TrimbleM5Parser(this)
        TotalStation.Leica -> LeicaGsiParser(this)
        TotalStation.Sokkia -> SokkiaParser(this)
    }
}

fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, color: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        DrawableCompat.setTint(this, color)
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

fun LatLng.toPoint(): Point {
    val factory = CRSFactory()
    // wgs84
    val src = factory.createFromName("EPSG:4326")
    // mggt
    val dst = factory.createFromParameters("mggt", "+proj=tmerc " +
            "+lat_0=55.66666666667 +lon_0=37.5 +k=1 +x_0=16.098 +y_0=14.512 +ellps=bessel " +
            "+towgs84=316.151,78.924,589.650,-1.57273,2.69209,2.34693,8.4507 +units=m +no_defs")

    val transform = BasicCoordinateTransform(src, dst)

    val srcCoord = ProjCoordinate(this.longitude, this.latitude)
    val dstCoord = ProjCoordinate()

    transform.transform(srcCoord, dstCoord)

    return Point(dstCoord.y / SCALE, dstCoord.x / SCALE)
}

fun Point.toLatLng(): LatLng {
    val factory = CRSFactory()
    // mggt
    val src = factory.createFromParameters("mggt", "+proj=tmerc " +
            "+lat_0=55.66666666667 +lon_0=37.5 +k=1 +x_0=16.098 +y_0=14.512 +ellps=bessel " +
            "+towgs84=316.151,78.924,589.650,-1.57273,2.69209,2.34693,8.4507 +units=m +no_defs")
    // wgs84
    val dst = factory.createFromName("EPSG:4326")

    val transform = BasicCoordinateTransform(src, dst)

    val srcCoord = ProjCoordinate(this.y * SCALE, this.x * SCALE)
    val dstCoord = ProjCoordinate()

    transform.transform(srcCoord, dstCoord)

    return LatLng(dstCoord.y, dstCoord.x)
}

fun Point.directAngTo(pointB: Point): Double {
    val pointA = this
    val deltaX = pointB.x - pointA.x
    val deltaY = pointB.y - pointA.y
    val rumb = Math.toDegrees(atan(abs(deltaY / deltaX)))
    var directAng = 0.0
    if (deltaX == 0.0 && deltaY == 0.0) {
        throw IllegalArgumentException("Same point")
    } else if (deltaX == 0.0 && deltaY > 0) {
        directAng = 90.0
    } else if (deltaX == 0.0 && deltaY < 0) {
        directAng = 270.0
    } else if (deltaX > 0 && deltaY == 0.0) {
        directAng = 0.0
    } else if (deltaX < 0 && deltaY == 0.0) {
        directAng = 180.0
    } else if (deltaX > 0 && deltaY > 0) {
        directAng = rumb
    } else if (deltaX < 0 && deltaY > 0) {
        directAng = 180.0 - rumb
    } else if (deltaX < 0 && deltaY < 0) {
        directAng = 180.0 + rumb
    } else if (deltaX > 0 && deltaY < 0) {
        directAng = 360.0 - rumb
    }

    return directAng
}

fun Point.polylineAngleTo(pointB: Point): Double {
    val directAng = this.directAngTo(pointB)
    return if (directAng < 90) {
        directAng + 270
    } else {
        directAng - 90
    }
}

fun Measurement.directAngTo(measurementB: Measurement): Double {
    val measurementA = this
    val latLngA = LatLng(measurementA.latitude, measurementA.longitude)
    val latLngB = LatLng(measurementB.latitude, measurementB.longitude)
    val pointA = latLngA.toPoint()
    val pointB = latLngB.toPoint()

    return pointA.directAngTo(pointB)
}

fun Station.getCoordinate(ha: Double, va: Double, sd: Double): Point {
    if (sd == 0.0) {
        throw IllegalArgumentException("Same point")
    }

    val station = this

    var beta = ha - station.backsightHA
    if (beta < 0) {
        beta += 360.0
    }
    var directAng = beta + station.backsightDA
    if (directAng >= 360.0) {
        directAng -= 360.0
    }

    val d = sd * cos(va.toRadians())

    val deltaX = d * cos(directAng.toRadians())
    val deltaY = d * sin(directAng.toRadians())

    return Point(station.x + deltaX, station.y + deltaY)
}

fun Station.setMeasurementRaw(
    measurement: Measurement,
    totalStation: TotalStation
) {
    val stationPoint = Point(x, y)
    val measurementPoint = LatLng(measurement.latitude, measurement.longitude).toPoint()
    val measurementDa = stationPoint.directAngTo(measurementPoint)
    var beta = measurementDa - backsightDA
    if (beta < 0.0) {
        beta += 360.0
    }
    var measurementHa = backsightHA + beta
    if (measurementHa >= 360.0) {
        measurementHa -= 360.0
    }
    var measurementVa = 0.0
    if (totalStation == TotalStation.Sokkia) {
        measurementVa = 90.0
    }
    val measurementSd = stationPoint.distanceTo(measurementPoint)

    measurement.va = measurementVa
    measurement.ha = measurementHa
    measurement.sd = measurementSd
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun Double.toDegrees(): Double {
    return Math.toDegrees(this)
}

fun Double.toRadians(): Double {
    return Math.toRadians(this)
}

fun Double.toRawDegrees(): String {
    val deg =  this.toInt()
    val minSec = ((this - deg) * 60)
    val min = minSec.toInt()
    val sec = ((minSec - min) * 60).toInt()
    return "$deg.${min.format()}${sec.format()}"
}

fun Double.format() = "%.3f".format(this).replace(",", ".")

fun Int.format() = "%02d".format(this)

fun Point.distanceTo(end: Point): Double {
    val start = this
    return sqrt(((end.x - start.x) * (end.x - start.x)) + ((end.y - start.y) * (end.y - start.y)))
}

fun pointOnLineCoordinate(start: Point, end: Point, distance: Double): Point {
    val length = start.distanceTo(end)
    if (distance >= length || distance <= 0) {
        throw IllegalArgumentException()
    }
    val ratio = distance / length
    val x = start.x + ((end.x - start.x) * ratio)
    val y = start.y + ((end.y - start.y) * ratio)
    return Point(x, y)
}
