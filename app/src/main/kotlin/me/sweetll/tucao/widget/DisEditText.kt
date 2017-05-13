package me.sweetll.tucao.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText

/**
 * Created by sweet on 5/13/17.
 */

class DisEditText : EditText {

    var dismissListener: KeyboardDismissListener? = null

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            dismissListener?.onKeyboardDismiss()
        }
        return super.onKeyPreIme(keyCode, event)
    }

    interface KeyboardDismissListener {
        fun onKeyboardDismiss()
    }
}
