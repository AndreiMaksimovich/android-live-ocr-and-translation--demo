package com.amaxsoftware.ocrplayground.src.translation

interface ITranslationService {

    suspend fun translate(string: String): String

    fun close()

}