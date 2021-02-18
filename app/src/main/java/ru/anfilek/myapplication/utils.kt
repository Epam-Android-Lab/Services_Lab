package ru.anfilek.myapplication

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.util.Log


fun getProcessName(context: Context): String? {
    val pid = Process.myPid()
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (processInfo in activityManager.runningAppProcesses) {
        if (processInfo.pid == pid) {
            return processInfo.processName
        }
    }
    return null
}

fun Context.sayHelloFromCurrentProcessAndThread(tag : String) {
    Log.d(tag, "say hello from process: ${getProcessName(this)} and thread: ${Thread.currentThread().name}")
}