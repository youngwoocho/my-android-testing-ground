package com.example.camerafocustest

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.FocusMeteringAction.FLAG_AF
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.camerafocustest.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var cameraControl: CameraControl? = null

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

        setUpTapToFocus()

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

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpTapToFocus() {
        viewBinding.previewView.setOnTouchListener { _: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                MotionEvent.ACTION_UP -> {

                    // If tapped again, cancel previous focus and metering
                    cameraControl?.cancelFocusAndMetering()
                    // Get the MeteringPointFactory from PreviewView
                    val factory = viewBinding.previewView.meteringPointFactory

                    // Create a MeteringPoint from the tap coordinates
                    val point = factory.createPoint(motionEvent.x, motionEvent.y)

                    // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                    val action = FocusMeteringAction.Builder(point, FLAG_AF)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS).build()

                    // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                    // is asynchronous. You can use it get notified when the focus is successful or if it fails.

                    try {
                        val result = cameraControl?.startFocusAndMetering(action)
                        result?.addListener({
                            if (result.get().isFocusSuccessful) {
                                updateSupportAutoFocusText("SUCCESS")
                            } else {
                                updateSupportAutoFocusText("FAIL")
                            }

                        }, ContextCompat.getMainExecutor(this))
                    } catch (e: CameraControl.OperationCanceledException) {
                        showToast("Focus cancelled by user!")
                        e.printStackTrace()
                    }

                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }

        }
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
        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(viewBinding.previewView.surfaceProvider)

        val camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)

        cameraControl = camera.cameraControl
    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1000
        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

        private fun allPermissionsGranted(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}