package com.example.detectordehumedad.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class DataViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("humidity")

    private val _humidityState = MutableStateFlow<HumidityState>(HumidityState.Idle)
    val humidityState: StateFlow<HumidityState> = _humidityState

    private val _recordsState = MutableStateFlow<RecordsState>(RecordsState.Loading)
    val recordsState: StateFlow<RecordsState> = _recordsState

    init {
        // Simulate real-time humidity data
        viewModelScope.launch {
            while (true) {
                val humidity = Random.nextInt(30, 90)
                _humidityState.value = HumidityState.Success(humidity)
                kotlinx.coroutines.delay(2000)
            }
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val records = snapshot.children.mapNotNull {
                    it.getValue(HumidityRecord::class.java)?.copy(id = it.key!!)
                }
                _recordsState.value = RecordsState.Success(records)
            }

            override fun onCancelled(error: DatabaseError) {
                _recordsState.value = RecordsState.Error(error.message)
            }
        })
    }

    fun addHumidityRecord(humidity: Int) {
        val recordId = database.push().key
        if (recordId != null) {
            database.child(recordId).setValue(mapOf("humidity" to humidity))
        }
    }

    fun updateHumidityRecord(record: HumidityRecord) {
        database.child(record.id).setValue(mapOf("humidity" to record.humidity))
    }

    fun deleteHumidityRecord(recordId: String) {
        database.child(recordId).removeValue()
    }
}

sealed class HumidityState {
    object Idle : HumidityState()
    data class Success(val humidity: Int) : HumidityState()
    data class Error(val message: String) : HumidityState()
}

sealed class RecordsState {
    object Loading : RecordsState()
    data class Success(val records: List<HumidityRecord>) : RecordsState()
    data class Error(val message: String) : RecordsState()
}
