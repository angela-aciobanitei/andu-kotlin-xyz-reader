package com.ang.acb.materialme.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ang.acb.materialme.data.repository.ArticlesRepository
import com.ang.acb.materialme.util.Event
import javax.inject.Inject

/**
 * Stores and manages UI-related data in a lifecycle conscious way.
 *
 * See: https://medium.com/androiddevelopers/viewmodels-a-simple-example-ed5ac416317e
 * See: https://medium.com/androiddevelopers/viewmodels-and-livedata-patterns-antipatterns-21efaef74a54
 */
class ArticlesViewModel @Inject constructor(
    private val articlesRepository: ArticlesRepository
): ViewModel() {

    val articles = articlesRepository.loadAllArticles()

    // Handles navigation to the selected article details.
    private val _navigateToArticleDetails = MutableLiveData<Event<Int?>>()
    val navigateToArticleDetails: LiveData<Event<Int?>>
        get() = _navigateToArticleDetails

    fun navigateToArticleDetailsEvent(id: Int?) {
        // Trigger the event by setting a new Event as a new value.
        _navigateToArticleDetails.value = Event(id)
    }
}