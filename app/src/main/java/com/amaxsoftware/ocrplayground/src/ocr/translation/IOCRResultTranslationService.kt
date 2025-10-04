package com.amaxsoftware.ocrplayground.src.ocr.translation

import com.amaxsoftware.ocrplayground.src.ocr.IOCRResult

interface IOCRResultTranslationService {
    suspend fun translate(result: IOCRResult, minConfidence: Float = 0f): OCRResultTranslation
    fun close()
}