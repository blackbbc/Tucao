package me.sweetll.tucao.extension

fun Int.formatByWan(): String {
    if (this < 10000) {
        return this.toString()
    } else {
        return "%.1f万".format(this / 10000f)
    }
}

fun Int.formatDanmuSizeToString(): String = String.format("%.2f", this.formatDanmuSizeToFloat())
fun Int.formatDanmuSizeToFloat(): Float = (this + 50) / 100f

fun Int.formatDanmuOpacityToString(): String = String.format("%d%%", this + 20)
fun Int.formatDanmuOpacityToFloat(): Float = (this + 20) / 100f

fun Int.formatDanmuSpeedToString(): String = String.format("%.2f", this.formatDanmuSpeedToFloat())
fun Int.formatDanmuSpeedToFloat(): Float = (this + 30) / 100f
