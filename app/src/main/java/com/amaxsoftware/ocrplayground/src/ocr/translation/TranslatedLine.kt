package com.amaxsoftware.ocrplayground.src.ocr.translation

import android.graphics.Rect
import com.amaxsoftware.ocrplayground.src.ocr.IElement
import com.amaxsoftware.ocrplayground.src.ocr.ILine

class TranslatedLine : ITranslatedLine {

    constructor(sourceLine: ILine, translatedText: String, isTranslated: Boolean) {
        this.originalText = sourceLine.text
        this.text = translatedText
        this.recognizedLanguage = sourceLine.recognizedLanguage
        this.confidence = sourceLine.confidence
        this.boundingBox = sourceLine.boundingBox
        this.isTranslated = isTranslated
    }

    override val originalText: String
    override val elements: List<IElement> = listOf()
    override val text: String
    override val recognizedLanguage: String?
    override val confidence: Float?
    override val boundingBox: Rect?
    override val isTranslated: Boolean
}