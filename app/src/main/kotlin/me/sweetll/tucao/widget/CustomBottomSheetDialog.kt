package me.sweetll.tucao.widget

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.sweetll.tucao.R
import me.sweetll.tucao.extension.logD

class CustomBottomSheetDialog: AppCompatDialog {

    private var mBehavior: CustomBottomSheetBehavior<FrameLayout>? = null
    private var mBottomLinear: LinearLayout? = null

    var mCancelable = true
    private var mCanceledOnTouchOutside = true
    private var mCanceledOnTouchOutsideSet = false

    companion object {
        fun getThemeResId(context: Context, themeId: Int): Int {
            var resultId = themeId
            if (themeId == 0) {
                val outValue = TypedValue()
                if (context.theme.resolveAttribute(R.attr.bottomSheetDialogTheme, outValue, true)) {
                    resultId = outValue.resourceId
                } else {
                    resultId = R.style.Theme_Design_Light_BottomSheetDialog;
                }
            }
            return resultId
        }
    }

    constructor(context: Context): this(context, 0)

    constructor(context: Context, theme: Int) : super(context, getThemeResId(context, theme)) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener):
            super(context, cancelable, cancelListener){
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        mCancelable = cancelable
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(wrapInBottomSheet(layoutResID, null, null))
    }

    override fun setContentView(view: View?) {
        super.setContentView(wrapInBottomSheet(0, view, null))
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(wrapInBottomSheet(0, view, params))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)
        if (mCancelable != cancelable) {
            mCancelable = cancelable
            mBehavior?.isHideable = cancelable
        }
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        super.setCanceledOnTouchOutside(cancel)
        if (cancel && !mCancelable) {
            mCancelable = true
        }
        mCanceledOnTouchOutside = cancel
        mCanceledOnTouchOutsideSet = true
    }

    private fun wrapInBottomSheet(layoutResId: Int, view: View?, params: ViewGroup.LayoutParams?): View {
        val coordinator = View.inflate(context, R.layout.dialog_bottom_sheet, null) as CoordinatorLayout
        mBottomLinear = coordinator.findViewById(R.id.linear_bottom)

        var inflateView = view
        if (layoutResId != 0 && view == null) {
            inflateView = layoutInflater.inflate(layoutResId, coordinator, false)
        }
        val bottomSheet = coordinator.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        mBehavior = CustomBottomSheetBehavior.from(bottomSheet)
        mBehavior?.setBottomSheetCallback(mBottomSheetCallback)
        mBehavior?.isHideable = mCancelable
        if (params == null) {
            bottomSheet.addView(inflateView)
        } else {
            bottomSheet.addView(inflateView, params)
        }
        coordinator.findViewById<View>(R.id.touch_outside).setOnClickListener {
            if (mCancelable && isShowing && shouldWindowCloseOnTouchOutside()) {
                cancel()
            }
        }
        ViewCompat.setAccessibilityDelegate(bottomSheet, object: AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                if (mCancelable) {
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_DISMISS)
                    info.isDismissable = true
                } else {
                    info.isDismissable = false
                }
            }

            override fun performAccessibilityAction(host: View?, action: Int, args: Bundle?): Boolean {
                if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS && mCancelable) {
                    cancel()
                    return true
                }
                return super.performAccessibilityAction(host, action, args)
            }
        })
        return coordinator
    }

    fun shouldWindowCloseOnTouchOutside(): Boolean {
        if (!mCanceledOnTouchOutsideSet) {
            val a = context.obtainStyledAttributes(
                    intArrayOf(android.R.attr.windowCloseOnTouchOutside)
            )
            mCanceledOnTouchOutside = a.getBoolean(0, true)
            a.recycle()
            mCanceledOnTouchOutsideSet = true
        }
        return mCanceledOnTouchOutside
    }

    private val mBottomSheetCallback = object: CustomBottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                cancel()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val translationY = Math.max(0f, -slideOffset)
            mBottomLinear?.let {
                it.translationY = translationY
            }
        }
    }

}