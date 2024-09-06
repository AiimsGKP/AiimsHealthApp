package com.example.aiimshealthapp

data class ReminderModels (
    val item : String = ""
)

data class Hydration(
    val user:User,
    val timers : List<Timer> = emptyList()
)
{
    fun toMap(): Map<String, Any> {
        return mapOf(
            "user" to mapOf(
                "username" to user.username,
                "email" to user.email
            ),
            "timers" to timers.map { timer ->
                mapOf(
                    "title" to timer.title,
                    "activated" to timer.activated,
                    "cardViewId" to timer.cardViewId,
                    "hour" to timer.hour,
                    "minute" to timer.minute,
                    "amPm" to timer.amPm
                )
            }
        )
    }
}

data class Medication(
    val user:User,
    val timers:List<Timer> = emptyList()
)
{
    fun toMap(): Map<String, Any> {
        return mapOf(
            "user" to mapOf(
                "username" to user.username,
                "email" to user.email
            ),
            "timers" to timers.map { timer ->
                mapOf(
                    "title" to timer.title,
                    "activated" to timer.activated,
                    "cardViewId" to timer.cardViewId,
                    "hour" to timer.hour,
                    "minute" to timer.minute,
                    "amPm" to timer.amPm
                )
            }
        )
    }
}

data class Timer(
    var title :String = "",
    var activated : Boolean = true,
    var cardViewId : String = "",
    var hour: String = "",
    var minute: String = "",
    var amPm : String = ""
)
