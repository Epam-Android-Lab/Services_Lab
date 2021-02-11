package ru.anfilek.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import ru.anfilek.myapplication.databinding.ActivityMainBinding
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private var bound: Boolean = false
    private var binder: LabService.LabBinder? = null

    private var requestId: UUID? = null

    companion object {
        private const val TAG = "MainActivityTAG"
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d(TAG, "onServiceConnected")
            binder as LabService.LabBinder
            val service = binder.getService()

            this@MainActivity.binder = binder
            this@MainActivity.binder?.doSomething()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")

            binder = null
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.run {
            start.setOnClickListener {
                startLabService()
            }

            bind.setOnClickListener {
                bindLabService()
            }

            unbind.setOnClickListener {
                unbindLabService()
            }

            stop.setOnClickListener {
                stopLabService()
            }

            startWithDelay.setOnClickListener {
                startWithDelay()
            }

            startJobIntentService.setOnClickListener {
                startJobIntentService()
            }

            enqueueWork.setOnClickListener {
                enqueueWork()
            }

            cancelWork.setOnClickListener {
                cancelWork()
            }
        }
        if (Build.VERSION.SDK_INT >= 24)
            scheduleNewPhotoWork()

    }

    private fun startLabService() {
        Log.d(TAG, "startLabService clicked")
//        val intent = Intent().also { it.action = "ru.anfilek.myapplication.ACTION1" }
        val intent = Intent(this, LabService::class.java)
        startService(intent)
    }

    private fun bindLabService() {
        Log.d(TAG, "bindLabService clicked")

        Intent(this, LabService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }

    }

    private fun unbindLabService() {
        Log.d(TAG, "unbindLabService clicked")
        unbindService(serviceConnection)
    }

    private fun stopLabService() {
        Log.d(TAG, "stopLabService clicked")
        stopService(Intent(this, LabService::class.java))
    }

    private fun startWithDelay() {
        Log.d(TAG, "startWIthDelay clicked")
        Handler(Looper.getMainLooper()).postDelayed({
            startLabService()
        }, 60000)
    }

    private fun startJobIntentService() {
        LabJobIntentService.enqueueWork(this)
    }


    private fun enqueueWork() {

        val constraints: Constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val myWorkRequest = OneTimeWorkRequestBuilder<LabWorker>()
            .setConstraints(constraints)
            .addTag("WorkTag")
            .setInitialDelay(2, TimeUnit.SECONDS)
            .build()

        requestId = myWorkRequest.id
        WorkManager.getInstance(this).enqueue(myWorkRequest)

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(myWorkRequest.id)
            .observe(this) { workInfo ->
                Log.d(TAG, "state changed: " + workInfo.state.name)
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Log.d(TAG, "state succeeded: " + workInfo.outputData)
                }
            }
    }

    private fun cancelWork() {
        requestId?.let {
            WorkManager.getInstance(this).cancelWorkById(it)
            requestId = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun scheduleNewPhotoWork() {
        val constraints: Constraints = Constraints.Builder()
            .addContentUriTrigger(MediaStore.Images.Media.INTERNAL_CONTENT_URI, true)
            .addContentUriTrigger(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true)
            .build()

        val myWorkRequest = OneTimeWorkRequestBuilder<NewPhotoWorker>()
            .setConstraints(constraints)
            .addTag("PHOTO_TAG")
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork("PHOTO_TAG", ExistingWorkPolicy.REPLACE, myWorkRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "on Destroy")
    }
}