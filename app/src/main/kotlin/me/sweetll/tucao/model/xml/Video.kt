package me.sweetll.tucao.model.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "video")
class Video {
    @Element(name = "result")
    var result: String = ""

    @Element(name = "timelength")
    var timeLength: Long = 0L

    @Element(name = "src")
    var src: Int = 0

    @ElementList(inline = true)
    var duals: MutableList<Video>? = null
}
