package com.amaxsoftware.ocrplayground.src.ocr.translation

import com.amaxsoftware.ocrplayground.src.translation.TranslationServiceFactory

class OCRResultTranslationServiceFactory {
    companion object {
        fun getInstance(sourceLanguage: String, targetLanguage: String): IOCRResultTranslationService {
            return OCRResultTranslationService(TranslationServiceFactory.createInstance(sourceLanguage, targetLanguage))
        }
    }
}