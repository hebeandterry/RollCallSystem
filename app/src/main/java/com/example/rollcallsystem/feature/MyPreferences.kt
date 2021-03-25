package com.example.rollcallsystem.feature

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.example.rollcallsystem.SingletonHolder
import com.google.gson.Gson
import com.example.rollcallsystem.data.MemberFormat

const val ROLL_CALL = "ROLL_CALL"
const val ACCOUNT = "ACCOUNT"
const val PASSWORD = "PASSWORD"
const val AUTO_LOGIN = "AUTO_LOGIN"
const val MEMBER_LIST = "MEMBER_LIST"

class MyPreferences private constructor(private val context: Context) {

    private lateinit var settings: SharedPreferences
    //private var settings: SharedPreferences = context.getSharedPreferences("ACCOUNT", MODE_PRIVATE)

    fun setPreferences(key: String, value: String) {
        settings = context.getSharedPreferences(ROLL_CALL, MODE_PRIVATE)
        settings.edit().putString(key, value).apply()
    }

    fun getPreferences(key: String, value: String?): String? {
        settings = context.getSharedPreferences(ROLL_CALL, MODE_PRIVATE)
        return settings.getString(key, value)
    }

    fun <T> setList(key: String, list: ArrayList<T>){
        val json = Gson().toJson(list)
        setPreferences(key, json)
    }

    // Get MemberFormat format list.
    fun getList(key: String): ArrayList<MemberFormat> {
        var arrayList = ArrayList<MemberFormat>()
        val data = getPreferences(key, null)

        data?.let {
            arrayList = Gson().fromJson(it, Array<MemberFormat>::class.java).toMutableList() as ArrayList<MemberFormat>
        }

        return arrayList
    }

    @SuppressLint("CommitPrefEdits")
    fun cleanPreferences() {
        settings = context.getSharedPreferences(ACCOUNT, MODE_PRIVATE)
        settings.edit().clear().apply()
    }

    companion object : SingletonHolder<MyPreferences, Context>(::MyPreferences)
}