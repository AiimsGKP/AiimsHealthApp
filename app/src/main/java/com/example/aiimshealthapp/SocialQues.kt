package com.example.aiimshealthapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.aiimshealthapp.databinding.ActivitySocialQuesBinding

class SocialQues : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivitySocialQuesBinding
    var currentQuestionIndex = 0
    var selectedAnswer = ""
    var allAnswers = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySocialQuesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            btn0.setOnClickListener(this@SocialQues)
            btn1.setOnClickListener(this@SocialQues)
            btn2.setOnClickListener(this@SocialQues)
            nextBtn.setOnClickListener(this@SocialQues)
        }
        loadQuestions()
    }


    @SuppressLint("SetTextI18n")
    private fun loadQuestions(){
        val listQuestionModel: ArrayList<Ques>? = intent.getParcelableArrayListExtra("socQuesList")

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
        intent.putExtra("id", 3)
        startActivity(intent)
        finish()
    }

    override fun onClick(view: View?) {
        binding.apply {
            btn0.setBackgroundColor(getColor(R.color.options))
            btn1.setBackgroundColor(getColor(R.color.options))
            btn2.setBackgroundColor(getColor(R.color.options))

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
            selectedAnswer = clickedBtn.text.toString()
            selectedAnswer = selectedAnswer.substringBefore(" /")
            clickedBtn.setBackgroundColor(getColor(R.color.fphysical))
        }
    }
}