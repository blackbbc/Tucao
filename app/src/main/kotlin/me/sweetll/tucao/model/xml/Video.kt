package me.sweetll.tucao.model.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "video")
data class Video(
        @field:Element(name = "result") var result: String = "",
        @field:Element(name = "timelength", required = false) var timeLength: Long = 0L,
        @field:Element(name = "src", required = false) var src: Int = 0,
        @field:ElementList(inline = true, required = false) var duals: MutableList<Dual>? = null
)
