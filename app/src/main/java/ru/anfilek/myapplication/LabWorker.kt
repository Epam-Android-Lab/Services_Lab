package ru.anfilek.myapplication

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit


class LabWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d(TAG, "doWork: start: ${Thread.currentThread().name}")
        try {
            TimeUnit.SECONDS.sleep(5)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        Log.d(TAG, "doWork: end")
        val output = Data.Builder().putString("KEY", "String_String_String").build()
        return Result.success(output)
    }

    companion object {
        const val TAG = "LabWorkerTAG"
    }
}