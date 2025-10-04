package com.amaxsoftware.ocrplayground.src.ocr

import android.graphics.Rect

interface IBaseElement {
    val text: String
    val recognizedLanguage: String?
    val confidence: Float?
    val boundingBox: Rect?
}

interface ISymbol: IBaseElement {}

interface IElement: IBaseElement {
    val symbols: List<ISymbol>
}

interface ILine: IBaseElement {
    val elements: List<IElement>
}

interface IBlock: IBaseElement {
    val lines: List<ILine>
}

interface IOCRResult {
    val blocks: List<IBlock>
    val text: String
}