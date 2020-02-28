package com.ang.acb.materialme.ui.grid

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.ang.acb.materialme.R
import com.ang.acb.materialme.data.local.Article
import com.ang.acb.materialme.databinding.ArticleItemBinding
import com.ang.acb.materialme.util.GlideApp
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import timber.log.Timber

/**
 * A simple [RecyclerView.ViewHolder] that can bind an [Article] item.
 */
class ArticleViewHolder
private constructor(
    val binding: ArticleItemBinding,
    val articleListener: ArticleItemListener)
    : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(
            parent: ViewGroup,
            articleListener: ArticleItemListener
        ): ArticleViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ArticleItemBinding.inflate(inflater, parent, false)
            return ArticleViewHolder(
                binding,
                articleListener
            )
        }
    }

    fun bind(article: Article) {
        // Bind article data (title, subtitle and thumbnail)
        binding.article = article
        bindArticleThumbnail(article)

        // Set the string value of the article ID as the unique transition name
        // for the image view that will be used in the shared element transition.
        ViewCompat.setTransitionName(
            binding.articleItemThumbnail,
            article.id.toString()
        )

        // Handle article items click events.
        binding.articleItemCardView.setOnClickListener{
            articleListener.onItemClicked(it, adapterPosition)
        }

        // Binding must be executed immediately.
        binding.executePendingBindings()
    }

    private fun bindArticleThumbnail(article: Article) {
        // Set the aspect ratio for this image.
        binding.articleItemThumbnail.setAspectRatio(article.aspectRatio)

        GlideApp.with(binding.root.context)
            // Calling Glide.with() returns a RequestBuilder.
            // By default you get a RequestBuilder<Drawable>, but
            // you can change the requested type using as... methods.
            // For example, asBitmap() returns a RequestListener<Bitmap>.
            .asBitmap()
            .load(article.thumbUrl)
            // Tell Glide not to use its standard crossfade animation.
            .dontAnimate()
            // Display a placeholder until the image is loaded and processed.
            .placeholder(R.drawable.loading_animation)
            // Provide an error placeholder when Glide is unable to load the
            // image. This will be shown for the non-existing-url.
            .error(R.color.photoPlaceholder)
            // Use fallback image resource when the url can be null.
            .fallback(R.color.photoPlaceholder)
            // Keep track of errors and successful image loading.
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    exception: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.d("Image loading failed: %s", exception?.message)
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let { generatePaletteAsync(bitmap = it) }
                    return false
                }
            })
            .into(binding.articleItemThumbnail)
    }

    private fun generatePaletteAsync(bitmap: Bitmap) {
        // Extract prominent colors from an image using a Platte.
        // https://developer.android.com/training/material/palette-colors
        Palette.from(bitmap).generate { palette ->
            val swatch = palette?.let { palette.getDominantColor() }
            swatch?.let {
                binding.articleItemCardView.setCardBackgroundColor(swatch.rgb)
                binding.articleItemCardView.strokeColor = swatch.rgb
            }
        }
    }

    private fun Palette.getDominantColor(): Palette.Swatch? {
        // Extract prominent colors from an image using the Platte class.
        // https://developer.android.com/training/material/palette-colors
        var result = dominantSwatch
        if (vibrantSwatch != null) {
            result = vibrantSwatch
        } else if (mutedSwatch != null) {
            result = mutedSwatch
        }
        return result
    }

}

/**
 * A listener that is attached to all ViewHolders to handle image loading events and clicks.
 */
interface ArticleItemListener {
    fun onItemClicked(rootView: View?, adapterPosition: Int)
    fun onLoadCompleted(adapterPosition: Int)
}