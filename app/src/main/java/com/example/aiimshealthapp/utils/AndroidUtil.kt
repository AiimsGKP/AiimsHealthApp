package com.example.aiimshealthapp.utils

import android.content.Context
import android.widget.Toast

class AndroidUtil {

    fun showToast(context: Context, message: String){
        Toast.makeText(context,message, Toast.LENGTH_LONG).show()
    }
}