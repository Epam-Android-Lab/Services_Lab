package ru.anfilek.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import kotlin.concurrent.thread
import kotlin.random.Random

/** Command to the service to display a message  */
const val MSG_SAY_HELLO = 1

class MessengerService : Service() {

    companion object {
        private const val TAG = "MessengerServiceTAG"
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private lateinit var mMessenger: Messenger

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")
    }

    /**
     * Handler of incoming messages from clients.
     */
    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SAY_HELLO -> sayHello(msg.replyTo)
                else -> super.handleMessage(msg)
            }
        }
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    override fun onBind(intent: Intent): IBinder? {
        Toast.makeText(applicationContext, "binding", Toast.LENGTH_SHORT).show()
        mMessenger = Messenger(handler)
        return mMessenger.binder
    }

    private fun sayHello(messenger: Messenger) {
        thread {
            applicationContext.sayHelloFromCurrentProcessAndThread(TAG)
            Thread.sleep(1500)
            Log.d(TAG, "hello from service")
            val message = Message.obtain().apply {
                what = MainActivity.MSG_SAVED_WHAT
                arg1 = Random.nextInt()

            }
            messenger.send(message)
        }
    }
}