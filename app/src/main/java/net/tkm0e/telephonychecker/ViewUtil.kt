package net.tkm0e.telephonychecker

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

object ViewUtil {
    fun setWindowInset(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (v.layoutParams as MarginLayoutParams).apply {
                setMargins(
                    v.marginLeft + systemBars.left,
                    v.marginTop + systemBars.top,
                    v.marginRight + systemBars.right,
                    v.marginBottom + systemBars.bottom
                )
            }
            ViewCompat.setOnApplyWindowInsetsListener(v, null)
            insets
        }
    }
}