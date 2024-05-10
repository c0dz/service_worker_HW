package com.example.serviceworker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// Work Request
		val loggerWorkRequest =
			OneTimeWorkRequestBuilder<LogWorker>()
				.build()
		
		WorkManager
			.getInstance(applicationContext)
			.enqueueUniqueWork(
				"loggerWorkRequest",
				ExistingWorkPolicy.REPLACE,
				loggerWorkRequest
			)
		setContent {
		
		}
	}
}