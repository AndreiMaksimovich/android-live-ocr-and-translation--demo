package com.amaxsoftware.ocrplayground.src.ocr
import android.graphics.Rect
import com.googlecode.tesseract.android.TessBaseAPI
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_BLOCK
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_WORD
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_SYMBOL

class TesseractSymbol(
    override val text: String,
    override val recognizedLanguage: String?,
    override val confidence: Float?,
    override val boundingBox: Rect?
) : ISymbol {}

class TesseractElement(
    override val text: String,
    override val recognizedLanguage: String?,
    override val confidence: Float?,
    override val boundingBox: Rect?,
    override val symbols: List<ISymbol>
) : IElement {}

class TesseractLine(
    override val text: String,
    override val recognizedLanguage: String?,
    override val confidence: Float?,
    override val boundingBox: Rect?,
    override val elements: List<IElement>
) : ILine {}

class TesseractBlock(
    override val text: String,
    override val recognizedLanguage: String?,
    override val confidence: Float?,
    override val boundingBox: Rect?,
    override val lines: List<ILine>
) : IBlock {}

class TesseractTextRecognitionResult: IOCRResult {

    constructor(tessApi: TessBaseAPI) {
        process(tessApi)
    }

    private fun process(tessApi: TessBaseAPI) {
        _text = tessApi.utF8Text

        val blocks = mutableListOf<IBlock>()
        _blocks = blocks

        val ri = tessApi.resultIterator
        ri.begin()

        if (!ri.isAtBeginningOf(RIL_BLOCK)) {
            if (!ri.next(RIL_BLOCK)) {
                return
            }
        }

        do  {
            val lines = mutableListOf<ILine>()
            blocks.add(TesseractBlock(
                ri.getUTF8Text(RIL_BLOCK),
                null,
                ri.confidence(RIL_BLOCK),
                ri.getBoundingRect(RIL_BLOCK),
                lines
            ))
            do {
                val words = mutableListOf<IElement>()
                lines.add(TesseractLine(
                    ri.getUTF8Text(RIL_TEXTLINE),
                    null,
                    ri.confidence(RIL_TEXTLINE),
                    ri.getBoundingRect(RIL_TEXTLINE),
                    words
                ))
                do {
                    val symbols = mutableListOf<ISymbol>()
                    words.add(TesseractElement(
                        ri.getUTF8Text(RIL_WORD),
                        null,
                        ri.confidence(RIL_WORD),
                        ri.getBoundingRect(RIL_WORD),
                        symbols
                    ))
                    do {
                        symbols.add(TesseractSymbol(
                            ri.getUTF8Text(RIL_SYMBOL),
                            null,
                            ri.confidence(RIL_SYMBOL),
                            ri.getBoundingRect(RIL_SYMBOL)
                        ))
                    } while (!ri.isAtFinalElement(RIL_WORD, RIL_SYMBOL) && ri.next(RIL_SYMBOL))
                } while (!ri.isAtFinalElement(RIL_TEXTLINE, RIL_WORD) && ri.next(RIL_WORD))
            } while (!ri.isAtFinalElement(RIL_BLOCK, RIL_TEXTLINE) && ri.next(RIL_TEXTLINE))
        } while (ri.next(RIL_BLOCK))
    }

    private lateinit var _text: String
    private lateinit var _blocks: List<IBlock>

    override val blocks: List<IBlock> get() = _blocks
    override val text: String get() = _text
}