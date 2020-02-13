package me.sweetll.tucao.business.uploader

import android.app.SharedElementCallback
import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import android.transition.*
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.appbar.AppBarLayout
import me.sweetll.tucao.Const
import me.sweetll.tucao.GlideApp
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.uploader.adapter.VideoAdapter
import me.sweetll.tucao.business.uploader.viewmodel.UploaderViewModel
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.ActivityUploaderBinding
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.transition.CircularPathReveal
import me.sweetll.tucao.transition.TransitionListenerAdapter
import me.sweetll.tucao.widget.HorizontalDividerBuilder

class UploaderActivity : BaseActivity() {

    lateinit var binding: ActivityUploaderBinding

    lateinit var viewModel: UploaderViewModel

    lateinit var videoAdapter: VideoAdapter

    override fun getToolbar(): Toolbar = binding.toolbar

    var isAvatarInBounds = true
    var transitionIn = true

    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_USERNAME = "username"
        const val ARG_AVATAR = "avatar"
        const val ARG_SIGNATURE = "signature"
        const val ARG_HEADER_BG = "header_bg"

        fun intentTo(context: Context, userId: String, username: String, avatar: String, signature: String, headerBg: String, options: Bundle?) {
            val intent = Intent(context, UploaderActivity::class.java)
            intent.putExtra(ARG_USER_ID, userId)
            intent.putExtra(ARG_USERNAME, username)
            intent.putExtra(ARG_AVATAR, avatar)
            intent.putExtra(ARG_SIGNATURE, signature)
            intent.putExtra(ARG_HEADER_BG, headerBg)
            context.startActivity(intent, options)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_uploader)
        viewModel = UploaderViewModel(this, intent.getStringExtra(ARG_USER_ID))
        binding.viewModel = viewModel

        val avatar = intent.getStringExtra(ARG_AVATAR)
        val username = intent.getStringExtra(ARG_USERNAME)
        val signature = intent.getStringExtra(ARG_SIGNATURE)
        val headerBg = intent.getStringExtra(ARG_HEADER_BG)

        binding.usernameText.text = username
        binding.signatureText.text = signature
        binding.avatarImg.load(this, avatar, R.drawable.default_avatar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initTransition()
            setEnterSharedElementCallback(object : SharedElementCallback() {

                override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
                    super.onMapSharedElements(names, sharedElements)
                    if (transitionIn) {
                        transitionIn = false
                    } else {
                        if (!isAvatarInBounds) {
                            names.clear()
                            sharedElements.clear()
                        }
                    }
                }

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onSharedElementStart(sharedElementNames: MutableList<String>?, sharedElements: MutableList<View>, sharedElementSnapshots: MutableList<View>?) {
                    window.enterTransition = makeEnterTransition(sharedElements.find { it is ImageView }!!)
                }
            })
            window.enterTransition.duration = 400
        } else {
            GlideApp.with(this)
                    .load(headerBg)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            if (isFirstResource) {
                                return false
                            }
                            binding.headerImg.alpha = 0f
                            binding.headerImg.setImageDrawable(resource)
                            binding.headerImg.animate()
                                    .alpha(1f)
                                    .setDuration(200)
                                    .start()
                            return true
                        }
                    })
                    .into(binding.headerImg)
        }

        setupRecyclerView()

        binding.collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE)
        binding.appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            val EXPANDED = 1 shl 0
            val COLLAPSED = 1 shl 1
            val IDLE = 1 shl 2

            var currentState = IDLE

            override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
                if (i == 0) {
                    if (currentState != EXPANDED) {
                        binding.collapsingToolbar.title = " "
                        isAvatarInBounds = true
                    }
                    currentState = EXPANDED
                } else if (Math.abs(i) >= appBarLayout.totalScrollRange) {
                    if (currentState != COLLAPSED) {
                        isAvatarInBounds = false
                        binding.collapsingToolbar.title = username
                    }
                    currentState = COLLAPSED
                } else {
                    if (currentState != IDLE) {
                        binding.collapsingToolbar.title = " "
                        isAvatarInBounds = true
                    }
                    currentState = IDLE
                }
            }
        })
    }

    fun setupRecyclerView() {
        videoAdapter = VideoAdapter(null)
        binding.videoRecycler.layoutManager = LinearLayoutManager(this)
        binding.videoRecycler.adapter = videoAdapter

        binding.videoRecycler.addItemDecoration(
                HorizontalDividerBuilder.newInstance(this)
                        .setDivider(R.drawable.divider_small)
                        .build()
        )

        videoAdapter.setOnLoadMoreListener({
            viewModel.loadMoreData()
        }, binding.videoRecycler)

        binding.videoRecycler.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val video: Video = helper.getItem(position) as Video
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val coverImg = view.findViewById<ImageView>(R.id.img_thumb)
                    val titleText = view.findViewById<View>(R.id.text_title)
                    val p1: Pair<View, String> = Pair.create(coverImg, "cover")
                    val cover = titleText.tag as String
                    val options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(this@UploaderActivity, p1)
                    VideoActivity.intentTo(this@UploaderActivity, video.hid, cover, options.toBundle())
                } else {
                    VideoActivity.intentTo(this@UploaderActivity, video.hid)
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initTransition() {
        val changeBounds = ChangeBounds()
        changeBounds.pathMotion = ArcMotion()
        changeBounds.interpolator = FastOutSlowInInterpolator()

        window.sharedElementEnterTransition = changeBounds

        val returnTransition = TransitionSet()

        val slideUp = Slide(Gravity.TOP)
        slideUp.addTarget(binding.appBar)

        val slideDown = Slide(Gravity.BOTTOM)
        slideDown.addTarget(binding.mainLinear)

        returnTransition.addTransition(slideUp)
        returnTransition.addTransition(slideDown)
        returnTransition.ordering = TransitionSet.ORDERING_TOGETHER
        returnTransition.duration = 400
        window.returnTransition = returnTransition
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun makeEnterTransition(sharedElement: View): Transition {
        val circularReveal = CircularPathReveal(sharedElement)
        circularReveal.addTarget(binding.appBar)

        val headerBg = intent.getStringExtra(ARG_HEADER_BG)
        circularReveal.addListener(object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition?) {
                GlideApp.with(this@UploaderActivity)
                        .load(headerBg)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                binding.headerImg.alpha = 0f
                                binding.headerImg.setImageDrawable(resource)
                                binding.headerImg.animate()
                                        .alpha(1f)
                                        .setDuration(2000)
                                        .start()
                                return true
                            }
                        })
                        .into(binding.headerImg)
            }
        })

        return circularReveal
    }

    fun loadData(data: MutableList<Video>) {
        videoAdapter.setNewData(data)
        if (data.size < viewModel.pageSize) {
            videoAdapter.setEnableLoadMore(false)
        }
    }

    fun loadMoreData(data: MutableList<Video>?, flag: Int) {
        when (flag) {
            Const.LOAD_MORE_COMPLETE -> {
                videoAdapter.addData(data!!)
                videoAdapter.loadMoreComplete()
            }
            Const.LOAD_MORE_END -> {
                videoAdapter.addData(data!!)
                videoAdapter.loadMoreEnd()
            }
            Const.LOAD_MORE_FAIL -> {
                videoAdapter.loadMoreFail()
            }
        }
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = ""
        }
    }
}
