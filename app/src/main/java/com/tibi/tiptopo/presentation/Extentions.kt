package com.tibi.tiptopo.presentation

import android.content.Context
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import org.osgeo.proj4j.BasicCoordinateTransform
import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.ProjCoordinate

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

    return Point(dstCoord.x, dstCoord.y)
}
