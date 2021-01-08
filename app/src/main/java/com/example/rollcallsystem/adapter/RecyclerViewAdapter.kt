package com.example.rollcallsystem.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rollcallsystem.R
import com.example.rollcallsystem.data.MemberFormat
import com.example.rollcallsystem.feature.BluetoothManager
import com.example.rollcallsystem.feature.RecyclerViewManager
import java.lang.Math.min

class RecyclerViewAdapter(data: List<MemberFormat>, category: String) : RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder>() {
    private var clickListener: AdapterView.OnItemClickListener? = null
    private var data = data.toMutableList()
    private var category = category

    init {
        println("RecyclerViewAdapter init")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {

        println("RecyclerViewAdapter  onCreateViewHolder")

        return ListItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {

        println("RecyclerViewAdapter  onBindViewHolder $position ${data.size}")
        holder.tvNumber.text = (position + 1).toString()

        holder.setData(data[position])

    }

    fun update(memberList: ArrayList<MemberFormat>) {

        setDataByCategory(memberList)
        this.notifyDataSetChanged()
    }

    //According the category to set data.
    private fun setDataByCategory(memberList: ArrayList<MemberFormat>) {
        println("RecyclerViewAdapter setDataByCategory $category")
        data.clear()

        this.data = when(this.category) {
            RecyclerViewManager.ALL_LIST -> memberList.toMutableList()
            RecyclerViewManager.ARRIVED -> memberList.filter{ it.bArrived }.toMutableList()
            RecyclerViewManager.NOT_ARRIVED -> memberList.filter{ !it.bArrived }.toMutableList()
            else -> memberList
        }
    }

    //Set scroll listener to record scrolled state. If it's scrolled, don't refresh.
    fun scrollListener() = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dx == 0 && dy == 0) //Pass onScrolled() when create the recyclerview.
                return
            RecyclerViewManager.isScrolled = true
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            println("onScrollStateChanged $newState")
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {RecyclerViewManager.isScrolled = false}
            }
        }

    }

    class ListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvMac: TextView = itemView.findViewById(R.id.tv_mac)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvArrived: TextView = itemView.findViewById(R.id.tv_arrive)
        val tvNumber: TextView = itemView.findViewById(R.id.tv_number)
        val list: ConstraintLayout = itemView.findViewById(R.id.list)

        fun setData(data: MemberFormat)
        {
            tvMac.text = data.mac
            tvName.text = data.name

            if (data.bArrived) {
                tvArrived.text = "抵達"
                tvArrived.setBackgroundColor(Color.GREEN)
            } else
            {
                tvArrived.text = "尚未抵達"
                tvArrived.setBackgroundColor(Color.WHITE)
            }
        }

    }
}