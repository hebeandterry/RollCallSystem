package com.example.rollcallsystem.feature

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.example.rollcallsystem.data.MemberFormat

object BluetoothManager {

    var memberList = ArrayList<MemberFormat>()
    private var listener: ScanDeviceDataListener? = null

    private val bluetoothLeScanner: BluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

    private val bleScanner = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            updateMemberListByScanData(result)
            listener?.onScanDeviceDataResponse()
            //Log.d("BluetoothManager","onScanResult: ${result?.device?.address} - ${result?.device?.name}")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d("BluetoothManager","onBatchScanResults:${results.toString()}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("BluetoothManager", "onScanFailed: $errorCode")
            listener?.onScanDeviceDataErrorResponse(errorCode)
            //BluetoothAdapter.getDefaultAdapter().disable();
        }

    }

    private fun updateMemberListByScanData(result: ScanResult?) {
        val scannedMember = MemberFormat(result?.device?.address.toString())
        //If repeated, not add.
        val member = memberList.find { it.mac == scannedMember.mac}
        if (member != null)
            member.bArrived = true
    }

    //Register a listener form RollCallActivity to get scan result.
    fun registerListener(listener: ScanDeviceDataListener) {
        this.listener = listener
    }

    //Only for legacy scan, if you want to scan extended package,
    //need to add scan filter and setting.
    fun onStart() {
        Log.d("BluetoothManager","onStart()")
        bluetoothLeScanner.startScan(
            bleScanner
        )

    }

    fun onStop() {
        Log.d("BluetoothManager","onStop()")
        bluetoothLeScanner.stopScan(
            bleScanner
        )
    }
}

interface ScanDeviceDataListener {
    fun onScanDeviceDataResponse()

    fun onScanDeviceDataErrorResponse(errorCode: Int)
}