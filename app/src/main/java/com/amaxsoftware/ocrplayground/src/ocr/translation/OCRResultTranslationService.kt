package com.amaxsoftware.ocrplayground.src.ocr.translation

import com.amaxsoftware.ocrplayground.src.ocr.IOCRResult
import com.amaxsoftware.ocrplayground.src.translation.ITranslationService

class OCRResultTranslationService: IOCRResultTranslationService {

    private val translationService: ITranslationService

    constructor(translationService: ITranslationService) {
        this.translationService = translationService
    }

    override suspend fun translate(
        result: IOCRResult,
        minConfidence: Float,
    ): OCRResultTranslation {
        val resultLines = mutableListOf<ITranslatedLine>()

        for (block in result.blocks) {
            for (line in block.lines) {
                if (line.confidence!! >= minConfidence) {
                    val translation = translationService.translate(line.text)
                    resultLines.add(TranslatedLine(line, translation, translation != line.text))
                } else {
                    resultLines.add(TranslatedLine(line, line.text, false))
                }
            }
        }

        return resultLines
    }

    override fun close() {}
}