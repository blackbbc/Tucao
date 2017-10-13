package me.sweetll.tucao.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.widget.EditText
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SweetEditor: EditText {

    var imgIndex = 1

    val imgMarkMap = mutableMapOf<String, String>()

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {

    }

    fun insertImage(imagePath: String) {
        var cursorPosition = selectionStart
        if (cursorPosition == -1) {
            cursorPosition = text.length
        }

        var imgMark = "![img$imgIndex]"

        var spanStart = 0
        var spanEnd = imgMark.length
        if (cursorPosition != 0 && text[cursorPosition - 1] != '\n') {
            imgMark = '\n' + imgMark
            spanStart++
            spanEnd++
        }
        if (cursorPosition == text.length || (text.isNotEmpty() && text[cursorPosition] != '\n')) {
            imgMark += '\n'
        }
        val spanString = SpannableString(imgMark)

        val fixedPath = fixImage(imagePath, width)

        imgMarkMap.put("$imgIndex", fixedPath)

        val bmp = getRawBitmap(fixedPath)
        val imgSpan = ImageSpan(context, bmp, ImageSpan.ALIGN_BASELINE)

        spanString.setSpan(imgSpan, spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        text.insert(cursorPosition, spanString)

        imgIndex++
    }

    fun getFiles(): List<Pair<String, File>> =
        "!\\[img((\\d+))\\]".toRegex().findAll(text).map {
            val index = it.groupValues[1]
            index to File(imgMarkMap[index])
        }.toList()

    private fun getRawBitmap(imagePath: String): Bitmap {
		val options = BitmapFactory.Options()
		options.inJustDecodeBounds = false
		return BitmapFactory.decodeFile(imagePath, options)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "PNG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".png", storageDir)
        return image
    }

	/**
	 * 根据view的宽度，动态缩放bitmap尺寸，旋转
	 *
	 * @param width view的宽度
     * @return 修复后的图片路径
	 */
	private fun fixImage(imagePath: String, width: Int): String {
        val angle = readPictureDegree(imagePath)

		val options = BitmapFactory.Options()
		options.inJustDecodeBounds = true
		BitmapFactory.decodeFile(imagePath, options)
		val sampleSize = if (options.outWidth > width) options.outWidth / width + 1 else 1
		options.inJustDecodeBounds = false
		options.inSampleSize = sampleSize
		val compressedBmp =  BitmapFactory.decodeFile(imagePath, options)

        val fixedBmp = rotateBitmap(compressedBmp, angle)

        val imageFile = createImageFile()

        val outStream = FileOutputStream(imageFile)

        try {
            fixedBmp.compress(Bitmap.CompressFormat.PNG, 100, outStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outStream.close()
            fixedBmp.recycle()
        }

        return imageFile.absolutePath
	}

    /**
     * 读取照片旋转角度
     *
     * @param imagePath 照片路径
     * @return 角度
     */
    private fun readPictureDegree(imagePath: String): Int {
        var degree = 0
        try {
            val exitInterface = ExifInterface(imagePath)
            val orientation = exitInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            degree = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    /**
     * 旋转图片
     * @param bmp 图片
     * @param angle 角度
     * @return 旋转后的图片
     */
    private fun rotateBitmap(bmp: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())

        var rotateBmp: Bitmap? = null
        try {
            rotateBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        } catch (ignored: Error) {

        }

        if (rotateBmp == null) {
            rotateBmp = bmp
        }
        if (rotateBmp != bmp) {
            bmp.recycle()
        }

        return rotateBmp
    }


}
