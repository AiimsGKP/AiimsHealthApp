package com.example.aiimshealthapp

data class FactModel(
   val facts : List<Fact> = emptyList()
)

data class Fact(
    val fact:String = "",
    val source:String = "",
    val topic:String = ""
)
//{
//    fun toMap():Map<String, Any>{
//        return mapOf(
//            "fact" to
//        )
//    }
//}