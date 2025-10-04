package com.amaxsoftware.ocrplayground

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amaxsoftware.ocrplayground.databinding.ActivityLoaderBinding

class LoaderActivity: AppCompatActivity() {

    private lateinit var binding: ActivityLoaderBinding
    private val model: LoaderActivityModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.model = model

        // state
        model.state.observe(this) {
            onStateChanged(it)
        }

        // load data
        model.loadData(application)

        // edge to edge nonsense
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, 0)
            insets // Consume the insets
        }
    }

    private fun onStateChanged(state: LoaderActivityModel.State) {
        when(state) {
            LoaderActivityModel.State.Failed -> showError()
            LoaderActivityModel.State.Done -> openMainActivity()
            else -> {  }
        }
    }

    private fun showError() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog
            .apply {
                setTitle("Error")
                setMessage("Initialization failed. Check your internet connection and try again.")
                setPositiveButton("Try Again") {dialog, button ->
                    model.loadData(application)
                }
            }
            .setCancelable(false)
            .create().show()
    }

    private fun openMainActivity() {
        val intent: Intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}