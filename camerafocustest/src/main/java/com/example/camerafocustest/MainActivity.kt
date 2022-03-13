package com.example.camerafocustest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.camerafocustest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        updateSupportAutoFocusText("")

    }

    private fun updateSupportAutoFocusText(text: String) {
        viewBinding.textViewSupportAutofocus.text =
            getString(R.string.text_view_support_autofocus, text)

    }
}