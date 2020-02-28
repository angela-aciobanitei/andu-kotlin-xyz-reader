package com.ang.acb.materialme.data.repository

import androidx.lifecycle.LiveData
import com.ang.acb.materialme.data.local.Article
import com.ang.acb.materialme.data.local.ArticleDao
import com.ang.acb.materialme.data.remote.*
import com.ang.acb.materialme.util.AppExecutors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticlesRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val apiService: ApiService,
    private val articleDao: ArticleDao
) {

    fun loadAllArticles(): LiveData<Resource<List<Article>>> {
        // Note that we are using the NetworkBoundResource<ResultType, RequestType> class
        // that we've created earlier which can provide a resource backed by both the
        // SQLite database and the network. It defines two type parameters, ResultType
        // and RequestType, because the data type used locally might not match the data
        // type returned from the API.
        return object : NetworkBoundResource<List<Article>, List<NetworkArticle>>(appExecutors){
            override fun createCall(): LiveData<ApiResponse<List<NetworkArticle>>> {
                // Create the API call to load the all recipes.
                return apiService.getAllArticles()
            }

            override fun saveCallResult(result: List<NetworkArticle>) {
                // Save the result of the API response into the database.
                articleDao.insertArticles(
                    result.map {
                        it.asDatabaseArticle()
                    }
                )
            }

            override fun shouldFetch(data: List<Article>?): Boolean {
                // Fetch fresh data only if it doesn't exist in database.
                return data == null || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<List<Article>> {
                return articleDao.getAllArticles()
            }
        }.asLiveData()
    }
}