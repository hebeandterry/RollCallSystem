package com.example.rollcallsystem.data

import com.google.gson.annotations.SerializedName


data class Beacon (@SerializedName("data") var data: AllBeacon)

data class AllBeacon (@SerializedName("allBeaconEntities") var allBeacon: Result)

data class Result (@SerializedName("results") var result: List<MemberFormat>)

data class MemberFormat (
    @SerializedName("mac")
    var mac: String = "FF:FF:FF:FF:FF:FF",
    @SerializedName("name")
    var name: String = "Name",
    var position_id: String = "-1",
    var bArrived: Boolean = false
) {
}
