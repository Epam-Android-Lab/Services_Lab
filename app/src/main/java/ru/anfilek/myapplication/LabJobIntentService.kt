package ru.anfilek.myapplication

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService


class LabJobIntentService : JobIntentService() {

    companion object {
        private val TAG = LabJobIntentService::class.java.simpleName
        private const val JOB_ID = 1000

        fun enqueueWork(
            context: Context,
            work: Intent = Intent(context, LabJobIntentService::class.java)
        ) {
            enqueueWork(context, LabJobIntentService::class.java, JOB_ID, work)
        }
    }


    override fun onHandleWork(intent: Intent) {
        // todo do work here
        Log.d(TAG, "on handle work in thread:" + Thread.currentThread().name)
        Thread.sleep(2000)
        Log.d(TAG, "on handle work end in thread:" + Thread.currentThread().name)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "All work complete")
    }
}