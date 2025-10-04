package com.amaxsoftware.ocrplayground.src.ocr.translation

import com.amaxsoftware.ocrplayground.src.ocr.ILine

interface ITranslatedLine: ILine {
    val originalText: String
    val isTranslated: Boolean
}