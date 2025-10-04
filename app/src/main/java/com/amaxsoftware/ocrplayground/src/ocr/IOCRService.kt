package com.amaxsoftware.ocrplayground.src.ocr

import android.graphics.Bitmap
import com.google.android.gms.tasks.CancellationToken

interface IOCRService {
    suspend fun run(bitmap: Bitmap): IOCRResult
    fun close()
}