package com.amaxsoftware.ocrplayground.src.ocr

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

class MLKitSymbol: ISymbol {

    override val text: String
    override val recognizedLanguage: String?
    override val confidence: Float?
    override val boundingBox: Rect?

    constructor(element: Text.Symbol) {
        this.text = element.text
        this.recognizedLanguage = element.recognizedLanguage
        this.confidence = element.confidence
        this.boundingBox = element.boundingBox
    }
}

class MLKitElement: IElement {

    override val text: String
    override val recognizedLanguage: String?
    override val confidence: Float?
    override val boundingBox: Rect?
    override val symbols: List<ISymbol>

    constructor(element: Text.Element) {
        this.text = element.text
        this.recognizedLanguage = element.recognizedLanguage
        this.confidence = element.confidence
        this.boundingBox = element.boundingBox
        this.symbols = element.symbols.map { MLKitSymbol(it)}
    }
}

class MLKitLine: ILine {
    override val text: String
    override val recognizedLanguage: String?
    override val confidence: Float?
    override val boundingBox: Rect?
    override val elements: List<IElement>

    constructor(element: Text.Line) {
        this.text = element.text
        this.recognizedLanguage = element.recognizedLanguage
        this.confidence = element.confidence
        this.boundingBox = element.boundingBox
        this.elements = element.elements.map { MLKitElement(it) }
    }
}

class MLKitBlock: IBlock {

    override val text: String
    override val recognizedLanguage: String?
    override val confidence: Float?
    override val boundingBox: Rect?
    override val lines: List<ILine>

    constructor(element: Text.TextBlock) {
        this.text = element.text
        this.recognizedLanguage = element.recognizedLanguage
        this.confidence = null
        this.boundingBox = element.boundingBox
        this.lines = element.lines.map { MLKitLine(it) }
    }
}

class MLKitTextRecognitionResult: IOCRResult {
    private val result: Text

    constructor(result: Text) {
        this.result = result
    }

    override val blocks: List<IBlock> get() = result.textBlocks.map { MLKitBlock(it) }
    override val text: String get() = result.text
}