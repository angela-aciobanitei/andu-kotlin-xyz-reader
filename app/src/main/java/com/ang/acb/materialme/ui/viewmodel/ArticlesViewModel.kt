package com.ang.acb.materialme.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ang.acb.materialme.data.repository.ArticlesRepository
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
}