package com.example.aiimshealthapp

data class User(
    val username : String,
    val email : String,
){
    constructor():this("","")
}
