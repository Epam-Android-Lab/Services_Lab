package ru.anfilek.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class LabService : Service() {

    companion object {
        private const val TAG = "LabServiceTAG"
    }

    inner class LabBinder : Binder() {
        fun doSomething() {
            Log.d(TAG, "doSomething")
        }

        fun getService(): LabService = this@LabService
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return LabBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return false
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(TAG, "onRebind")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "on start command")
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy")
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")
    }
}
