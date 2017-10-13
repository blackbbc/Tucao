package me.sweetll.tucao.business.drrr

import android.Manifest
import android.accounts.NetworkErrorException
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.widget.Toolbar
import com.tbruyelle.rxpermissions2.RxPermissions
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.BuildConfig
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.databinding.ActivityDrrrNewPostBinding
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.di.service.JsonApiService
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.widget.RichTextEditor
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DrrrNewPostActivity : BaseActivity() {
    lateinit var binding: ActivityDrrrNewPostBinding

    override fun getToolbar(): Toolbar = binding.toolbar

    var currentPhotoPath: String = ""

    @Inject
    lateinit var jsonApiService: JsonApiService

    companion object {

        const val REQUEST_PICK_IMAGE = 1
        const val REQUEST_CAPTURE_IMAGE = 2

        fun intentTo(context: Context) {
            val intent = Intent(context, DrrrNewPostActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        AppApplication.get()
                .getApiComponent()
                .inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_drrr_new_post)

        binding.galleryBtn.setOnClickListener {
            RxPermissions(this)
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe {
                        granted ->
                        if (granted) {
                            val intent = Intent()
                            intent.type = "image/*"
                            intent.action = Intent.ACTION_GET_CONTENT
                            startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_PICK_IMAGE)
                        } else {
                            "请给予读取SD卡权限".toast()
                        }
                    }
        }

        binding.cameraBtn.setOnClickListener {
            RxPermissions(this)
                    .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe{
                        granted ->
                        if (granted) {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            val photoFile = createImageFile()
                            if (intent.resolveActivity(packageManager) != null) {
                                val photoURI = FileProvider.getUriForFile(this, "me.sweetll.tucao.fileprovider", photoFile)
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                startActivityForResult(intent, REQUEST_CAPTURE_IMAGE)
                            }
                        } else {
                            "请给予相应权限".toast()
                        }
                    }
        }

        binding.sendBtn.setOnClickListener {
            send(binding.editor.text.toString(), binding.editor.getFiles())
        }
    }

    private fun send(content: String, files: List<Pair<String, File>>) {
        if (content.isNotEmpty()) {
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
                    .addFormDataPart("content", content)
                    .addFormDataPart("brand", Build.BRAND)
                    .addFormDataPart("model", Build.MODEL)
                    .addFormDataPart("systemVersion", Build.VERSION.RELEASE)
                    .addFormDataPart("appVersion", BuildConfig.VERSION_NAME)
            val mediaType = MediaType.parse("multipart/form-data")
            files.forEach {
                pair ->
                val index = pair.first
                val file = pair.second
                val fileBody = RequestBody.create(mediaType, file)
                builder.addFormDataPart(index, file.name, fileBody)
            }
            val body = builder.build()

            jsonApiService.drrrCreatePost(body)
                    .bindToLifecycle(this)
                    .retryWhen(ApiConfig.RetryWithDelay())
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        response ->
                        if (response.code == 0) {
                            Observable.just(Any())
                        } else {
                            Observable.error(NetworkErrorException(response.msg))
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        "发射成功".toast()
                        finish()
                    }, {
                        error ->
                        error.printStackTrace()
                        error.message?.toast()
                    })

        } else {
            "内容不得为空！".logD()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = image.absolutePath
        return image
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val result: String
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor == null) {
            result = uri.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
        }
        cursor.close()
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_IMAGE) {
                data?.let {
                    val uri = it.data
                    binding.editor.insertImage(getRealPathFromURI(uri))
                }
            } else if (requestCode == REQUEST_CAPTURE_IMAGE) {
                binding.editor.insertImage(currentPhotoPath)
            }
        } else {
            return super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "新帖"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
