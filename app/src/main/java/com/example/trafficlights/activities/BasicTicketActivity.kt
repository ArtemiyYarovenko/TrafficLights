package com.example.trafficlights.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.trafficlights.*
import com.example.trafficlights.Utils.hasPermissions
import com.example.trafficlights.`object`.CustomTicketBody
import com.example.trafficlights.api.ApiService
import com.example.trafficlights.background.PollingWorker
import com.example.trafficlights.background.UploadPhotoWorker
import kotlinx.android.synthetic.main.activity_basic_ticket.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class BasicTicketActivity : AppCompatActivity() {
    var userId: String? = null
    var mCurrentPhotoPath: String? = null
    var isImage1Empty:Boolean = true
    var isImage2Empty:Boolean = true
    var isImage3Empty:Boolean = true
    var bigPhoto1:Uri? = null
    var bigPhoto2:Uri? = null
    var bigPhoto3:Uri? = null
    val info = Build.MANUFACTURER
    lateinit var service : LocationManager
    var enabled = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_ticket)
        checkPermissions()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        userId = sharedPreferences.getString(USER_ID, null)!!
        //if(ActivityCompat.checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        //    Utils.getGeolocation(applicationContext)
    }



    // получение миниатюры с камеры, чтобы отобразить на экране
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                //success
                //val extras: Bundle = data.extras!!
               // val image: Bitmap = extras.get("data") as Bitmap
                if (isImage1Empty){
                    //imageView.setImageBitmap(image)
                    imageView.setImageURI(bigPhoto1)
                    imageView.visibility = View.VISIBLE
                    isImage1Empty = false
                } else {
                    if (isImage2Empty){
                        //imageView2.setImageBitmap(image)
                        imageView2.setImageURI(bigPhoto2)
                        imageView2.visibility = View.VISIBLE
                        isImage2Empty = false
                    } else {
                        if (isImage3Empty){
                            //imageView3.setImageBitmap(image)
                            imageView3.setImageURI(bigPhoto3)
                            imageView3.visibility = View.VISIBLE
                            isImage3Empty = false
                            buttonAddPhoto.visibility = View.INVISIBLE
                        }
                    }
                }
            } else {
                //fail
            }
        }
    }

    private fun checkPermissions() {
        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {

        // создание файла с уникальным именем
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        )
        storageDir.mkdirs()
        val image: File = File.createTempFile(
                imageFileName,  /* префикс */
                ".jpg",  /* расширение */
                storageDir /* директория */
        )
        mCurrentPhotoPath = "file:" + image.absolutePath
        return image
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // проверяем, что есть приложение способное обработать интент
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // создать файл для фотографии
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // ошибка, возникшая в процессе создания файла
            }

            // если файл создан, запускаем приложение камеры

            if (isImage1Empty) {
                bigPhoto1 = FileProvider.getUriForFile(
                        this,
                        applicationContext.packageName + ".provider",
                        createImageFile()

                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, bigPhoto1)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } else {
                if (isImage2Empty) {
                    bigPhoto2 = FileProvider.getUriForFile(
                            this,
                            applicationContext.packageName + ".provider",
                            photoFile!!
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, bigPhoto2)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                } else {
                    if (isImage3Empty) {
                        bigPhoto3 = FileProvider.getUriForFile(
                                this,
                                applicationContext.packageName + ".provider",
                                photoFile!!
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, bigPhoto3)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }

    fun clickOnButtonAddPhoto(view: View){
        dispatchTakePictureIntent()
    }

    fun clickOnButtonSendTicket(view: View) {
        val data = Intent()
        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        service = getSystemService(LOCATION_SERVICE) as LocationManager
        enabled = service
            .isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!enabled) {
            Toast.makeText(this, "Включите геолокацию в шторке или настройках телефона", Toast.LENGTH_LONG).show()
        } else {
            //Toast.makeText(this, "Включено!!", Toast.LENGTH_LONG).show()
            if(ActivityCompat.checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                Utils.getGeolocation(applicationContext)

            if (Utils.isNetworkAvailable(applicationContext)) {

                val thread = Thread {
                    try {
                        var w8:Boolean = false
                        while (!w8 && ActivityCompat.checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                            w8 = Utils.isInit()
                        }
                        var location: Location = Utils.location
                        Log.d(DEBUG_TAG, location.latitude.toString())


                        val lat: Double = location.latitude
                        val long: Double = location.longitude
                        val description = descriptionTextView.text.toString()


                        val customTicketBody = CustomTicketBody(userId!!, description, long, lat)
                        val response = ApiService.sendCustomTicket(customTicketBody)
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

                                    val sendTicketWithPhotoWork = OneTimeWorkRequestBuilder<UploadPhotoWorker>()
                                        .setInputData(
                                            workDataOf(
                                            "file1Uri" to bigPhoto1.toString(),
                                            "file2Uri" to bigPhoto2.toString(),
                                            "file3Uri" to bigPhoto3.toString(),
                                            "ticket_id" to message.toInt(),
                                            USER_ID to userId)
                                        )
                                        .build()

                                    WorkManager.getInstance(applicationContext)
                                        .enqueue(sendTicketWithPhotoWork)
                                    Log.d(DEBUG_TAG, "Запущен загрузчик")

                                    val tokenWorkPeriodicRequest = PeriodicWorkRequestBuilder<PollingWorker>(
                                        15, TimeUnit.MINUTES)
                                        .addTag(message)
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
                        setResult(RESULT_OK, data)
                        finish()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                thread.start()




            } else {
                Toast.makeText(this, "Проверьте Интернет соединение", Toast.LENGTH_LONG).show()
            }
        }

    }

}
