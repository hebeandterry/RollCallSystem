package com.example.rollcallsystem.data

import com.google.gson.annotations.SerializedName

data class MemberFormat (
    @SerializedName("mac")
    var mac: String = "FF:FF:FF:FF:FF:FF",
    @SerializedName("name")
    var name: String = "Name",
    var position_id: String = "-1",
    var bArrived: Boolean = false,
    @SerializedName("success")
    var success: String = "false"
) {
}

data class Beacon (
    @SerializedName("data") var data: AllBeacon)

data class AllBeacon (@SerializedName("allBeaconEntities") var allBeacon: Result)

data class Result (@SerializedName("results") var result: List<MemberFormat>)

data class Test (@SerializedName("data") var rollCallPosition: AllAreas)

data class AllAreas (@SerializedName("allAreas") var allAreas: List<Positions>)

data class Positions (@SerializedName("positions") var positions: List<Position>)

data class Position (
    @SerializedName("id")
    var id: String = "",
    @SerializedName("name")
    var name: String = ""
)

data class Status (
    var beacons: List<MemberFormat> = listOf(),
    var locate_time: String = "")