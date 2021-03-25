package com.example.rollcallsystem.ui

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.example.rollcallsystem.R
import com.example.rollcallsystem.data.Position
import com.example.rollcallsystem.data.RollCallPosition
import com.example.rollcallsystem.feature.ACCOUNT
import com.example.rollcallsystem.feature.BluetoothManager
import com.example.rollcallsystem.network.HttpRequestListener
import com.example.rollcallsystem.network.HttpUrlConnection
import kotlinx.android.synthetic.main.activity_choose_location.*


class ChooseLocationActivity : AppCompatActivity(), View.OnClickListener, HttpRequestListener {

    var rollCallLocation = ArrayList<Position>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_location)

        val httpUrlConnection = HttpUrlConnection()
        httpUrlConnection.registerListener(this)

        Thread {val rollCallPosition
                = httpUrlConnection.httpRequest(QUERY_POSITION_BODY,
                HttpRequestType.GET_ROLL_CALL_POSITION, intent.getStringExtra("TOKEN")!!
            ) as RollCallPosition?

        for (i in rollCallPosition?.rollCallPosition?.allAreas?.get(0)?.positions!!)
        {
            rollCallLocation.add(Position(i.id, i.name))
        }

            createLocationButtons()
        }.start()

    }

    fun createLocationButtons() {

        val height = Resources.getSystem().displayMetrics.heightPixels

        val spacing : Int = (height - tv_choose_location.bottom)/(rollCallLocation.size + 1)

        runOnUiThread {
            for (i in 1 .. rollCallLocation.size) {
                // creating the button
                val dynamicButton = Button(this)
                // setting layout_width and layout_height using layout parameters
                dynamicButton.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.topToBottom =  tv_choose_location.id
                    this.leftToLeft = cl_choose_location.id
                    this.rightToRight = cl_choose_location.id
                    this. topMargin = spacing * i
                }

                dynamicButton.apply {
                    this.setOnClickListener(this@ChooseLocationActivity )
                    this.text = rollCallLocation[i-1].name
                    this.id = i - 1
                    this.typeface = ResourcesCompat.getFont( this@ChooseLocationActivity, R.font.wcl)
                }
                // add Button to Layout
                cl_choose_location.addView(dynamicButton)
            }
        }

    }

    override fun onClick(v: View) {

        setPositionToMemberList(v.id)

        startActivity(
            Intent(this,
            RollCallActivity::class.java).apply
        {
            putExtra(ACCOUNT, intent.getStringExtra("ACCOUNT"))
            putExtra("LOCATION", rollCallLocation[v.id].name)
        })
    }

    private fun setPositionToMemberList(id: Int) {
        for (member in BluetoothManager.memberList)
            member.position_id = rollCallLocation[id].id//id
    }

    override fun onHttpRequestResponse() {
        TODO("Not yet implemented")
    }

    override fun onHttpRequestErrorResponse(errorBody: String?) {
        TODO("Not yet implemented")
    }


}