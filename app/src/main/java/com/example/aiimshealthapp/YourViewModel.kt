package com.example.aiimshealthapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class YourViewModel : ViewModel() {
    val selectedOption = MutableLiveData<Boolean?>(null) // Use Boolean? for unselected state

    fun onOptionSelected(isYes: Boolean) {
        selectedOption.value = isYes
    }
}
