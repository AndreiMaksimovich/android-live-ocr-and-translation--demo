package com.amaxsoftware.ocrplayground.src

import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import java.io.File

class AppAssets {
    companion object {
        private var _application: Application? = null
        private val appContext: Context get() = _application!!.applicationContext

        lateinit var assetDir: File
        lateinit var assetPath: String
        lateinit var tesseractDataDir: File
        lateinit var tesseractDataPath: String

        fun initialize(application: Application) {
            _application = application

            val context = appContext

            assetDir = context.filesDir
            assetPath = assetDir.absolutePath

            tesseractDataDir = File(context.filesDir, "tessdata")
            tesseractDataPath = tesseractDataDir.absolutePath
        }

        private fun mkdirs() {
            if (!assetDir.exists()) {
                assetDir.mkdirs()
            }
            if (!tesseractDataDir.exists()) {
                tesseractDataDir.mkdirs()
            }
        }

        fun extractAssets() {
            mkdirs()
        }

        fun extractTesseractTrainedDataAsset(language: String) {
            val assetManager = appContext.assets
            val fileName = "${language}.traineddata";
            val targetFile = File(tesseractDataDir, fileName);
            if (!targetFile.exists()) {
                copyFile(assetManager, "tesseract/${fileName}", targetFile)
            }
        }

        private fun copyFile(assetManager: AssetManager, fileName: String, targetFile: File) {
            val inputStream = assetManager.open(fileName)
            val outputStream = targetFile.outputStream()

            inputStream.use { inputStream ->
                outputStream.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            inputStream.close()
            outputStream.close()
        }
    }
}