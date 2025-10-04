package com.amaxsoftware.ocrplayground.fragments.mlkittranslationdemo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amaxsoftware.ocrplayground.src.Language
import com.amaxsoftware.ocrplayground.src.SupportedLanguages
import com.amaxsoftware.ocrplayground.src.translation.ITranslationService
import com.amaxsoftware.ocrplayground.src.translation.TranslationServiceFactory
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MLKitTranslationDemoFragmentModel: ViewModel {
    private val LOG_TAG = "MLKTranslationModel"

    private var translationService: ITranslationService? = null
    private var isTranslationInProgress = false
    private var isTranslationUpdateRequired = false

    private val _languageFrom = MutableLiveData<Language>(SupportedLanguages.defaultLanguage)
    val languageFrom: LiveData<Language> = _languageFrom

    private val _languageTo = MutableLiveData<Language>(SupportedLanguages.defaultTranslateToLanguage)
    val languageTo: LiveData<Language> = _languageTo

    val textFrom = MutableLiveData("")
    val textTo = MutableLiveData("")

    constructor(): super() {
        prepareTranslationModelAndTranslate()
    }

    fun setTextFrom(value: String) {
        if (value == textFrom.value) return
        textFrom.value = value
        translate()
    }

    fun setLanguageTo(language: Language) {
        if (language == languageTo.value) return
        _languageTo.value = language
        prepareTranslationModelAndTranslate()
    }

    fun setLanguageFrom(language: Language) {
        if (language == languageFrom.value) return
        _languageFrom.value = language
        prepareTranslationModelAndTranslate()
    }

    private fun prepareTranslationModel() {
        translationService?.close()
        translationService = null
        if (languageFrom.value == languageTo.value) return
        translationService =
                TranslationServiceFactory.createInstance(
            languageFrom.value!!.mlKitTranslationLanguage,
            languageTo.value!!.mlKitTranslationLanguage)
    }

    private fun prepareTranslationModelAndTranslate() {
        prepareTranslationModel()
        translate()
    }

    private fun translate() {
        if (isTranslationInProgress) {
            isTranslationUpdateRequired = true
            return
        }

        if (translationService == null) {
            textTo.postValue(textFrom.value)
            isTranslationUpdateRequired = false
            return
        }

        viewModelScope.launch {
            isTranslationInProgress = true
            isTranslationUpdateRequired = false

            try {
                if (translationService == null) {
                    textTo.postValue(textFrom.value)
                } else{
                    textTo.postValue(translationService!!.translate(textFrom.value!!))
                }
            } catch (ex: Exception) {
                Log.e(LOG_TAG, "Translation failed", ex)
            }

            isTranslationInProgress = false
            if (isTranslationUpdateRequired) {
                translate()
            }
        }
    }

}