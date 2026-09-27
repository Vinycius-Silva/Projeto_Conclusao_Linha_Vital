package com.linhavital.app.ui.common

import android.animation.ValueAnimator
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.linhavital.app.R
import com.linhavital.app.databinding.ViewBottomNavigationBinding

/** Presentation only: this file never schedules work, navigates or calls the API. */
enum class NavigationTab(val index: Int) { CRITERIOS(0), HOME(1), CONTATOS(2) }

fun ViewBottomNavigationBinding.selectTab(tab: NavigationTab) {
    val items = listOf(btnNavCriterios, btnNavHome, btnNavContatos)
    val icons = listOf(iconNavCriterios, iconNavHome, iconNavContatos)
    val labels = listOf(labelNavCriterios, labelNavHome, labelNavContatos)
    items.forEachIndexed { index, item ->
        val selected = index == tab.index
        item.isSelected = selected
        icons[index].setColorFilter(ContextCompat.getColor(item.context,
            if (selected) R.color.lv_on_brand else R.color.lv_brand))
        labels[index].setTextColor(ContextCompat.getColor(item.context,
            if (selected) R.color.lv_ink else R.color.lv_muted))
        labels[index].setTypeface(labels[index].typeface,
            if (selected) Typeface.BOLD else Typeface.NORMAL)
    }
    navigationWave.selectedIndex = tab.index
}

/** Contents change immediately; animation never delays a click or business operation. */
fun View.revealPage() {
    animate().cancel()
    alpha = 1f
    translationX = 0f
    if (!ValueAnimator.areAnimatorsEnabled()) return
    alpha = 0.6f
    translationX = 12f * resources.displayMetrics.density
    animate()
        .alpha(1f)
        .translationX(0f)
        .setDuration(resources.getInteger(R.integer.lv_motion_medium).toLong())
        .setInterpolator(DecelerateInterpolator())
        .withEndAction { alpha = 1f; translationX = 0f }
        .start()
}

fun TextView.showFormError(message: String?) {
    text = message.orEmpty()
    visibility = if (message.isNullOrBlank()) View.GONE else View.VISIBLE
}

fun TextView.clearErrorWhenEditing(vararg inputs: EditText) {
    inputs.forEach { input -> input.doAfterTextChanged { showFormError(null) } }
}

/** Change emphasis only; do not rewrite names, labels or their capitalization. */
fun TextView.accentLastWord() {
    val value = text.toString()
    val start = value.lastIndexOf(' ') + 1
    if (start <= 0 || start >= value.length) return
    text = SpannableString(value).apply {
        setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.lv_brand)),
            start, value.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun TextView.accentGreeting() {
    val value = text.toString()
    val comma = value.indexOf(',')
    if (comma < 0 || comma + 1 >= value.length) return
    text = SpannableString(value).apply {
        setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.lv_brand)),
            comma + 1, value.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
