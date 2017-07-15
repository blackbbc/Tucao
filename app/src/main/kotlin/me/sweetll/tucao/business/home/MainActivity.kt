package me.sweetll.tucao.business.home

import android.accounts.AccountManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.content.FileProvider
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.archivepatcher.applier.FileByFileV1DeltaApplier
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.BuildConfig
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.login.LoginActivity
import me.sweetll.tucao.business.download.DownloadActivity
import me.sweetll.tucao.business.home.adapter.HomePagerAdapter
import me.sweetll.tucao.business.search.SearchActivity
import me.sweetll.tucao.databinding.ActivityMainBinding
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.di.service.JsonApiService
import me.sweetll.tucao.di.service.RawApiService
import me.sweetll.tucao.extension.formatWithUnit
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.rxdownload.entity.DownloadEvent
import me.sweetll.tucao.rxdownload.entity.DownloadStatus
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.inject.Inject

class MainActivity : BaseActivity() {

    companion object {
        const val NOTIFICATION_ID = 10
    }

    lateinit var binding : ActivityMainBinding
    lateinit var drawerToggle: ActionBarDrawerToggle

    @Inject
    lateinit var jsonApiService: JsonApiService

    @Inject
    lateinit var rawApiService: RawApiService

    lateinit var accountManager: AccountManager

    lateinit var avatarImg: ImageView

    lateinit var usernameText: TextView

    lateinit var updateDialog: DialogPlus

    lateinit var downloadUrl: String

    override fun getToolbar(): Toolbar = binding.toolbar

    override fun getStatusBar(): View? = binding.statusBar

    fun initDialog() {
        val updateView = LayoutInflater.from(this).inflate(R.layout.dialog_update, null)
        val descriptionText = updateView.findViewById(R.id.text_description) as TextView
        descriptionText.movementMethod = ScrollingMovementMethod()
        updateDialog = DialogPlus.newDialog(this)
                .setContentHolder(ViewHolder(updateView))
                .setGravity(Gravity.CENTER)
                .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentBackgroundResource(R.drawable.bg_round_white_rectangle)
                .setOverlayBackgroundResource(R.color.mask)
                .setOnClickListener {
                    dialog, view ->
                    when (view.id) {
                        R.id.btn_cancel -> dialog.dismiss()
                        R.id.btn_full_update -> {
                            // 完整更新
                            fullUpdate()
                            dialog.dismiss()
                        }
                        R.id.btn_save_update -> {
                            // 省流量更新
                            saveUpdate()
                            dialog.dismiss()
                        }
                    }
                }
                .create()
    }

