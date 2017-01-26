package me.sweetll.tucao.extension

fun Int.formatByWan(): String {
    if (this < 10000) {
        return this.toString()
    } else {
        return "%.1f万".format(this / 10000f)
    }
}
