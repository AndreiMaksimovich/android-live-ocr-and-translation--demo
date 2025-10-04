package com.amaxsoftware.ocrplayground.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Size
import android.view.View
import com.amaxsoftware.ocrplayground.src.ocr.ILine
import com.amaxsoftware.ocrplayground.src.ocr.IOCRResult
import com.amaxsoftware.ocrplayground.src.ocr.translation.OCRResultTranslation
import kotlin.math.max
import kotlin.math.min

class OCRResultOverlay: View {

    private val LOG_TAG = "OCRResultOverlay"

    private val minimalConfidence = 0.6f;

    val rectPaint = Paint()
    var textPaint = Paint()
    private var imageSize: Size? = null
    private var ocrResult: IOCRResult? = null
    private var ocrResultTranslation: OCRResultTranslation? = null

    constructor(context: Context,  attrs: AttributeSet?): super(context, attrs) {
        init()
    }

    fun init() {
        rectPaint.color = Color.GRAY
        textPaint.color = Color.GREEN
        textPaint.textAlign = Paint.Align.CENTER
    }

    fun apply(imageSize: Size, ocrResult: IOCRResult, ocrResultTranslation: OCRResultTranslation?) {
        this.imageSize = imageSize
        this.ocrResult = ocrResult
        this.ocrResultTranslation = ocrResultTranslation
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (imageSize == null || ocrResult == null) return

        val result = ocrResult!!

        val imageWidth = imageSize!!.width.toFloat()
        val imageHeight = imageSize!!.height.toFloat()

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()


        val ratio = if (viewWidth > imageWidth || viewHeight > imageHeight)
        // Scale down
        {
            max(imageWidth/viewWidth, imageHeight/viewHeight)
        }
        // Scale up
        else {
            min(viewWidth/imageWidth, viewHeight/imageHeight)
        }

        val activeAreaWidth = imageWidth * ratio
        val activeAreaHeight = imageHeight * ratio

        canvas.translate((viewWidth - activeAreaWidth)/2f, (viewHeight - activeAreaHeight)/2f)

        @SuppressLint("DrawAllocation")
        fun drawLine(line: ILine) {
            if (line.text.isEmpty() || line.confidence!! < minimalConfidence) return
            val rect = Rect(
                (line.boundingBox!!.left * ratio).toInt(),
                (line.boundingBox!!.top * ratio).toInt(),
                (line.boundingBox!!.right * ratio).toInt(),
                (line.boundingBox!!.bottom * ratio).toInt()
            )
            canvas.drawRect(
                rect,
                rectPaint
            )
            textPaint.textSize = min(
                rect.height().toFloat() * 0.8f,
                rect.width().toFloat()  * 2f / line.text.length.toFloat()
            )

            val fm = textPaint.fontMetrics
            val y: Float = rect.centerY() - (fm.ascent + fm.descent) / 2

            canvas.drawText(
                line.text,
                rect.centerX().toFloat(),
                y,
                textPaint
            )

        }

        if (ocrResultTranslation != null) {
            for (line in ocrResultTranslation) {
                    drawLine(line)
            }
        } else {
            result.blocks.forEach { block ->
                block.lines.forEach { line ->
                    drawLine(line)
                }
            }
        }
    }

}