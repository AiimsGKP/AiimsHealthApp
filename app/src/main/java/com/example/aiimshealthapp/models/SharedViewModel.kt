package com.example.aiimshealthapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> get() = _stepCount

    fun setStepCount(count: Int) {
        _stepCount.value = count
    }
}
