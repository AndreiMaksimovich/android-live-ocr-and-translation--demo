package com.amaxsoftware.ocrplayground.src.ocr
import android.graphics.Bitmap
import com.amaxsoftware.ocrplayground.src.Language
import com.google.android.gms.tasks.CancellationToken
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlin.coroutines.coroutineContext

class OCRServiceMLKit: IOCRService {

    val textRecognizer: TextRecognizer
    private var isRunning = false

    constructor(language: Language) {
        textRecognizer = TextRecognition.getClient(getTextRecognitionOptions(language));
    }

    private fun getTextRecognitionOptions(language: Language): TextRecognizerOptionsInterface {
        val mlKitModel = language.mlKitModel!!

        if (mlKitModel == TextRecognizerOptions.LATIN_AND_JAPANESE) {
            return JapaneseTextRecognizerOptions.Builder().build();
        }

        if (mlKitModel == TextRecognizerOptions.LATIN_AND_KOREAN) {
            return KoreanTextRecognizerOptions.Builder().build();
        }

        if (mlKitModel == TextRecognizerOptions.LATIN_AND_CHINESE) {
            return ChineseTextRecognizerOptions.Builder().build();
        }

        if (mlKitModel == TextRecognizerOptions.LATIN_AND_DEVANAGARI) {
            return DevanagariTextRecognizerOptions.Builder().build();
        }

        return TextRecognizerOptions.DEFAULT_OPTIONS
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
            bitmap.recycle()
            isRunning = false
        }
    }

    private suspend fun processImage(bitmap: Bitmap): IOCRResult {
        return suspendCoroutine { continuation ->
            try {
                textRecognizer.process(bitmap, 0)
                    .addOnSuccessListener {
                        continuation.resume(MLKitTextRecognitionResult(it))
                    }
                    .addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
                    .addOnCanceledListener {
                        continuation.resumeWithException(CancellationException())
                    }
            } catch (ex: Exception) {
                continuation.resumeWithException(ex)
            }
        }
    }

    override fun close() {
        textRecognizer.close()
    }
}