package me.sweetll.tucao.business.personal.fragment

import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jph.takephoto.app.TakePhoto
import com.jph.takephoto.app.TakePhotoImpl
import com.jph.takephoto.compress.CompressConfig
import com.jph.takephoto.model.*
import com.jph.takephoto.permission.InvokeListener
import com.jph.takephoto.permission.PermissionManager
import com.jph.takephoto.permission.TakePhotoInvocationHandler
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.home.event.RefreshPersonalEvent
import me.sweetll.tucao.business.personal.PersonalActivity
import me.sweetll.tucao.business.personal.viewmodel.PersonalViewModel
import me.sweetll.tucao.databinding.FragmentPersonalBinding
import me.sweetll.tucao.extension.logD
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class PersonalFragment: BaseFragment(), TakePhoto.TakeResultListener, InvokeListener {
    private lateinit var binding: FragmentPersonalBinding
    private lateinit var viewModel: PersonalViewModel

    private var invokeParam: InvokeParam? = null
    private val takePhoto by lazy {
        val photo = TakePhotoInvocationHandler.of(this).bind(TakePhotoImpl(this, this)) as TakePhoto
        // Compress Config
        val config = CompressConfig.Builder()
                .enableReserveRaw(false) // 是否保留原图
                .create()
        photo.onEnableCompress(config, false)
        // Take Photo Options
        val options = TakePhotoOptions.Builder()
                .setWithOwnGallery(true)
                .setCorrectImage(false)
                .create()
        photo.setTakePhotoOptions(options)
        photo
    }
    private val cropOptions by lazy {
        CropOptions.Builder()
                .setAspectX(1)
                .setAspectY(1)
                .setOutputX(180)
                .setOutputY(180)
                .setWithOwnCrop(false)
                .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePhoto.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        takePhoto.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        takePhoto.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionManager.handlePermissionsResult(activity, type, invokeParam, this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal, container, false)
        viewModel = PersonalViewModel(activity as PersonalActivity, this)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshPersonal(event: RefreshPersonalEvent) {
        viewModel.refresh()
    }

    fun choosePickType() {
        val file = File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg")
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        val imageUri = Uri.fromFile(file)

        takePhoto.onPickFromGalleryWithCrop(imageUri, cropOptions)
    }

    override fun invoke(invokeParam: InvokeParam): PermissionManager.TPermissionType {
        val type = PermissionManager.checkPermission(TContextWrap.of(this), invokeParam.method)
        if (type == PermissionManager.TPermissionType.WAIT) {
            this.invokeParam = invokeParam
        }
        return type
    }

    override fun takeSuccess(result: TResult) {
        val bitmap = BitmapFactory.decodeFile(result.image.compressPath)
        "Get image! width = ${bitmap.width}, height = ${bitmap.height}".logD()
    }

    override fun takeFail(result: TResult?, msg: String?) {

    }

    override fun takeCancel() {

    }
}