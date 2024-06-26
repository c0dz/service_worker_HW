package com.example.serviceworker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager


class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// Start Service
		startService(Intent(this, NetworkChangeService::class.java))
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(
				"service_channel",
				"My Notification",
				NotificationManager.IMPORTANCE_HIGH
			)
			val notificationManager = getSystemService(NotificationManager::class.java)
			notificationManager.createNotificationChannel(channel)
		}
		
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
		
		val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return LogViewModel(applicationContext) as T
			}
		})[LogViewModel::class.java]
		setContent {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(5.dp),
				verticalArrangement = Arrangement.Top,
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				ConnectionStatus()
				Divider(
					modifier = Modifier
						.height(3.dp),
					color = Color.Black
				)
				Logs(viewModel)
			}
		}
	}
}

@Composable
fun ConnectionStatus() {
	val context = LocalContext.current
	
	val isConnected = remember { mutableStateOf(false) }
	
	val bcr = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if (intent.action != "com.example.androidhw3.ACTION_NETWORK_CHANGE") {
				return
			}
			val isConnectedValue = intent.getBooleanExtra("CONNECTION_STATUS", false)
			isConnected.value = isConnectedValue
		}
	}
	
	
	val filter = IntentFilter("com.example.androidhw3.ACTION_NETWORK_CHANGE")
	
	DisposableEffect(Unit) {
		ContextCompat.registerReceiver(context, bcr, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
		onDispose {
			context.unregisterReceiver(bcr)
		}
	}
	
	
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(text = "Internet Access", fontWeight = FontWeight.Bold, fontSize = 35.sp)
		Text(
			text = if (isConnected.value) "Connected" else "Disconnected",
			fontWeight = FontWeight.SemiBold, fontSize = 25.sp,
			color = if (isConnected.value) Color.Green else Color.Red
		)
	}
}

@Composable
fun Logs(viewModel: LogViewModel) {
	val logLines by viewModel.logLines.collectAsState(initial = listOf())
	
	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.padding(5.dp)
	) {
		items(logLines.size) { index ->
			Text(
				text = logLines.reversed()[index],
				fontSize = 12.sp,
				fontWeight = FontWeight.Normal
			)
		}
	}
}