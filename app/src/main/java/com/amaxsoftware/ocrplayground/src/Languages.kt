package com.amaxsoftware.ocrplayground.src

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val isMLKitSupported: Boolean,
    val mlKitModel: Int?,
    var isTesseractSupported: Boolean,
    var tesseractLanguageCode: String?,
    var mlKitTranslationLanguage: String
)

class SupportedLanguages {
    companion object {

        val languages= mapOf<String, Language>(
            TranslateLanguage.ENGLISH to Language(TranslateLanguage.ENGLISH, "English", "English", true, TextRecognizerOptions.LATIN, true, "eng", TranslateLanguage.ENGLISH),
            TranslateLanguage.BELARUSIAN to Language(TranslateLanguage.BELARUSIAN, "Belarusian", "Беларускі", false, null, true, "bel", TranslateLanguage.BELARUSIAN),
            TranslateLanguage.GEORGIAN to Language(TranslateLanguage.GEORGIAN, "Georgian", "ქართული", false, null, true, "kat", TranslateLanguage.GEORGIAN),
            TranslateLanguage.POLISH to Language(code = TranslateLanguage.POLISH, "Polish", "Polski", true, TextRecognizerOptions.LATIN, true, "pol", TranslateLanguage.POLISH),
        )

        const val DEFAULT_LANGUAGE_CODE = TranslateLanguage.ENGLISH
        val defaultLanguage: Language = languages[DEFAULT_LANGUAGE_CODE]!!

        const val DEFAULT_TRANSLATION_LANGUAGE_CODE = TranslateLanguage.GEORGIAN
        val defaultTranslateToLanguage: Language = languages[DEFAULT_TRANSLATION_LANGUAGE_CODE]!!

    }
}