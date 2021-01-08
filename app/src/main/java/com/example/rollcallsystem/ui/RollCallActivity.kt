package com.example.rollcallsystem.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.rollcallsystem.feature.BluetoothManager
import com.example.rollcallsystem.feature.MyPreferences
import com.example.rollcallsystem.R
import com.example.rollcallsystem.netework.UpdateToServer
import com.example.rollcallsystem.adapter.PageAdapter
import com.example.rollcallsystem.feature.RecyclerViewManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_roll_call.*
import kotlin.concurrent.thread


class RollCallActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager2: ViewPager2
    lateinit var updateToServer: UpdateToServer

    private var mScanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roll_call)
        RollCall_scanButton.setOnClickListener(this)

        toolBarSetting()

        tabLayout = findViewById(R.id.tabLayout)
        viewPager2 = findViewById(R.id.viewPager2)

        viewPager2.adapter = PageAdapter(supportFragmentManager, lifecycle)

        viewPager2.registerOnPageChangeCallback((viewPager2.adapter as PageAdapter).pageChangeListener())


        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> "應到人數(${BluetoothManager.memberList.size})"
                1 -> "抵達人數(${BluetoothManager.memberList.filter { it.bArrived }.size})"
                2 -> "未到人數(${BluetoothManager.memberList.filter { !it.bArrived }.size})"
                else -> null
            }
        }.attach()

        updateToServer = UpdateToServer()

        startRollCalling()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRollCalling()
        updateToServer.stopUpdateToServer()
        RecyclerViewManager.unregister()
    }

    private val regularTask = Runnable {

        stopRollCalling()

        modifyTabName()
        MyPreferences.getInstance(this).setList("MEMBER_STATE", BluetoothManager.memberList)

        RecyclerViewManager.notifyData(viewPager2.currentItem)

        println("regularTask ${viewPager2.currentItem}")

        //var currentFragment = supportFragmentManager.findFragmentByTag("f" + viewPager2.currentItem)!!

        //BluetoothManager.memberList.clear()

        Thread.sleep(300)  //stop 1 second for each scan period.
        startRollCalling()
    }

    private fun startRollCalling () {
        if (!mScanning) { // Stops scanning after a pre-defined scan period.
            mScanning = true
            BluetoothManager.onStart()
            handler.postDelayed( regularTask , SCAN_PERIOD)
            println("startRollCalling")
        }
    }

    private fun stopRollCalling() {
        mScanning = false
        BluetoothManager.onStop()
        handler.removeCallbacks(regularTask)
        println("stopRollCalling")
    }

    private fun modifyTabName() {
        for (position in 0 .. tabLayout.tabCount) {

            tabLayout.getTabAt(position)?.text = when (position) {
                0 -> "應到人數(${BluetoothManager.memberList.size})"
                1 -> "抵達人數(${BluetoothManager.memberList.filter { it.bArrived }.size})"
                2 -> "未到人數(${BluetoothManager.memberList.filter { !it.bArrived }.size})"
                else -> null
            }
        }
    }

    private fun toolBarSetting () {

        toolbar.inflateMenu(R.menu.menu_setting)
        toolbar.title = intent.getStringExtra("ACCOUNT")
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_setting -> startActivity(Intent(this, SystemActivity::class.java))
            }
            false
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            RollCall_scanButton.id -> {
                if (mScanning) {
                    stopRollCalling()
                    RollCall_scanButton.text = getString(R.string.start_scan)
                } else {
                    startRollCalling()
                    RollCall_scanButton.text = getString(R.string.stop_scan)
                }
            }
        }
    }
}

