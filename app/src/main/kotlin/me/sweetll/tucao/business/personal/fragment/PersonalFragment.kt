package me.sweetll.tucao.business.personal.fragment

import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
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
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
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
    private val takePhoto: TakePhoto by lazy {
        TakePhotoInvocationHandler.of(this).bind(TakePhotoImpl(this, this)) as TakePhoto
    }

    private fun TakePhoto.config(): TakePhoto {
        // Compress Config
        val config = CompressConfig.Builder()
                .enableReserveRaw(false) // 是否保留原图
                .create()
        onEnableCompress(config, false)
        // Take Photo Options
        val options = TakePhotoOptions.Builder()
                .setWithOwnGallery(true)
                .setCorrectImage(false)
                .create()
        setTakePhotoOptions(options)
        return this
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

    private val uploadAvatarDialog by lazy {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_upload_avatar, null)
        DialogPlus.newDialog(activity)
                .setContentHolder(ViewHolder(view))
                .setGravity(Gravity.CENTER)
                .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentBackgroundResource(R.drawable.bg_round_white_rectangle)
                .setOverlayBackgroundResource(R.color.mask)
                .setCancelable(false)
                .create()
    }

    private val choosePickTypeDialog by lazy {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_choose_pick_type, null)
        DialogPlus.newDialog(activity)
                .setContentHolder(ViewHolder(view))
                .setGravity(Gravity.CENTER)
                .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentBackgroundResource(R.drawable.bg_round_white_rectangle)
                .setOverlayBackgroundResource(R.color.mask)
                .setCancelable(true)
                .setOnClickListener {
                    dialog, view ->

                    val file = File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg")
                    if (!file.parentFile.exists()) {
                        file.parentFile.mkdirs()
                    }
                    val imageUri = Uri.fromFile(file)

                    when (view.id) {
                        R.id.linear_gallery -> {
                            dialog.dismiss()
                            takePhoto.config().onPickFromGalleryWithCrop(imageUri, cropOptions)
                        }
                        R.id.linear_camera -> {
                            dialog.dismiss()
                            takePhoto.config().onPickFromCaptureWithCrop(imageUri, cropOptions)
                        }
                        else -> dialog.dismiss()
                    }
                }
                .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePhoto.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal, container, false)
        viewModel = PersonalViewModel(activity as PersonalActivity, this)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

    fun showUploadingDialog() {
        uploadAvatarDialog.show()
    }

    fun dismissUploadingDialog() {
        uploadAvatarDialog.dismiss()
    }

    fun choosePickType() {
        choosePickTypeDialog.show()
    }

    override fun invoke(invokeParam: InvokeParam): PermissionManager.TPermissionType {
        val type = PermissionManager.checkPermission(TContextWrap.of(this), invokeParam.method)
        if (type == PermissionManager.TPermissionType.WAIT) {
            this.invokeParam = invokeParam
        }
        return type
    }

    override fun takeSuccess(result: TResult) {
        viewModel.uploadAvatar(result.image)
    }

    override fun takeFail(result: TResult?, msg: String?) {

    }

    override fun takeCancel() {

    }
}