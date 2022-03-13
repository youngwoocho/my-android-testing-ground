package com.example.camerafocustest

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.camerafocustest.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (!allPermissionsGranted(this)) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_REQUIRED,
                REQUEST_CODE_CAMERA_PERMISSION
            )
        } else {
            setUpCamera()
        }


        updateSupportAutoFocusText("")

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (allPermissionsGranted(this)) {
                setUpCamera()
            } else {
                showToast(getString(R.string.camera_permission_not_granted))
                finish()
            }
        }
    }

    private fun Activity.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateSupportAutoFocusText(text: String) {
        viewBinding.textViewSupportAutofocus.text =
            getString(R.string.text_view_support_autofocus, text)

    }

    private fun setUpCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        var preview = Preview.Builder().build()
        var cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(viewBinding.previewView.surfaceProvider)

        var camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)

    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1000
        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

        private fun allPermissionsGranted(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}