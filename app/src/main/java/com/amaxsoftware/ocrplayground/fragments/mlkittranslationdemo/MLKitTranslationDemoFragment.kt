package com.amaxsoftware.ocrplayground.fragments.mlkittranslationdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.amaxsoftware.ocrplayground.databinding.FragmentMlKitTranslationDemoBinding
import com.amaxsoftware.ocrplayground.databinding.FragmentMlKitTranslationDemoLanguageToggleButtonBinding
import com.amaxsoftware.ocrplayground.src.SupportedLanguages

class MLKitTranslationDemoFragment: Fragment() {
    private lateinit var binding: FragmentMlKitTranslationDemoBinding
    private lateinit var model: MLKitTranslationDemoFragmentModel

    private var languageFromToButton = mutableMapOf<String, Button>()
    private var languageToToButton = mutableMapOf<String, Button>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        model = ViewModelProvider(this)[MLKitTranslationDemoFragmentModel::class.java]
        binding = FragmentMlKitTranslationDemoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // ---- Language From

        SupportedLanguages.languages.values.forEach {language ->
            val buttonBinding = FragmentMlKitTranslationDemoLanguageToggleButtonBinding.inflate(inflater, container, false)
            val button = buttonBinding.button
            button.id = View.generateViewId()
            languageFromToButton[language.code] = button
            button.text = language.code
            button.setOnClickListener {
                model.setLanguageFrom(language)
            }
            binding.translationLanguageFrom.addView(buttonBinding.root)
        }

        model.languageFrom.observe(viewLifecycleOwner) { language ->
            binding.translationLanguageFrom.check(languageFromToButton[language.code]!!.id);
        }
        model.setLanguageFrom(SupportedLanguages.defaultLanguage)

        // ---- Language To

        SupportedLanguages.languages.values.forEach {language ->
            val buttonBinding = FragmentMlKitTranslationDemoLanguageToggleButtonBinding.inflate(inflater, container, false)
            val button = buttonBinding.button
            button.id = View.generateViewId()
            languageToToButton[language.code] = button
            button.text = language.code
            button.setOnClickListener {
                model.setLanguageTo(language)
            }
            binding.translationLanguageTo.addView(buttonBinding.root)
        }

        model.languageTo.observe(viewLifecycleOwner) { language ->
            binding.translationLanguageTo.check(languageToToButton[language.code]!!.id);
        }

        model.setLanguageTo(SupportedLanguages.defaultTranslateToLanguage)

        // ---- Text From

        binding.translationTextFrom.addTextChangedListener {
            model.setTextFrom(it.toString())
        }

        model.textFrom.observe(viewLifecycleOwner) {
            if (binding.translationTextFrom.text.toString() != it) {
                binding.translationTextFrom.setText(it)
            }
        }

        // ---- Text To

        model.textTo.observe(viewLifecycleOwner) {
            binding.translationTextTo.setText(it)
        }

        return root
    }

}