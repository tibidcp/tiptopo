package com.tibi.tiptopo.presentation

import android.content.Context
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.parser.IDataParser
import org.osgeo.proj4j.BasicCoordinateTransform
import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.ProjCoordinate
import kotlin.math.*

const val SCALE = 10.0

fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

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