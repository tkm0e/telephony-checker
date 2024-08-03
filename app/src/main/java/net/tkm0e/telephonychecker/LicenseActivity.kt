package net.tkm0e.telephonychecker

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LicenseActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_license)
        ViewUtil.setWindowInsetPadding(findViewById(R.id.scroll_wrapper))

        findViewById<TextView>(R.id.section1).text = "LINE Seed"
        findViewById<TextView>(R.id.text1).text = assets.open("OFL.txt").bufferedReader().use { it.readText() }

        findViewById<TextView>(R.id.section2).text = "Material Icons"
        findViewById<TextView>(R.id.text2).text = assets.open("LICENSE.txt").bufferedReader().use { it.readText() }
    }
}