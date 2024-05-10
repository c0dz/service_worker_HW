package com.example.serviceworker

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.json.JSONObject

class NetworkChangeService : Service() {
	private val networkChangeReceiver = NetworkChangeReceiver()
	private var notification: NotificationCompat.Builder? = null
	
	companion object {
		const val ACTION_NETWORK_CHANGE = "com.example.androidhw3.ACTION_NETWORK_CHANGE"
	}
	
	override fun onBind(intent: Intent): IBinder? {
		return null
	}
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		registerReceiver(
			networkChangeReceiver,
			IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
		)
		
		notification = NotificationCompat.Builder(this, "service_channel")
			.setSmallIcon(R.drawable.ic_launcher_foreground)
			.setContentTitle("Internet Access")
			.setContentText("Connection Status: Disconnected")
		
		startForeground(1, notification!!.build())
		
		return START_STICKY
	}
	
	override fun onDestroy() {
		unregisterReceiver(networkChangeReceiver)
		super.onDestroy()
	}
	
	inner class NetworkChangeReceiver : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if (intent.action != "android.net.conn.CONNECTIVITY_CHANGE") {
				return
			}
			val connectivityManager =
				context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
			
			connectivityManager.registerDefaultNetworkCallback(object :
				ConnectivityManager.NetworkCallback() {
				override fun onAvailable(network: Network) {
					// Network is available
					val internetAccess = mapOf(
						"Status" to "Connected",
					)
					Log.i("InternetService", JSONObject(internetAccess).toString())
					
					val isConnected = true
					updateNotification(isConnected)
					sendBroadCastToUI(isConnected)
				}
				
				override fun onLost(network: Network) {
					// Network is lost
					val internetAccess = mapOf(
						"Status" to "Not Connected",
					)
					Log.i("InternetService", JSONObject(internetAccess).toString())
					
					val isConnected = false
					updateNotification(isConnected)
					sendBroadCastToUI(isConnected)
				}
			})
		}
	}
	
	private fun sendBroadCastToUI(isConnected: Boolean) {
		val connectionStatusIntent = Intent(ACTION_NETWORK_CHANGE)
		connectionStatusIntent.putExtra("CONNECTION_STATUS", isConnected)
		sendBroadcast(connectionStatusIntent)
	}
	
	private fun updateNotification(isConnected: Boolean) {
		val statusText = if (isConnected) "Connected" else "Disconnected"
		notification?.setContentText("Connection Status: $statusText")
		val notificationManager =
			getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.notify(1, notification?.build())
	}
}