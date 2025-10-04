package com.amaxsoftware.ocrplayground.src

import android.graphics.Bitmap
import android.graphics.Matrix

class ImageUtils {
    companion object {

        fun rotateBitmap(bitmap: Bitmap, angle: Int): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        }

    }
}