package com.amaxsoftware.ocrplayground.src.translation

class TranslationServiceFactory {
    companion object {

        fun createInstance(sourceLanguage: String, targetLanguage: String): ITranslationService {
            return TranslationServiceMLKit(sourceLanguage, targetLanguage)
        }

    }
}