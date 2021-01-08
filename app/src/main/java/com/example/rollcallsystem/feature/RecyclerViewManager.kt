package com.example.rollcallsystem.feature

import com.example.rollcallsystem.adapter.RecyclerViewAdapter

object RecyclerViewManager {

    const val ALL_LIST = "all"
    const val NOT_ARRIVED = "not_arrived"
    const val ARRIVED = "arrived"

    var isScrolled = false
    val All_FRAGMENT = 99

    private var interList = arrayListOf<RecyclerViewAdapter>()

    fun register(input: RecyclerViewAdapter) {
        interList.add(input)
    }

    fun unregister() {
        interList.clear()
    }

    //Notify recycler view that are in the interList.
    fun notifyData(fragmentId: Int) {
        if (!isScrolled) {

            if (fragmentId == All_FRAGMENT) {
                for (list in interList) {
                    println("Now is $list, the size is ${interList.size} ")
                    list.update(BluetoothManager.memberList)
                }
            } else {
                println("Now is ${interList[fragmentId]}")
                interList[fragmentId].update((BluetoothManager.memberList))
            }

        }
    }
}