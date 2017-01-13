package me.sweetll.tucao.widget

import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuFactory
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.helpers.XMLReaderFactory
import java.io.IOException

class TucaoDanmukuParser: BaseDanmakuParser() {
    var dispScaleX = 0f
    var dispScaleY = 0f

    override fun parse(): IDanmakus? {
        mDataSource?.let {
            val source = it as AndroidFileSource
            try {
                val xmlReader = XMLReaderFactory.createXMLReader()
                val contentHandler = XmlContentHandler()
                xmlReader.contentHandler = contentHandler
                xmlReader.parse(InputSource(it.data()))
                return contentHandler.result
            } catch (e: SAXException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    class XmlContentHandler : DefaultHandler() {
        companion object {
            val TRUE_STRING = "true"
        }

        var result: Danmakus? = null

        var item: BaseDanmaku? = null

        var completed = false

        var index = 0

        override fun startDocument() {
            result = Danmakus()
        }

        override fun endDocument() {
            completed = true
        }

        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            super.startElement(uri, localName, qName, attributes)
        }
    }

    override fun setDisplayer(disp: IDisplayer?): BaseDanmakuParser {
        super.setDisplayer(disp)
        dispScaleX = mDispWidth / DanmakuFactory.BILI_PLAYER_WIDTH
        dispScaleY = mDispHeight / DanmakuFactory.BILI_PLAYER_HEIGHT
        return this
    }
}
