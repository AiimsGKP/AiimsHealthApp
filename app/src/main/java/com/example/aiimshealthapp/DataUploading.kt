package com.example.aiimshealthapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DataUploading : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val tag = "CHECK_RESPONSE"
    private val collection = "fact_collection"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_uploading)
        uploadData()
    }

    private fun uploadData() {
        val facts = listOf(
            Fact("Choose a variety of foods in amounts appropriate for age, gender, physiological status, and physical activity.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Prefer fresh, locally available vegetables and fruits in plenty.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Use a combination of whole grains, grams, and greens. Include jaggery or sugar and cooking oils to bridge the calorie or energy gap.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Include in the diets, foods of animal origin such as milk, eggs, and meat, particularly for pregnant and lactating women and children.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Adults should choose low-fat, protein-rich foods such as lean meat, fish, pulses, and low-fat milk.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Develop healthy eating habits and exercise regularly and move as much as you can to avoid a sedentary lifestyle.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Eat more food during pregnancy.","ICMR-National Institute of Nutrition, India","Pregnancy"),
            Fact("Eat more whole grains, sprouted grams, and fermented foods.","ICMR-National Institute of Nutrition, India","Pregnancy"),
            Fact("Take milk/meat/eggs in adequate amounts.","ICMR-National Institute of Nutrition, India","Pregnancy"),
            Fact("Eat plenty of vegetables and fruits.","ICMR-National Institute of Nutrition, India","Pregnancy"),
            Fact("Avoid superstitions and food taboos.","ICMR-National Institute of Nutrition, India","Pregnancy"),
            Fact("Do not use alcohol and tobacco. Take medicines only when prescribed.","ICMR-National Institute of Nutrition, India","Pregnancy"),
            Fact("Take iron, folate, and calcium supplements regularly, after 14-16 weeks of pregnancy, and continue the same during lactation.","ICMR-National Institute of Nutrition, India","Pregnancy"),
            Fact("Start breastfeeding within an hour after delivery and do not discard colostrum.","ICMR-National Institute of Nutrition, India","Breast-feeding"),
            Fact("Breastfeed exclusively (not even water) for a minimum of six months if the growth of the infant is adequate.","ICMR-National Institute of Nutrition, India","Breast-feeding"),
            Fact("Continue breastfeeding in addition to nutrient-rich complementary foods (weaning foods), preferably up to 2 years.","ICMR-National Institute of Nutrition, India","Breast-feeding"),
            Fact("Breastfeed the infant frequently and on demand to establish and maintain a good milk supply.","ICMR-National Institute of Nutrition, India","Breast-feeding"),
            Fact("Take a nutritionally adequate diet both during pregnancy and lactation.","ICMR-National Institute of Nutrition, India","Breast-feeding"),
            Fact("Avoid tobacco (smoking and chewing), alcohol, and drugs during lactation.","ICMR-National Institute of Nutrition, India","Breast-feeding"),
            Fact("Ensure active family support for breastfeeding.","ICMR-National Institute of Nutrition, India","Infant-feeding"),
            Fact("Breast milk alone is not enough for infants after 6 months of age.","ICMR-National Institute of Nutrition, India","Infant-feeding"),
            Fact("Complementary foods should be given after 6 months of age, in addition to breastfeeding.","ICMR-National Institute of Nutrition, India","Infant-feeding"),
            Fact("Do not delay complementary feeding.","ICMR-National Institute of Nutrition, India","Infant-feeding"),
            Fact("Feed low-cost homemade complementary foods.","ICMR-National Institute of Nutrition, India","Infant-feeding"),
            Fact("Feed complementary food on demand 3-4 times a day.","ICMR-National Institute of Nutrition, India","Infant-feeding"),
            Fact("Provide fruits and well-cooked vegetables.","ICMR-National Institute of Nutrition, India","Infant-feeding"),
            Fact("Observe hygienic practices while preparing and feeding the complementary food.","ICMR-National Institute of Nutrition, India","Infant-feeding"),
            Fact("Read nutrition labels on baby foods carefully.","ICMR-National Institute of Nutrition, India","Infant-feeding"),
            Fact("Take extra care in feeding a young child and include soft-cooked vegetables and seasonal fruits.","ICMR-National Institute of Nutrition, India","Child-feeding"),
            Fact("Give plenty of milk and milk products to children and adolescents.","ICMR-National Institute of Nutrition, India","Child-feeding"),
            Fact("Promote physical activity and appropriate lifestyle practices.","ICMR-National Institute of Nutrition, India","Child-feeding"),
            Fact("Discourage overeating as well as indiscriminate dieting.","ICMR-National Institute of Nutrition, India","Child-feeding"),
            Fact("Include green leafy vegetables in the daily diet.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Eat as much of other vegetables as possible daily.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Eat vegetables/fruits in all your meals in various forms (curry, soups, mixed with curd, added to pulse preparations, and rice).","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Consume raw and fresh vegetables as salads.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Grow the family's requirements of vegetables in the kitchen garden if possible.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Green leafy vegetables, when properly cleaned and cooked, are safe even for infants.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Let different varieties of vegetables and fruits add color to your plate and vitality to your life.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Beta-carotene-rich foods like dark green, yellow, and orange-colored vegetables and fruits (GLVs, carrots, papaya, and mangoes) protect from vitamin A deficiency.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Take just enough fat.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Substitute part of visible fat and invisible fat from animal foods with whole nuts.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Limit the use of ghee, butter, especially vanaspati as a cooking oil.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Choose low-fat dairy foods in place of regular whole-fat dairy foods.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Use of reheated fats and oils should be avoided.","ICMR-National Institute of Nutrition, India","Diet"),
            Fact("Slow and steady reduction in body weight is advisable.","ICMR-National Institute of Nutrition, India","Obesity"),
            Fact("Severe fasting may lead to health hazards.","ICMR-National Institute of Nutrition, India","Obesity"),
            Fact("Achieve energy balance and appropriate weight for height.","ICMR-National Institute of Nutrition, India","Obesity"),
            Fact("Encourage regular physical activity.","ICMR-National Institute of Nutrition, India","Obesity"),
            Fact("Eat small meals regularly at frequent intervals.","ICMR-National Institute of Nutrition, India","Obesity"),
            Fact("Cut down on sugar, salt, fatty foods, refined foods, soft drinks, and alcohol.","ICMR-National Institute of Nutrition, India","Obesity"),
            Fact("Eat complex carbohydrates, low glycemic foods, and fiber-rich diets.","ICMR-National Institute of Nutrition, India","Obesity"),
            Fact("Limit fat intake and shift from saturated to unsaturated fats.","ICMR-National Institute of Nutrition, India","Obesity"),
            Fact("Use low-fat milk.","ICMR-National Institute of Nutrition, India","Obesity"),
            Fact("A minimum of 30-45 minutes of brisk walking/physical activity of moderate intensity improves overall health.","ICMR-National Institute of Nutrition, India","Physical Activity"),
            Fact("Include �warm-up� and �cool-down� periods before and after the exercise regimen.","ICMR-National Institute of Nutrition, India","Physical Activity"),
            Fact("Forty-five minutes per day of moderate-intensity physical activity provides many health benefits.","ICMR-National Institute of Nutrition, India","Physical Activity"),
            Fact("Buy food items from reliable sources after careful examination.","ICMR-National Institute of Nutrition, India","Food Hygiene"),
            Fact("Wash vegetables and fruits thoroughly before use.","ICMR-National Institute of Nutrition, India","Food Hygiene"),
            Fact("Store the raw and cooked foods properly and prevent microbial, rodent, and insect invasion.","ICMR-National Institute of Nutrition, India","Food Hygiene"),
            Fact("Refrigerate perishable food items.","ICMR-National Institute of Nutrition, India","Food Hygiene"),
            Fact("Maintain good personal hygiene and keep the cooking and food storage areas clean and safe.","ICMR-National Institute of Nutrition, India","Food Hygiene"),
            Fact("Always use thoroughly cleaned utensils for cooking/eating.","ICMR-National Institute of Nutrition, India","Food Hygiene"),
            Fact("Do not wash food grains repeatedly before cooking.","ICMR-National Institute of Nutrition, India","Food Hygiene"),
            Fact("Do not wash vegetables after cutting.","ICMR-National Institute of Nutrition, India","Food Hygiene"),
            Fact("Do not soak the cut vegetables in water for long periods.","ICMR-National Institute of Nutrition, India","Food Hygiene"),
            Fact("Cook foods in vessels covered with lids.","ICMR-National Institute of Nutrition, India","Food Hygiene"),
            Fact("Drink enough safe and wholesome water to meet daily fluid requirements.","ICMR-National Institute of Nutrition, India","Hydration"),
            Fact("Drink boiled water when the safety of the water is in doubt.","ICMR-National Institute of Nutrition, India","Hydration"),
            Fact("Consume at least 250 ml of boiled or pasteurized milk per day.","ICMR-National Institute of Nutrition, India","Hydration"),
            Fact("Drink natural and fresh fruit juices instead of carbonated beverages.","ICMR-National Institute of Nutrition, India","Hydration"),
            Fact("Prefer tea over coffee.","ICMR-National Institute of Nutrition, India","Hydration"),
            Fact("Avoid alcohol. Those who drink should limit their intake.","ICMR-National Institute of Nutrition, India","Alcohol"),
            Fact("Avoid smoking, chewing of tobacco, and tobacco products (Khaini, Zarda, Paan masala) and consumption of alcohol.","ICMR-National Institute of Nutrition, India","General"),
            Fact("Check regularly for blood sugar, lipids, and blood pressure after the age of 30 years at least every 6 months.","ICMR-National Institute of Nutrition, India","General"),
            Fact("Avoid self-medication.","ICMR-National Institute of Nutrition, India","General"),
            Fact("Adopt stress management techniques (Yoga and Meditation).","ICMR-National Institute of Nutrition, India","General"),
            Fact("Brush your teeth twice a day once in the morning and once before bed.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Take a bath twice a day using soap.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Wear only clean and washed clothes.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Wear footwear while outside.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Avoid drinking liquor, smoking, and chewing tobacco.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Consult a doctor at the time of sickness.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Wash your hands with soap and water after using the toilet.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Keep your house and surrounding areas clean.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Use compost pits for dumping biodegradable waste as it can be used as manure.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Do not let water stagnate around your house as it becomes a breeding ground for mosquitoes.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Always use a sanitary toilet; do not defecate in the open as it contaminates the land and water.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Food items must be stored in airtight containers.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Always keep cooked food covered.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Avoid roadside and unhygienic food.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Do not store cooked food for a long time, especially milk products and non-vegetarian food.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Safe drinking water is important to prevent waterborne diseases, so drink only boiled water.","ICMR-National Institute of Nutrition, India","Hygiene"),
            Fact("Vegetables and fruits should be washed well before cooking.","ICMR-National Institute of Nutrition, India","Hygiene")
        )
        val data = mapOf("facts" to facts)
        if (currentUser != null) {
            db.collection(collection)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Get the first document if the collection is not empty
                        val document = querySnapshot.documents.first()
                        val documentId = document.id
                        db.collection(collection).document(documentId)
                            .set(data)
                            .addOnSuccessListener {
                                Log.i(tag, "Success!!")
                            }
                    } else {
                        // Handle the case where the collection is empty
                        Log.i(tag, "No documents found in the collection.")
                        val documentId = "111"
                        db.collection(collection).document(documentId)
                            .set(data)
                            .addOnSuccessListener {
                                Log.i(tag, "Success!!")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "Error getting documents: ", exception)
                }
        }
    }

}