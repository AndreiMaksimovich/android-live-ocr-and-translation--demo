package com.amaxsoftware.ocrplayground.fragments.ocrdemo
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amaxsoftware.ocrplayground.src.ImageUtils
import com.amaxsoftware.ocrplayground.src.Language
import com.amaxsoftware.ocrplayground.src.SupportedLanguages
import com.amaxsoftware.ocrplayground.src.ocr.IOCRResult
import com.amaxsoftware.ocrplayground.src.ocr.IOCRService
import com.amaxsoftware.ocrplayground.src.ocr.OCRLibrary
import com.amaxsoftware.ocrplayground.src.ocr.OCRServiceFactory
import com.amaxsoftware.ocrplayground.src.ocr.translation.IOCRResultTranslationService
import com.amaxsoftware.ocrplayground.src.ocr.translation.OCRResultTranslation
import com.amaxsoftware.ocrplayground.src.ocr.translation.OCRResultTranslationServiceFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OCRDemoFragmentModel: ViewModel() {
    private val LOG_TAG = "OCRDemoFragmentModel"

    val state = MutableLiveData<State>(State.NotInitialized)

    var error: Exception? = null
        private set

    val isTranslationEnabled = MutableLiveData(false)
    val languageIn = MutableLiveData(SupportedLanguages.defaultLanguage)
    val translationLanguage = MutableLiveData(SupportedLanguages.defaultTranslateToLanguage)

    val languageArray = SupportedLanguages.languages.values.map { it }.toTypedArray()
    val languageNameArray = languageArray.map { it.name }.toTypedArray()

    val ocrModel = MutableLiveData(OCRLibrary.MLKit)

    val ocrResult = MutableLiveData<IOCRResult>()
    val ocrResultTranslation = MutableLiveData<OCRResultTranslation?>()
    var capturedImageSize: Size = Size(0, 0)

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: Executor? = null
    private val handler = Handler(Looper.getMainLooper())
    private var ocrJob: Job? = null

    private val ocrJobDelay: Long = 100

    private var ocrService: IOCRService? = null
    private var translationService: IOCRResultTranslationService? = null

    fun setIsTranslationEnabled(value: Boolean) {
        if (isTranslationEnabled.value == value) return
        isTranslationEnabled.value = value
        updateServicesAndStartOcrCycleIfPossible()
    }

    fun setOCRModel(model: OCRLibrary) {
        if (model == ocrModel.value) return
        ocrModel.value = model
        updateServicesAndStartOcrCycleIfPossible()
    }

    fun setLanguageIn(language: Language) {
        if (languageIn.value == language) return
        languageIn.value =language
        if (ocrModel.value == OCRLibrary.MLKit && !language.isMLKitSupported) {
            ocrModel.value = OCRLibrary.Tesseract
        }
        updateServicesAndStartOcrCycleIfPossible()
    }

    fun setTranslationLanguage(language: Language) {
        if (translationLanguage.value == language) return
        translationLanguage.value = language
        updateServicesAndStartOcrCycleIfPossible()
    }

    fun initialize(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            state.postValue(State.CameraInitialization)
        } else {
            state.postValue(State.PermissionRequest)
        }
    }

    fun onCameraPermissionGranted() {
        state.postValue(State.CameraInitialization)
    }

    fun onCameraPermissionDenied() {
        state.postValue(State.PermissionDenied)
    }

    fun onCameraInitializationFailed(ex: Exception) {
        error = ex
        state.postValue(State.CameraInitializationError)
    }

    fun onCameraInitialized(cameraProvider: ProcessCameraProvider, imageCapture: ImageCapture, executor: Executor) {
        Log.i(LOG_TAG, "onCameraInitialized()")
        this.cameraProvider = cameraProvider
        this.imageCapture = imageCapture
        this.cameraExecutor = executor
        state.value = State.Running
        updateServicesAndStartOcrCycleIfPossible()
    }

    private fun stopOcrCycle() {
        Log.i(LOG_TAG, "stopOcrCycle()")
        ocrJob?.cancel()
        handler.removeCallbacksAndMessages(null);
    }

    fun onResume() {
        Log.i(LOG_TAG, "onResume()")
        updateServicesAndStartOcrCycleIfPossible()

    }

    fun onPause() {
        Log.i(LOG_TAG, "onPause()")
        stopOcrCycle()
    }

    private fun updateServicesAndStartOcrCycleIfPossible() {
        Log.i(LOG_TAG, "updateServicesAndStartOcrCycleIfPossible()")
        if (state.value != State.Running) return

        stopOcrCycle()

        val sourceLanguage = languageIn.value!!
        val targetLanguage = translationLanguage.value!!

        ocrService = OCRServiceFactory.createInstance(sourceLanguage, ocrModel.value!!)

        if (isTranslationEnabled.value!! && sourceLanguage != targetLanguage) {
            translationService = OCRResultTranslationServiceFactory.getInstance(sourceLanguage.mlKitTranslationLanguage, targetLanguage.mlKitTranslationLanguage)
        }

        startImageCaptureCycle()
    }

    private fun startImageCaptureCycle() {
        Log.i(LOG_TAG, "startImageCaptureCycle()")
        val ocrService = this.ocrService
        val translationService = this.translationService
        ocrJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                var imageProxy: ImageProxy? = null

                try {
                    if (!isActive) {
                        throw CancellationException()
                    }

                    imageProxy = captureImageAsync()
                    capturedImageSize = Size(imageProxy.height, imageProxy.width)

                    if (!isActive) {
                        throw CancellationException()
                    }

                    ocrService?.let {
                        val bitmap = imageProxy.toBitmap()
                        val rotatedBitmap = ImageUtils.rotateBitmap(bitmap, 90)
                        bitmap.recycle()

                        try {
                            val result = ocrService.run(rotatedBitmap)

                            if (!isActive) {
                                throw CancellationException()
                            }

                            var translation: OCRResultTranslation? = null
                            translationService?.let { translationService ->
                                translation = translationService.translate(result)
                            }

                            if (isActive) {
                                ocrResultTranslation.postValue(translation)
                                ocrResult.postValue(result)
                            }

                        } finally {
                            rotatedBitmap.recycle()
                        }
                    }

                } catch (_: CancellationException) {
                    Log.i(LOG_TAG, "ocr cycle: Canceled")
                } catch (ex: ImageCaptureException) {
                    if (isActive) {
                        Log.e(LOG_TAG, "ocr cycle ImageCaptureException", ex)
                    }
                } catch (ex: Exception) {
                    Log.e(LOG_TAG, "ocr cycle", ex)
                } finally {
                    imageProxy?.close()
                }

                delay(ocrJobDelay)
            }
        }
        // Clean up services
        ocrJob?.invokeOnCompletion {
            ocrService?.close()
            translationService?.close()
        }
    }

    private suspend fun captureImageAsync(): ImageProxy {
        return suspendCoroutine { continuation ->
            imageCapture!!.takePicture(
                cameraExecutor!!,
                object : OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        continuation.resume(image)
                    }
                    override fun onError(exc: ImageCaptureException) {
                        continuation.resumeWithException(exc)
                    }
                }
            )
        }
    }

    enum class State {
        NotInitialized,
        PermissionRequest,
        PermissionDenied,
        CameraInitialization,
        Running,
        CameraInitializationError,
        CameraError
    }

}