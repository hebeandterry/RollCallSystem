package com.example.rollcallsystem.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.rollcallsystem.R
import com.example.rollcallsystem.network.UpdateToServer
import com.example.rollcallsystem.adapter.PageAdapter
import com.example.rollcallsystem.feature.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_roll_call.*


class RollCallActivity : AppCompatActivity(), View.OnClickListener, ScanDeviceDataListener {

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
        BluetoothManager.registerListener(this)

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
        // Record the roll calling status every period, to make sure the state can be keep if APP restart.
        MyPreferences.getInstance(this).setList(MEMBER_LIST, BluetoothManager.memberList)
        //Only update the current fragment for every SCAN_PERIOD.
        RecyclerViewManager.notifyData(viewPager2.currentItem)

        //var currentFragment = supportFragmentManager.findFragmentByTag("f" + viewPager2.currentItem)!!

        Thread.sleep(300)  //stop 0.3 second for each scan period.
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
        toolbar.apply {
            this.title =
                intent.getStringExtra("ACCOUNT") + ", " + intent.getStringExtra("LOCATION")
        }

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
                    setRollCallStateText(getString(R.string.roll_call_stop), R.color.black)
                } else {
                    startRollCalling()
                    RollCall_scanButton.text = getString(R.string.stop_scan)
                    setRollCallStateText(getString(R.string.roll_calling), R.color.blue_2)
                }
            }
        }
    }

    //Called when scan successfully form BluetoothManager.kt
    override fun onScanDeviceDataResponse() {
        setRollCallStateText(getString(R.string.roll_calling), R.color.blue_2)
    }
    //Called when can error form BluetoothManager.kt
    override fun onScanDeviceDataErrorResponse(errorCode: Int) {
        setRollCallStateText(getString(R.string.roll_call_error), R.color.black)
        Toast.makeText(this, "藍牙掃描錯誤 $errorCode", Toast.LENGTH_LONG).show()
    }


    private fun setRollCallStateText(state: String, color: Int) {
        RollCall_state.text = state
        RollCall_state.setTextColor(ContextCompat.getColor(this, color))
    }
}

