package com.amaxsoftware.ocrplayground.src.ocr

import com.amaxsoftware.ocrplayground.src.Language

class OCRServiceFactory {
    companion object {

        fun createInstance(language: Language): IOCRService {
            return if (language.isMLKitSupported) {
                OCRServiceMLKit(language)
            } else {
                OCRServiceTesseract(language)
            }
        }

        fun createInstance(language: Language, library: OCRLibrary): IOCRService {
            return when (library) {
                OCRLibrary.Tesseract -> OCRServiceTesseract(language)
                OCRLibrary.MLKit -> OCRServiceMLKit(language)
            }
        }

    }
}