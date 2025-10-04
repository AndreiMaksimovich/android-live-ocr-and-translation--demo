package com.amaxsoftware.ocrplayground

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amaxsoftware.ocrplayground.src.AppAssets
import com.amaxsoftware.ocrplayground.src.Language
import com.amaxsoftware.ocrplayground.src.SupportedLanguages
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LoaderActivityModel: ViewModel() {
    val log = MutableLiveData("")
    val state = MutableLiveData<State>(State.NotInitialized)

    var errorMessage: String? = null
        private set
    var error: Exception? = null
        private set

    private fun appendLogLine(message: String) {
        log.value = log.value + "$message\n"
    }

    fun loadData(application: Application) {
        viewModelScope.launch {
            state.postValue(State.Loading)

            try {
                appendLogLine(" >> >> Initialization")

                appendLogLine(" >> Preparing Assets")
                AppAssets.initialize(application)
                AppAssets.extractAssets()

                appendLogLine(" >> Extracting Tesseract Assets")
                for (language in SupportedLanguages.languages.values) {
                    if (language.isTesseractSupported) {
                        appendLogLine("Extracted: ${language.tesseractLanguageCode}")
                        AppAssets.extractTesseractTrainedDataAsset(language.tesseractLanguageCode!!)
                    }
                }

                appendLogLine(" >> Downloading ML Kit Translation Models ")
                for (language in SupportedLanguages.languages.values) {
                    loadMLKitTranslationLanguage(language)
                    appendLogLine("Downloaded: ${language.code}")
                }

                state.postValue(State.Done)
            } catch (e: Exception) {
                error = e
                errorMessage = e.message
                state.postValue(State.Failed)
            }
        }
    }

    private suspend fun loadMLKitTranslationLanguage(language: Language) {
        return suspendCoroutine { continuation ->
            val modelManager = RemoteModelManager.getInstance()
            val model = TranslateRemoteModel.Builder(language.mlKitTranslationLanguage).build()
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            modelManager.download(model, conditions)
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    enum class State {
        NotInitialized,
        Loading,
        Failed,
        Done
    }

}