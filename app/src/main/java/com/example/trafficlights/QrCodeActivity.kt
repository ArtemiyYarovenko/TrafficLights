package com.example.trafficlights

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.qr_code_activity.*


class QrCodeActivity : AppCompatActivity() {

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_code_activity)
        cameraSurfaceView.visibility = View.INVISIBLE

        textResult.text = "hello"
        if (ContextCompat.checkSelfPermission(
                this@QrCodeActivity,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) {

                askForCameraPermission()
        } else {
            setupControls()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.release()
        cameraSource.stop()
        cameraSource.release()
    }

    private fun setupControls() {
        detector = BarcodeDetector.Builder(this@QrCodeActivity).build()
        cameraSource = CameraSource.Builder(this@QrCodeActivity, detector)
            .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(640, 480)
            .build()
        cameraSurfaceView.holder.addCallback(surfaceCallBack)
        cameraSurfaceView.visibility = View.VISIBLE
        detector.setProcessor(processor)
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@QrCodeActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
        cameraSurfaceView.clearFocus()
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