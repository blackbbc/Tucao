package me.sweetll.tucao.model.xml

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "i", strict = false)
data class DanmuInfo(
        @field:ElementList(inline = true, required = false) var danmus: MutableList<Danmu> = mutableListOf()
)
