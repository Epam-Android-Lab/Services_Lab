package ru.anfilek.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import ru.androidlab.aidllab.IRemoteService
import ru.anfilek.myapplication.databinding.ActivityMainBinding
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivityTAG"
        const val MSG_SAVED_WHAT = 1
    }

    //    ------------------  AIDL LAB ------------------
    private var remoteService: IRemoteService? = null
    private var remoteServiceConnected: Boolean = false

    private val remoteServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d(TAG, "IRemoteService onServiceConnected")

            remoteService = IRemoteService.Stub.asInterface(binder)
            remoteServiceConnected = true
            logText("connected to remote service")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "IRemoteService onServiceDisconnected")

            remoteService = null
            remoteServiceConnected = false
            logText("disconnected from remote service")
        }
    }

    private fun bindToRemoteService() {
        val intent = Intent()
        intent.setClassName("ru.androidlab.aidllab", "ru.androidlab.aidllab.RemoteService")
        bindService(intent, remoteServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindFromRemoteService() {
        if (remoteServiceConnected) unbindService(remoteServiceConnection)
    }

    private fun getPid() {
        showToast("get Pid From remote: ${remoteService?.pid}")
    }

    private fun sendLongRequest() {
        // call oneway function
        logText("Call one way function")
        remoteService?.basicTypes(1, 2L, true, 0.5F, 0.6, "String")
        logText("AFTER Call one way function")

        showToast("send long request to remote")
    }
    //    -----------------  AIDL LAB end ---------------

    //    ------------------  Messenger LAB -----------------
    private var messengerBound: Boolean = false
    private var messenger: Messenger? = null

    private val messengerServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d(TAG, "messengerServiceConnection onServiceConnected")

            messenger = Messenger(binder)
            messengerBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "messengerServiceConnection onServiceDisconnected")

            messenger = null
            messengerBound = false
        }
    }

    private fun bindWithMessenger() {
        Intent(this, MessengerService::class.java).also { intent ->
            bindService(intent, messengerServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun sendMessage() {
        val message = Message.obtain().apply { what = MSG_SAY_HELLO }
        message.replyTo = activityMessenger
        messenger?.let {
            it.send(message)
            logText("sendMessage: $message}")
        } ?: showToast("Unable to send message: messenger is null")
    }
    //    ---------------  Messenger LAB end ---------------

    //    ------------------  SERVICE LAB ------------------
    private var bound: Boolean = false
    private var binder: LabService.LabBinder? = null
    private var requestId: UUID? = null

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

            bindMessenger.setOnClickListener {
                bindWithMessenger()
            }

            sendMessage.setOnClickListener {
                sendMessage()
            }

            bindRemote.setOnClickListener {
                bindToRemoteService()
            }

            unbindRemote.setOnClickListener {
                unbindFromRemoteService()
            }

            getPid.setOnClickListener {
                getPid()
            }

            longRun.setOnClickListener {
                sendLongRequest()
            }
        }
        if (Build.VERSION.SDK_INT >= 24)
            scheduleNewPhotoWork()

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

    private val activityMessenger by lazy {
        Messenger(object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_SAVED_WHAT -> {
                        showToast("Message was saved, arg1: ${msg.arg1}")
                    }
                    else -> logText("else was put into the messenger")
                }
            }
        })
    }

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    private fun logText(text: String) = Log.d(TAG, text)
}