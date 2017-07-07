package me.sweetll.tucao.extension

import com.shuyu.gsyvideoplayer.video.PreviewGSYVideoPlayer
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.model.xml.Durl
import java.io.File
import java.io.FileOutputStream

fun PreviewGSYVideoPlayer.setUp(durls: MutableList<Durl>, cache: Boolean, vararg objects: Any) {
    /*
     * 使用concat协议拼接多段视频
     */
    try {
        val outputFile = File.createTempFile("tucao", ".concat", AppApplication.get().cacheDir)
        val outputStream = FileOutputStream(outputFile)

        val concatContent = buildString {
            appendln("ffconcat version 1.0")
            for (durl in durls) {
                appendln("file '${if (cache) durl.getCacheAbsolutePath() else durl.url}'")
                appendln("duration ${durl.length / 1000f}")
            }
        }

//        val concatContent = buildString {
//            appendln("ffconcat version 1.0")
//            appendln("file 'http://58.216.103.180/youku/697354F8AE94983EA216016D28/0300010900553A4D76DD1718581209B15F3A81-E563-D647-2FC4-06C5A8F05A9A.flv?sid=048412648117712c2f3d3_00&ctype=12'")
//            appendln("duration 200.992")
//            appendln("file 'http://222.73.245.134/youku/6772EA596B8368109D3AE55035/0300010901553A4D76DD1718581209B15F3A81-E563-D647-2FC4-06C5A8F05A9A.flv?sid=048412648117712c2f3d3_00&ctype=12'")
//            appendln("duration 181.765")
//        }

        outputStream.write(concatContent.toByteArray())

        outputStream.flush()
        outputStream.close()

        this.setUp(outputFile.absolutePath)
    } catch (e: Exception) {
        e.message?.toast()
        e.printStackTrace()
    }
}
