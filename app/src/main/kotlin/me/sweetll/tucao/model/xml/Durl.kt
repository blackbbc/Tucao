package me.sweetll.tucao.model.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "durl")
data class Durl(
        @field:Element(name = "order") var order: Int = 0,
        @field:Element(name = "length") var length: Long = 0L,
        @field:Element(name = "url") var url: String = "",
        var size: Int = 0
)
