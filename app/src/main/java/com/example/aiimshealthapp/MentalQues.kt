package com.example.aiimshealthapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.aiimshealthapp.databinding.ActivityMentalQuesBinding

class MentalQues : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityMentalQuesBinding
    var currentQuestionIndex = 0
    var selectedAnswer = ""
    var allAnswers = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMentalQuesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            btn0.setOnClickListener(this@MentalQues)
            btn1.setOnClickListener(this@MentalQues)
            btn2.setOnClickListener(this@MentalQues)
            btn3.setOnClickListener(this@MentalQues)
            btn4.setOnClickListener(this@MentalQues)
            nextBtn.setOnClickListener(this@MentalQues)
        }
        loadQuestions()
    }

    @SuppressLint("SetTextI18n")
    private fun loadQuestions(){
        val listQuestionModel: ArrayList<Ques>? = intent.getParcelableArrayListExtra("menQuesList")

        if(listQuestionModel != null) {
            if(currentQuestionIndex == listQuestionModel.size){
                finishQuiz()
                return
            }
            binding.apply {
                questionIndicatorTextview.text = "Question ${currentQuestionIndex+1}/${listQuestionModel.size}"
                questionProgressIndicator.progress = (((currentQuestionIndex+1).toFloat()/ listQuestionModel.size.toFloat()) * 100).toInt()
                questionTextview.text = listQuestionModel[currentQuestionIndex].questions[0]
            }
        }
    }

    private fun finishQuiz() {
        val intent = Intent(this, QuizPageActivity::class.java)
        intent.putStringArrayListExtra("all_answers", ArrayList(allAnswers))
        intent.putExtra("id", 2)
        startActivity(intent)
        finish()
    }

    override fun onClick(view: View?) {
        binding.apply {
            btn0.setBackgroundColor(getColor(R.color.options))
            btn1.setBackgroundColor(getColor(R.color.options))
            btn2.setBackgroundColor(getColor(R.color.options))
            btn3.setBackgroundColor(getColor(R.color.options))
            btn4.setBackgroundColor(getColor(R.color.options))

        }

        val clickedBtn = view as Button
        if(clickedBtn.id == R.id.next_btn){
            if(selectedAnswer != ""){
                allAnswers.add(selectedAnswer)
                currentQuestionIndex++
                loadQuestions()
            }
            selectedAnswer = ""

        }
        else{
            if(clickedBtn.id == R.id.btn0) selectedAnswer = "1"
            else if(clickedBtn.id == R.id.btn1) selectedAnswer = "2"
            else if(clickedBtn.id == R.id.btn2) selectedAnswer = "3"
            else if(clickedBtn.id == R.id.btn3) selectedAnswer = "4"
            else if(clickedBtn.id == R.id.btn4) selectedAnswer = "5"

            clickedBtn.setBackgroundColor(getColor(R.color.fphysical))
        }
    }
}