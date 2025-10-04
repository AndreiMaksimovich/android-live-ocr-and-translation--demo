package com.amaxsoftware.ocrplayground.src.translation

import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.concurrent.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TranslationServiceMLKit: ITranslationService {
    private val translator: Translator

    constructor(sourceLanguage: String, targetLanguage: String) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        translator = Translation.getClient(options)
    }

    override suspend fun translate(string: String): String {
        return translateAsync(string)
    }

    private suspend fun translateAsync(text: String): String {
        return suspendCoroutine { continuation ->

            if (text.isEmpty()) {
                continuation.resume("")
                return@suspendCoroutine
            }

            translator.translate(text)
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
                .addOnCanceledListener {
                    continuation.resumeWithException(CancellationException())
                }
        }
    }

    override fun close() {
        translator.close()
    }
}