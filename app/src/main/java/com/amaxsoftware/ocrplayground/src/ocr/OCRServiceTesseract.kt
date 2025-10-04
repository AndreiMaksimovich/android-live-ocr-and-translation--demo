package com.amaxsoftware.ocrplayground.src.ocr

import android.graphics.Bitmap
import com.amaxsoftware.ocrplayground.src.AppAssets
import com.amaxsoftware.ocrplayground.src.Language
import com.google.android.gms.tasks.CancellationToken
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OCRServiceTesseract: IOCRService {
    private val tessApi: TessBaseAPI = TessBaseAPI()
    private var isRunning = false

    constructor(language: Language) {
        tessApi.init(AppAssets.assetPath, language.tesseractLanguageCode, TessBaseAPI.OEM_TESSERACT_LSTM_COMBINED)
    }

    override suspend fun run(
        bitmap: Bitmap
    ): IOCRResult {
        if (isRunning) {
            throw CancellationException()
        }

        isRunning = true

        try {
            return processImage(bitmap)
        } catch (ex: Exception) {
            throw ex
        } finally {
            isRunning = false
        }
    }

    private suspend fun processImage(bitmap: Bitmap): IOCRResult {
        return suspendCoroutine { continuation ->
            try {
                tessApi.setImage(bitmap)
                tessApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT)
                continuation.resume(TesseractTextRecognitionResult(tessApi))
            } catch (ex: Exception) {
                continuation.resumeWithException(ex)
            }
        }
    }

    override fun close() {
        tessApi.clear()
    }

}