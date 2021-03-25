package com.example.rollcallsystem.data

import com.google.gson.annotations.SerializedName

//The data class to get the position ID from the server.
data class RollCallPosition (@SerializedName("data") var rollCallPosition: AllAreas)

data class AllAreas (@SerializedName("allAreas") var allAreas: List<Positions>)

data class Positions (@SerializedName("positions") var positions: List<Position>)

data class Position (
    @SerializedName("id")
    var id: String = "",
    @SerializedName("name")
    var name: String = ""
)