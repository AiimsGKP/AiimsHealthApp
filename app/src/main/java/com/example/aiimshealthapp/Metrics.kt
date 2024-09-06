package com.example.aiimshealthapp

data class Metrics(
    val user:User,
    val metrics: MetricsData
)
{
    fun toMap(): Map<String, Any> {
        return mapOf(
            "user" to mapOf(
                "username" to user.username,
                "email" to user.email
            ),
            "metrics" to mapOf(
                "firstName" to metrics.firstName,
                "lastName" to metrics.lastName,
                "gender" to metrics.gender,
                "age" to metrics.age,
                "educationLevel" to metrics.educationLevel,
                "employmentStatus" to metrics.employmentStatus,
                "height" to metrics.height,
                "weight" to metrics.weight,
                "bmi" to metrics.bmi,
                "waist" to metrics.waist,

                "smoking" to metrics.smoking,
                "alcohol" to metrics.alcohol,
                "diabetes" to metrics.diabetes,
                "hypertension" to metrics.hypertension,
                "medication" to metrics.medication,
                "sleep" to metrics.sleep,
                "diet" to metrics.diet,

                "stepGoal" to metrics.stepGoal
                )
        )
    }
}

data class MetricsData(
    val firstName:String = "",
    val lastName:String = "",
    val gender:String = "",
    val age:String = "",
    val educationLevel:String = "",
    val employmentStatus:String = "",
    val height:String = "",
    val weight:String = "",
    val bmi:String = "",
    val waist:String = "",
    val smoking:Boolean = false,
    val alcohol:Boolean = false,
    val diabetes:String = "",
    val hypertension:String = "",
    val medication:String = "",
    val sleep:String = "",
    val diet:String = "",
    val stepGoal:String = "-1"
)

