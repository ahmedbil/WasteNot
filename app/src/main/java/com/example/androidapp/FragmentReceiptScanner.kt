package com.example.androidapp


import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.concurrent.futures.await
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.androidapp.databinding.FragmentScannerBinding
import kotlinx.coroutines.launch


class FragmentReceiptScanner : Fragment() {
    private var _binding: FragmentScannerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private var preview: Preview? = null
    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)

    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireActivity()) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture.targetRotation = rotation
            }
        }
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)

        super.onCreate(savedInstanceState)
        val view = binding.root
        previewView = binding.previewView
        requestCameraPermission()
        binding.fabCapture.setOnClickListener {
            takePicture()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (
            grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun startCamera() {
        initPreview()
        initImageCapture()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        lifecycleScope.launch {
            val cameraProvider = ProcessCameraProvider
                .getInstance(requireActivity())
                .await()

            try {
                cameraProvider.unbindAll() // unbind all usecases
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("TAG", "Error: binding usecases $e")
            }
        }

        }

        private fun initPreview() {
            preview = Preview.Builder().build()
            preview?.setSurfaceProvider(previewView.surfaceProvider)
        }

        private fun initImageCapture() {
            imageCapture = ImageCapture.Builder()
                .setJpegQuality(100)
                .build()
        }

    private fun takePicture() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireActivity()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {

                    Log.i("rotation", image.imageInfo.rotationDegrees.toString())

                    val scanner = ReceiptScanner();

                    scanner.parseReceiptMediaImage(image);

                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("TAG", "Image capture failed ${exception.message}")
                }
            })
    }
}