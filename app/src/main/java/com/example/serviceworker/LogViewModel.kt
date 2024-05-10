package com.example.serviceworker

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class LogViewModel(private val applicationContext: Context) : ViewModel() {
	private val _logLines = MutableStateFlow(listOf<String>())
	val logLines: StateFlow<List<String>> = _logLines
	
	init {
		viewModelScope.launch {
			val file = File(applicationContext.filesDir, "SavedLogs.txt")
			while (true) {
				try {
					val lines = file.readLines()
					_logLines.update { lines }
					Log.d("ViewModel", "Updated log lines: $lines")
				} catch (e: Exception) {
					Log.e("ViewModel", "Error reading file: $e")
				}
				delay(500)
			}
		}
	}
}