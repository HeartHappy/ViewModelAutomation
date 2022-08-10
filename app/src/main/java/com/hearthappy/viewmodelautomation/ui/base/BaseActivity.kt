package com.hearthappy.viewmodelautomation.ui.base

import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hearthappy.ktorexpand.code.network.RequestState
import com.hearthappy.ktorexpand.code.network.Result
import com.hearthappy.ktorexpand.code.network.asFailedMessage
import com.hearthappy.ktorexpand.code.network.asThrowableMessage

open class BaseActivity: AppCompatActivity() {

    protected fun ProgressBar.show(max:Int=0) {
        if(max!=0) this.max=max
        this.visibility = View.VISIBLE
    }

    protected fun ProgressBar.hide() {
        this.max=0
        this.visibility = View.GONE
    }

    protected fun TextView.showSucceedMsg(result: String, progressBar: ProgressBar? = null) {
        progressBar?.hide()
        this.text = result
    }

    protected fun TextView.showFailedMsg(it: RequestState.FAILED, progressBar: ProgressBar? = null) {
        progressBar?.hide()
        this.text = it.asFailedMessage()
    }

    protected fun TextView.showThrowableMsg(it: RequestState.Throwable, progressBar: ProgressBar? = null) {
        progressBar?.hide()
        this.text = it.asThrowableMessage()
    }

    protected fun TextView.showFailedMsg(it: Result.Failed, progressBar: ProgressBar? = null) {
        progressBar?.hide()
        this.text = it.asFailedMessage()
    }

    protected fun TextView.showThrowableMsg(it: Result.Throwable, progressBar: ProgressBar? = null) {
        progressBar?.hide()
        this.text = it.asThrowableMessage()
    }

    protected fun startActivity(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }

    protected fun toast(text:String){
        Toast.makeText(this,text , Toast.LENGTH_SHORT).show()
    }
}