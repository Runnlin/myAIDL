package io.github.runnlin.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class mService: Service() {

    val TAG = "server:"

    override fun onBind(intent: Intent?): IBinder? {
        Log.e(TAG, "service connected")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.e(TAG, "service disconnected")
        return super.onUnbind(intent)
    }

}