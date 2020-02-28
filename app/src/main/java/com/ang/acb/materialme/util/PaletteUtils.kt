package com.ang.acb.materialme.util

import androidx.palette.graphics.Palette

fun Palette.getDominantColor(): Palette.Swatch? {
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