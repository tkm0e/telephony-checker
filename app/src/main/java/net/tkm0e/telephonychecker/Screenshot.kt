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

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Screenshot {
    private const val FILENAME_PREFIX = "TelephonyChecker_"

    fun take(view: View): Boolean {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            take(view.context, bitmap)
        } else {
            takePieOrOlder(bitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun take(context: Context, bitmap: Bitmap): Boolean {
        val contentResolver = context.contentResolver
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val writeUri = contentResolver.insert(collection, ContentValues().also { values ->
            values.put(MediaStore.Images.Media.DISPLAY_NAME, createFileName())
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            values.put(MediaStore.Images.Media.IS_PENDING, 1)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        })
        if (writeUri == null) {
            return false
        }
        contentResolver.openFileDescriptor(writeUri, "w").use { descriptor ->
            if (descriptor == null) {
                return false
            }
            FileOutputStream(descriptor.fileDescriptor).use { out ->
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                } catch (e: Exception) {
                    return false
                } finally {
                    out.close()
                }
            }
        }
        contentResolver.update(
            writeUri, ContentValues().also { values ->
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null
        )
        return true
    }

    private fun takePieOrOlder(bitmap: Bitmap): Boolean {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() + File.separator + createFileName() + ".png"
        FileOutputStream(path).use { out ->
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            } catch (e: Exception) {
                return false
            } finally {
                out.close()
            }
        }
        return true
    }

    private fun createFileName(): String {
        return FILENAME_PREFIX + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }
}