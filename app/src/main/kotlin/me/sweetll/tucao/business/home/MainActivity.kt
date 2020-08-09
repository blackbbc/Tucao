package me.sweetll.tucao.business.home

import android.accounts.AccountManager
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.databinding.DataBindingUtil
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.appcompat.widget.Toolbar
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.BuildConfig
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.download.DownloadActivity
import me.sweetll.tucao.business.home.adapter.HomePagerAdapter
import me.sweetll.tucao.business.home.event.RefreshPersonalEvent
import me.sweetll.tucao.business.login.LoginActivity
import me.sweetll.tucao.business.personal.PersonalActivity
import me.sweetll.tucao.business.search.SearchActivity
import me.sweetll.tucao.databinding.ActivityMainBinding
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.di.service.JsonApiService
import me.sweetll.tucao.di.service.RawApiService
import me.sweetll.tucao.extension.formatWithUnit
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.other.User
import me.sweetll.tucao.rxdownload.entity.DownloadEvent
import me.sweetll.tucao.rxdownload.entity.DownloadStatus
import me.sweetll.tucao.AppApplication.Companion.PRIMARY_CHANNEL
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.inject.Inject

class MainActivity : BaseActivity() {

    companion object {
        const val LOGIN_REQUEST = 1

        const val NOTIFICATION_ID = 10
    }

    private val notifyMgr by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    lateinit var binding : ActivityMainBinding
    lateinit var drawerToggle: ActionBarDrawerToggle

    private var lastBackTime = 0L

    @Inject
    lateinit var jsonApiService: JsonApiService

    @Inject
    lateinit var rawApiService: RawApiService

    @Inject
    lateinit var user: User

    lateinit var accountManager: AccountManager

    lateinit var avatarImg: ImageView

    lateinit var usernameText: TextView

    lateinit var messageMenu: MenuItem

    lateinit var messageCounter: TextView

    lateinit var updateDialog: DialogPlus

    lateinit var logoutDialog: DialogPlus

    lateinit var downloadUrl: String

    lateinit var apkFile: File

    override fun getToolbar(): Toolbar = binding.toolbar

