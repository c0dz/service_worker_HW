package com.example.serviceworker

import android.content.Context
import java.io.FileOutputStream
import java.io.IOException

fun saveLog(context: Context, tag: String, msg: String) {
	val currentDateTime = System.currentTimeMillis()
	val formattedDateTime =
		android.text.format.DateFormat.format("yyyy/MM/dd hh:mm:ss", currentDateTime)
	val logEntry = String.format(
		"{\"timestamp\":\"%s\" , \"type\":\"%s\" , %s}\n",
		formattedDateTime,
		tag,
		msg
	)
	
	try {
		val fileName = "SavedLogs.txt"
		val fileOutputStream: FileOutputStream =
			context.openFileOutput(fileName, Context.MODE_APPEND)
		fileOutputStream.write(logEntry.toByteArray())
		fileOutputStream.close()
	} catch (e: IOException) {
		// Handle the error appropriately
		e.printStackTrace()
	}
}