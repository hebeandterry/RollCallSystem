package com.example.rollcallsystem.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View

import com.example.rollcallsystem.R
import com.example.rollcallsystem.feature.AUTO_LOGIN
import com.example.rollcallsystem.feature.BluetoothManager
import com.example.rollcallsystem.feature.MEMBER_LIST
import com.example.rollcallsystem.feature.MyPreferences
import kotlinx.android.synthetic.main.activity_system.*

class SystemActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system)
        system_toolbar.title = getString(R.string.app_name)
        system_logoutTv.setOnClickListener(listener)
    }

    private val listener = View.OnClickListener { view ->
        when (view.id) {
            R.id.system_logoutTv -> {
                showAlertDialog("Warning",
                    "登出將會清空點名資料 \n\nThe roll call status will reset after logout.") {
                    MyPreferences.getInstance(this).setPreferences(AUTO_LOGIN, "0")
                    BluetoothManager.memberList.clear()
                    MyPreferences.getInstance(this).setList(MEMBER_LIST, BluetoothManager.memberList)
                    val intent = Intent(this, LoginActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
    }
}