    fun initDialog() {
        val updateView = LayoutInflater.from(this).inflate(R.layout.dialog_update, null)
        val descriptionText = updateView.findViewById<TextView>(R.id.text_description)
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
                            // saveUpdate()
                            dialog.dismiss()
                        }
                    }
                }
                .create()

        val logoutView = LayoutInflater.from(this).inflate(R.layout.dialog_logout, null)
        logoutDialog = DialogPlus.newDialog(this)
                .setContentHolder(ViewHolder(logoutView))
                .setGravity(Gravity.BOTTOM)
                .setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentBackgroundResource(android.R.color.transparent)
                .setOverlayBackgroundResource(R.color.scrim)
                .setOnClickListener {
                    dialog, view ->
                    when (view.id) {
                        R.id.btn_logout -> {
                            rawApiService.logout()
                                    .bindToLifecycle(this)
                                    .sanitizeHtml {
                                        Object()
                                    }
                                    .subscribe({

                                    }, {

                                    })
                            user.invalidate()
                            doRefresh()
                            dialog.dismiss()
                        }
                    }
                }
                .create()
    }

    override fun initView(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        EventBus.getDefault().register(this)

        initDialog()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupDrawer()

        initCounter()

        accountManager = AccountManager.get(this)

        binding.viewPager.adapter = HomePagerAdapter(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = 6
        binding.tab.setupWithViewPager(binding.viewPager)

        val headerView = binding.navigation.getHeaderView(0)
        avatarImg = headerView.findViewById(R.id.img_avatar)
        usernameText = headerView.findViewById(R.id.text_username)

        doRefresh()

        avatarImg.setOnClickListener {
            if (!user.isValid()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this, avatarImg, "transition_login"
                ).toBundle() ?: Bundle()
                options.putInt(LoginActivity.ARG_FAB_COLOR, ContextCompat.getColor(this, R.color.colorPrimary))
                options.putInt(LoginActivity.ARG_FAB_RES_ID, R.drawable.default_avatar)
                LoginActivity.intentTo(this, LOGIN_REQUEST, options)
            } else {
                PersonalActivity.intentTo(this)
//                logoutDialog.show()
            }
        }

//        checkUpdate(true)
    }

    override fun initToolbar() {
        super.initToolbar()
    }

    fun setupDrawer() {
        binding.navigation.setNavigationItemSelectedListener {
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
                R.id.nav_message -> {
                    MessageListActivity.intentTo(this)
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
        }
        drawerToggle = ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.drawer_open, R.string.drawer_close)
        binding.drawer.addDrawerListener(drawerToggle)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
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
                    val searchView = getToolbar().findViewById<View>(R.id.action_search)
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

    private fun initCounter() {
        messageMenu = binding.navigation.menu.findItem(R.id.nav_message)
        messageCounter = messageMenu.actionView as TextView
        messageCounter.gravity = Gravity.CENTER_VERTICAL
        messageCounter.setTypeface(null, Typeface.BOLD)
        messageCounter.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        messageCounter.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST && resultCode == Activity.RESULT_OK) {
            doRefresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshPersonal(event: RefreshPersonalEvent) {
        doRefresh()
    }

    private fun doRefresh() {
        if (user.isValid()) {
            avatarImg.load(this, user.avatar, R.drawable.default_avatar, User.signature())
            usernameText.text = user.name
            if (user.message > 0) {
                messageCounter.text = "${user.message}"
                messageCounter.visibility = View.VISIBLE
            } else {
                messageCounter.visibility = View.INVISIBLE
            }
            messageMenu.isVisible = true
        } else {
            usernameText.text = "点击头像登录"
            messageMenu.isVisible = false
            Glide.with(this)
                    .load(R.drawable.default_avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(avatarImg)
        }
    }

    fun fullUpdate() {
        if (apkFile.exists()) {
            installFromFile(apkFile)
            return
        }
        val processor = BehaviorProcessor.create<DownloadEvent>()
        processor.onNext(DownloadEvent(DownloadStatus.READY, 0, 0))
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
                    processor.onNext(DownloadEvent(DownloadStatus.STARTED, 0, 0))

                    var count = 0
                    var downloadLength = 0L
                    val contentLength = body!!.contentLength()
                    val data = ByteArray(1024 * 8)

                    try {
                        val inputStream = BufferedInputStream(body.byteStream())
                        val outputStream = BufferedOutputStream(apkFile.outputStream())

                        count = inputStream.read(data)
                        while (count != -1) {
                            outputStream.write(data, 0, count)
                            downloadLength += count
                            processor.onNext(DownloadEvent(DownloadStatus.STARTED, downloadLength, contentLength))
                            count = inputStream.read(data)
                        }
                        outputStream.flush()

                        processor.onNext(DownloadEvent(DownloadStatus.COMPLETED, downloadLength, contentLength))

                        inputStream.close()
                        outputStream.close()

                        installFromFile(apkFile)
                    } catch (error: Exception) {
                        error.printStackTrace()
                        // TODO: 下载失败
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    // TODO: 下载失败
                })

        val builder = NotificationCompat.Builder(this, PRIMARY_CHANNEL)
                        .setSmallIcon(R.mipmap.ic_launcher)

        processor.sample(500, TimeUnit.MILLISECONDS)
                .subscribe {
                    event ->
                    // 更新进度
                    when (event.status) {
                        DownloadStatus.READY -> {
                            builder.setContentTitle("新版本")
                                    .setContentText("连接中...")
                            notifyMgr.notify(NOTIFICATION_ID, builder.build())
                        }
                        DownloadStatus.COMPLETED -> notifyMgr.cancel(NOTIFICATION_ID)
                        DownloadStatus.STARTED -> {
                            builder.setProgress(event.totalSize.toInt(), event.downloadSize.toInt(), false)
                                .setContentTitle("新版本")
                                .setContentText("${event.downloadSize.formatWithUnit()}/${event.totalSize.formatWithUnit()}")
                            notifyMgr.notify(NOTIFICATION_ID, builder.build())
                        }
                    }
                }
    }

    /*
    fun saveUpdate() {
        if (apkFile.exists()) {
            installFromFile(apkFile)
        }
        val processor = BehaviorProcessor.create<DownloadEvent>()
        processor.onNext(DownloadEvent(DownloadStatus.READY, 0, 0))
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
                    processor.onNext(DownloadEvent(DownloadStatus.STARTED, 0, 0))

                    var count = 0
                    var downloadLength = 0L
                    val contentLength = body!!.contentLength()
                    val data = ByteArray(1024 * 8)

                    try {
                        val inputStream = BufferedInputStream(body.byteStream())
                        val file = File.createTempFile("tucao", ".patch", cacheDir)
                        val outputStream = BufferedOutputStream(file.outputStream())

                        count = inputStream.read(data)
                        while (count != -1) {
                            outputStream.write(data, 0, count)
                            downloadLength += count
                            processor.onNext(DownloadEvent(DownloadStatus.STARTED, downloadLength, contentLength))
                            count = inputStream.read(data)
                        }
                        outputStream.flush()

                        processor.onNext(DownloadEvent(DownloadStatus.COMPLETED, downloadLength, contentLength))

                        inputStream.close()
                        outputStream.close()

                        // 合成安装包
                        val info = packageManager.getApplicationInfo(packageName, 0)
                        val oldFile = File(info.sourceDir)
                        val patchIn = GZIPInputStream(file.inputStream())
                        FileByFileV1DeltaApplier().applyDelta(oldFile, patchIn, apkFile.outputStream())

                        installFromFile(apkFile)
                    } catch (error: Exception) {
                        error.printStackTrace()
                        // TODO: 下载失败
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    // TODO: 下载失败
                })

        val builder = NotificationCompat.Builder(this, PRIMARY_CHANNEL)
                        .setSmallIcon(R.mipmap.ic_launcher)

        processor.sample(500, TimeUnit.MILLISECONDS)
                .subscribe {
                    event ->
                    // 更新进度
                    when (event.status) {
                        DownloadStatus.READY -> {
                            builder.setContentTitle("补丁包")
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
                                .setContentTitle("补丁包")
                                .setContentText("${event.downloadSize.formatWithUnit()}/${event.totalSize.formatWithUnit()}")
                            notifyMgr.notify(NOTIFICATION_ID, builder.build())
                        }
                    }
                }
    }
    */

    fun installFromFile(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            Runtime.getRuntime().exec("chmod 666 ${file.absolutePath}")
            uri = Uri.fromFile(file)
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        startActivity(intent)
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
                        val saveUpdateBtn = updateDialog.findViewById(R.id.btn_save_update) as Button
                        if (version.patchUrl.isNotEmpty()) {
                            downloadUrl = version.patchUrl
                            saveUpdateBtn.text = "省流量更新(${version.patchSize.formatWithUnit()})"
                            saveUpdateBtn.visibility = View.VISIBLE
                        }
                        downloadUrl = version.apkUrl
                        apkFile = File(filesDir, "吐槽_${version.versionName}.apk")
                        updateDialog.show()
                    } else {
                        if (!quiet) {
                            "你已经是最新版了".toast()
                        }
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    if (!quiet) {
                        Snackbar.make(binding.root, "服务器异常，请手动检查更新", Snackbar.LENGTH_LONG)
                                .setAction("打开百度网盘", {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pan.baidu.com/s/1bptILyR"))
                                    startActivity(intent)
                                })
                                .show()
                    }
                })
    }

    override fun onBackPressed() {
        val currentBackTime = System.currentTimeMillis()
        if (currentBackTime - lastBackTime < 2000) {
            super.onBackPressed()
        } else {
            lastBackTime = currentBackTime
            "再按一次退出".toast()
        }
    }
}
