package com.example.androidapp


import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.airbnb.lottie.LottieAnimationView
import com.example.androidapp.databinding.FragmentScannerBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import android.widget.Toast


class FragmentReceiptScanner : Fragment() {
    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var previewView: PreviewView
    private var imageCapture: ImageCapture = ImageCapture.Builder()
        .setJpegQuality(100)
        .build()
    private lateinit var successToast : LottieAnimationView
    private lateinit var warningToast : LottieAnimationView
    private var preview: Preview? = null
    private val REQUEST_CODE_PERMISSIONS = 10

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

    override fun onResume() {
        super.onResume()
        orientationEventListener.enable()
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener.disable()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)

        super.onCreate(savedInstanceState)
        val view = binding.root
        previewView = binding.previewView
        requestCameraPermission()

        successToast = view.findViewById(R.id.processed)
        warningToast = view.findViewById(R.id.warning)
        warningToast.visibility = View.INVISIBLE

        val fab: FloatingActionButton = view.findViewById(R.id.fabCapture)


        binding.fabCapture.setOnClickListener {
            fab.visibility = View.INVISIBLE
            binding.previewView.visibility = View.INVISIBLE
            takePicture()
        }

        fab.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    ObjectAnimator.ofFloat(view, "scaleX", 0.9f).start()
                    ObjectAnimator.ofFloat(view, "scaleY", 0.9f).start()
                }
                MotionEvent.ACTION_UP -> {
                    ObjectAnimator.ofFloat(view, "scaleX", 1f).start()
                    ObjectAnimator.ofFloat(view, "scaleY", 1f).start()
                }
            }
            false
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (
            grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun startCamera() {
        initPreview()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        lifecycleScope.launch {
            val cameraProvider = ProcessCameraProvider
                .getInstance(requireActivity())
                .await()

            try {
                cameraProvider.unbindAll()
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

    private fun takePicture() {
        imageCapture.takePicture(

            ContextCompat.getMainExecutor(requireActivity()),

            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {

                    Log.i("rotation", image.imageInfo.rotationDegrees.toString())

                        val scanner = ReceiptScanner.getInstance();

                        scanner.parseReceiptMediaImage(image) { result ->

                            displayToastMessage(result)

                            image.close()
                        }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("TAG", "Image capture failed ${exception.message}")
                    displayToastMessage(null)
                }
            })
    }

    fun displayToastMessage(result : List<Pair<String, Pair<Double, String>>>?) {
        var navFragment : Fragment = FragmentRecipes();

        var toastMessage : String = getString(R.string.success_toast)

        if (result.isNullOrEmpty()) {
            toastMessage = getString(R.string.warning_toast)

            navFragment = FragmentReceiptScanner()

            warningToast.visibility = View.VISIBLE

            warningToast.playAnimation()

        } else {
            successToast.playAnimation()
        }

        Toast.makeText(requireActivity(), toastMessage, Toast.LENGTH_LONG).show();

        Handler(Looper.getMainLooper()).postDelayed({
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, navFragment).commit()
        }, 2000)
    }
}