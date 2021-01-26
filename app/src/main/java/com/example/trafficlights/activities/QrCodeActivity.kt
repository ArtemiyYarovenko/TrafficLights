package com.example.trafficlights.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.example.trafficlights.R
import com.example.trafficlights.REQUEST_CAMERA_CODE_PERMISSION
import com.example.trafficlights.USER_ID
import com.example.trafficlights.`object`.QrTicketBody
import com.example.trafficlights.api.ApiService
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.qr_code_activity.*


class QrCodeActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_code_activity)
        cameraSurfaceView.visibility = View.INVISIBLE
        userId = intent.getStringExtra(USER_ID)!!

        textResult.text = getString(R.string.qr_scan_invite)
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
        if(this::detector.isInitialized){
           detector.release()
       }
        if(this::cameraSource.isInitialized) {
            cameraSource.stop()
            cameraSource.release()
        }
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
            REQUEST_CAMERA_CODE_PERMISSION
        )
        cameraSurfaceView.clearFocus()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_CODE_PERMISSION && grantResults.isNotEmpty()) {
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
                val handler = Handler(Looper.getMainLooper())
                handler.post(Runnable {textResult.text = "QR-код успешно отсканирован"  })
                handler.post(Runnable { cameraSource.stop() })
                val hashCode = code.displayValue
                Log.d("debug", hashCode)
                val data:Intent
                data = if (hashCode != null) {
                    val ticketBody = QrTicketBody(hashCode, userId, null )
                    ApiService.sendQrTicket(ticketBody)
                    Intent().apply {
                        putExtra("QR-code scan result", true)
                    }

                } else {
                    Intent().apply {
                        putExtra("QR-code scan result", false)
                    }
                }
                setResult(RESULT_OK, data)
                finish()
            } else {
//                textResult.text = ""
            }


        }
    }

}