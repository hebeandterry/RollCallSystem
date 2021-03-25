package com.example.rollcallsystem.ui

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.rollcallsystem.R

open class BaseActivity : AppCompatActivity(){

    fun showToast(context: Context, message: String) {
        runOnUiThread {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).apply {
            val text = (this.view?.findViewById<View>(android.R.id.message) as TextView)
            text.setTextColor(getColor(R.color.blue_2))
            text.typeface = ResourcesCompat.getFont( context, R.font.carter_one)}
            .show()
        }
    }

    fun showProgressDialog(dialogView : View): AlertDialog =
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .show()


    fun closeDialog(dialog: AlertDialog) =
        dialog.dismiss()

    fun showAlertDialog(title: String, message: String, func:() -> Unit) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ -> func()}
            .setNegativeButton("Cancel", null)
            .show()

        setAlertDialogFont(dialog)
    }

    fun setAlertDialogFont(dialog : AlertDialog){
        val textView =
            dialog.findViewById<View>(android.R.id.message) as TextView?
        textView!!.typeface = ResourcesCompat.getFont(this, R.font.wcl)
        //OK button
        val button1 = dialog.findViewById<View>(android.R.id.button1) as Button
        button1.typeface = ResourcesCompat.getFont(this, R.font.carter_one)
        //Cancel button
        val button2 = dialog.findViewById<View>(android.R.id.button2) as Button
        button2.typeface = ResourcesCompat.getFont(this, R.font.carter_one)
    }

}