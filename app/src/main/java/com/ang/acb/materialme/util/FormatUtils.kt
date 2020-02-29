package com.ang.acb.materialme.util

import android.text.Html
import android.text.Spanned
import android.text.format.DateUtils
import androidx.core.text.HtmlCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {

    fun formatPublishedDate(publishedDate: String): Date? {
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.sss",
            Locale.getDefault()
        )
        return try {
            dateFormat.parse(publishedDate)
        } catch (ex: ParseException) {
            Date()
        }
    }

    fun formatArticleByline(
        publishedDate: Date,
        author: String
    ): Spanned? {
        // See: https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568
        val dateFormat = SimpleDateFormat()
        val startOfEpoch = GregorianCalendar(2, 1, 1)
        val byline: Spanned
        byline = if (!publishedDate.before(startOfEpoch.time)) {
            val source = DateUtils.getRelativeTimeSpanString(
                publishedDate.time,
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL
            ).toString()
            HtmlCompat.fromHtml(
                "$source by <font color='#ffffff'>$author</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        } else {
            // If date is before 1902, just show the string.
            HtmlCompat.fromHtml(
                "${dateFormat.format(publishedDate)} by <font color='#ffffff'>$author</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        return byline
    }
}

