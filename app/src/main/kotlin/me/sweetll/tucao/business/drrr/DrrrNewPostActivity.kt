package me.sweetll.tucao.business.drrr

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.widget.Toolbar
import com.tbruyelle.rxpermissions2.RxPermissions
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.databinding.ActivityDrrrNewPostBinding
import me.sweetll.tucao.extension.toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DrrrNewPostActivity : BaseActivity() {
    lateinit var binding: ActivityDrrrNewPostBinding

    override fun getToolbar(): Toolbar = binding.toolbar

    var currentPhotoPath: String = ""

    companion object {

        const val REQUEST_PICK_IMAGE = 1
        const val REQUEST_CAPTURE_IMAGE = 2

        fun intentTo(context: Context) {
            val intent = Intent(context, DrrrNewPostActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_drrr_new_post)

        binding.galleryBtn.setOnClickListener {
            RxPermissions(this)
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE)
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
