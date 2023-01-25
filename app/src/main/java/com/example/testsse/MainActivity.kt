package com.example.testsse

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.example.testsse.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val eventSourceListener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                super.onOpen(eventSource, response)
                Log.d(TAG, "Connection Opened")
            }

            override fun onClosed(eventSource: EventSource) {
                super.onClosed(eventSource)
                Log.d(TAG, "Connection Closed")
            }

            override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
            ) {
                super.onEvent(eventSource, id, type, data)
                Log.d(TAG, "On Event Received! Data -: $data")
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                super.onFailure(eventSource, t, response)
                Log.d(TAG, "On Failure -: ${response?.body}")
            }
        }

        val client = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build()

        val request = Request.Builder()
                .url("https://eas.entrustdev.cloud/events/4741fa2b-d25e-4298-89b7-b2f0748a5845")
                .header("Accept", "application/json; q=0.5")
                .addHeader("Accept", "text/event-stream")
            .addHeader("X-Tenant-ID", "6c0ca266-f38c-439a-bc85-1924d22a24cc")
                .build()

        EventSources.createFactory(client)
                .newEventSource(request = request, listener = eventSourceListener)

        lifecycleScope.launchWhenCreated {
            withContext(Dispatchers.IO) {
                client.newCall(request).enqueue(responseCallback = object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "API Call Failure ${e.localizedMessage}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.d(TAG, "APi Call Success ${response.body.toString()}")
                    }
                })
            }
        }
    }
}