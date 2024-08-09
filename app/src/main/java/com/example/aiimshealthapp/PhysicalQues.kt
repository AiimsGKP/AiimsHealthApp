package com.example.aiimshealthapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.aiimshealthapp.databinding.ActivityPhysicalQuesBinding
import com.google.android.material.progressindicator.LinearProgressIndicator

class PhysicalQues : AppCompatActivity(), View.OnClickListener {

    companion object{
        var id:Int = -1
    }

    lateinit var binding:ActivityPhysicalQuesBinding
    var currentQuestionIndex = 0
    var selectedAnswer = ""
    var noDays = ""
    var noHours = ""
    var noMins = ""
    var perPageAns = mutableListOf<String>("", "", "")
    var allAnswers = mutableListOf<List<String>>()


    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        binding = ActivityPhysicalQuesBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.apply {
            btnYes.setOnClickListener(this@PhysicalQues)
            btnNo.setOnClickListener(this@PhysicalQues)
            nextBtn.setOnClickListener(this@PhysicalQues)
        }
        loadQuestions()

    }

    @SuppressLint("SetTextI18n")
    private fun loadQuestions(){
        val card2 = findViewById<LinearLayout>(R.id.llsecond)
        val card3 = findViewById<LinearLayout>(R.id.llthird)
        card2.visibility = View.GONE
        card3.visibility = View.GONE
        val listQuestionModel: ArrayList<Ques>? = intent.getParcelableArrayListExtra("physQuesList")

        if(listQuestionModel != null) {
            if (currentQuestionIndex == listQuestionModel.size) {
                finishQuiz()
                return
            }

            findViewById<TextView>(R.id.question_indicator_textview).text =
                "Question ${currentQuestionIndex + 1}/${listQuestionModel.size}"
            findViewById<LinearProgressIndicator>(R.id.question_progress_indicator).progress =
                (((currentQuestionIndex + 1).toFloat() / listQuestionModel.size.toFloat()) * 100).toInt()
            findViewById<TextView>(R.id.question_textview).text =
                listQuestionModel[currentQuestionIndex].questions[0]
            findViewById<TextView>(R.id.question2_textview).text =
                listQuestionModel[currentQuestionIndex].questions[1]
            findViewById<TextView>(R.id.question3_textview).text =
                listQuestionModel[currentQuestionIndex].questions[2]

        }
    }


    override fun onClick(view: View?) {

        val card2 = findViewById<LinearLayout>(R.id.llsecond)
        val card3 = findViewById<LinearLayout>(R.id.llthird)

        val clickedBtn = view as Button
        if(clickedBtn.id == R.id.btnYes){
            binding.apply {
                btnYes.setBackgroundColor(getColor(R.color.options))
                btnNo.setBackgroundColor(getColor(R.color.options))
            }
            clickedBtn.setBackgroundColor(getColor(R.color.fphysical))
            card2.visibility = View.VISIBLE
            card3.visibility = View.VISIBLE

        }
        if(clickedBtn.id == R.id.btnNo){
            binding.apply {
                btnYes.setBackgroundColor(getColor(R.color.options))
                btnNo.setBackgroundColor(getColor(R.color.options))
            }
            clickedBtn.setBackgroundColor(getColor(R.color.fphysical))
            card2.visibility = View.GONE
            card3.visibility = View.GONE

        }

        if(clickedBtn.id == R.id.next_btn){
            perPageAns = mutableListOf("", "", "",)
            if(selectedAnswer != ""){
                if(selectedAnswer == "Yes") {
                    noDays = findViewById<EditText>(R.id.etDays).text.toString()
                    noHours = findViewById<EditText>(R.id.etHours).text.toString()
                    noMins = (noHours.toInt()*60 + findViewById<EditText>(R.id.etMins).text.toString().toInt()).toString()
                    if (noDays.isNotEmpty() && noHours.isNotEmpty() && noMins.isNotEmpty()) {
                        if(noDays.toInt() > 7 || noMins.toInt() > 960 ){
                            return
                        }
                        perPageAns.set(0, selectedAnswer)
                        perPageAns.set(1, noDays)
                        perPageAns.set(2, noMins)
                        allAnswers.add(perPageAns)
                        currentQuestionIndex++
                        selectedAnswer = ""
                        binding.apply {
                            btnYes.setBackgroundColor(getColor(R.color.options))
                            btnNo.setBackgroundColor(getColor(R.color.options))
                        }
                        loadQuestions()
                    }
                }
                else{
                    perPageAns.set(0, selectedAnswer)
                    allAnswers.add(perPageAns)
                    currentQuestionIndex++
                    selectedAnswer = ""
                    binding.apply {
                        btnYes.setBackgroundColor(getColor(R.color.options))
                        btnNo.setBackgroundColor(getColor(R.color.options))
                    }
                    loadQuestions()
                }
            }

        }
        else{
            selectedAnswer = clickedBtn.text.toString()
            clickedBtn.setBackgroundColor(getColor(R.color.fphysical))
        }
    }
    private fun finishQuiz() {
        val intent = Intent(this, QuizPageActivity::class.java)
        intent.putStringArrayListExtra("all_answers", ArrayList(allAnswers.flatten()))
        intent.putExtra("id", 1)
        startActivity(intent)
        finish()

    }
}