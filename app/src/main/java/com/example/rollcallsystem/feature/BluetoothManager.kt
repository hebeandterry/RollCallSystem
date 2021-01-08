package com.example.rollcallsystem.feature

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.example.rollcallsystem.data.MemberFormat

object BluetoothManager {

    var memberList = ArrayList<MemberFormat>()

    private val bluetoothLeScanner: BluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

    private val bleScanner = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            updateMemberListByScanData(result)
            //Log.d("BluetoothManager","onScanResult: ${result?.device?.address} - ${result?.device?.name}")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d("BluetoothManager","onBatchScanResults:${results.toString()}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("BluetoothManager", "onScanFailed: $errorCode")
        }

    }

    private fun updateMemberListByScanData(result: ScanResult?) {
        val scannedMember = MemberFormat(result?.device?.address.toString())
        //If repeated, not add.
        val member = memberList.find { it.mac == scannedMember.mac}
        if (member != null)
            member.bArrived = true
    }


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