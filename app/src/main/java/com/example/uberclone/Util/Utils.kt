package com.example.uberclone.Util

import android.content.Context
import android.util.Log

fun logger(message: String) {
    Log.d("mytag", message)
}

fun Context.showToast(msg: String?) {
    Log.d("mytag", msg.toString())
}