package com.example.rollcallsystem.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.rollcallsystem.feature.RecyclerViewManager
import com.example.rollcallsystem.ui.fragment.AllListFragment
import com.example.rollcallsystem.ui.fragment.ArrivedListFragment
import com.example.rollcallsystem.ui.fragment.NotArrivedListFragment

// For ViewPager2
class PageAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    //Fragment list
    var fragments: ArrayList<Fragment> = arrayListOf(
        AllListFragment(),
        ArrivedListFragment(),
        NotArrivedListFragment()
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        println("create fragment $position")
        return fragments[position]
    }

    //Refresh recycler view when change fragment.
    fun pageChangeListener() = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            when (state) {
                ViewPager2.SCROLL_STATE_IDLE -> RecyclerViewManager.notifyData(RecyclerViewManager.All_FRAGMENT)
            }
            println("pageChangeListener onPageScrollStateChanged $state")
        }
    }
}