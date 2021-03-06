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
import androidx.preference.PreferenceManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.trafficlights.*
import com.example.trafficlights.`object`.QrTicketBody
import com.example.trafficlights.api.ApiService
import com.example.trafficlights.background.PollingWorker
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.qr_code_activity.*
import java.util.concurrent.TimeUnit


class QrCodeActivity : AppCompatActivity() {

    lateinit var userId: String
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_code_activity)
        cameraSurfaceView.visibility = View.INVISIBLE
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        userId = sharedPreferences.getString(USER_ID, null)!!

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
                Log.d(DEBUG_TAG, hashCode)
                val data = Intent()
                val metacontext = this@QrCodeActivity
                val context = this

                if (hashCode != null) {
                    val ticketBody = QrTicketBody(hashCode, userId, null )
                    val response = ApiService.sendQrTicket2(ticketBody)
                    if (response.isSuccessful){
                        val body = response.body()
                        val error = body?.error
                        val message = body?.message
                        if (error != null) {
                            data.apply {
                                putExtra(STATUS, false)
                                putExtra(REASON, error)
                            }
                        }
                        if (message != null) {
                            data.apply {
                                putExtra(STATUS, true)
                                putExtra("ticket_id", message)
                                val tokenWorkPeriodicRequest = PeriodicWorkRequestBuilder<PollingWorker>(
                                        15, TimeUnit.MINUTES)
                                        .addTag(message.toString())
                                        .setInputData(workDataOf("Token" to message.toInt()))
                                        .build()

                                WorkManager.getInstance(applicationContext)
                                        .enqueue(tokenWorkPeriodicRequest)
                                Log.d(DEBUG_TAG, "Запущен поллинг")
                            }
                        }
                    } else {
                        //response не успешный 404 503 и т.д
                        data.apply {
                            putExtra(STATUS, false)
                            putExtra(REASON, response.errorBody().toString())
                        }
                    }

                } else {
                    data.apply {
                        putExtra(STATUS, false)
                        putExtra(REASON, "unknown")
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