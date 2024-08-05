/*
 * Copyright 2024 Takumi Oe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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