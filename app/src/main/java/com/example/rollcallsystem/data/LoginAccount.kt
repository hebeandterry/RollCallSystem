package com.example.rollcallsystem.data

import com.google.gson.annotations.SerializedName

data class LoginAccount(
    var username: String = "",
    var password: String = ""){
}

data class AccountToken (@SerializedName("token") var token: String = "")