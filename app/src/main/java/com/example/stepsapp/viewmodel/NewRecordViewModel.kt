package com.example.stepsapp.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepsapp.HealthConnectManager
import com.example.stepsapp.SharedData.date
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

class NewRecordViewModel(private val healthConnectManager: HealthConnectManager): ViewModel() {
    var startTime by mutableStateOf(ZonedDateTime.now())
    var endTime by mutableStateOf(ZonedDateTime.now())
    var recordState by mutableStateOf(RecordDetails(
    ))

    fun updateUiState(recordDetails: RecordDetails) {
        recordState = recordDetails
    }

    fun insertRecord() {
        viewModelScope.launch {
            healthConnectManager.writeSteps(startTime, endTime, recordState.steps.toLong())
        }
    }
}

data class RecordDetails(
    val startTime: ZonedDateTime = ZonedDateTime.now(),
    val endTime: ZonedDateTime = ZonedDateTime.now(),
    val steps: Int = 0
)