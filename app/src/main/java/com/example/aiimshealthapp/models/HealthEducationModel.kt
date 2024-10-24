package com.example.aiimshealthapp.models

data class HealthEducationModel(
    val diseases : List<Diseases>
)

data class Diseases(
    val name: String,
    val definition: String,
    val causes: String,
    val prevention:String,
    val symptoms:String,
    val whatToEat:String
)
