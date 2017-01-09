package me.sweetll.tucao.model.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "dual")
class Dual {
    @Element(name = "order")
    var order: Int = 0

    @Element(name = "length")
    var length: Long = 0L

    @Element(name = "url")
    var url: String = ""
}
