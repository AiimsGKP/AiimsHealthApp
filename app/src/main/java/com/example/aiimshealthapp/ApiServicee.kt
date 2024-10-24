package com.example.aiimshealthapp.api

import com.example.aiimshealthapp.models.FoodData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("product/{barcode}.json")
    fun getProductInfo(@Path("barcode") barcode: String): Call<FoodData>
}
