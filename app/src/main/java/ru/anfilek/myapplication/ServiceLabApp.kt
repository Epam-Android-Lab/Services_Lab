package ru.anfilek.myapplication

import android.app.Application
import android.util.Log

class ServiceLabApp : Application() {

    init {
        Log.d(TAG, "init")
    }

    companion object {
        private const val TAG = "ServiceLabAppTAG"
    }
    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")
    }
}