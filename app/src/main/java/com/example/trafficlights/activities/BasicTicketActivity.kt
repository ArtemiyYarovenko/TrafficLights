package com.example.trafficlights.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.trafficlights.*
import com.example.trafficlights.Utils.hasPermissions
import com.example.trafficlights.Utils.isInit
import com.example.trafficlights.background.UploadPhotoWorker
import kotlinx.android.synthetic.main.activity_basic_ticket.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class BasicTicketActivity : AppCompatActivity() {
    var mCurrentPhotoPath: String? = null
    var isImage1Empty:Boolean = true
    var isImage2Empty:Boolean = true
    var isImage3Empty:Boolean = true
    var bigPhoto1:Uri? = null
    var bigPhoto2:Uri? = null
    var bigPhoto3:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_ticket)
        checkPermissions()
        Utils.getGeolocation(this)
    }

    // получение миниатюры с камеры, чтобы отобразить на экране
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                //success
                val extras: Bundle = data.extras!!
                val image: Bitmap = extras.get("data") as Bitmap
                if (isImage1Empty){
                    imageView.setImageBitmap(image)
                    //imageView.setImageURI(bigPhoto1)
                    imageView.visibility = View.VISIBLE
                    isImage1Empty = false
                } else {
                    if (isImage2Empty){
                        imageView2.setImageBitmap(image)
                        imageView2.visibility = View.VISIBLE
                        isImage2Empty = false
                    } else {
                        if (isImage3Empty){
                            imageView3.setImageBitmap(image)
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

    public final fun clickOnButtonAddPhoto(view: View){
        dispatchTakePictureIntent()
    }

    public final fun clickOnButtonSendTicket(view: View) {
        if (isInit()) {
            val location = Utils.location
            Log.d("debug", location.latitude.toString())
        }

        val sendTicketWithPhotoWork = OneTimeWorkRequestBuilder<UploadPhotoWorker>()
                .setInputData(workDataOf(
                    "file1Uri" to bigPhoto1.toString(),
                    "file2Uri" to bigPhoto2.toString(),
                    "file3Uri" to bigPhoto3.toString()))
                .build()

        WorkManager.getInstance()
                .enqueue(sendTicketWithPhotoWork)
        Log.d("debug", "Запущен загрузчик")

    }

}
