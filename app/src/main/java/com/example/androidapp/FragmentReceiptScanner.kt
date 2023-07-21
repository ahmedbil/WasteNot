package com.example.androidapp


import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    /*private var activityResultLauncher: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { result ->
        var allAreGranted = true
        for(b in result.values) {
            allAreGranted = allAreGranted && b
        }

        if(allAreGranted) {
            //startCamera()
        }
    }*/


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)

        super.onCreate(savedInstanceState)
        val view = binding.root
        previewView = binding.previewView
        requestCameraPermission()
        //initPreview()
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
                    //get bitmap from image
                    //val bitmap = imageProxyToBitmap(image)
                    //super.onCaptureSuccess(image)

                    Log.i("rotation", image.imageInfo.rotationDegrees.toString())

                    val scanner = ReceiptScanner();
                    //val image = Picasso.get().load("https://ocr.space/Content/Images/receipt-ocr-original.jpg").into(imageview)
                    //Log.i("image-receipt", image.toString())
                    scanner.parseReceiptMediaImage(image);

                    //val bitmap = image.convertImageProxyToBitmap()
                    //val r = rotateImage(bitmap, image.imageInfo.rotationDegrees.toFloat())
                    //binding.imageView.visibility = View.VISIBLE
                    //binding.imageView.setImageBitmap(r)
                    //binding.previewView.visibility = View.INVISIBLE
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("TAG", "Image capture failed ${exception.message}")
                }
            })
    }

    fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }
}