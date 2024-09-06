package com.example.aiimshealthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CountDownLatch

class QuizPageActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val physQuestionModel = mutableListOf<Ques>()
    private val menQuestionModel = mutableListOf<Ques>()
    private val socQuestionModel = mutableListOf<Ques>()
    private val Ques = mutableListOf<QuesModel>()
    private val ids = mutableListOf<Int>()
    private lateinit var dashBtn : Button
    private val tag = "CHECK_RESPONSE"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz_page)

        val physicalBtn = findViewById<CardView>(R.id.cvPhysical)
        val mentalBtn = findViewById<CardView>(R.id.cvMental)
        val socialBtn = findViewById<CardView>(R.id.cvSocial)
        dashBtn = findViewById(R.id.button2)
        dashBtn.isEnabled = false
        loadDatabase()

        physicalBtn.setOnClickListener {
            loadPhysicalQues()
        }

        mentalBtn.setOnClickListener {
            loadMentalQues()
        }

        socialBtn.setOnClickListener {
            loadSocialQues()
        }

        dashBtn.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun enableBtn(){
        var count = 0
        dashBtn.isEnabled = false  // Initially disable the button to indicate loading

        if (currentUser != null) {
            db.collection("questions2")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val data = document.data
                        val quizes = data["quizes"] as? List<Map<String, Any>>

                        if (quizes != null) {
                            for (quiz in quizes) {
                                val answers = quiz["answers"] as? List<String>

                                // Check if the answers list is not null or empty
                                if (!answers.isNullOrEmpty()) count += 1

                                // If the count reaches 3, enable the button and break out of the loop early
                                if (count == 3) {
                                    dashBtn.isEnabled = true
                                    break
                                }
                            }
                        }

                        // No need to process further documents if count == 3
                        if (count == 3) break
                    }
                }
                .addOnFailureListener {
                    // Optionally handle errors, e.g., show a message to the user
                }
        }
    }


    private fun loadPhysicalQues(){
        val intent = Intent(this, PhysicalQues::class.java)
        intent.putParcelableArrayListExtra("physQuesList", ArrayList(physQuestionModel))
        startActivity(intent)
    }

    private fun loadSocialQues(){
        val intent = Intent(this, SocialQues::class.java)
        intent.putParcelableArrayListExtra("socQuesList", ArrayList(socQuestionModel))
        startActivity(intent)
    }

    private fun loadMentalQues(){
        val intent = Intent(this, MentalQues::class.java)
        intent.putParcelableArrayListExtra("menQuesList", ArrayList(menQuestionModel))
        startActivity(intent)
    }


    private fun loadDatabase(){
        // P1
        physQuestionModel.add(Ques(listOf(
            "Does your work involve vigorous-intensity activity that causes large increases in breathing or heart rate like (carrying or lifting heavy loads, digging or construction work) for at least 10 minutes continuously? \n" +
                    "क्या आपके काम में अत्यधिक तीव्रता वाली गतिविधि शामिल है जिससे सांस लेने या हृदय गति में बहुत अधिक वृद्धि होती है जैसे (भारी वजन उठाना या उठाना, खुदाई या निर्माण कार्य) कम से कम 10 मिनट तक लगातार?\n",
            "In a typical week, on how many days do you do vigorous intensity activities as part of your work? \n" +
                    "एक सामान्य सप्ताह में आप कितने दिन जोरदार व्यायाम करते हैं - आपके काम के हिस्से के रूप में गहन गतिविधियाँ?\n",
            "How much time do you spend doing vigorous-intensity activities at work on a typical day? \n" +
                    "आप ज़ोरदार तीव्रता वाली गतिविधियाँ करने में कितना समय व्यतीत करते हैं? एक सामान्य दिन पर काम पर?\n"
        )))

// P4
        physQuestionModel.add(Ques(listOf(
            "Does your work involve moderate-intensity activity that causes small increases in breathing or heart rate such as brisk walking (or carrying light loads) for at least 10 minutes continuously? \n" +
                    "क्या आपके काम में मध्यम-तीव्रता वाली गतिविधि शामिल है, जिससे सांस लेने या हृदय गति में थोड़ी वृद्धि होती है जैसे कि कम से कम 10 मिनट तक लगातार तेज चलना (या हल्का भार उठाना)?\n",
            "In a typical week, on how many days do you do moderate-intensity activities as part of your work? \n" +
                    "एक सामान्य सप्ताह में आप कितने दिन मध्यम कार्य करते हैं- आपके काम के हिस्से के रूप में गहन गतिविधियाँ?\n",
            "How much time do you spend doing moderate-intensity activities at work on a typical day? \n" +
                    "आप मध्यम तीव्रता वाली गतिविधियाँ करने में कितना समय व्यतीत करते हैं? एक सामान्य दिन पर काम पर?\n"
        )))

// P7
        physQuestionModel.add(Ques(listOf(
            "Do you walk or use a bicycle (pedal cycle) for at least 10 minutes continuously to get to and from places? \n" +
                    "क्या आप किसी स्थान पर आने-जाने के लिए लगातार कम से कम 10 मिनट तक पैदल चलते हैं या साइकिल (पैडल साइकिल) का उपयोग करते हैं?\n",
            "In a typical week, on how many days do you walk or bicycle for at least 10 minutes continuously to get to and from places? \n" +
                    "एक सामान्य सप्ताह में आप कितने दिन पैदल चलते हैं या साइकिल चलाते हैं स्थानों पर आने-जाने के लिए लगातार कम से कम 10 मिनट?\n",
            "How much time do you spend walking or bicycling for travel on a typical day? \n" +
                    "आप यात्रा के लिए पैदल चलने या साइकिल चलाने में कितना समय व्यतीत करते हैं? किसी सामान्य दिन?\n"
        )))

// P10
        physQuestionModel.add(Ques(listOf(
            "Do you do any vigorous-intensity sports, fitness or recreational (leisure) activities that cause large increases in breathing or heart rate like (running or football) for at least 10 minutes continuously? \n" +
                    "क्या आप कोई ज़ोरदार खेल, फिटनेस या मनोरंजक (अवकाश) गतिविधियाँ करते हैं जो कम से कम 10 मिनट तक लगातार (दौड़ना या फुटबॉल) जैसी सांस लेने या हृदय गति में बड़ी वृद्धि का कारण बनती हैं?\n",
            "In a typical week, on how many days do you do vigorous intensity sports, fitness or recreational (leisure) activities? \n" +
                    "एक सामान्य सप्ताह में आप कितने दिन जोरदार व्यायाम करते हैं गहन खेल, फिटनेस या मनोरंजक (अवकाश) गतिविधियाँ?\n",
            "How much time do you spend doing vigorous-intensity sports, fitness or recreational activities on a typical day? \n" +
                    "आप ज़ोरदार तीव्रता वाले खेल करने में कितना समय बिताते हैं, किसी सामान्य दिन में फिटनेस या मनोरंजक गतिविधियाँ?\n"
        )))

// P13
        physQuestionModel.add(Ques(listOf(
            "Do you do any moderate-intensity sports, fitness or recreational (leisure) activities that cause a small increase in breathing or heart rate such as brisk walking, (cycling, swimming, volleyball) for at least 10 minutes continuously? \n" +
                    "क्या आप कोई मध्यम-तीव्रता वाले खेल, फिटनेस या मनोरंजक (अवकाश) गतिविधियाँ करते हैं जिससे सांस लेने या हृदय गति में थोड़ी वृद्धि होती है जैसे कि तेज चलना, (साइकिल चलाना, तैराकी, वॉलीबॉल) लगातार कम से कम 10 मिनट तक?\n",
            "In a typical week, on how many days do you do moderate intensity sports, fitness or recreational (leisure) activities? \n" +
                    "एक सामान्य सप्ताह में आप कितने दिन मध्यम कार्य करते हैं-गहन खेल, फिटनेस या मनोरंजक (अवकाश) गतिविधियाँ?\n",
            "How much time do you spend doing moderate-intensity sports, fitness or recreational (leisure) activities on a typical day? \n" +
                    "आप मध्यम तीव्रता वाले खेल करने में कितना समय व्यतीत करते हैं, किसी सामान्य दिन में फिटनेस या मनोरंजक (अवकाश) गतिविधियाँ?\n"
        )))
//        physQuestionModel.add(Ques(listOf("","",
//            "How much time do you usually spend sitting or reclining on a  typical day?  \n" +
//            "आप आमतौर पर सामान्य दिन में कितना समय बैठे या लेटे हुए बिताते हैं?\n"
//        )))


        socQuestionModel.add(Ques(listOf("I experience a general sense of emptiness \n" +
                "     मुझे खालीपन का सामान्य अहसास होता है \n")))
        socQuestionModel.add(Ques(listOf("I miss having people around me [EL]\n" +
                "     झे अपने आस-पास लोगों की कमी खलती है [EL] \n")))
        socQuestionModel.add(Ques(listOf("I often feel rejected [EL]\n" +
                "     मैं अक्सर खुद को नकारा हुआ महसूस करता हूँ [EL]\n")))
        socQuestionModel.add(Ques(listOf("There are plenty of people I can rely on when I have problems \n" +
                "     जब मुझे कोई समस्या होती है तो मैं कई लोगों पर भरोसा कर सकता हूँ [SL]\n")))
        socQuestionModel.add(Ques(listOf("There are many people I can trust completely \n" +
                "     कई लोग हैं जिन पर मैं पूरी तरह भरोसा कर सकता हूँ [SL]\n")))
        socQuestionModel.add(Ques(listOf("There are enough people I feel close to \n" +
                "     कई लोग हैं जिनके मैं करीब महसूस करता हूँ [SL]\n")))

        menQuestionModel.add(Ques(listOf("I’ve been feeling optimistic about the Future.\n" + "मैं भविष्य को लेकर आशावादी महसूस कर रहा हूँ।\n")))
        menQuestionModel.add(Ques(listOf("I’ve been feeling useful.\n" + "मैं  उपयोगी महसूस कर रहा हूँ।\n")))
        menQuestionModel.add(Ques(listOf("I’ve been feeling relaxed.\n" +"मैं  आराम महसूस कर रहा हूं।\n")))
        menQuestionModel.add(Ques(listOf("I’ve been dealing with problems well.\n" + "मैं समस्याओं से अच्छी तरह निपट रहा हूं।\n")))
        menQuestionModel.add(Ques(listOf("I’ve been thinking clearly.\n" + "मैं स्पष्ट रूप से  सोच रहा हूं।\n")))
        menQuestionModel.add(Ques(listOf("I’ve been feeling close to other people. \n" + "मैं अन्य लोगों के करीब महसूस कर रहा हूँ।\n")))
        menQuestionModel.add(Ques(listOf("I’ve been able to make up my own mind about things.\n" + "मैं चीज़ों के बारे में अपना मन बनाने में सक्षम हूँ।\n")))

        val latch = CountDownLatch(3)
        //checking if the answers exists on the database
        checkAnswer(0, object : CheckAnswerCallback {
            override fun onResult(result: Boolean, answers: List<String>?, score: Int?) {
                val finalAnswers = answers?.toList() ?: emptyList()  // Default to emptyList if answers is null
                val finalScore = score ?: 0  // Default to 0 if score is null

                Ques.add(QuesModel("1", physQuestionModel, finalAnswers, finalScore))
                Log.i(tag, finalScore.toString())
                latch.countDown()
            }
        })

        checkAnswer(1, object : CheckAnswerCallback {
            override fun onResult(result: Boolean, answers: List<String>?, score: Int?) {
                val finalAnswers = answers?.toList() ?: emptyList()  // Default to emptyList if answers is null
                val finalScore = score ?: 0  // Default to 0 if score is null

                Ques.add(QuesModel("2", menQuestionModel, finalAnswers, finalScore))
                Log.i(tag, finalScore.toString())
                latch.countDown()
            }
        })

        checkAnswer(2, object : CheckAnswerCallback {
            override fun onResult(result: Boolean, answers: List<String>?, score: Int?) {
                val finalAnswers = answers?.toList() ?: emptyList()  // Default to emptyList if answers is null
                val finalScore = score ?: 0  // Default to 0 if score is null

                Ques.add(QuesModel("3", socQuestionModel, finalAnswers, finalScore))
                Log.i(tag, finalScore.toString())
                latch.countDown()
            }
        })


        Thread {
            try {
                latch.await() // Wait until the countDownLatch reaches zero
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            runOnUiThread {
                if (Ques.isNotEmpty()) {
                    if(currentUser != null){
                        val username = currentUser.email.toString().substringBefore("@")
                        val user = User(username?: "Unknown User", currentUser.email ?: "No Email")
                        val userId = auth.currentUser?.uid ?: "unknown_user"
                        val allAnswers = intent.getStringArrayListExtra("all_answers")?.toList()
                            ?: emptyList()

                        val id = intent.getIntExtra("id", -1)
                        var score = 0
                        if(allAnswers.isNotEmpty()){
                            ids.add(id)
                            if(id==1){
                                score = calculatePhysicalScore(allAnswers)
                            }
                            else if(id==2){
                                score = calculateMentalScore(allAnswers)
                            }
                            else if(id==3){
                                score = calculateSocialScore(allAnswers)
                            }
                        }
                        db.collection("questions2")
                            .whereEqualTo("user.email", currentUser.email)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                // Check if any documents were found
                                if (querySnapshot.isEmpty) {
                                    // No document found with the given email
                                    val question = Question(user = user, quizes = Ques)
                                    db.collection("questions2").document(userId).set(question)
                                        .addOnSuccessListener {
                                            // Data was stored successfully
                                        }
                                        .addOnFailureListener {
                                            // Handle possible errors
                                        }
                                } else {
                                    for (quiz in Ques){
                                        if(quiz.id.toInt() in ids) {
                                            quiz.answers = allAnswers
                                            quiz.score = score
                                        }
                                    }
                                    val question = Question(user = user, quizes = Ques)
                                    updateDocumentByUserEmail(currentUser.email.toString(), question)

                                }
                            }
                            .addOnFailureListener { e ->
                                // Handle possible errors
                                Toast.makeText(baseContext, "Error finding document: ${e.message}", Toast.LENGTH_SHORT).show()

                            }

                    }
                } else {
                    Log.w("LoadDatabase", "Ques list is still empty after waiting")
                }
            }
        }.start()


    }

    private fun calculatePhysicalScore(allAnswers: List<String>): Int {
        val metValues = intArrayOf(8, 4, 4, 8, 4, 4)
        var x = 1
        var p = 1
        var sum = 0
        var y = 0
        for(i in allAnswers){
            if((x-1)%3==0 || i.isEmpty()){
                x++
                continue
            }
            p *= i.toInt()
            if(x%3==0){
                sum += p*metValues[y]
                p = 1
                y++
            }
            x++
        }
        return sum
    }

    private fun calculateMentalScore(allAnswers: List<String>): Int {
        var score = 0
        for(ans in allAnswers){
            score += ans.toInt()
        }
        return score
    }

    interface CheckAnswerCallback {
        fun onResult(result: Boolean, answers: List<String>?, score: Int?)
    }


    private fun checkAnswer(index: Int, callback: CheckAnswerCallback) {
        if (currentUser != null) {
            val userEmail = currentUser.email
            val questionsCollection = db.collection("questions2")

            questionsCollection
                .whereEqualTo("user.email", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (document in documents) {
                            val data = document.data
                            val quizes = data["quizes"] as? List<Map<String, Any>>
                            if (quizes != null && quizes.size > index) {
                                val quiz = quizes[index]
                                val answers = quiz["answers"] as? List<String>
                                val score = (quiz["score"] as? Number)?.toInt() ?: 0
                                if (answers != null && answers.isNotEmpty()) {
                                    callback.onResult(true, answers, score)
                                    return@addOnSuccessListener
                                }
                            }
                        }
                    }
                    callback.onResult(false, null, null)
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error getting documents.", exception)
                    callback.onResult(false, null, null)
                }
        } else {
            callback.onResult(false, null, null)
        }
    }



    fun calculateSocialScore(answers: List<String>): Int {
        if (answers.size != 6) {
            throw IllegalArgumentException("Exactly 6 answers are required")
        }

        var score = 0

        // Questions 1-3 are negatively worded
        for (i in 0..2) {
            when (answers[i].lowercase()) {
                "yes", "more or less" -> score += 1
                "no" -> score += 0
                else -> throw IllegalArgumentException("Invalid answer: ${answers[i]}")
            }
        }

        // Questions 4-6 are positively worded
        for (i in 3..5) {
            when (answers[i].lowercase()) {
                "yes" -> score += 0
                "more or less", "no" -> score += 1
                else -> throw IllegalArgumentException("Invalid answer: ${answers[i]}")
            }
        }

        return score
    }


    private fun updateDocumentByUserEmail(email: String, question: Question) {
        val firestore = FirebaseFirestore.getInstance()

        // Query the collection for a document where the nested field "user.email" matches the provided email
        firestore.collection("questions2")
            .whereEqualTo("user.email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // No document found with the given email
                    Toast.makeText(baseContext, "No document found with the provided email", Toast.LENGTH_SHORT).show()
                } else {
                    // There should be only one document per email if email is unique
                    val document = querySnapshot.documents.first()
                    val documentId = document.id
                    // Convert the Question object to a Map
                    val newData = question.toMap()
                    // Update the document with new data
                    firestore.collection("questions2").document(documentId)
                        .set(newData) // Use set() to replace the whole document or update fields
                        .addOnSuccessListener {
                            enableBtn()
                            Toast.makeText(baseContext, "Data Recorded successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(baseContext, "Error Recording Data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(baseContext, "Error finding document: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}