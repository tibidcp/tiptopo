package com.tibi.tiptopo.domain

import androidx.annotation.StringRes
import com.squareup.moshi.JsonClass
import com.tibi.tiptopo.R

data class Measurement(
    var id: String = "",
    var stationId: String = "",
    var name: String = "",
    var number: Int = 0,
    var type: PointType = PointType.Point,
    var rawString: String = "",
    var va: Double = 0.0,
    var ha: Double = 0.0,
    var sd: Double = 0.0,
    var ht: Double = 0.0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var date: Long = 0
)

enum class PointType(
    val vectorResId: Int,
    @StringRes val contentDescription: Int,
    val anchorX: Float,
    val anchorY: Float
) {
    Point(R.drawable.ic_point, R.string.point, 0.5f, 0.5f),
    BusSign(R.drawable.ic_bus_sign, R.string.bus_sign, 0.252f, 0.8837f),
    LampMetal(R.drawable.ic_lamp_metal, R.string.lamp_metal, 0.373f, 0.7755f),
    LampStone(R.drawable.ic_lamp_stone, R.string.lamp_stone, 0.373f, 0.7755f),
    PostMetal(R.drawable.ic_post_metal, R.string.post_metal, 0.5f, .7755f),
    PostSidewalk(R.drawable.ic_post_sidewalk, R.string.post_sidewalk, 0.5f, 0.5f),
    PostStone(R.drawable.ic_post_stone, R.string.post_stone, 0.5f, 0.5f),
    TrafficLight(R.drawable.ic_traffic_light, R.string.traffic_light, 0.5f, 0.899f),
    TrafficSign(R.drawable.ic_traffic_sign, R.string.traffic_sign, 0.5f, 0.9156f),
    Tree(R.drawable.ic_tree, R.string.tree, 0.5f, 0.83f),
    TreeConifer(R.drawable.ic_tree_conifer, R.string.tree_conifer, 0.5f, 0.885f),
    Hatchway(R.drawable.ic_hatchway, R.string.hatchway, 0.5f, 0.5f),
    Station(R.drawable.ic_station, R.string.station, 0.5f, 0.5f)
}
