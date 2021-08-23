package com.tibi.tiptopo.domain

import androidx.annotation.StringRes
import com.google.firebase.firestore.ServerTimestamp
import com.squareup.moshi.JsonClass
import com.tibi.tiptopo.R
import java.util.*

data class Measurement(
    override var id: String = "",
    var stationId: String = "",
    var name: String = "",
    var note: String = "",
    var isMeasured: Boolean = true,
    var number: Int = 0,
    var type: PointType = PointType.Point,
    var rawString: String = "",
    var va: Double = 0.0,
    var ha: Double = 0.0,
    var sd: Double = 0.0,
    var ht: Double = 0.0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    @ServerTimestamp
    var date: Date? = null
) : FirestoreMembers

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
    Station(R.drawable.ic_station, R.string.station, 0.5f, 0.5f),
    Grid(R.drawable.ic_grid, R.string.grid, 0.5f, 0.5f),
    Asphalt(R.drawable.ic_asphalt, R.string.asphalt, 0.5f, 0.5f),
    Tile(R.drawable.ic_tile, R.string.tile, 0.5f, 0.5f),
    HatchwayTsod(R.drawable.ic_hatchway_tsod, R.string.hatchway_tsod, 0.5f, 0.5f),
    HatchwayK(R.drawable.ic_hatchway_k, R.string.hatchway_k, 0.5f, 0.5f),
    HatchwayKover(R.drawable.ic_hatchway_kover, R.string.hatchway_kover, 0.5f, 0.5f),
    HatchwayD(R.drawable.ic_hatchway_d, R.string.hatchway_d, 0.5f, 0.5f),
    HatchwayV(R.drawable.ic_hatchway_v, R.string.hatchway_v, 0.5f, 0.5f),
    HatchwayTs(R.drawable.ic_hatchway_ts, R.string.hatchway_ts, 0.5f, 0.5f),
    HatchwayGk(R.drawable.ic_hatchway_gk, R.string.hatchway_gk, 0.5f, 0.5f),
    HatchwayGts(R.drawable.ic_hatchway_gts, R.string.hatchway_gts, 0.5f, 0.5f),
    HatchwayMg(R.drawable.ic_hatchway_mg, R.string.hatchway_mg, 0.5f, 0.5f),
    HatchwayVd(R.drawable.ic_hatchway_vd, R.string.hatchway_vd, 0.5f, 0.5f),
    HatchwayGv(R.drawable.ic_hatchway_gv, R.string.hatchway_gv, 0.5f, 0.5f),
    HatchwayMe(R.drawable.ic_hatchway_me, R.string.hatchway_me, 0.5f, 0.5f),
    HatchwayLawn(R.drawable.ic_lawn, R.string.lawn, 0.5f, 0.5f)
}
