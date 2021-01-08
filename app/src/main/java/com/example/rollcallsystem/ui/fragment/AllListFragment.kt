package com.example.rollcallsystem.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rollcallsystem.feature.BluetoothManager
import com.example.rollcallsystem.R
import com.example.rollcallsystem.adapter.RecyclerViewAdapter
import com.example.rollcallsystem.feature.RecyclerViewManager

class AllListFragment() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_all_list, container, false)

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = RecyclerViewAdapter(BluetoothManager.memberList, RecyclerViewManager.ALL_LIST)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            .apply { setDrawable(context!!.getDrawable(R.drawable.divider)!!)}
        recyclerView.addItemDecoration(dividerItemDecoration)

        //Register the adapter for updating list.
        RecyclerViewManager.register(adapter)

        println("All list fragment show!")

        recyclerView.addOnScrollListener(adapter.scrollListener())
    }

}