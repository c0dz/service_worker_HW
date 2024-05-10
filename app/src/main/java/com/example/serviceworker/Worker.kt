package com.example.serviceworker

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit


class LogWorker(appContext: Context, params: WorkerParameters) :
	CoroutineWorker(appContext, params) {
	
	override suspend fun doWork(): Result {
		val bluetoothEnabled = isBluetoothEnabled(applicationContext)
		val airplaneModeStatus = isAirplaneModeOn()
		
		val airPlaneStatus = "\"Status\": ${if (airplaneModeStatus) "\"On\"" else "\"Off\""}"
		val bluetoothStatus = "\"Status\": ${if (bluetoothEnabled) "\"On\"" else "\"Off\""}"
		
		Log.i("worker_airplane", airPlaneStatus)
		Log.i("worker_bluetooth", bluetoothStatus)
		
		// Save Logs
		saveLog(
			context = applicationContext,
			tag = "worker_airplane",
			msg = airPlaneStatus
		)
		saveLog(
			context = applicationContext,
			tag = "worker_bluetooth",
			msg = bluetoothStatus
		)
		
		// Work Request
		val loggerWorkRequest =
			OneTimeWorkRequestBuilder<LogWorker>()
				.setInitialDelay(5, TimeUnit.SECONDS)
				.build()
		
		WorkManager
			.getInstance(applicationContext)
			.enqueueUniqueWork(
				"loggerWorkRequest",
				ExistingWorkPolicy.REPLACE,
				loggerWorkRequest
			)
		
		
		return Result.success()
	}
	
	private fun isAirplaneModeOn(): Boolean {
		return Settings.Global.getInt(
			applicationContext.contentResolver,
			Settings.Global.AIRPLANE_MODE_ON, 0
		) != 0;
	}
	
	private fun isBluetoothEnabled(context: Context): Boolean {
		val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?
		val bluetoothAdapter = bluetoothManager?.adapter
		return bluetoothAdapter != null && bluetoothAdapter.isEnabled
	}
}
