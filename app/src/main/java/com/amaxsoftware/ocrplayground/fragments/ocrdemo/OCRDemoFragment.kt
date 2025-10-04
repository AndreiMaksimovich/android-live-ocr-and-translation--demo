package com.amaxsoftware.ocrplayground.fragments.ocrdemo

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.amaxsoftware.ocrplayground.R
import com.amaxsoftware.ocrplayground.databinding.FragmentOcrDemoBinding
import com.amaxsoftware.ocrplayground.src.ocr.OCRLibrary

class OCRDemoFragment: Fragment() {
    private val LOG_TAG = "OCRDemoFragment"

    private val maxLiveOcrImageSize = 2048

    private lateinit var binding: FragmentOcrDemoBinding
    private lateinit var model: OCRDemoFragmentModel

    private lateinit var requestPermissionLauncher:  ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        // Camera permission launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted ->
            if (isGranted) {
                model.onCameraPermissionGranted()
            } else {
                model.onCameraPermissionDenied()
            }
        }

        binding = FragmentOcrDemoBinding.inflate(inflater, container, false)
        model = ViewModelProvider(this)[OCRDemoFragmentModel::class.java]

        val context = requireContext()

        // ---- Language in

        binding.ocrDemoLanguageInSpinner.adapter = ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            model.languageNameArray)

        binding.ocrDemoLanguageInSpinner.setSelection(model.languageArray.indexOf(model.languageIn.value!!))
        binding.ocrDemoLanguageInSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.setLanguageIn(model.languageArray[position])
            }
            override fun onNothingSelected(adapter: AdapterView<*>?) {}
        }

        // ---- Translation language

        binding.ocrDemoTranslationLanguageSpinner.adapter = ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            model.languageNameArray)

        binding.ocrDemoTranslationLanguageSpinner.setSelection(model.languageArray.indexOf(model.translationLanguage.value!!))
        binding.ocrDemoTranslationLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.setTranslationLanguage(model.languageArray[position])
            }
            override fun onNothingSelected(adapter: AdapterView<*>?) {}
        }

        // ---- OCR Model

        binding.ocrDemoOcrModel.check(ocrModelToButtonId(model.ocrModel.value!!))

        binding.ocrDemoOcrModel.addOnButtonCheckedListener { group, buttonId, isChecked ->
            if (isChecked) model.setOCRModel(modelButtonToModel(buttonId))
        }

        model.ocrModel.observe(viewLifecycleOwner) {
            val viewId = ocrModelToButtonId(it)
            if (binding.ocrDemoOcrModel.checkedButtonId != viewId) {
                binding.ocrDemoOcrModel.check(ocrModelToButtonId(it))
            }
        }

        model.languageIn.observe(viewLifecycleOwner) {
            binding.ocrDemoModelMLKitButton.isEnabled = it.isMLKitSupported
        }

        // ---- Translation check box

        binding.ocrDemoTranslateCheckbox.setOnCheckedChangeListener { button, isChecked ->
            model.setIsTranslationEnabled(isChecked)
        }

        // ---- State

        model.state.observe(viewLifecycleOwner) {
            onStateChanged(it)
        }

        // ---- OCR Result

        model.ocrResult.observe(viewLifecycleOwner) {
            binding.ocrDemoResultOverlay.apply(model.capturedImageSize, it, model.ocrResultTranslation.value)
        }

        model.initialize(context)

        return binding.root
    }

    private fun onError(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                model.initialize(requireContext())
            }
            .setCancelable(false)
            .show()
    }

    private fun onStateChanged(state: OCRDemoFragmentModel.State) {
        Log.i(LOG_TAG, "onStateChanged state=${state}")

        when (state) {
            OCRDemoFragmentModel.State.NotInitialized -> {}

            OCRDemoFragmentModel.State.PermissionRequest -> requestCameraPermission()
            OCRDemoFragmentModel.State.PermissionDenied -> onError("PermissionDenied")

            OCRDemoFragmentModel.State.CameraInitialization -> startCamera()
            OCRDemoFragmentModel.State.CameraInitializationError -> onError("CameraInitializationError")


            OCRDemoFragmentModel.State.Running -> {}
            OCRDemoFragmentModel.State.CameraError -> onError("CameraError")
        }
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun ocrModelToButtonId(model: OCRLibrary): Int {
        return when (model) {
            OCRLibrary.Tesseract -> R.id.ocrDemo_modelTesseractButton
            OCRLibrary.MLKit -> R.id.ocrDemo_modelMLKitButton
        }
    }

    private fun modelButtonToModel(id: Int): OCRLibrary {
        return when (id) {
            R.id.ocrDemo_modelTesseractButton -> OCRLibrary.Tesseract
            R.id.ocrDemo_modelMLKitButton -> OCRLibrary.MLKit
            else -> throw IllegalArgumentException()
        }
    }

    private fun startCamera() {
        Log.i(LOG_TAG, "startCamera()")

        val context = requireContext()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val resolutionSelector = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy(AspectRatio.RATIO_4_3, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                    .setResolutionFilter { sizes, rotationDegrees ->
                        sizes.filter { it.width <= maxLiveOcrImageSize && it.height <= maxLiveOcrImageSize }
                    }
                    .build()

                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also {
                        it.surfaceProvider = binding.cameraPreview.surfaceProvider
                    }

                val imageCapture = ImageCapture.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        viewLifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    model.onCameraInitialized(cameraProvider, imageCapture, executor)
                } catch (exc: Exception) {
                    model.onCameraInitializationFailed(exc)
                }
            },
            executor)
    }

    override fun onResume() {
        super.onResume()
        model.onResume()
    }

    override fun onPause() {
        super.onPause()
        model.onPause()
    }
}