    override fun initView(savedInstanceState: Bundle?) {
        AppApplication.get()
                .getApiComponent()
                .inject(this)
        initDialog()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        accountManager = AccountManager.get(this)

        binding.viewPager.adapter = HomePagerAdapter(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = 6
        binding.tab.setupWithViewPager(binding.viewPager)

        val headerView = binding.navigation.getHeaderView(0)
        avatarImg = headerView.findViewById(R.id.img_avatar) as ImageView
        usernameText = headerView.findViewById(R.id.text_username) as TextView

        Glide.with(this)
                .load(R.drawable.default_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(avatarImg)

        avatarImg.setOnClickListener {
            LoginActivity.intentTo(this)
        }

        checkUpdate(true)
    }

    override fun initToolbar() {
        super.initToolbar()
        setupDrawer()
    }

    fun setupDrawer() {
        binding.navigation.setNavigationItemSelectedListener({
            menuItem ->
            when (menuItem.itemId) {
                R.id.nav_star -> {
                    StarActivity.intentTo(this)
                }
                R.id.nav_play_history -> {
                    PlayHistoryActivity.intentTo(this)
                }
                R.id.nav_download -> {
                    DownloadActivity.intentTo(this)
                }
                R.id.nav_upgrade -> {
                    "检查更新中...".toast()
                    checkUpdate(false)
                }
                R.id.nav_setting -> {
                    "没什么好设置的啦( ﾟ∀ﾟ)".toast()
                }
                R.id.nav_about -> {
                    AboutActivity.intentTo(this)
                }
            }
            binding.drawer.closeDrawers()
            true
        })
        drawerToggle = ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.drawer_open, R.string.drawer_close)
        binding.drawer.addDrawerListener(drawerToggle)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        } else {
            when (item.itemId) {
                android.R.id.home -> {
                    binding.drawer.openDrawer(GravityCompat.START)
                    return true
                }
                R.id.action_search -> {
                    val searchView = getToolbar().findViewById(R.id.action_search)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, searchView,
                            "transition_search_back").toBundle()
                    SearchActivity.intentTo(this, options = options)
                    return true
                }
                else -> {
                    return super.onOptionsItemSelected(item)
                }
            }
        }
    }

    fun fullUpdate() {
        val processor = BehaviorProcessor.create<DownloadEvent>()
        processor.onNext(DownloadEvent(DownloadStatus.READY, 0, 0, "新版本"))
        rawApiService.download(downloadUrl)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    response ->
                    if (response.code() == 200) {
                        Observable.just(response.body())
                    } else {
                        Observable.error(Error(response.message()))
                    }
                }
                .subscribe({
                    body ->
                    processor.onNext(DownloadEvent(DownloadStatus.STARTED, 0, 0, "新版本"))

                    var count = 0
                    var downloadLength = 0L
                    val contentLength = body.contentLength()
                    val data = ByteArray(1024 * 8)

                    try {
                        val inputStream = BufferedInputStream(body.byteStream())
                        val file = File.createTempFile("tucao", ".apk", cacheDir)
                        val outputStream = BufferedOutputStream(file.outputStream())

                        count = inputStream.read(data)
                        while (count != -1) {
                            outputStream.write(data, 0, count)
                            downloadLength += count
                            processor.onNext(DownloadEvent(DownloadStatus.STARTED, downloadLength, contentLength, "新版本"))
                            count = inputStream.read(data)
                        }
                        outputStream.flush()

                        processor.onNext(DownloadEvent(DownloadStatus.COMPLETED, downloadLength, contentLength, "新版本"))

                        inputStream.close()
                        outputStream.close()

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                        val uri: Uri
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        } else {
                            uri = Uri.fromFile(file)
                        }
                        intent.setDataAndType(uri, "application/vnd.android.package-archive")
                        startActivity(intent)
                    } catch (error: Exception) {
                        error.printStackTrace()
                        // TODO: 下载失败
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    // TODO: 下载失败
                })

        val builder = NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
        val notifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        processor.sample(500, TimeUnit.MILLISECONDS)
                .subscribe {
                    event ->
                    // 更新进度
                    when (event.status) {
                        DownloadStatus.READY -> {
                            builder.setContentTitle(event.taskName)
                                    .setContentText("连接中...")
                            notifyMgr.notify(NOTIFICATION_ID, builder.build())
                        }
                        DownloadStatus.COMPLETED -> notifyMgr.cancel(NOTIFICATION_ID)
                        DownloadStatus.STARTED -> {
                            builder.setProgress(event.totalSize.toInt(), event.downloadSize.toInt(), false)
                                .setContentTitle(event.taskName)
                                .setContentText("${event.downloadSize.formatWithUnit()}/${event.totalSize.formatWithUnit()}")
                            notifyMgr.notify(NOTIFICATION_ID, builder.build())
                        }
                    }
                }
    }

    fun saveUpdate() {
        val processor = BehaviorProcessor.create<DownloadEvent>()
        processor.onNext(DownloadEvent(DownloadStatus.READY, 0, 0, "补丁包"))
        rawApiService.download(downloadUrl)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    response ->
                    if (response.code() == 200) {
                        Observable.just(response.body())
                    } else {
                        Observable.error(Error(response.message()))
                    }
                }
                .subscribe({
                    body ->
                    processor.onNext(DownloadEvent(DownloadStatus.STARTED, 0, 0, "补丁包"))

                    var count = 0
                    var downloadLength = 0L
                    val contentLength = body.contentLength()
                    val data = ByteArray(1024 * 8)

                    try {
                        val inputStream = BufferedInputStream(body.byteStream())
                        val file = File.createTempFile("tucao", ".patch", cacheDir)
                        val outputStream = BufferedOutputStream(file.outputStream())

                        count = inputStream.read(data)
                        while (count != -1) {
                            outputStream.write(data, 0, count)
                            downloadLength += count
                            processor.onNext(DownloadEvent(DownloadStatus.STARTED, downloadLength, contentLength, "补丁包"))
                            count = inputStream.read(data)
                        }
                        outputStream.flush()

                        processor.onNext(DownloadEvent(DownloadStatus.COMPLETED, downloadLength, contentLength, "补丁包"))

                        inputStream.close()
                        outputStream.close()

                        // 合成安装包
                        val info = packageManager.getApplicationInfo(packageName, 0)
                        val oldFile = File(info.sourceDir)
                        val patchIn = GZIPInputStream(file.inputStream())
                        val newFile = File.createTempFile("tucao", ".apk", cacheDir)
                        FileByFileV1DeltaApplier().applyDelta(oldFile, patchIn, newFile.outputStream())

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                        val uri: Uri
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", newFile)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        } else {
                            uri = Uri.fromFile(newFile)
                        }
                        intent.setDataAndType(uri, "application/vnd.android.package-archive")
                        startActivity(intent)
                    } catch (error: Exception) {
                        error.printStackTrace()
                        // TODO: 下载失败
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    // TODO: 下载失败
                })

        val builder = NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
        val notifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        processor.sample(500, TimeUnit.MILLISECONDS)
                .subscribe {
                    event ->
                    // 更新进度
                    when (event.status) {
                        DownloadStatus.READY -> {
                            builder.setContentTitle(event.taskName)
                                    .setContentText("连接中...")
                            notifyMgr.notify(NOTIFICATION_ID, builder.build())
                        }
                        DownloadStatus.COMPLETED -> {
                            builder.setContentText("正在合成安装包...")
                                    .setProgress(0, 0, false)
                            notifyMgr.notify(NOTIFICATION_ID, builder.build())
                        }
                        DownloadStatus.STARTED -> {
                            builder.setProgress(event.totalSize.toInt(), event.downloadSize.toInt(), false)
                                .setContentTitle(event.taskName)
                                .setContentText("${event.downloadSize.formatWithUnit()}/${event.totalSize.formatWithUnit()}")
                            notifyMgr.notify(NOTIFICATION_ID, builder.build())
                        }
                    }
                }
    }

    fun checkUpdate(quiet: Boolean) {
        jsonApiService.update("3990dcd7-49e1-4040-92e9-912082dc1896", "3d580ea3-54e9-4659-9131-a78c56cf9b86", BuildConfig.VERSION_CODE)
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    version ->
                    if (version.status == 1 && version.versionCode > BuildConfig.VERSION_CODE) {
                        val titleText = updateDialog.findViewById(R.id.text_title) as TextView
                        val descriptionText = updateDialog.findViewById(R.id.text_description) as TextView
                        titleText.text = "发现新版本V${version.versionName}(${version.apkSize.formatWithUnit()})"
                        descriptionText.text = version.description
                        val fullUpdateBtn = updateDialog.findViewById(R.id.btn_full_update) as Button
                        val saveUpdateBtn = updateDialog.findViewById(R.id.btn_save_update) as Button
                        if (version.patchUrl.isNotEmpty()) {
                            downloadUrl = version.patchUrl
                            saveUpdateBtn.text = "省流量更新(${version.patchSize.formatWithUnit()})"
                            saveUpdateBtn.visibility = View.VISIBLE
                        } else {
                            downloadUrl = version.apkUrl
                            fullUpdateBtn.visibility = View.VISIBLE
                        }
                        updateDialog.show()
                    } else {
                        if (!quiet) {
                            "你已经是最新版了".toast()
                        }
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    Snackbar.make(binding.root, "服务器异常，请手动检查更新", Snackbar.LENGTH_LONG)
                        .setAction("打开百度网盘", {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pan.baidu.com/s/1bptILyR"))
                            startActivity(intent)
                        })
                        .show()
                })
    }
}
