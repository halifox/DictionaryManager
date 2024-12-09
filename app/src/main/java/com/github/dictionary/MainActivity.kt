package com.github.dictionary

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.github.dictionary.databinding.ActivityMainBinding
import com.github.dictionary.importer.DictionaryImporter
import org.koin.android.ext.android.inject
import java.util.Locale


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val context = this
    private val downloadManager by inject<DownloadManager>()
    private val dictionaryFileImporter by inject<DictionaryImporter>()


    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appbar) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                updateMargins(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, 0)
            }
            binding.navHostFragment.setPadding(0, 0, 0, systemBarsInsets.top + systemBarsInsets.bottom)
            insets
        }


        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        val navController = navHostFragment.navController
        binding.collapsingToolbarLayout.setupWithNavController(binding.toolbar, navController)

        navController.addOnDestinationChangedListener { navController, navDestination, bundle ->
            binding.toolbar.menu.clear()
        }
        addDownloadCompleteListener()
    }

    private fun addDownloadCompleteListener() {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (downloadId == -1L) {
                        return
                    }
                    val query = DownloadManager.Query().apply {
                        setFilterById(downloadId)
                    }
                    downloadManager.query(query)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    val uriString = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                                    dictionaryFileImporter.addImportTask(DictionaryImporter.Task(Uri.parse(uriString), Locale.SIMPLIFIED_CHINESE))
                                }

                                DownloadManager.STATUS_FAILED -> {
                                    // 下载失败，处理错误
                                }
                            }
                        }
                    }
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                ContextCompat.registerReceiver(context, receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_EXPORTED)
            }

            override fun onStop(owner: LifecycleOwner) {
                unregisterReceiver(receiver)
            }
        })
    }
}