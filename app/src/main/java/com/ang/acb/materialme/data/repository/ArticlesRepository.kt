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
        return object : NetworkBoundResource<List<Article>, List<NetworkArticle>>(appExecutors){
            override fun createCall(): LiveData<ApiResponse<List<NetworkArticle>>> {
                return apiService.getAllArticles()
            }

            override fun saveCallResult(result: List<NetworkArticle>) {
                articleDao.insertArticles(
                    result.map { it.asDatabaseArticle() }
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

    fun getArticleById(id: Long): LiveData<Article> = articleDao.getArticleById(id)
}