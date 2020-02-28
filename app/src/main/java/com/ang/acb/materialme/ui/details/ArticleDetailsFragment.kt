package com.ang.acb.materialme.ui.details

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.palette.graphics.Palette
import com.ang.acb.materialme.R
import com.ang.acb.materialme.data.local.Article
import com.ang.acb.materialme.databinding.FragmentArticleDetailsBinding
import com.ang.acb.materialme.ui.viewmodel.ArticlesViewModel
import com.ang.acb.materialme.util.GlideApp
import com.ang.acb.materialme.util.Utils
import com.ang.acb.materialme.util.autoCleared
import com.ang.acb.materialme.util.getDominantColor
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import timber.log.Timber
import java.util.*
import javax.inject.Inject


private const val ARG_ARTICLE_ID = "ARG_ARTICLE_ID"
private const val INVALID_ARTICLE_ID = -1

class ArticleDetailsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // Note: multiple fragments can share a ViewModel using their activity scope.
    // https://developer.android.com/topic/libraries/architecture/viewmodel#sharing
    private val viewModel: ArticlesViewModel by activityViewModels { viewModelFactory }
    private var binding: FragmentArticleDetailsBinding by autoCleared()

    private var articleId: Long = 0L

    companion object {
        @JvmStatic
        fun newInstance(articleId: Long) =
            ArticleDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ARTICLE_ID, articleId)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            articleId = it.getLong(ARG_ARTICLE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        binding = FragmentArticleDetailsBinding.inflate(inflater, container, false)

        // Set the string value of the article id as the unique transition name for the view.
        ViewCompat.setTransitionName(binding.detailsArticlePhoto, articleId.toString())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupShareFab()
        observeCurrentArticle()

    }

    private fun setupToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.detailsToolbar)
        binding.detailsToolbar.setNavigationOnClickListener {
            // Attempts to navigate up in the navigation hierarchy.
            findNavController(this).navigateUp()

            (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    private fun setToolbarTitleIfCollapsed(article: Article) {
        // To set the toolbar title only when the toolbar is collapsed,
        // we need to add an OnOffsetChangedListener to AppBarLayout to determine
        // when CollapsingToolbarLayout is collapsed or expanded.
        binding.detailsAppBar.addOnOffsetChangedListener(
            object : AppBarLayout.OnOffsetChangedListener {
                var isShown = true
                var totalScrollRange = -1

                override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                    // Init total scroll range
                    if (totalScrollRange == -1) {
                        totalScrollRange = appBarLayout.totalScrollRange
                    }

                    // If toolbar is completely collapsed, set the collapsing bar title.
                    if (totalScrollRange + verticalOffset == 0) {
                        binding.detailsCollapsingToolbar.title = article.title
                        isShown = true
                    } else if (isShown) {
                        // When toolbar is expanded, display an empty string.
                        binding.detailsCollapsingToolbar.title = " "
                        isShown = false
                    }
                }
        })
    }

    private fun setupShareFab() {
        binding.articleDetails.shareFab.setOnClickListener {
            startActivity(
                Intent.createChooser(
                    ShareCompat.IntentBuilder.from(requireActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .intent, getString(R.string.action_share)
                )
            )
        }
    }

    private fun observeCurrentArticle() {
        viewModel.getArticleById(articleId).observe(viewLifecycleOwner, Observer {
            populateUi(it)
        })
    }

    private fun populateUi(article: Article) {
        setToolbarTitleIfCollapsed(article)

        binding.article = article

        binding.articleDetails.articleTitle.text = article.title
        binding.articleDetails.articleByline.text =
            article.publishedDate?.let {
                Utils.formatPublishedDate(it)?.let {
                    Utils.formatArticleByline(
                        publishedDate = it,
                        author = article.author
                    )
                }
            }

        binding.articleDetails.articleBody.text = Html.fromHtml(article.body
                // Careful: this can trigger an IndexOutOfBoundsException.
                .substring(0, 1000)
                .replace("\r\n\r\n", "<br /><br />")
                .replace("\r\n", " ")
                .replace(" {2}", "")
        )

        binding.articleDetails.readMoreButton.setOnClickListener {
            binding.articleDetails.readMoreButton.visibility = View.GONE
            binding.articleDetails.articleBody.text = Html.fromHtml(article.body
                .replace("\r\n\r\n", "<br /><br />")
                .replace("\r\n", " ")
                .replace(" {2}", "")
            )
        }

        bindArticlePhoto(article)
    }

    private fun bindArticlePhoto(article: Article) {
        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(article.thumbUrl)
            .dontAnimate()
            .placeholder(R.drawable.loading_animation)
            .error(R.color.photoPlaceholder)
            .fallback(R.color.photoPlaceholder)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    exception: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.d("Image loading failed: %s", exception?.message)
                    schedulePostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let {
                        generatePaletteAsync(bitmap = it)
                    }
                    schedulePostponedEnterTransition()
                    return false
                }
            })
            .into(binding.detailsArticlePhoto)
    }


    private fun schedulePostponedEnterTransition() {
        // Before calling startPostponedEnterTransition(), make sure that the view is drawn.
        binding.root.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                    // The postponeEnterTransition() is called on the parent, that is
                    // ArticlesPagerFragment, so the startPostponedEnterTransition()
                    // should also be called on the parent to get the transition going.
                    parentFragment?.startPostponedEnterTransition()
                    return true
                }
            }
        )
    }

    private fun generatePaletteAsync(bitmap: Bitmap) {
        // Extract prominent colors from an image using a Platte.
        // https://developer.android.com/training/material/palette-colors
        Palette.from(bitmap).generate { palette ->
            val swatch = palette?.let { palette.getDominantColor() }
            swatch?.let {
                binding.articleDetails.metaBar.setBackgroundColor(swatch.rgb)
                binding.detailsCollapsingToolbar.setContentScrimColor(swatch.rgb)
            }
        }
    }

}
