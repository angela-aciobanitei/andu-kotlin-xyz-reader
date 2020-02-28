package com.ang.acb.materialme.ui.grid

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ang.acb.materialme.data.local.Article


/**
 * A custom [ListAdapter] for the [Article] list.
 */
class ArticlesAdapter(
    val itemClickListener: (rootView: View, position: Int) -> Unit,
    val imageLoadListener: (position: Int) -> Unit
) : ListAdapter<Article, ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder.create(parent, itemClickListener, imageLoadListener)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}


/**
 * Callback for calculating the diff between two non-null items in a list. Used by
 * [ListAdapter] to calculate the minimum number of changes between and old list
 * and a new list that's been passed to `submitList`.
 *
 * https://codelabs.developers.google.com/codelabs/kotlin-android-training-diffutil-databinding
 */
class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}