package me.sweetll.tucao.model.xml

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root(name = "d", strict = false)
data class Danmu(
        @field:Attribute(name = "p") var p: String = "",
        @field:Text var value: String = ""
)