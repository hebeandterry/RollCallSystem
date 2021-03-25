package com.example.rollcallsystem.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.rollcallsystem.BuildConfig
import com.example.rollcallsystem.R
import com.example.rollcallsystem.data.*
import com.example.rollcallsystem.feature.*
import com.example.rollcallsystem.network.HttpUrlConnection
import com.example.rollcallsystem.network.HttpRequestListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*

private const val REQUEST_ENABLE_BT = 0
private const val REQUEST_ENABLE_SYSTEM_LOCATION = 1
private const val PERMISSION_REQUEST_FINE_LOCATION = 2

const val QUERY_BEACON_BODY = "{\"query\":\"{ allBeaconEntities(active: true) {results(limit: 5000) { mac name }}}\"}"
const val QUERY_POSITION_BODY = "{\"query\":\"{ allAreas(limit: 5000, enableForRollCall: true) {name positions{id name} } }\"}"

enum class HttpRequestType {
    LOGIN, GET_ROLL_CALL_POSITION, GET_MEMBER_LIST
}

class LoginActivity : BaseActivity(), HttpRequestListener {

    private lateinit var dialogView : View
    private lateinit var dialog: AlertDialog
    private lateinit var httpUrlConnection: HttpUrlConnection

    var accountToken: AccountToken? = null
    var isPermissionOK = false
    private val yes = "1"
    private val no = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        checkPermission()
        httpUrlConnection = HttpUrlConnection()
        httpUrlConnection.registerListener(this)

        dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_item, null)
        Login_versionTextView.append(BuildConfig.VERSION_NAME)

        login_accountEt.setText( MyPreferences.getInstance(this).getPreferences(ACCOUNT, ""))
        login_passwordEt.setText( MyPreferences.getInstance(this).getPreferences(PASSWORD, ""))

        BluetoothManager.memberList = MyPreferences.getInstance(this).getList(MEMBER_LIST)
        autoLogin()
    }

    override fun onResume() {
        super.onResume()
    }

    // Check BT, GPS, location permission.
    private fun checkPermission () {

        if (!isBluetoothEnable())
            enableBluetooth()
        else if (!isSystemLocationEnable())
            startSystemLocationPage()
        else if (!checkRequestPermission( Manifest.permission.ACCESS_FINE_LOCATION))
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_FINE_LOCATION)
        else
            isPermissionOK = true

    }

    private fun checkRequestPermission(requestPermission: String) : Boolean{
        return checkSelfPermission(requestPermission) == PackageManager.PERMISSION_GRANTED
    }

    //return false if adapter is NULL.
    private fun isBluetoothEnable() = BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false

    private fun enableBluetooth () {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent,
            REQUEST_ENABLE_BT
        )
    }

    private fun isSystemLocationEnable() = (getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                                           .isProviderEnabled(LocationManager.GPS_PROVIDER)

    //Show a dialog and turn to start system location page.
    private fun startSystemLocationPage () {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

        showAlertDialog("Warning",
            "請打開手機定位功能 \n\nSystem Location is not Available. Please Enable It.") {
            startActivityForResult(intent, REQUEST_ENABLE_SYSTEM_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(this, "Location Permission Granted")
                } else {
                    checkPermission()
                }
                return
            }
        }
    }

    // The response of the bluetooth dialog, show the dialog until BT enable.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("Request code: $requestCode")

        when (requestCode) {
            REQUEST_ENABLE_BT -> {checkPermission()}
            REQUEST_ENABLE_SYSTEM_LOCATION -> {checkPermission()}
        }
    }

    // It will login automatically after the first time login until logging out.
    private fun autoLogin() {
        if (MyPreferences.getInstance(this).getPreferences(AUTO_LOGIN, no) == yes &&
                isPermissionOK)
        {
            startLogin(activityLogin_loginButton)
        }

        println("${MyPreferences.getInstance(this).getPreferences(AUTO_LOGIN, no)} $isPermissionOK")
    }

    //Called when loginButton is clicked.
    fun startLogin(view: View) {
        val loginAccount = LoginAccount(login_accountEt.text.toString(), login_passwordEt.text.toString())

        if (isOkToLogin(loginAccount)) {
            //Can't set view repeatedly, need to remove.
            if(this.dialogView.parent !=null) {
                (this.dialogView.parent as ViewGroup).removeView(dialogView)
            }

            dialog = showProgressDialog(dialogView)
            Thread {
                val rtn = loginToServer(loginAccount)
                closeDialog(dialog)

                if (rtn) {
                    recordAccount(loginAccount)
                    finish()
                    startActivity(Intent(this,
                        ChooseLocationActivity::class.java).apply
                    {
                        putExtra(ACCOUNT, loginAccount.username)
                        putExtra("TOKEN", accountToken?.token)

                    })
                }

            }.start()
            println("Start login")
        }

    }

    //Check types data and the internet.
    private fun isOkToLogin(loginAccount: LoginAccount) :Boolean {
        return if ( !isAccountAvailable(loginAccount)) {
            showToast(this, "Account or Password can't be empty!")
            false
        } else if ( !isInternetAvailable(this)) {
            showToast(this, "Internet is not available, please check it!")
            false
        } else
            true
    }

    //Account and password can't be empty.
    private fun isAccountAvailable(loginAccount: LoginAccount) :Boolean {
        return loginAccount.username != "" && loginAccount.password != ""
    }

    private fun isInternetAvailable(context: Context) =
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false
        }

    private fun recordAccount(loginAccount: LoginAccount) {
        MyPreferences.getInstance(this).setPreferences(ACCOUNT, loginAccount.username)
        MyPreferences.getInstance(this).setPreferences(PASSWORD, loginAccount.password)
        MyPreferences.getInstance(this).setPreferences(AUTO_LOGIN, yes)
    }

    //Get token first and then get member list.
    private fun loginToServer(loginAccount: LoginAccount): Boolean{

        accountToken = httpUrlConnection
            .httpRequest(Gson().toJson(loginAccount), HttpRequestType.LOGIN) as AccountToken?

        //If get account successful, get member list.
        if (accountToken != null)
        {
            if (BluetoothManager.memberList.isEmpty()) {

                val data: Beacon?
                        = httpUrlConnection.httpRequest(QUERY_BEACON_BODY,
                    HttpRequestType.GET_MEMBER_LIST, accountToken!!.token) as Beacon?

                if (data != null) {
                    addServerDataToList(data)//, rollCallPosition.rollCallPosition.allAreas[0].positions[0].id)
                    return true
                }
            }
            return true
        }
        return false

        /*
        mApiService!!.loginPost(loginAccount.account, loginAccount.password).enqueue(object : Callback<MemberFormat> {
            //mApiService!!.updatePost(memberList).enqueue(object : Callback<MemberFormat>{

            override fun onResponse(call: Call<MemberFormat>, response: Response<MemberFormat>) {

                println("post submitted to API." + response.body()!!)

                if (response.isSuccessful) {
                    println( "post login to API" + response.body()!!.toString())

                }
            }

            override fun onFailure(call: Call<MemberFormat>, t: Throwable) {
                println( "post login to API failed :$t")
            }
        })

         */
    }

    //Set the result to list, and save it local.
    private fun addServerDataToList (data: Beacon) {
        for (member in data.data.allBeacon.result)
        {
            val beacon = MemberFormat(member.mac, member.name)
            BluetoothManager.memberList.add(beacon)
        }

        MyPreferences.getInstance(this).setList(MEMBER_LIST, BluetoothManager.memberList)

        println(
            "${BluetoothManager.memberList.size} response: " + data.data.allBeacon.result
        )
    }

    override fun onHttpRequestResponse() {
        TODO("Not yet implemented")
    }

    override fun onHttpRequestErrorResponse(errorBody: String?) {
        showToast(this,
            when {
                errorBody!!.contains("non_field") -> "Account or Password isn't correct."
                else -> errorBody
            })
    }

}