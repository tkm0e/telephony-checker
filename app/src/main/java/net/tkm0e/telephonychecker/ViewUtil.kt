package net.tkm0e.telephonychecker

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object ViewUtil {
    fun setWindowInset(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (v.layoutParams as MarginLayoutParams).apply {
                setMargins(bars.left, bars.top, bars.right, bars.bottom)
            }
            insets
        }
    }
}