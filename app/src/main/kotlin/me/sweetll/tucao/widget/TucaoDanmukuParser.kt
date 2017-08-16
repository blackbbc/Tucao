package me.sweetll.tucao.widget

import android.graphics.Color
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuFactory
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource
import master.flame.danmaku.danmaku.util.DanmakuUtils
import me.sweetll.tucao.extension.BlockListHelpers
import me.sweetll.tucao.extension.decode
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.helpers.XMLReaderFactory
import java.io.IOException

class TucaoDanmukuParser: BaseDanmakuParser() {

    companion object {
        init {
            System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver")
        }
    }

    var dispScaleX = 0f
    var dispScaleY = 0f

    override fun parse(): IDanmakus? {
        mDataSource?.let {
            val source = it as AndroidFileSource
            try {
                val xmlReader = XMLReaderFactory.createXMLReader()
                val contentHandler = XmlContentHandler()
                xmlReader.contentHandler = contentHandler
                xmlReader.parse(InputSource(source.data()))
                return contentHandler.result
            } catch (e: SAXException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    inner class XmlContentHandler : DefaultHandler() {
        val TRUE_STRING = "true"

        val result: Danmakus = Danmakus()

        var item: BaseDanmaku? = null

        var completed = false

        var index = 0

        val blockList = BlockListHelpers.loadBlockList()

        override fun startDocument() {

        }

        override fun endDocument() {
            completed = true
        }

        override fun startElement(uri: String?, localName: String, qName: String, attributes: Attributes) {
            val tagName = if (localName.isNotEmpty()) localName else qName
            if ("d" == tagName) {
                // <d p="23.826000213623,1,25,16777215,1422201084,0,057075e9,757076900">我从未见过如此厚颜无耻之猴</d>
                // 0:时间(弹幕出现时间)
                // 1:类型(1从右至左滚动弹幕|6从左至右滚动弹幕|5顶端固定弹幕|4底端固定弹幕|7高级弹幕|8脚本弹幕)
                // 2:字号
                // 3:颜色
                // 4:时间戳 ?
                // 5:弹幕池id
                // 6:用户hash
                // 7:弹幕id
                val pValue = attributes.getValue("p")
                val values = pValue.split(",")
                if (values.isNotEmpty()) {
                    val time = (values[0].toFloat() * 1000).toLong()
                    val type = values[1].toInt()
                    val textSize = values[2].toFloat()
                    val textColor = ((0x00000000ff000000 or values[3].toLong()) and 0x00000000ffffffff).toInt()

                    item = mContext.mDanmakuFactory.createDanmaku(type, mContext)
                    item?.let {
                        it.time = time
                        it.textSize = textSize * (mDispDensity - 0.6f)
                        it.textColor = textColor
                        it.textShadowColor = if (textColor <= Color.BLACK) Color.WHITE else Color.BLACK
                    }
                }
            }
        }

        override fun endElement(uri: String?, localName: String, qName: String) {
            if (item != null && item!!.text != null)
                if (item!!.duration != null) {
                    val tagName = if (localName.isNotEmpty()) localName else qName
                    if ("d" == tagName && blockList.all { it !in item!!.text }) {
                        item!!.timer = mTimer
                        item!!.flags = mContext.mGlobalFlagValues
                        val lock = result.obtainSynchronizer()
                        synchronized(lock) {
                            result.addItem(item)
                        }
                    }
                }
                item = null
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            item?.let {
                DanmakuUtils.fillText(it, String(ch, start, length).decode())
                it.index = index++
            }
        }
    }

    override fun setDisplayer(disp: IDisplayer?): BaseDanmakuParser {
        super.setDisplayer(disp)
        dispScaleX = mDispWidth / DanmakuFactory.BILI_PLAYER_WIDTH
        dispScaleY = mDispHeight / DanmakuFactory.BILI_PLAYER_HEIGHT
        return this
    }
}
