package com.example.trafficlights

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textResult.text = "hello"
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) {
            askForCameraPermission()
        } else {
            setupControls()
        }
    }

    private fun setupControls() {
        detector = BarcodeDetector.Builder(this@MainActivity).build()
        cameraSource = CameraSource.Builder(this@MainActivity, detector)
            .setAutoFocusEnabled(true)
            .build()
        cameraSurfaceView.holder.addCallback(surfaceCallBack)
        detector.setProcessor(processor)
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val surfaceCallBack = object  : SurfaceHolder.Callback{
        @SuppressLint("MissingPermission")
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                cameraSource.start(holder)
            }catch (exception: Exception) {
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG)
                    .show()
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            cameraSource.stop()
        }
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {

            if (detections != null && detections.detectedItems.isNotEmpty()) {
                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                val code = qrCodes.valueAt(0)
                //Toast.makeText(this@MainActivity, code.displayValue, Toast.LENGTH_LONG)
                val handler = Handler(Looper.getMainLooper())
                handler.post(Runnable {textResult.text = code.displayValue  })
                handler.post(Runnable { cameraSource.stop() })

            } else {
//                textResult.text = ""
            }


        }
    }

}