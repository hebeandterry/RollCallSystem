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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.rollcallsystem.BuildConfig
import com.example.rollcallsystem.R
import com.example.rollcallsystem.data.*
import com.example.rollcallsystem.feature.*
import com.example.rollcallsystem.netework.ApiService
import com.example.rollcallsystem.netework.ApiUtils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

private const val REQUEST_ENABLE_BT = 0
private const val REQUEST_ENABLE_SYSTEM_LOCATION = 1
private const val PERMISSION_REQUEST_FINE_LOCATION = 2

const val QUERY_BEACON_BODY = "{\"query\":\"{ allBeaconEntities(active: true) {results(limit: 5000) { mac name }}}\"}"
const val QUERY_POSITION_BODY = "{\"query\":\"{ allAreas(limit: 5000, enableForRollCall: true) {name positions{id name} } }\"}"

class LoginActivity : AppCompatActivity() {

    private lateinit var view : View
    private lateinit var dialog: AlertDialog

    var mApiService: ApiService? = null
    private val yes = "1"
    private val no = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        checkPermission()
        mApiService = ApiUtils.apiService

        view = LayoutInflater.from(this).inflate(R.layout.dialog_item, null)
        Login_versionTextView.append(BuildConfig.VERSION_NAME)

        login_accountEt.setText( MyPreferences.getInstance(this).getPreferences(ACCOUNT, ""))
        login_passwordEt.setText( MyPreferences.getInstance(this).getPreferences(PASSWORD, ""))

        BluetoothManager.memberList = MyPreferences.getInstance(this).getList("MEMBER_STATE")

        autoLogin()
    }

    // Check BT, GPS, location permission.
    private fun checkPermission () {
        if (!isBluetoothEnable())
            enableBluetooth()
        else if (!isSystemLocationEnable())
            startSystemLocationPage()
        else
            checkRequestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                PERMISSION_REQUEST_FINE_LOCATION
            )
    }

    private fun checkRequestPermission(requestPermission: String, requestCode: Int) : Boolean{
        return if (checkSelfPermission(requestPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(requestPermission), requestCode)
            false
        }else false
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
        AlertDialog.Builder(this)
            .setMessage("System Location is not Available. Please Enable It.")
            .setTitle("Warning")
            .setPositiveButton("OK") { _, _ -> startActivityForResult(intent,
                REQUEST_ENABLE_SYSTEM_LOCATION
            )}
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show()
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
        if (MyPreferences.getInstance(this).getPreferences(AUTO_LOGIN, no) == yes)
        {
            startLogin(activityLogin_loginButton)
        }
    }

    //Called when loginButton is clicked.
    fun startLogin(view: View) {
        val loginAccount = LoginAccount(login_accountEt.text.toString(), login_passwordEt.text.toString())

        if (isOkToLogin(loginAccount)) {
            recordAccount(loginAccount)

            dialog = showDialog()
            Thread {
                val rtn = loginToServer(loginAccount)
                closeDialog()

                if (rtn) {
                    finish()
                    startActivity(Intent(this,
                        RollCallActivity::class.java).apply
                    {
                        putExtra(ACCOUNT, loginAccount.username)
                    })
                }

            }.start()
            println("Start login")
        }

    }

    //Check types data and the internet.
    private fun isOkToLogin(loginAccount: LoginAccount) :Boolean {
        return if ( !isAccountAvailable(loginAccount)) {
            Toast.makeText(this, "Account or Password can't be empty!", Toast.LENGTH_SHORT).show()
            false
        } else if ( !isInternetAvailable(this)) {
            Toast.makeText(this, "Internet is not available, please check it!", Toast.LENGTH_SHORT).show()
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

        val accountToken: AccountToken? = httpRequest(Gson().toJson(loginAccount), 1) as AccountToken?

        if (accountToken != null)
        {
            if (BluetoothManager.memberList.isEmpty()) {

                val rollCallPosition = httpRequest(QUERY_POSITION_BODY, 2, accountToken?.token) as Test?

                val data: Beacon? = httpRequest(QUERY_BEACON_BODY, 3, accountToken?.token) as Beacon?

                if (data != null && rollCallPosition != null) {
                    addServerDataToList(data, rollCallPosition.rollCallPosition.allAreas[0].positions[0].id)
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

    private fun httpRequest (data: String, type: Int, vararg parameters: String): Any? {

        try {
            val body: RequestBody =
                data.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val response = when (type) {
                1 -> {
                    mApiService!!.loginPost(body).execute()
                }
                2-> {
                    mApiService!!.getRollCallPositionPost(parameters[0], body).execute()
                }
                else -> {
                    mApiService!!.getBeaconsPost(parameters[0], body).execute()
                }
            }

            return if (response.isSuccessful) {
                response.body()
            } else {

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "response: ${response.errorBody()?.string() ?: "The error response is NULL"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                null
            }
        }catch (e: IOException)
        {
            println("Login error: $e")
            return null
        }
    }

    //Set the result to list, and save it local.
    private fun addServerDataToList (data: Beacon, position: String) {
        for (member in data.data.allBeacon.result)
        {
            val beacon = MemberFormat(member.mac, member.name, position)
            BluetoothManager.memberList.add(beacon)
        }

        MyPreferences.getInstance(this).setList("MEMBER_STATE", BluetoothManager.memberList)

        println(
            "${BluetoothManager.memberList.size} response: " + data.data.allBeacon.result
        )
    }

    private fun showDialog(): AlertDialog =
         AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .show()


    fun closeDialog() = dialog.dismiss()
}