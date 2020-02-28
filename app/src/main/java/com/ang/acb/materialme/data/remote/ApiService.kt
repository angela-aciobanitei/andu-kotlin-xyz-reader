package com.ang.acb.materialme.data.remote

import androidx.lifecycle.LiveData
import retrofit2.http.GET

/**
 * Defines the REST API access points for Retrofit.
 */
interface ApiService {

    @GET("xyz-reader-json")
    fun getAllArticles(): LiveData<ApiResponse<List<NetworkArticle>>>
}