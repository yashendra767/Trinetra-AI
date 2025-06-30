package com.example.trinetraai.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class ResponseData(
    val zone: String,
    val type: String

)
data class RequestData(
    val timestamp: String,
    val lat: Double,
    val lng: Double,
    val ipc_sections: List<String>,
    val area: String,
    val act_category: String,
    val status: String,
    val reporting_station: String
)


interface ApiInterface {
    @POST("/predict")


    suspend fun predict(@Body request: RequestData): ResponseData

}

