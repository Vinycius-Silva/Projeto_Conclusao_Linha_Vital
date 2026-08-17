package com.linhavital.app.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

/**
 * Adapta a interface às áreas ocupadas pelo sistema Android sem esconder
 * a barra de navegação, a área de gestos ou a barra de status.
 */
fun View.applySystemBarsPadding(
    top: Boolean = false,
    bottom: Boolean = false,
    left: Boolean = false,
    right: Boolean = false
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val systemGestures = insets.getInsets(WindowInsetsCompat.Type.systemGestures())
        val safeBottom = max(systemBars.bottom, systemGestures.bottom)

        view.setPadding(
            initialLeft + if (left) systemBars.left else 0,
            initialTop + if (top) systemBars.top else 0,
            initialRight + if (right) systemBars.right else 0,
            initialBottom + if (bottom) safeBottom else 0
        )

        insets
    }

    ViewCompat.requestApplyInsets(this)
}
