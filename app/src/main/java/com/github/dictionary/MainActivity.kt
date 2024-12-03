package com.github.dictionary

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.github.dictionary.databinding.ActivityMainBinding
import org.koin.android.ext.android.inject
import java.util.Locale


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    val downloadManager by inject<DownloadManager>()
    val fileImporter by inject<FileImporter>()


    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        val navController = navHostFragment.navController
//        val navController = binding.navHostFragment.findNavController()
        binding.collapsingToolbarLayout.setupWithNavController(binding.toolbar, navController)

        navController.addOnDestinationChangedListener { navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
            clearToolbarMenu()
        }

        val notificationManagerCompat by inject<NotificationManagerCompat>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannelCompat.Builder("default_channel", NotificationManager.IMPORTANCE_HIGH)
                .setName("Default Channel")
                .setDescription("Default notification channel")
                .setSound(null, null)
                .setVibrationEnabled(false)
                .build();
            notificationManagerCompat.createNotificationChannel(channel)
        }



        lifecycle.addObserver(object : DefaultLifecycleObserver {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                    if (downloadId != -1L) {
                        val query = DownloadManager.Query().apply {
                            setFilterById(downloadId)
                        }
                        downloadManager.query(query)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                                when (status) {
                                    DownloadManager.STATUS_SUCCESSFUL -> {
                                        val uriString = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                                        fileImporter.addImportTask(FileImporter.Task(Uri.parse(uriString), Locale.SIMPLIFIED_CHINESE))
                                    }

                                    DownloadManager.STATUS_FAILED -> {
                                        // 下载失败，处理错误
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED)
            }

            override fun onStop(owner: LifecycleOwner) {
                unregisterReceiver(receiver)
            }
        })
    }

    private fun clearToolbarMenu() {
        binding.toolbar.menu.clear()
    }


}