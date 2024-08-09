package com.example.aiimshealthapp

import android.os.Parcel
import android.os.Parcelable

data class Question(
    val user: User,
    val quizes: List<QuesModel>
){
    fun toMap(): Map<String, Any> {
        return mapOf(
            "user" to mapOf(
                "username" to user.username,
                "email" to user.email
            ),
            "quizes" to quizes.map { quiz ->
                mapOf(
                    "id" to quiz.id,
                    "questions" to quiz.questions.map { question ->
                        mapOf(
                            "questions" to question.questions
                        )
                    },
                    "answers" to quiz.answers,
                    "score" to quiz.score
                )
            }
        )
    }
}


data class QuesModel(
    val id :String = "",
    val questions: List<Ques> = emptyList(),
    var answers: List<String> = emptyList(),
    var score : Int = 0
)


data class Ques(
    val questions : List<String> = emptyList()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(questions)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Ques> {
        override fun createFromParcel(parcel: Parcel): Ques {
            return Ques(parcel)
        }

        override fun newArray(size: Int): Array<Ques?> {
            return arrayOfNulls(size)
        }
    }
}

