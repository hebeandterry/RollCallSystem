package com.example.rollcallsystem.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.rollcallsystem.feature.BluetoothManager
import com.example.rollcallsystem.feature.MyPreferences
import com.example.rollcallsystem.R
import kotlinx.android.synthetic.main.activity_system.*

class SystemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system)
        system_toolbar.title = getString(R.string.app_name)
        system_logoutTv.setOnClickListener(listener)
    }

    private val listener = View.OnClickListener { view ->
        when (view.id) {
            R.id.system_logoutTv -> {
                showDialog()
            }
        }
    }

    fun showDialog() {
        AlertDialog.Builder(this)
            .setTitle("Warning")
            .setMessage("The roll call state will reset after logout.")
            .setPositiveButton(R.string.ok) { _, _ -> MyPreferences.getInstance(this).setPreferences("AUTO_LOGIN", "0")
                BluetoothManager.memberList.clear()
                MyPreferences.getInstance(this).setList("MEMBER_STATE", BluetoothManager.memberList)
                val intent = Intent(this, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)}
            .setNegativeButton("Cancel", null)
            .show()
    }

}